package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityGlowSquid extends EntitySquid {
	public EntityGlowSquid(World world) { super(world); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(16, 0); }
	public boolean isGlowing() { return dataWatcher.getWatchableObjectInt(16) == 0; }
	@Override public boolean isCreatureType(net.minecraft.entity.EnumCreatureType type, boolean count) { return type == ganymedes01.etfuturum.world.MobSpawning.GLOW_SQUID; }
	@Override public int getBrightnessForRender(float partial) { return isGlowing() ? 15728880 : super.getBrightnessForRender(partial); }
	@Override public float getBrightness(float partial) { return isGlowing() ? 1 : super.getBrightness(partial); }
	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.glow_squid.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.glow_squid.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.glow_squid.death"; }
	@Override public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean hit = super.attackEntityFrom(source, amount);
		if (hit && !worldObj.isRemote) dataWatcher.updateObject(16, 100);
		return hit;
	}
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && !isGlowing()) dataWatcher.updateObject(16, dataWatcher.getWatchableObjectInt(16) - 1); }
	@Override public boolean getCanSpawnHere() {
		int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
		return worldObj.provider.dimensionId == 0 && y < 30 && isInWater() && !worldObj.canBlockSeeTheSky(x, y, z)
				&& worldObj.getBlockLightValue(x, y, z) == 0 && worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
	}
	@Override protected void dropFewItems(boolean player, int looting) { ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ModItems.GLOW_INK_SAC.newItemStack(1 + rand.nextInt(3) + rand.nextInt(looting + 1)), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("DarkTicksRemaining", dataWatcher.getWatchableObjectInt(16)); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(16, Math.max(0, Math.min(100, tag.getInteger("DarkTicksRemaining")))); }
}
