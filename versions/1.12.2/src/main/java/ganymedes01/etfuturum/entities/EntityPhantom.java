package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.Content;
import ganymedes01.etfuturum.ModEnchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.network.datasync.*;
import net.minecraft.world.World;

public class EntityPhantom extends EntityMob {
	private static final DataParameter<Integer> SIZE = EntityDataManager.createKey(EntityPhantom.class, DataSerializers.VARINT);
	private int phaseTicks, attackCooldown;
	private boolean swooping;
	private double angle, anchorX, anchorY, anchorZ;
	public EntityPhantom(World world) { super(world); setSize(.9F, .5F); }
	@Override public boolean isAIDisabled() { return false; }
	@Override protected void entityInit() { super.entityInit(); dataManager.register(SIZE, 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20); getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6); getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64); }
	private void sizeChanged() { float scale = 1 + getPhantomSize() * .15F; setSize(.9F * scale, .5F * scale); getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6 + getPhantomSize()); }
	public int getPhantomSize() { return dataManager.get(SIZE); }
	@Override public void fall(float distance, float multiplier) { }
	@Override public boolean isOnLadder() { return false; }
	@Override public void onLivingUpdate() {
		if (!world.isRemote && ticksExisted < 2) { anchorX = posX; anchorY = posY; anchorZ = posZ; phaseTicks = 160; }
		super.onLivingUpdate(); if (Math.abs(width - .9F * (1 + getPhantomSize() * .15F)) > .01F) sizeChanged(); if (world.isRemote) return;
		if (world.isDaytime() && world.canSeeSky(new BlockPos(posX, posY, posZ))) setFire(8);
		if (attackCooldown > 0) --attackCooldown;
		EntityLivingBase target = getAttackTarget();
		if (target == null || !target.isEntityAlive() || getDistanceSq(target) > 4096 || target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.disableDamage) {
			setAttackTarget(world.getClosestPlayerToEntity(this, 64)); target = getAttackTarget(); swooping = false;
		}
		if (ticksExisted % 20 == 0 && target != null && !world.getEntitiesWithinAABB(EntityOcelot.class, target.getEntityBoundingBox().grow(16, 8, 16)).isEmpty()) { swooping = false; phaseTicks = 160; }
		if (--phaseTicks <= 0) {
			swooping = target != null && !swooping; phaseTicks = swooping ? 100 : 160 + rand.nextInt(120);
			anchorX = target == null ? posX : target.posX; anchorY = Math.min(245, (target == null ? posY : target.posY) + 20); anchorZ = target == null ? posZ : target.posZ;
		}
	}
	@Override public void travel(float strafe, float vertical, float forward) {
		if (world.isRemote) return;
		EntityLivingBase target = getAttackTarget();
		angle += .035;
		double x = anchorX + Math.cos(angle) * 10, y = anchorY + Math.sin(angle * .5) * 3, z = anchorZ + Math.sin(angle) * 10;
		if (swooping && target != null) {
			x = target.posX; y = target.posY + target.height * .6; z = target.posZ;
			if (getDistanceSq(target) < 3 && attackCooldown == 0) { attackEntityAsMob(target); attackCooldown = 20; swooping = false; phaseTicks = 160; }
		}
		if (collidedHorizontally || world.rayTraceBlocks(new Vec3d(posX, posY, posZ), new Vec3d(posX + motionX * 8, posY + motionY * 8, posZ + motionZ * 8), false, true, false) != null) { swooping = false; motionY += .15; angle += Math.PI / 2; }
		if (!world.isBlockLoaded(new BlockPos(x, y, z))) { x = posX; y = posY + 1; z = posZ; }
		double dx = x - posX, dy = y - posY, dz = z - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length > 1) { double speed = swooping ? .8 : .35; motionX += (dx / length * speed - motionX) * .1; motionY += (dy / length * speed - motionY) * .1; motionZ += (dz / length * speed - motionZ) * .1; rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; rotationPitch = (float) (-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI); renderYawOffset = rotationYaw; }
		move(MoverType.SELF, motionX, motionY, motionZ);
	}
	@Override protected void dropLoot(boolean player, int looting, DamageSource source) { if (player) { int count = rand.nextInt(2) + rand.nextInt(looting + 1); if (count > 0) dropMembranes(count); } }
	private void dropMembranes(int count) { if (Content.available("phantom_membrane")) { net.minecraft.item.ItemStack item = Content.stack("phantom_membrane"); item.setCount(count); entityDropItem(item, 0); } }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Size", getPhantomSize()); tag.setDouble("AX", anchorX); tag.setDouble("AY", anchorY); tag.setDouble("AZ", anchorZ); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataManager.set(SIZE, MathHelper.clamp(tag.getInteger("Size"), 0, 64)); anchorX = tag.hasKey("AX") ? tag.getDouble("AX") : posX; anchorY = tag.hasKey("AY") ? tag.getDouble("AY") : posY + 10; anchorZ = tag.hasKey("AZ") ? tag.getDouble("AZ") : posZ; if (!Double.isFinite(anchorX) || !Double.isFinite(anchorY) || !Double.isFinite(anchorZ)) { anchorX = posX; anchorY = posY + 10; anchorZ = posZ; } }
}
