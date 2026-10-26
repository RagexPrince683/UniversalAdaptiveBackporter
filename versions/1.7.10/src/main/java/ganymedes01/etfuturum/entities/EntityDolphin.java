package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityDolphin extends EntityAquaticAnimal {
	private int moisture = 2400, attackTicks, playTicks;
	private EntityPlayer following;
	private EntityItem toy;
	private int treasureTicks;
	private int treasureX, treasureY, treasureZ;
	private boolean treasureKnown;
	public EntityDolphin(World world) { super(world); setSize(.9F, .6F); setAir(4800); targetTasks.addTask(1, new EntityAIHurtByTarget(this, true)); }
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == ganymedes01.etfuturum.world.MobSpawning.DOLPHINS; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1.2); }
	@Override public EntityAgeable createChild(EntityAgeable other) { return null; }
	@Override protected boolean canDespawn() { return !hasCustomNameTag(); }
	@Override public boolean isBreedingItem(ItemStack stack) { return false; }
	@Override public boolean getCanSpawnHere() { return wetSpawn(45, 63); }
	@Override protected double swimAcceleration() { return .035; }
	@Override public int getMaxSpawnedInChunk() { return 5; }
	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.dolphin.ambient_water"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.dolphin.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.dolphin.death"; }
	@Override protected void fall(float distance) { }
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (held != null && held.getItem() == Items.fish && held.getItemDamage() < 2) {
			if (!worldObj.isRemote) {
				if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				treasureTicks = 1200; treasureKnown = false;
				// Prefer real saved treasure, retaining the existing monument fallback without generating terrain.
				net.minecraft.world.ChunkPosition position = ganymedes01.etfuturum.world.structure.OceanTreasure.nearestSaved(worldObj, posX, posZ);
				if (position == null) position = ganymedes01.etfuturum.world.structure.OceanSites.nearestSaved(worldObj, posX, posZ);
				if (position == null) position = ganymedes01.etfuturum.world.structure.MapGenOceanMonument.nearestSaved(worldObj, posX, posZ);
				if (position != null) { treasureKnown = true; treasureX = position.chunkPosX; treasureY = position.chunkPosY; treasureZ = position.chunkPosZ; }
			}
			return true;
		}
		return super.interact(player);
	}
	@Override protected boolean swimDestination() {
		EntityLivingBase target = getAttackTarget();
		if (target != null && target.isEntityAlive() && target.isInWater() && getDistanceSqToEntity(target) < 256) {
			swimX = target.posX; swimY = target.posY; swimZ = target.posZ;
			if (attackTicks == 0 && getDistanceSqToEntity(target) < 3 && canEntityBeSeen(target)) { attackTicks = 20; target.attackEntityFrom(DamageSource.causeMobDamage(this), 3); }
			return true;
		}
		setAttackTarget(null);
		if (getAir() < 200) { swimX = posX; swimY = posY + 2; swimZ = posZ; return true; }
		if (treasureTicks > 0 && treasureKnown) {
			double dx = treasureX - posX, dz = treasureZ - posZ, length = Math.sqrt(dx * dx + dz * dz);
			if (length < 5) { treasureTicks = 0; return false; }
			swimX = posX + dx / length * 6; swimY = Math.min(61, posY); swimZ = posZ + dz / length * 6; return true;
		}
		if (toy != null && toy.isEntityAlive() && getDistanceSqToEntity(toy) < 64 && toy.isInWater()) {
			swimX = toy.posX; swimY = toy.posY; swimZ = toy.posZ;
			if (getDistanceSqToEntity(toy) < 2 && ++playTicks >= 20) { Vec3 look = getLookVec(); toy.addVelocity(look.xCoord * .3, .2, look.zCoord * .3); toy.delayBeforeCanPickup = 20; playTicks = 0; toy = null; }
			return true;
		}
		toy = null;
		if (following != null && following.isEntityAlive() && following.isInWater() && getDistanceSqToEntity(following) < 256 && !following.capabilities.disableDamage) {
			if (getDistanceSqToEntity(following) < 6) return false;
			swimX = following.posX; swimY = following.posY; swimZ = following.posZ; return true;
		}
		return false;
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (!isInWater()) setAir(4800);
		if (isWet()) moisture = 2400; else if (--moisture <= 0 && ticksExisted % 20 == 0) attackEntityFrom(DamageSource.drown, 1);
		if (!isInWater() && onGround && ticksExisted % 20 == 0) { motionX += (rand.nextDouble() - .5) * .4; motionZ += (rand.nextDouble() - .5) * .4; motionY = .5; onGround = false; }
		if (attackTicks > 0) --attackTicks; if (treasureTicks > 0) --treasureTicks;
		if (ticksExisted % 40 == 0) {
			following = worldObj.getClosestVulnerablePlayerToEntity(this, 16);
			if (following != null && following.isInWater() && getDistanceSqToEntity(following) < 64) following.addPotionEffect(new net.minecraft.potion.PotionEffect(ganymedes01.etfuturum.potion.ModPotions.dolphinGrace.id, 100, 0));
			if (toy == null) for (Object object : worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(6, 3, 6))) { EntityItem item = (EntityItem) object; if (item.isInWater() && item.delayBeforeCanPickup <= 0) { toy = item; break; } }
		}
	}
	@Override protected void dropFewItems(boolean player, int looting) { if (rand.nextInt(3) == 0) entityDropItem(new ItemStack(isBurning() ? Items.cooked_fished : Items.fish), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Moistness", moisture); tag.setInteger("TreasureTicks", treasureTicks); tag.setBoolean("TreasureKnown", treasureKnown); tag.setInteger("TreasureX", treasureX); tag.setInteger("TreasureY", treasureY); tag.setInteger("TreasureZ", treasureZ); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); moisture = tag.hasKey("Moistness") ? MathHelper.clamp_int(tag.getInteger("Moistness"), 0, 2400) : 2400; treasureTicks = MathHelper.clamp_int(tag.getInteger("TreasureTicks"), 0, 1200); treasureKnown = tag.getBoolean("TreasureKnown"); treasureX = tag.getInteger("TreasureX"); treasureY = tag.getInteger("TreasureY"); treasureZ = tag.getInteger("TreasureZ"); }
}
