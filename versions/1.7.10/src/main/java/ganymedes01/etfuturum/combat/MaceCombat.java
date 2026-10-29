package ganymedes01.etfuturum.combat;

import ganymedes01.etfuturum.ModEnchantments;
import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.items.ItemMace;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.block.*;
import net.minecraftforge.event.entity.living.*;
import cpw.mods.fml.common.eventhandler.*;

/** Native damage/armor callbacks own combat; successful item hits own impact and fall-context changes. */
public final class MaceCombat {
    private static final String IMPACT = "UabMaceImpactY";
    private static EntityLivingBase attacker(DamageSource source) {
        if (!source.damageType.equals("player") && !source.damageType.equals("mob")) return null;
        if (source.getSourceOfDamage() != source.getEntity() || !(source.getEntity() instanceof EntityLivingBase)) return null;
        EntityLivingBase entity = (EntityLivingBase) source.getEntity();
        return entity.getHeldItem() != null && entity.getHeldItem().getItem() instanceof ItemMace ? entity : null;
    }
    public static double armor(double before, double after, DamageSource source) {
        EntityLivingBase entity = attacker(source);
        return entity == null ? after : MaceRules.breach(before, after, ModEnchantments.level(ModEnchantments.breach, entity.getHeldItem()));
    }
    public static boolean allowed(Enchantment enchantment) {
        return enchantment == Enchantment.smite || enchantment == Enchantment.baneOfArthropods || enchantment == Enchantment.fireAspect
                || enchantment == Enchantment.unbreaking || enchantment == ModEnchantments.mending || enchantment == ModEnchantments.density
                || enchantment == ModEnchantments.breach || enchantment == ModEnchantments.windBurst;
    }
    public static float damage(DamageSource source, Entity victim, float damage) {
        EntityLivingBase entity = attacker(source);
        if (entity == null || entity.worldObj.isRemote || damage <= 0 || entity.fallDistance <= 1.5F) return damage;
        float multiplier = entity instanceof EntityPlayer && victim instanceof EntityLivingBase && !entity.onGround && !entity.isOnLadder() && !entity.isInWater()
                && !entity.isPotionActive(Potion.blindness) && entity.ridingEntity == null ? 1.5F : 1;
        return damage + MaceRules.smashDamage(entity.fallDistance, ModEnchantments.level(ModEnchantments.density, entity.getHeldItem())) * multiplier;
    }
    public static void impact(EntityLivingBase entity, EntityLivingBase victim) {
        if (entity.worldObj.isRemote || entity.fallDistance <= 1.5F) return;
        float distance = entity.fallDistance;
        NBTTagCompound data = entity.getEntityData();
        double height = data.hasKey(IMPACT) ? Math.min(data.getDouble(IMPACT), entity.posY) : entity.posY;
        if (data.getLong("EFRWindLaunchUntil") > entity.worldObj.getTotalWorldTime()) height = Math.min(height, data.getDouble("EFRWindLaunchY"));
        data.removeTag("EFRWindLaunchUntil"); data.removeTag("EFRWindLaunchY"); data.setDouble(IMPACT, height);
        entity.motionY = .01; entity.fallDistance = 0;
        for (Object object : entity.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, victim.boundingBox.expand(3.5, 3.5, 3.5))) {
            EntityLivingBase other = (EntityLivingBase) object;
            if (other == entity || other == victim || entity.isOnSameTeam(other) || other instanceof EntityPlayer && ((EntityPlayer) other).capabilities.isCreativeMode && ((EntityPlayer) other).capabilities.isFlying
                    || other instanceof EntityTameable && ((EntityTameable) other).isTamed() && ((EntityTameable) other).getOwner() == victim) continue;
            double dx = other.posX - victim.posX, dy = other.posY - victim.posY, dz = other.posZ - victim.posZ;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double power = MaceRules.knockback(length, distance, other.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue());
            if (power > 0 && length > .0001) { other.addVelocity(dx / length * power, .7, dz / length * power); sync(other); }
        }
        int wind = ModEnchantments.level(ModEnchantments.windBurst, entity.getHeldItem());
        if (wind > 0) burst(entity, wind == 1 ? 1.2 : wind == 2 ? 1.75 : 2.2);
        sync(entity);
        entity.worldObj.playSoundAtEntity(entity, Tags.MC_ASSET_VER + ":item.mace." + (victim.onGround ? distance > 5 ? "smash_ground_heavy" : "smash_ground" : "smash_air"), 1, 1);
    }
    private static void burst(EntityLivingBase source, double multiplier) {
        Vec3 center = Vec3.createVectorHelper(source.posX,source.posY,source.posZ);
        for (Object object : source.worldObj.getEntitiesWithinAABBExcludingEntity(null,source.boundingBox.expand(7,7,7))) {
            Entity entity = (Entity) object;
            if (entity instanceof ganymedes01.etfuturum.entities.EntityBreeze || entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode && ((EntityPlayer)entity).capabilities.isFlying) continue;
            double distance=entity.getDistance(source.posX,source.posY,source.posZ)/7;
            double dx=entity.posX-source.posX,dy=entity.posY+entity.getEyeHeight()-source.posY,dz=entity.posZ-source.posZ,length=Math.sqrt(dx*dx+dy*dy+dz*dz);
            if(distance>=1||length<.0001)continue;
            double power=(1-distance)*source.worldObj.getBlockDensity(center,entity.boundingBox)*multiplier;
            if(power>0){entity.addVelocity(dx/length*power,dy/length*power,dz/length*power);if(entity!=source)sync(entity);}
        }
        if(source instanceof EntityPlayer){
            EntityPlayer player=(EntityPlayer)source;
            for(int x=MathHelper.floor_double(source.posX-3.5);x<=MathHelper.floor_double(source.posX+3.5);x++)for(int y=MathHelper.floor_double(source.posY-3.5);y<=MathHelper.floor_double(source.posY+3.5);y++)for(int z=MathHelper.floor_double(source.posZ-3.5);z<=MathHelper.floor_double(source.posZ+3.5);z++){
                double dx=x+.5-source.posX,dy=y+.5-source.posY,dz=z+.5-source.posZ;
                if(dx*dx+dy*dy+dz*dz>12.25||!source.worldObj.blockExists(x,y,z)||!source.worldObj.canMineBlock(player,x,y,z)||!player.canPlayerEdit(x,y,z,1,player.getHeldItem()))continue;
                Block block=source.worldObj.getBlock(x,y,z);if(!(block instanceof BlockTrapDoor||block instanceof BlockDoor||block instanceof BlockLever||block instanceof BlockButton))continue;
                MovingObjectPosition obstruction=source.worldObj.rayTraceBlocks(center,Vec3.createVectorHelper(x+.5,y+.5,z+.5));
                if(obstruction!=null&&(obstruction.blockX!=x||obstruction.blockY!=y||obstruction.blockZ!=z))continue;
                if(block instanceof BlockDoor){int meta=source.worldObj.getBlockMetadata(x,y,z);((BlockDoor)block).func_150014_a(source.worldObj,x,y,z,(meta&4)==0);}
                else block.onBlockActivated(source.worldObj,x,y,z,player,1,.5F,.5F,.5F);
            }
        }
        source.worldObj.playSoundAtEntity(source, Tags.MC_ASSET_VER + ":entity.wind_charge.wind_burst", 1, 1);
        if(source.worldObj instanceof net.minecraft.world.WorldServer)((net.minecraft.world.WorldServer)source.worldObj).func_147487_a("cloud",source.posX,source.posY+.2,source.posZ,40,1,.2,1,.1);
    }
    private static void sync(Entity entity) {
        entity.velocityChanged = true;
        if (entity instanceof EntityPlayerMP) ((EntityPlayerMP) entity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(entity));
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public void fall(LivingFallEvent event) {
        NBTTagCompound data = event.entityLiving.getEntityData();
        if (!event.entityLiving.worldObj.isRemote && data.hasKey(IMPACT)) {
            event.distance = Math.max(0, Math.min(event.distance, (float) (data.getDouble(IMPACT) - event.entityLiving.posY)));
            data.removeTag(IMPACT);
        }
    }
    @SubscribeEvent public void update(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity.worldObj.isRemote || !entity.getEntityData().hasKey(IMPACT)) return;
        if (entity.onGround || entity.isInWater() || entity.isOnLadder() || entity.ridingEntity != null || entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isFlying) entity.getEntityData().removeTag(IMPACT);
    }
}
