package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.entities.EntityHappyGhast;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

/** Loaded game ticks only: no wall-clock catch-up or lost scheduled update on unload. */
public class TileEntityDriedGhast extends TileEntity {
	private int progress = -1, ticks, dryTicks;
	private boolean lastWet;
	@Override public void updateEntity() {
		if (worldObj.isRemote) return;
		int meta = getBlockMetadata();
		if (progress < 0) progress = (meta & 3) * 6000;
		boolean wet = (meta & 4) != 0;
		if (wet != lastWet) { progress = (meta & 3) * 6000; dryTicks = 0; }
		if (wet) progress = Math.min(24000, progress + 1);
		else if (progress > 0 && ++dryTicks >= 6000) { progress = Math.max(0, (meta & 3) * 6000 - 6000); dryTicks = 0; }
		lastWet = wet;
		int stage = Math.min(3, progress / 6000);
		if (stage != (meta & 3)) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & 4) | stage, 2);
			worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, Tags.MC_ASSET_VER + ":block.dried_ghast.transition", 1, 1);
		}
		if (++ticks % 20 == 0) markDirty();
		if (wet && progress == 24000 && ticks % 20 == 0) {
			EntityHappyGhast ghast = new EntityHappyGhast(worldObj);
			ghast.setGrowingAge(-24000);
			ghast.setLocationAndAngles(xCoord + .5, yCoord, zCoord + .5, 0, 0);
			if (worldObj.spawnEntityInWorld(ghast)) {
				worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, Tags.MC_ASSET_VER + ":block.dried_ghast.spawn", 1, 1);
				worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.water, 0, 3);
			}
		}
	}
	@Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setInteger("HydrationTicks", progress); tag.setInteger("DryTicks", dryTicks); tag.setBoolean("LastWaterlogged", lastWet); }
	@Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); progress = tag.hasKey("HydrationTicks") ? MathHelper.clamp_int(tag.getInteger("HydrationTicks"), -1, 24000) : -1; dryTicks = MathHelper.clamp_int(tag.getInteger("DryTicks"), 0, 5999); lastWet = tag.getBoolean("LastWaterlogged"); }
}
