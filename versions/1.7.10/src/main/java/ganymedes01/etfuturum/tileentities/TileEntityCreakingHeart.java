package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockCreakingHeart;
import ganymedes01.etfuturum.entities.EntityCreaking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import java.util.List;
import java.util.UUID;

public class TileEntityCreakingHeart extends TileEntity {
	private UUID creakingId;
	private long nextResinTime;
	@Override public void updateEntity() {
		if (worldObj.isRemote || (worldObj.getTotalWorldTime() + xCoord + zCoord) % 20 != 0) return;
		if (worldObj.difficultySetting == net.minecraft.world.EnumDifficulty.PEACEFUL || !BlockCreakingHeart.isActive(worldObj, xCoord, yCoord, zCoord)) return;
		boolean playerNearby = false;
		for (Object value : worldObj.playerEntities) {
			EntityPlayer player = (EntityPlayer) value;
			if (!player.isDead && !player.capabilities.disableDamage && !ganymedes01.etfuturum.api.spectator.SpectatorUtils.isSpectator(player) && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 256) { playerNearby = true; break; }
		}
		if (!playerNearby) return;
		List<EntityCreaking> mobs = worldObj.getEntitiesWithinAABB(EntityCreaking.class, AxisAlignedBB.getBoundingBox(xCoord - 32, yCoord - 32, zCoord - 32, xCoord + 33, yCoord + 33, zCoord + 33));
		for (EntityCreaking mob : mobs) if (!mob.isDead && mob.belongsTo(xCoord, yCoord, zCoord)) {
			if (!mob.getUniqueID().equals(creakingId)) { creakingId = mob.getUniqueID(); markDirty(); }
			return;
		}
		// A saved mob may be in an unloaded neighboring chunk. Do not create its replacement until absence is provable.
		if (creakingId != null && !worldObj.checkChunksExist(xCoord - 32, 0, zCoord - 32, xCoord + 32, 255, zCoord + 32)) return;
		for (int attempt = 0; attempt < 12; attempt++) {
			int x = xCoord + worldObj.rand.nextInt(17) - 8, z = zCoord + worldObj.rand.nextInt(17) - 8;
			if (!worldObj.blockExists(x, yCoord, z)) continue;
			for (int y = Math.min(254, yCoord + 8); y >= Math.max(1, yCoord - 16); y--) {
				if (!worldObj.getBlock(x, y - 1, z).isNormalCube() || !worldObj.isAirBlock(x, y, z) || !worldObj.isAirBlock(x, y + 1, z) || !worldObj.isAirBlock(x, y + 2, z)) continue;
				EntityCreaking mob = new EntityCreaking(worldObj);
				mob.bind(xCoord, yCoord, zCoord);
				mob.setLocationAndAngles(x + 0.5, y, z + 0.5, worldObj.rand.nextFloat() * 360, 0);
				if (!worldObj.checkNoEntityCollision(mob.boundingBox) || !worldObj.getCollidingBoundingBoxes(mob, mob.boundingBox).isEmpty()) continue;
				if (worldObj.spawnEntityInWorld(mob)) { creakingId = mob.getUniqueID(); markDirty(); return; }
			}
		}
	}
	public void createResin() {
		long now = worldObj.getTotalWorldTime();
		if (now < nextResinTime || !worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) return;
		nextResinTime = now + 100; markDirty();
		int count = 1 + worldObj.rand.nextInt(3);
		for (int tries = 0; tries < 48 && count > 0; tries++) {
			int x = xCoord + worldObj.rand.nextInt(9) - 4, y = yCoord + worldObj.rand.nextInt(9) - 4, z = zCoord + worldObj.rand.nextInt(9) - 4;
			if (!worldObj.blockExists(x, y, z) || worldObj.getBlock(x, y, z) != ModBlocks.PALE_OAK_LOG.get()) continue;
			int side = worldObj.rand.nextInt(4);
			int dx = side == 0 ? 1 : side == 1 ? -1 : 0, dz = side == 2 ? 1 : side == 3 ? -1 : 0;
			if (worldObj.blockExists(x + dx, y, z + dz) && worldObj.isAirBlock(x + dx, y, z + dz)) {
				int meta = dx == 1 ? 2 : dx == -1 ? 8 : dz == 1 ? 4 : 1;
				worldObj.setBlock(x + dx, y, z + dz, ModBlocks.RESIN_CLUMP.get(), meta, 2); count--;
			}
		}
	}
	@Override public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag); if (creakingId != null) tag.setString("Creaking", creakingId.toString()); tag.setLong("NextResinTime", nextResinTime);
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag); nextResinTime = tag.getLong("NextResinTime");
		try { creakingId = tag.hasKey("Creaking") ? UUID.fromString(tag.getString("Creaking")) : null; } catch (IllegalArgumentException ignored) { creakingId = null; }
	}
}
