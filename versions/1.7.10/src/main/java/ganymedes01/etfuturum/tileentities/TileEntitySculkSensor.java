package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockSculkShrieker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

/** Legacy vibration adaptation: loaded moving players, wool occlusion, redstone pulse and shrieker relay. */
public class TileEntitySculkSensor extends TileEntity {
	private int cooldown;
	@Override public void updateEntity() {
		if (worldObj.isRemote) return;
		if (cooldown > 0) {
			if (--cooldown == 10) { worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 3); markDirty(); }
			return;
		}
		if ((worldObj.getTotalWorldTime() + xCoord + zCoord) % 10 != 0) return;
		for (Object object : worldObj.playerEntities) {
			EntityPlayer player = (EntityPlayer) object;
			if (player.capabilities.disableDamage || player.isSneaking() || player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) > 64 || player.getDistanceSq(player.prevPosX, player.prevPosY, player.prevPosZ) < .0001) continue;
			int footX = MathHelper.floor_double(player.posX), footY = MathHelper.floor_double(player.boundingBox.minY - .01), footZ = MathHelper.floor_double(player.posZ);
			if (!worldObj.blockExists(footX, footY, footZ) || worldObj.getBlock(footX, footY, footZ) == Blocks.wool || worldObj.getBlock(footX, footY, footZ) == Blocks.carpet) continue;
			boolean blocked = false;
			for (int i = 0; i <= 16; i++) {
				double a = i / 16.0;
				int x = MathHelper.floor_double(xCoord + .5 + (player.posX - xCoord - .5) * a), y = MathHelper.floor_double(yCoord + .5 + (player.posY - yCoord - .5) * a), z = MathHelper.floor_double(zCoord + .5 + (player.posZ - zCoord - .5) * a);
				if (!worldObj.blockExists(x, y, z) || worldObj.getBlock(x, y, z) == Blocks.wool) { blocked = true; break; }
			}
			if (blocked) continue;
			cooldown = 40; worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 4, 3); markDirty();
			worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.sculk_sensor.clicking", 1, 1);
			for (TileEntitySculkShrieker shrieker : TileEntitySculkShrieker.nearby(worldObj, xCoord, yCoord, zCoord)) {
				((BlockSculkShrieker) ModBlocks.SCULK_SHRIEKER.get()).shriek(worldObj, shrieker.xCoord, shrieker.yCoord, shrieker.zCoord, player);
			}
			break;
		}
	}
	@Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setInteger("Cooldown", cooldown); }
	@Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); cooldown = MathHelper.clamp_int(tag.getInteger("Cooldown"), 0, 40); }
}
