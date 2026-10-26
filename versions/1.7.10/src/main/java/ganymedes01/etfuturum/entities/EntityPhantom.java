package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityPhantom extends EntityMob {
	private int phaseTicks, attackCooldown;
	private boolean swooping;
	private double angle, anchorX, anchorY, anchorZ;
	public EntityPhantom(World world) { super(world); setSize(.9F, .5F); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(16, 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6); getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64); }
	private void sizeChanged() { float scale = 1 + getPhantomSize() * .15F; setSize(.9F * scale, .5F * scale); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6 + getPhantomSize()); }
	public int getPhantomSize() { return dataWatcher.getWatchableObjectInt(16); }
	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.phantom.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.phantom.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.phantom.death"; }
	@Override protected void fall(float distance) { }
	@Override public boolean isOnLadder() { return false; }
	@Override public void onLivingUpdate() {
		if (!worldObj.isRemote && ticksExisted < 2) { anchorX = posX; anchorY = posY; anchorZ = posZ; phaseTicks = 160; }
		super.onLivingUpdate(); if (Math.abs(width - .9F * (1 + getPhantomSize() * .15F)) > .01F) sizeChanged(); if (worldObj.isRemote) return;
		if (worldObj.isDaytime() && worldObj.canBlockSeeTheSky(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ))) setFire(8);
		if (attackCooldown > 0) --attackCooldown;
		EntityLivingBase target = getAttackTarget();
		if (target == null || !target.isEntityAlive() || getDistanceSqToEntity(target) > 4096 || target instanceof EntityPlayer player && player.capabilities.disableDamage) {
			setAttackTarget(worldObj.getClosestVulnerablePlayerToEntity(this, 64)); target = getAttackTarget(); swooping = false;
		}
		if (ticksExisted % 20 == 0 && target != null && !worldObj.getEntitiesWithinAABB(EntityOcelot.class, target.boundingBox.expand(16, 8, 16)).isEmpty()) { swooping = false; phaseTicks = 160; }
		if (--phaseTicks <= 0) {
			swooping = target != null && !swooping; phaseTicks = swooping ? 100 : 160 + rand.nextInt(120);
			anchorX = target == null ? posX : target.posX; anchorY = Math.min(245, (target == null ? posY : target.posY) + 20); anchorZ = target == null ? posZ : target.posZ;
		}
	}
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (worldObj.isRemote) return;
		EntityLivingBase target = getAttackTarget();
		angle += .035;
		double x = anchorX + Math.cos(angle) * 10, y = anchorY + Math.sin(angle * .5) * 3, z = anchorZ + Math.sin(angle) * 10;
		if (swooping && target != null) {
			x = target.posX; y = target.posY + target.height * .6; z = target.posZ;
			if (getDistanceSqToEntity(target) < 3 && attackCooldown == 0) { attackEntityAsMob(target); attackCooldown = 20; swooping = false; phaseTicks = 160; }
		}
		if (isCollidedHorizontally || worldObj.func_147447_a(Vec3.createVectorHelper(posX, posY, posZ), Vec3.createVectorHelper(posX + motionX * 8, posY + motionY * 8, posZ + motionZ * 8), false, true, false) != null) { swooping = false; motionY += .15; angle += Math.PI / 2; }
		if (!worldObj.blockExists(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z))) { x = posX; y = posY + 1; z = posZ; }
		double dx = x - posX, dy = y - posY, dz = z - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length > 1) { double speed = swooping ? .8 : .35; motionX += (dx / length * speed - motionX) * .1; motionY += (dy / length * speed - motionY) * .1; motionZ += (dz / length * speed - motionZ) * .1; rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; rotationPitch = (float) (-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI); renderYawOffset = rotationYaw; }
		moveEntity(motionX, motionY, motionZ);
	}
	@Override protected void dropFewItems(boolean player, int looting) { if (player) { int count = rand.nextInt(2) + rand.nextInt(looting + 1); if (count > 0) ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ModItems.PHANTOM_MEMBRANE.newItemStack(count), 0); } }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Size", getPhantomSize()); tag.setDouble("AX", anchorX); tag.setDouble("AY", anchorY); tag.setDouble("AZ", anchorZ); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(16, MathHelper.clamp_int(tag.getInteger("Size"), 0, 64)); anchorX = tag.hasKey("AX") ? tag.getDouble("AX") : posX; anchorY = tag.hasKey("AY") ? tag.getDouble("AY") : posY + 10; anchorZ = tag.hasKey("AZ") ? tag.getDouble("AZ") : posZ; if (!Double.isFinite(anchorX) || !Double.isFinite(anchorY) || !Double.isFinite(anchorZ)) { anchorX = posX; anchorY = posY + 10; anchorZ = posZ; } }
}
