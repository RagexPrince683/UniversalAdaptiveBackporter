package ganymedes01.etfuturum;

import ganymedes01.etfuturum.combat.MaceRules;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.*;
import net.minecraft.init.Enchantments;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import java.util.*;

@Mod.EventBusSubscriber(modid = UAB.ID)
public final class MaceCombat {
    private static final String IMPACT = "UabMaceImpactY";
    private static final Map<EntityPlayer, Critical> CRITICAL = new WeakHashMap<>();
    private static final class Critical {
        final int target; final long tick; final float multiplier;
        Critical(EntityPlayer player,Entity target,float multiplier){this.target=target.getEntityId();this.tick=player.world.getTotalWorldTime();this.multiplier=multiplier;}
    }
    private MaceCombat() { }
    private static EntityLivingBase attacker(DamageSource source) {
        if (!source.getDamageType().equals("player") && !source.getDamageType().equals("mob")) return null;
        if (source.getImmediateSource() != source.getTrueSource() || !(source.getTrueSource() instanceof EntityLivingBase)) return null;
        EntityLivingBase entity = (EntityLivingBase) source.getTrueSource();
        return entity.getHeldItemMainhand().getItem() instanceof MaceItem ? entity : null;
    }
    public static double armor(double before, double after, DamageSource source) {
        EntityLivingBase entity = attacker(source);
        return entity == null ? after : MaceRules.breach(before, after, ModEnchantments.level(ModEnchantments.breach, entity.getHeldItemMainhand()));
    }
    public static boolean allowed(Enchantment enchantment) {
        return enchantment == Enchantments.SMITE || enchantment == Enchantments.BANE_OF_ARTHROPODS || enchantment == Enchantments.FIRE_ASPECT
                || enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.MENDING || enchantment == Enchantments.VANISHING_CURSE
                || enchantment == ModEnchantments.density || enchantment == ModEnchantments.breach || enchantment == ModEnchantments.windBurst;
    }
    public static void critical(EntityPlayer player,Entity target,float multiplier) {
        if(player.world.isRemote)return;
        if(player.getHeldItemMainhand().getItem() instanceof MaceItem)CRITICAL.put(player,new Critical(player,target,multiplier));else CRITICAL.remove(player);
    }
    public static float damage(DamageSource source,Entity victim,float damage) {
        EntityLivingBase entity = attacker(source);
        if (entity == null || entity.world.isRemote) return damage;
        Critical critical=entity instanceof EntityPlayer?CRITICAL.remove((EntityPlayer)entity):null;
        if (damage <= 0 || entity.fallDistance <= 1.5F || entity.isElytraFlying()) return damage;
        float multiplier = critical!=null&&critical.tick==entity.world.getTotalWorldTime()&&critical.target==victim.getEntityId()?critical.multiplier:1;
        return damage + MaceRules.smashDamage(entity.fallDistance, ModEnchantments.level(ModEnchantments.density, entity.getHeldItemMainhand())) * multiplier;
    }
    public static void impact(EntityLivingBase entity, EntityLivingBase victim) {
        if (entity.world.isRemote || entity.fallDistance <= 1.5F || entity.isElytraFlying()) return;
        float distance = entity.fallDistance;
        NBTTagCompound data = entity.getEntityData();
        double height = data.hasKey(IMPACT) ? Math.min(data.getDouble(IMPACT), entity.posY) : entity.posY;
        if (data.getLong("EFRWindLaunchUntil") > entity.world.getTotalWorldTime()) height = Math.min(height, data.getDouble("EFRWindLaunchY"));
        data.removeTag("EFRWindLaunchUntil"); data.removeTag("EFRWindLaunchY"); data.setDouble(IMPACT, height);
        entity.motionY = .01; entity.fallDistance = 0;
        for (EntityLivingBase other : entity.world.getEntitiesWithinAABB(EntityLivingBase.class, victim.getEntityBoundingBox().grow(3.5))) {
            if (other == entity || other == victim || entity.isOnSameTeam(other)
                    || other instanceof EntityPlayer && (((EntityPlayer) other).capabilities.isCreativeMode && ((EntityPlayer) other).capabilities.isFlying || ((EntityPlayer) other).isSpectator())
                    || other instanceof EntityTameable && ((EntityTameable) other).isTamed() && ((EntityTameable) other).getOwner() == victim) continue;
            double dx = other.posX - victim.posX, dy = other.posY - victim.posY, dz = other.posZ - victim.posZ;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double power = MaceRules.knockback(length, distance, other.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue());
            if (power > 0 && length > .0001) { other.addVelocity(dx / length * power, .7, dz / length * power); sync(other); }
        }
        int wind = ModEnchantments.level(ModEnchantments.windBurst, entity.getHeldItemMainhand());
        if (wind > 0) burst(entity, wind == 1 ? 1.2 : wind == 2 ? 1.75 : 2.2);
        sync(entity);
        entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, ModernSounds.get("item.mace." + (victim.onGround ? distance > 5 ? "smash_ground_heavy" : "smash_ground" : "smash_air")), SoundCategory.PLAYERS, 1, 1);
    }
    private static void burst(EntityLivingBase source,double multiplier) {
        Vec3d center=new Vec3d(source.posX,source.posY,source.posZ);
        for(Entity entity:source.world.getEntitiesWithinAABBExcludingEntity(null,source.getEntityBoundingBox().grow(7))){
            if(entity instanceof ganymedes01.etfuturum.entities.EntityBreeze||entity instanceof EntityPlayer&&(((EntityPlayer)entity).isSpectator()||((EntityPlayer)entity).capabilities.isCreativeMode&&((EntityPlayer)entity).capabilities.isFlying))continue;
            double distance=entity.getDistance(source.posX,source.posY,source.posZ)/7;
            double dx=entity.posX-source.posX,dy=entity.posY+entity.getEyeHeight()-source.posY,dz=entity.posZ-source.posZ,length=Math.sqrt(dx*dx+dy*dy+dz*dz);
            if(distance>=1||length<.0001)continue;
            double power=(1-distance)*source.world.getBlockDensity(center,entity.getEntityBoundingBox())*multiplier;
            if(power>0){entity.addVelocity(dx/length*power,dy/length*power,dz/length*power);if(entity!=source)sync(entity);}
        }
        if(source instanceof EntityPlayer){
            EntityPlayer player=(EntityPlayer)source;
            for(BlockPos pos:BlockPos.getAllInBox(new BlockPos(source.posX-3.5,source.posY-3.5,source.posZ-3.5),new BlockPos(source.posX+3.5,source.posY+3.5,source.posZ+3.5))){
                double dx=pos.getX()+.5-source.posX,dy=pos.getY()+.5-source.posY,dz=pos.getZ()+.5-source.posZ;
                if(dx*dx+dy*dy+dz*dz>12.25||!source.world.isBlockLoaded(pos)||!source.world.isBlockModifiable(player,pos)||!player.canPlayerEdit(pos,EnumFacing.UP,player.getHeldItemMainhand()))continue;
                IBlockState state=source.world.getBlockState(pos);Block block=state.getBlock();if(!(block instanceof BlockTrapDoor||block instanceof BlockDoor||block instanceof BlockLever||block instanceof BlockButton))continue;
                RayTraceResult obstruction=source.world.rayTraceBlocks(center,new Vec3d(pos).add(.5,.5,.5));
                if(obstruction!=null&&!obstruction.getBlockPos().equals(pos))continue;
                if(block instanceof BlockTrapDoor)source.world.setBlockState(pos,state.cycleProperty(BlockTrapDoor.OPEN),3);
                else if(block instanceof BlockDoor)((BlockDoor)block).toggleDoor(source.world,pos,!((BlockDoor)block).isOpen(source.world,pos));
                else block.onBlockActivated(source.world,pos,state,player,EnumHand.MAIN_HAND,EnumFacing.UP,.5F,.5F,.5F);
            }
        }
        source.world.playSound(null,source.posX,source.posY,source.posZ,ModernSounds.get("entity.wind_charge.wind_burst"),SoundCategory.PLAYERS,1,1);
        if(source.world instanceof net.minecraft.world.WorldServer)((net.minecraft.world.WorldServer)source.world).spawnParticle(EnumParticleTypes.CLOUD,source.posX,source.posY+.2,source.posZ,40,1,.2,1,.1);
    }
    private static void sync(Entity entity) {
        entity.velocityChanged = true;
        if (entity instanceof EntityPlayerMP) ((EntityPlayerMP) entity).connection.sendPacket(new SPacketEntityVelocity(entity));
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void fall(LivingFallEvent event) {
        EntityLivingBase entity = event.getEntityLiving(); NBTTagCompound data = entity.getEntityData();
        if (!entity.world.isRemote && data.hasKey(IMPACT)) {
            event.setDistance(Math.max(0, Math.min(event.getDistance(), (float) (data.getDouble(IMPACT) - entity.posY))));
            data.removeTag(IMPACT);
        }
    }
    @SubscribeEvent public static void update(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || !entity.getEntityData().hasKey(IMPACT)) return;
        if (entity.onGround || entity.isInWater() || entity.isOnLadder() || entity.isRiding() || entity.isElytraFlying() || entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isFlying) entity.getEntityData().removeTag(IMPACT);
    }
}
