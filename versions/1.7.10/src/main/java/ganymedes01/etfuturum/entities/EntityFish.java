package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.Tags;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

/** Fish steering stays in loaded water and is owned by the native entity tick. */
public abstract class EntityFish extends EntityWaterMob {

	private boolean fromBucket;
	private int destinationTicks;
	private double swimX, swimY, swimZ;
	private EntityFish schoolLeader;
	private int panicTicks;

	public EntityFish(World world) { super(world); setSize(.5F, .3F); }
	public abstract int fishType();
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == ganymedes01.etfuturum.world.MobSpawning.FISH; }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(16, 0); }
	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(3);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1);
	}
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected boolean canDespawn() { return !fromBucket && !hasCustomNameTag(); }
	@Override public boolean canBreatheUnderwater() { return true; }
	public int getVariant() { return dataWatcher.getWatchableObjectInt(16); }
	public void setVariant(int variant) { dataWatcher.updateObject(16, variant); }
	public void readBucket(NBTTagCompound tag) {
		fromBucket = true; func_110163_bv();
		setVariant(tag.getInteger("Variant"));
		if (tag.hasKey("Health")) setHealth(MathHelper.clamp_float(tag.getFloat("Health"), 1, getMaxHealth()));
	}
	public ItemStack bucket() {
		ItemStack bucket = switch (fishType()) {
			case 1 -> ModItems.SALMON_BUCKET.newItemStack();
			case 2 -> ModItems.TROPICAL_FISH_BUCKET.newItemStack();
			case 3 -> ModItems.PUFFERFISH_BUCKET.newItemStack();
			default -> ModItems.COD_BUCKET.newItemStack();
		};
		NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("Variant", getVariant()); tag.setFloat("Health", getHealth()); bucket.setTagCompound(tag);
		if (hasCustomNameTag()) bucket.setStackDisplayName(getCustomNameTag());
		return bucket;
	}
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (isEntityAlive() && held != null && held.getItem() == Items.water_bucket) {
			if (!worldObj.isRemote) {
				ItemStack filled = bucket();
				if (!player.capabilities.isCreativeMode && held.stackSize == 1) player.inventory.setInventorySlotContents(player.inventory.currentItem, filled);
				else { if (!player.capabilities.isCreativeMode) --held.stackSize; if (!player.inventory.addItemStackToInventory(filled)) player.dropPlayerItemWithRandomChoice(filled, false); }
				playSound("random.splash", 1, 1); setDead();
			}
			return true;
		}
		return super.interact(player);
	}
	@Override public boolean getCanSpawnHere() {
		int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
		return worldObj.provider.dimensionId == 0 && y >= 45 && y <= 63 && waterAt(x, y, z) && waterAt(x, y + 1, z)
				&& worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
	}
	protected final boolean waterAt(int x, int y, int z) { return worldObj.blockExists(x, y, z) && worldObj.getBlock(x, y, z).getMaterial() == Material.water; }
	protected final boolean canSwimTo(double x, double y, double z) {
		int bx = MathHelper.floor_double(x), by = MathHelper.floor_double(y), bz = MathHelper.floor_double(z);
		return waterAt(bx, by, bz) && worldObj.func_147447_a(Vec3.createVectorHelper(posX, posY + height * .5, posZ), Vec3.createVectorHelper(x, y, z), false, true, false) == null;
	}
	@Override public boolean attackEntityFrom(DamageSource source, float damage) { boolean hit = super.attackEntityFrom(source, damage); if (hit) panicTicks = 100; return hit; }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		if (worldObj.isRemote) return;
		if (isInWater()) { setAir(300); if (panicTicks > 0) --panicTicks; }
		else {
			if (onGround) { motionX += (rand.nextFloat() - .5) * .1; motionZ += (rand.nextFloat() - .5) * .1; motionY = .4; onGround = false; isAirBorne = true; }
		}
		if (fishType() != 3 && (ticksExisted + getEntityId()) % 200 == 0) {
			List<? extends EntityFish> fish = worldObj.getEntitiesWithinAABB(getClass(), boundingBox.expand(8, 4, 8));
			schoolLeader = null;
			int maximumSchool = fishType() == 1 ? 5 : 8;
			if (fish.size() <= maximumSchool) for (EntityFish other : fish) {
				if (other != this && other.isEntityAlive() && other.getEntityId() < getEntityId()
						&& (schoolLeader == null || other.getEntityId() < schoolLeader.getEntityId())) schoolLeader = other;
			}
		}
	}
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (!isInWater()) { super.moveEntityWithHeading(strafe, forward); return; }
		if (worldObj.isRemote) return;
		if (schoolLeader != null && (!schoolLeader.isEntityAlive() || schoolLeader.worldObj != worldObj || getDistanceSqToEntity(schoolLeader) > 121)) schoolLeader = null;
		if (--destinationTicks <= 0 || isCollidedHorizontally) {
			destinationTicks = 20 + rand.nextInt(30);
			EntityPlayer avoid = worldObj.getClosestVulnerablePlayerToEntity(this, 8);
			for (int attempt = 0; attempt < 5; attempt++) {
				double x = posX + (rand.nextDouble() - .5) * 8, y = posY + (rand.nextDouble() - .5) * 4, z = posZ + (rand.nextDouble() - .5) * 8;
				if (avoid != null) { x = posX + (posX - avoid.posX) * .7; z = posZ + (posZ - avoid.posZ) * .7; }
				else if (schoolLeader != null) { x = schoolLeader.posX + (rand.nextDouble() - .5); y = schoolLeader.posY; z = schoolLeader.posZ + (rand.nextDouble() - .5); }
				if (canSwimTo(x, y, z)) { swimX = x; swimY = y; swimZ = z; break; }
			}
		}
		double dx = swimX - posX, dy = swimY - posY, dz = swimZ - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length > .4 && canSwimTo(swimX, swimY, swimZ)) {
			double acceleration = panicTicks > 0 ? .03 : .012;
			motionX += dx / length * acceleration; motionY += dy / length * acceleration; motionZ += dz / length * acceleration;
			rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; renderYawOffset = rotationYaw;
		} else destinationTicks = 0;
		moveEntity(motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9;
	}
	@Override protected void fall(float distance) { }
	@Override protected void dropFewItems(boolean player, int looting) { entityDropItem(new ItemStack(isBurning() && fishType() < 2 ? Items.cooked_fished : Items.fish, 1, fishType()), 0); if (player && rand.nextInt(20) == 0) entityDropItem(new ItemStack(Items.dye, 1, 15), 0); }
	@Override protected String getLivingSound() { return null; }
	@Override protected String getHurtSound() { return Tags.MC_ASSET_VER + ":entity." + fishName() + ".hurt"; }
	@Override protected String getDeathSound() { return Tags.MC_ASSET_VER + ":entity." + fishName() + ".death"; }
	private String fishName() { return switch (fishType()) { case 1 -> "salmon"; case 2 -> "tropical_fish"; case 3 -> "puffer_fish"; default -> "cod"; }; }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("FromBucket", fromBucket); tag.setInteger("Variant", getVariant()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); fromBucket = tag.getBoolean("FromBucket"); setVariant(tag.getInteger("Variant")); }
}
