package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;

/** Server-owned native conduit: bounded loaded-block frame checks and native timed effects. */
public final class TileEntityConduit extends TileEntity {
	private int frameBlocks;
	public int getFrameBlocks() { return frameBlocks; }
	public int getEffectRange() { return frameBlocks < 16 ? 0 : frameBlocks / 7 * 16; }
	private boolean frame(Block block) { return block == ModBlocks.PRISMARINE_BLOCK.get() || block == ModBlocks.SEA_LANTERN.get(); }
	public void refresh() {
		frameBlocks = 0;
		if (!ModBlocks.CONDUIT.isEnabled() || getBlockType() != ModBlocks.CONDUIT_UNDERWATER.get()) return;
		for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++) {
			if (!worldObj.blockExists(xCoord + x, yCoord + y, zCoord + z)) { frameBlocks = 0; return; }
			if (Math.abs(x) <= 1 && Math.abs(y) <= 1 && Math.abs(z) <= 1 && worldObj.getBlock(xCoord + x, yCoord + y, zCoord + z).getMaterial() != Material.water) { frameBlocks = 0; return; }
		}
		for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++) {
			if ((x == 0 || y == 0 || z == 0) && (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2) && frame(worldObj.getBlock(xCoord + x, yCoord + y, zCoord + z))) frameBlocks++;
		}
	}
	@Override public void updateEntity() {
		if (worldObj.isRemote || (worldObj.getTotalWorldTime() + xCoord + zCoord) % 40 != 0) return;
		int previous = frameBlocks; refresh(); int range = getEffectRange();
		if ((previous >= 16) != (frameBlocks >= 16)) worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.conduit." + (range > 0 ? "activate" : "deactivate"), 1, 1);
		if (range == 0) return;
		for (Object object : worldObj.playerEntities) {
			EntityPlayer player = (EntityPlayer) object;
			if (player.isWet() && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= range * range) {
				player.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, 260, 0));
				player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 260, 0));
				player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 260, 0));
			}
		}
		if (frameBlocks == 42) for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(xCoord - 8, yCoord - 8, zCoord - 8, xCoord + 9, yCoord + 9, zCoord + 9))) {
			EntityLivingBase target = (EntityLivingBase) object;
			if (target instanceof IMob && target.isEntityAlive() && target.isWet() && target.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64) { target.attackEntityFrom(DamageSource.magic, 4); worldObj.playSoundEffect(target.posX, target.posY, target.posZ, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.conduit.attack.target", 1, 1); break; }
		}
	}
}
