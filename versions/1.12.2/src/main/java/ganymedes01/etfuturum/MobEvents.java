package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = UAB.ID)
public final class MobEvents {
    private MobEvents() { }
    @SubscribeEvent public static void player(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) return;
        NBTTagCompound data = player.getEntityData();
        int insomnia = player.isPlayerSleeping() ? 0 : Math.min(2147480, Math.max(0, data.getInteger("EFRTimeSinceRest")) + 1);
        data.setInteger("EFRTimeSinceRest", insomnia);
        if (!Features.enabled("entities.enablePhantomSpawning") || insomnia < 3600 || player.capabilities.disableDamage || player.world.provider.getDimension() != 0 || player.world.isDaytime()
                || player.world.getDifficulty() == EnumDifficulty.PEACEFUL || !player.world.getGameRules().getBoolean("doMobSpawning") || player.ticksExisted % 1200 != 0 || player.getRNG().nextInt(insomnia) < 3600) return;
        BlockPos origin = player.getPosition();
        if (origin.getY() < player.world.getSeaLevel() || !player.world.canSeeSky(origin) || !player.world.getEntitiesWithinAABB(EntityPhantom.class, player.getEntityBoundingBox().grow(64)).isEmpty()) return;
        for (int i = 0, count = 1 + player.getRNG().nextInt(player.world.getDifficulty().getId()); i < count; i++) {
            BlockPos pos = origin.add(player.getRNG().nextInt(21) - 10, 20 + player.getRNG().nextInt(15), player.getRNG().nextInt(21) - 10);
            if (pos.getY() > 250 || !player.world.isBlockLoaded(pos)) continue;
            EntityPhantom phantom = new EntityPhantom(player.world); phantom.setLocationAndAngles(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0); phantom.setAttackTarget(player);
            if (player.world.getCollisionBoxes(phantom, phantom.getEntityBoundingBox()).isEmpty() && player.world.checkNoEntityCollision(phantom.getEntityBoundingBox())) player.world.spawnEntity(phantom);
        }
    }
    @SubscribeEvent public static void zombieWater(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (living.world.isRemote || living.getClass() != EntityZombie.class || !Features.enabled("entities.enableDrownedSpawning")) return;
        EntityZombie zombie = (EntityZombie) living; NBTTagCompound data = zombie.getEntityData();
        int wet = zombie.isInsideOfMaterial(Material.WATER) ? Math.min(600, data.getInteger("EFRDrownedTime") + 1) : 0;
        data.setInteger("EFRDrownedTime", wet);
        if (!data.hasKey("EFRDrownedConversion") && wet >= 600) data.setInteger("EFRDrownedConversion", 300);
        if (!data.hasKey("EFRDrownedConversion")) return;
        int remaining = MathHelper.clamp(data.getInteger("EFRDrownedConversion"), 0, 300);
        data.setInteger("EFRDrownedConversion", Math.max(0, remaining - 1));
        if (remaining > 0) return;
        NBTTagCompound saved = new NBTTagCompound(); zombie.writeToNBT(saved);
        EntityDrowned drowned = new EntityDrowned(zombie.world); drowned.readFromNBT(saved);
        zombie.setDead();
        if (zombie.world.spawnEntity(drowned)) {
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) zombie.setItemStackToSlot(slot, net.minecraft.item.ItemStack.EMPTY);
        } else {
            zombie.isDead = false;
            if (((WorldServer) zombie.world).getEntityFromUuid(zombie.getUniqueID()) == null) zombie.world.spawnEntity(zombie);
        }
    }
}
