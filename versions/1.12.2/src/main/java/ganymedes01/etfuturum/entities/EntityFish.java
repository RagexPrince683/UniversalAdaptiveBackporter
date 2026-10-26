package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.Content;
import ganymedes01.etfuturum.Features;

import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.network.datasync.*;
import net.minecraft.world.World;

import java.util.List;

/** Fish steering stays in loaded water and is owned by the native entity tick. */
public abstract class EntityFish extends EntityWaterMob {

	private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityFish.class, DataSerializers.VARINT);
	private static final String[] FISH_NAMES = {"cod", "salmon", "tropical_fish", "pufferfish"};
	private boolean fromBucket;
	private int destinationTicks;
	private double swimX, swimY, swimZ;
	private EntityFish schoolLeader;
	private int panicTicks;

	public EntityFish(World world) { super(world); setSize(.5F, .3F); }
	public abstract int fishType();
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == ganymedes01.etfuturum.MobRegistry.FISH; }
	@Override protected void entityInit() { super.entityInit(); dataManager.register(VARIANT, 0); }
	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1);
	}
	@Override protected boolean canDespawn() { return !fromBucket && !hasCustomName(); }
	@Override public boolean canBreatheUnderwater() { return true; }
	public int getVariant() { return dataManager.get(VARIANT); }
	public void setVariant(int variant) { dataManager.set(VARIANT, variant); }
	public void readBucket(NBTTagCompound tag) {
		fromBucket = true; enablePersistence();
		setVariant(tag.getInteger("Variant"));
		if (tag.hasKey("Health")) setHealth(MathHelper.clamp(tag.getFloat("Health"), 1, getMaxHealth()));
	}
	public ItemStack bucket() {
		ItemStack bucket = Content.stack(FISH_NAMES[fishType()] + "_bucket");
		NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("Variant", getVariant()); tag.setFloat("Health", getHealth()); bucket.setTagCompound(tag);
		if (hasCustomName()) bucket.setStackDisplayName(getCustomNameTag());
		return bucket;
	}
	@Override public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (isEntityAlive() && held.getItem() == Items.WATER_BUCKET && Features.enabled("content.fish_buckets")) {
            if (!world.isRemote) {
                ItemStack filled = bucket();
                if (!player.capabilities.isCreativeMode && held.getCount() == 1) player.setHeldItem(hand, filled);
                else { if (!player.capabilities.isCreativeMode) held.shrink(1); if (!player.inventory.addItemStackToInventory(filled)) player.dropItem(filled, false); }
                setDead();
            }
            return true;
        }
        return super.processInteract(player, hand);
    }
	@Override public boolean getCanSpawnHere() {
		int x = MathHelper.floor(posX), y = MathHelper.floor(posY), z = MathHelper.floor(posZ);
		return Features.enabled("mobs." + FISH_NAMES[fishType()]) && Features.enabled("entities.enableAquaticSpawning") && world.provider.getDimension() == 0 && y >= 45 && y <= 63 && waterAt(x, y, z) && waterAt(x, y + 1, z)
				&& world.checkNoEntityCollision(getEntityBoundingBox()) && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty();
	}
	protected final boolean waterAt(int x, int y, int z) { return world.isBlockLoaded(new BlockPos(x, y, z)) && world.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.WATER; }
	protected final boolean canSwimTo(double x, double y, double z) {
		int bx = MathHelper.floor(x), by = MathHelper.floor(y), bz = MathHelper.floor(z);
		return waterAt(bx, by, bz) && world.rayTraceBlocks(new Vec3d(posX, posY + height * .5, posZ), new Vec3d(x, y, z), false, true, false) == null;
	}
	@Override public boolean attackEntityFrom(DamageSource source, float damage) { boolean hit = super.attackEntityFrom(source, damage); if (hit) panicTicks = 100; return hit; }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		if (world.isRemote) return;
		if (isInWater()) { setAir(300); if (panicTicks > 0) --panicTicks; }
		else {
			if (onGround) { motionX += (rand.nextFloat() - .5) * .1; motionZ += (rand.nextFloat() - .5) * .1; motionY = .4; onGround = false; isAirBorne = true; }
		}
		if (fishType() != 3 && (ticksExisted + getEntityId()) % 200 == 0) {
			List<? extends EntityFish> fish = world.getEntitiesWithinAABB(getClass(), getEntityBoundingBox().grow(8, 4, 8));
			schoolLeader = null;
			int maximumSchool = fishType() == 1 ? 5 : 8;
			if (fish.size() <= maximumSchool) for (EntityFish other : fish) {
				if (other != this && other.isEntityAlive() && other.getEntityId() < getEntityId()
						&& (schoolLeader == null || other.getEntityId() < schoolLeader.getEntityId())) schoolLeader = other;
			}
		}
	}
	@Override public void travel(float strafe, float vertical, float forward) {
		if (!isInWater() || isAIDisabled()) { super.travel(strafe, vertical, forward); return; }
		if (world.isRemote) return;
		if (schoolLeader != null && (!schoolLeader.isEntityAlive() || schoolLeader.world != world || getDistanceSq(schoolLeader) > 121)) schoolLeader = null;
		if (--destinationTicks <= 0 || collidedHorizontally) {
			destinationTicks = 20 + rand.nextInt(30);
			EntityPlayer avoid = world.getClosestPlayerToEntity(this, 8);
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
		move(MoverType.SELF, motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9;
	}
	@Override public void fall(float distance, float multiplier) { }
	@Override protected void dropLoot(boolean player, int looting, DamageSource source) { entityDropItem(new ItemStack(isBurning() && fishType() < 2 ? Items.COOKED_FISH : Items.FISH, 1, fishType()), 0); if (player && rand.nextInt(20) == 0) entityDropItem(new ItemStack(Items.DYE, 1, 15), 0); }
	@Override protected SoundEvent getAmbientSound() { return null; }

	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("FromBucket", fromBucket); tag.setInteger("Variant", getVariant()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); fromBucket = tag.getBoolean("FromBucket"); setVariant(tag.getInteger("Variant")); }
}
