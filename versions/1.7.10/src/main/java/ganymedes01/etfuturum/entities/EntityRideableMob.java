package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Native rider input controls movement on the server. Additional passengers use registered native seats. */
public abstract class EntityRideableMob extends EntityAnimal {
	protected int dashCooldown;
	private boolean jumpWasDown;
	private EntityRideableMob swimmingMate;
	private boolean hasFlightHome;
	private double flightHomeX, flightHomeY, flightHomeZ, flyX, flyY, flyZ;
	private int flyTicks;
	public EntityRideableMob(World world) { super(world); tasks.addTask(1, new EntityAIMate(this, 1)); tasks.addTask(3, new EntityAIFollowParent(this, 1)); tasks.addTask(6, new EntityAIWander(this, .7)); tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8)); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); dataWatcher.addObject(21, 0); dataWatcher.addObjectByDataType(22, 5); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(32); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.2); }
	public abstract int passengerCount();
	public boolean isSaddled() { return (dataWatcher.getWatchableObjectByte(20) & 1) != 0; }
	protected void flag(int bit, boolean enabled) { byte flags = dataWatcher.getWatchableObjectByte(20); dataWatcher.updateObject(20, (byte) (enabled ? flags | bit : flags & ~bit)); }
	protected boolean flag(int bit) { return (dataWatcher.getWatchableObjectByte(20) & bit) != 0; }
	public int getVariant() { return dataWatcher.getWatchableObjectInt(21); }
	public ItemStack getBodyEquipment() { return dataWatcher.getWatchableObjectItemStack(22); }
	protected void bodyEquipment(ItemStack stack) { dataWatcher.updateObject(22, stack == null ? null : stack.copy()); }
	protected boolean saddleItem(ItemStack held) { return held != null && held.getItem() == Items.saddle; }
	protected boolean mayRide(EntityPlayer player) { return !isChild() && isSaddled(); }
	protected void consume(EntityPlayer player, ItemStack held) { if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); }
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem(); if (isBreedingItem(held)) return super.interact(player);
		if (!isChild() && !isSaddled() && saddleItem(held)) { if (!worldObj.isRemote) { flag(1, true); if (this instanceof EntityHappyGhast) dataWatcher.updateObject(21, held.getItemDamage() & 15); consume(player, held); } return true; }
		if (mayRide(player) && !player.isSneaking()) { if (!worldObj.isRemote) {
			if (riddenByEntity == null) player.mountEntity(this);
			else for (int slot = 1; slot < passengerCount(); slot++) {
				EntityMobSeat seat = null;
				for (Object object : worldObj.getEntitiesWithinAABB(EntityMobSeat.class, boundingBox.expand(8, 8, 8))) { EntityMobSeat candidate = (EntityMobSeat) object; if (candidate.belongsTo(this, slot)) { seat = candidate; break; } }
				if (seat == null) { seat = new EntityMobSeat(worldObj, this, slot); if (!worldObj.spawnEntityInWorld(seat)) break; }
				if (seat.riddenByEntity == null) { player.mountEntity(seat); break; }
			}
		} return true; }
		return super.interact(player);
	}
	protected boolean flyingMount() { return false; }
	protected boolean aquaticMount() { return false; }
	protected double mountedSpeed() { return .3; }
	protected void dash() { if (dashCooldown == 0 && (onGround || aquaticMount() && isInWater())) { double yaw = rotationYaw * Math.PI / 180; motionX += -Math.sin(yaw) * (aquaticMount() ? 1.2 : 1.5); motionZ += Math.cos(yaw) * (aquaticMount() ? 1.2 : 1.5); if (!aquaticMount()) motionY = .4; dashCooldown = aquaticMount() ? 40 : 55; velocityChanged = true; } }
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (worldObj.isRemote && riddenByEntity instanceof EntityPlayer) return;
		if (!worldObj.isRemote && riddenByEntity instanceof EntityPlayer rider && mayRide(rider)) {
			if (flyingMount()) { flightHomeX = posX; flightHomeY = posY; flightHomeZ = posZ; hasFlightHome = true; }
			rotationYaw = Float.isFinite(rider.rotationYaw) ? rider.rotationYaw : rotationYaw; renderYawOffset = rotationYaw;
			strafe = Float.isFinite(rider.moveStrafing) ? MathHelper.clamp_float(rider.moveStrafing, -1, 1) * .5F : 0;
			forward = Float.isFinite(rider.moveForward) ? MathHelper.clamp_float(rider.moveForward, -1, 1) : 0;
			boolean jumping = rider instanceof RiderInput input && input.etfuturum$isJumping(); if (jumping && !jumpWasDown) dash(); jumpWasDown = jumping;
			if (flyingMount() || aquaticMount() && isInWater()) {
				double yaw = rotationYaw * Math.PI / 180, speed = mountedSpeed();
				motionX += ((-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * speed - motionX) * .15;
				motionZ += ((Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * speed - motionZ) * .15;
				double vertical = Float.isFinite(rider.rotationPitch) ? -Math.sin(MathHelper.clamp_float(rider.rotationPitch, -80, 80) * Math.PI / 180) * Math.abs(forward) * speed : 0;
				motionY += (vertical - motionY) * .15; moveEntity(motionX, motionY, motionZ); return;
			}
			setAIMoveSpeed((float) mountedSpeed());
		}
		if (flyingMount()) {
			if (worldObj.isRemote) return;
			if (!hasFlightHome) { flightHomeX = posX; flightHomeY = posY; flightHomeZ = posZ; hasFlightHome = true; }
			if (!isChild() && !worldObj.getEntitiesWithinAABB(EntityPlayer.class, net.minecraft.util.AxisAlignedBB.getBoundingBox(boundingBox.minX, boundingBox.maxY - .1, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY + .6, boundingBox.maxZ)).isEmpty()) { motionX = motionY = motionZ = 0; rotationYaw = Math.round(rotationYaw / 90) * 90; return; }
			if (--flyTicks <= 0 || isCollidedHorizontally) {
				flyTicks = 60 + rand.nextInt(100); flyX = flightHomeX + (rand.nextDouble() - .5) * 24; flyY = MathHelper.clamp_double(flightHomeY + (rand.nextDouble() - .5) * 12, 8, 240); flyZ = flightHomeZ + (rand.nextDouble() - .5) * 24;
				EntityPlayer tempting = worldObj.getClosestPlayerToEntity(this, 16); ItemStack item = tempting == null ? null : tempting.getHeldItem(); if (item != null && (item.getItem() == Items.snowball || !isChild() && item.getItem() == ganymedes01.etfuturum.ModItems.HARNESS.get())) { flyX = tempting.posX; flyY = tempting.posY + 3; flyZ = tempting.posZ; flyTicks = 20; }
			}
			double dx = flyX - posX, dy = flyY - posY, dz = flyZ - posZ, length = Math.max(1, Math.sqrt(dx * dx + dy * dy + dz * dz));
			if (worldObj.blockExists(MathHelper.floor_double(flyX), MathHelper.floor_double(flyY), MathHelper.floor_double(flyZ)) && worldObj.func_147447_a(net.minecraft.util.Vec3.createVectorHelper(posX, posY + height / 2, posZ), net.minecraft.util.Vec3.createVectorHelper(flyX, flyY + height / 2, flyZ), false, true, false) == null) { motionX += dx / length * .006; motionY += dy / length * .006; motionZ += dz / length * .006; rotationYaw = (float) (Math.atan2(motionZ, motionX) * 180 / Math.PI) - 90; renderYawOffset = rotationYaw; } else { flyTicks = 0; motionY += .015; }
			moveEntity(motionX, motionY, motionZ); motionX *= .92; motionY *= .92; motionZ *= .92; return;
		}
		if (aquaticMount() && isInWater()) { if (!worldObj.isRemote) { if (swimmingMate != null && swimmingMate.isEntityAlive() && isInLove()) { double dx = swimmingMate.posX - posX, dy = swimmingMate.posY - posY, dz = swimmingMate.posZ - posZ, length = Math.max(1, Math.sqrt(dx * dx + dy * dy + dz * dz)); motionX += (dx / length * .12 - motionX) * .15; motionY += (dy / length * .12 - motionY) * .15; motionZ += (dz / length * .12 - motionZ) * .15; } else if (riddenByEntity instanceof net.minecraft.entity.monster.EntityMob mob && mob.getAttackTarget() != null) { EntityLivingBase target = mob.getAttackTarget(); double dx = target.posX - posX, dy = target.posY - posY, dz = target.posZ - posZ, length = Math.max(1, Math.sqrt(dx * dx + dy * dy + dz * dz)); motionX += (dx / length * .2 - motionX) * .1; motionY += (dy / length * .2 - motionY) * .1; motionZ += (dz / length * .2 - motionZ) * .1; } else if (ticksExisted % 40 == 0) { motionX = (rand.nextDouble() - .5) * .15; motionY = (rand.nextDouble() - .5) * .08; motionZ = (rand.nextDouble() - .5) * .15; } if (isCollidedHorizontally) motionY += .03; moveEntity(motionX, motionY, motionZ); motionX *= .98; motionY *= .98; motionZ *= .98; } return; }
		super.moveEntityWithHeading(strafe, forward);
	}
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote) { if (dashCooldown > 0) --dashCooldown; if (aquaticMount() && ticksExisted % 20 == 0) { swimmingMate = null; if (isInLove()) for (Object object : worldObj.getEntitiesWithinAABB(EntityRideableMob.class, boundingBox.expand(8, 4, 8))) { EntityRideableMob candidate = (EntityRideableMob) object; if (canMateWith(candidate)) { swimmingMate = candidate; break; } } } } }
	public void positionPassenger(Entity passenger, int index) {
		double localX = passengerCount() == 4 ? index == 1 ? 1.5 : index == 3 ? -1.5 : 0 : 0;
		double localZ = passengerCount() == 4 ? index == 2 ? 1.5 : index == 0 ? -1.5 : 0 : index == 0 ? -.6 : .7;
		double yaw = rotationYaw * Math.PI / 180;
		passenger.setPosition(posX + localX * Math.cos(yaw) - localZ * Math.sin(yaw), posY + getMountedYOffset() + passenger.getYOffset(), posZ + localZ * Math.cos(yaw) + localX * Math.sin(yaw));
	}
	@Override public void updateRiderPosition() { if (riddenByEntity != null) positionPassenger(riddenByEntity, 0); }
	@Override protected void dropFewItems(boolean player, int looting) { if (isSaddled()) entityDropItem(this instanceof EntityHappyGhast ? ganymedes01.etfuturum.ModItems.HARNESS.newItemStack(1, getVariant()) : new ItemStack(Items.saddle), 0); if (getBodyEquipment() != null) { entityDropItem(getBodyEquipment(), 0); bodyEquipment(null); } }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("Saddle", isSaddled()); tag.setInteger("Variant", getVariant()); tag.setInteger("DashCooldown", dashCooldown); if (getBodyEquipment() != null) tag.setTag("BodyArmorItem", getBodyEquipment().writeToNBT(new NBTTagCompound())); if (hasFlightHome) { tag.setDouble("FlightHomeX", flightHomeX); tag.setDouble("FlightHomeY", flightHomeY); tag.setDouble("FlightHomeZ", flightHomeZ); } }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); flag(1, tag.getBoolean("Saddle")); dataWatcher.updateObject(21, MathHelper.clamp_int(tag.getInteger("Variant"), 0, 15)); dashCooldown = MathHelper.clamp_int(tag.getInteger("DashCooldown"), 0, 55); if (tag.hasKey("BodyArmorItem", 10)) bodyEquipment(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("BodyArmorItem"))); if (tag.hasKey("FlightHomeX")) { flightHomeX = tag.getDouble("FlightHomeX"); flightHomeY = tag.getDouble("FlightHomeY"); flightHomeZ = tag.getDouble("FlightHomeZ"); hasFlightHome = Double.isFinite(flightHomeX) && Double.isFinite(flightHomeY) && Double.isFinite(flightHomeZ); } }
}
