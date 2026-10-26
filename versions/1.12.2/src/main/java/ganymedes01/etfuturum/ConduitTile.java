package ganymedes01.etfuturum;

import net.minecraft.block.material.Material;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

public final class ConduitTile extends TileEntity implements ITickable {
    @Override public void update() {
        if (world.isRemote || world.getTotalWorldTime() % 40 != 0 || !world.isAreaLoaded(pos, 8)) return;
        for (BlockPos p : BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            if (!p.equals(pos) && world.getBlockState(p).getMaterial() != Material.WATER) return;
        }
        int count = 0;
        for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++) {
            boolean ring = x == 0 && (Math.abs(y) == 2 || Math.abs(z) == 2)
                || y == 0 && (Math.abs(x) == 2 || Math.abs(z) == 2)
                || z == 0 && (Math.abs(x) == 2 || Math.abs(y) == 2);
            if (ring && (world.getBlockState(pos.add(x, y, z)).getBlock() == Blocks.PRISMARINE || world.getBlockState(pos.add(x, y, z)).getBlock() == Blocks.SEA_LANTERN)) count++;
        }
        if (count < 16) return;
        int range = (count / 7) * 16;
        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(range))) {
            if (player.getDistanceSq(pos) > range * range || !player.isWet()) continue;
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 260, 0, true, false));
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 260, 0, true, false));
            player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 260, 0, true, false));
        }
        if (count == 42) for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(8))) {
            if (target instanceof IMob && target.isWet()) { target.attackEntityFrom(DamageSource.MAGIC, 4); break; }
        }
    }
}
