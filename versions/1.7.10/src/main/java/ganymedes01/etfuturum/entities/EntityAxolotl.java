package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityAxolotl extends EntityAquaticAnimal {
	private int moisture = 6000, playDeadTicks, huntCooldown, attackCooldown;
	private boolean fromBucket;
	private EntityAxolotl courting;
	public EntityAxolotl(World world) {
		super(world); setSize(.75F, .42F);
		tasks.addTask(1, new EntityAIMate(this, 1));
		tasks.addTask(2, new EntityAITempt(this, 1, ModItems.TROPICAL_FISH_BUCKET.get(), false));
		tasks.addTask(3, new EntityAIFollowParent(this, 1)); tasks.addTask(4, new EntityAIWander(this, .6));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, 0); dataWatcher.addObject(21, (byte) 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(14); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.15); }
	@Override public boolean canBreatheUnderwater() { return true; }
	@Override protected boolean canDespawn() { return !fromBucket && !hasCustomNameTag(); }
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == ganymedes01.etfuturum.world.MobSpawning.AXOLOTLS; }
	public int getVariant() { return dataWatcher.getWatchableObjectInt(20); }
	private void variant(int value) { dataWatcher.updateObject(20, MathHelper.clamp_int(value, 0, 4)); }
	public boolean isPlayingDead() { return dataWatcher.getWatchableObjectByte(21) != 0; }
	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.axolotl." + (isInWater() ? "idle_water" : "idle_air"); }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.axolotl.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.axolotl.death"; }
	@Override protected boolean canSwim() { return !isPlayingDead(); }
	@Override public boolean getCanSpawnHere() { return wetSpawn(1, 62) && !worldObj.canBlockSeeTheSky(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)); }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { super.onSpawnWithEgg(data); variant(rand.nextInt(4)); return data; }
	@Override public boolean isBreedingItem(ItemStack stack) { return stack != null && stack.getItem() == ModItems.TROPICAL_FISH_BUCKET.get(); }
	@Override public EntityAgeable createChild(EntityAgeable other) {
		EntityAxolotl child = new EntityAxolotl(worldObj); child.variant(rand.nextInt(1200) == 0 ? 4 : rand.nextBoolean() ? getVariant() : ((EntityAxolotl) other).getVariant()); return child;
	}
	public void readBucket(NBTTagCompound tag) { fromBucket = true; func_110163_bv(); variant(tag.getInteger("Variant")); setGrowingAge(MathHelper.clamp_int(tag.getInteger("Age"), -24000, 6000)); if (tag.hasKey("Health")) setHealth(MathHelper.clamp_float(tag.getFloat("Health"), 1, getMaxHealth())); }
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (held != null && held.getItem() == Items.water_bucket && isEntityAlive()) {
			if (!worldObj.isRemote) {
				ItemStack bucket = ModItems.AXOLOTL_BUCKET.newItemStack(); NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("Variant", getVariant()); tag.setInteger("Age", getGrowingAge()); tag.setFloat("Health", getHealth()); bucket.setTagCompound(tag);
				if (hasCustomNameTag()) bucket.setStackDisplayName(getCustomNameTag());
				if (!player.capabilities.isCreativeMode) player.inventory.setInventorySlotContents(player.inventory.currentItem, bucket);
				else if (!player.inventory.addItemStackToInventory(bucket)) player.dropPlayerItemWithRandomChoice(bucket, false);
				setDead();
			}
			return true;
		}
		boolean food = isBreedingItem(held), consumed = super.interact(player);
		if (food && consumed && !worldObj.isRemote && !player.capabilities.isCreativeMode) {
			ItemStack empty = new ItemStack(Items.water_bucket);
			if (held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, empty);
			else if (!player.inventory.addItemStackToInventory(empty)) player.dropPlayerItemWithRandomChoice(empty, false);
		}
		return consumed;
	}
	@Override public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean hit = super.attackEntityFrom(source, amount);
		if (hit && !worldObj.isRemote && isInWater() && source.getEntity() instanceof EntityLivingBase && rand.nextInt(3) == 0 && (getHealth() < getMaxHealth() / 2 || amount > rand.nextInt(3))) {
			playDeadTicks = 200; dataWatcher.updateObject(21, (byte) 1); setAttackTarget(null); getNavigator().clearPathEntity(); addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
		}
		return hit;
	}
	@Override protected boolean swimDestination() {
		if (isInLove()) { if (ticksExisted % 20 == 0) { courting = null; for (Object object : worldObj.getEntitiesWithinAABB(EntityAxolotl.class, boundingBox.expand(8, 4, 8))) { EntityAxolotl mate = (EntityAxolotl) object; if (canMateWith(mate)) { courting = mate; break; } } } if (courting != null && courting.isEntityAlive()) { swimX = courting.posX; swimY = courting.posY; swimZ = courting.posZ; return true; } }
		EntityLivingBase target = getAttackTarget(); if (target == null || !target.isEntityAlive() || getDistanceSqToEntity(target) > 256 || !target.isInWater()) { setAttackTarget(null); return false; }
		swimX = target.posX; swimY = target.posY + target.height / 2; swimZ = target.posZ;
		if (attackCooldown == 0 && getDistanceSqToEntity(target) < 2.5 && canEntityBeSeen(target)) {
			attackCooldown = 20; target.attackEntityFrom(DamageSource.causeMobDamage(this), 2);
			if (!target.isEntityAlive()) {
				huntCooldown = 2400;
				for (Object object : worldObj.getEntitiesWithinAABB(EntityPlayer.class, boundingBox.expand(20, 20, 20))) {
					EntityPlayer player = (EntityPlayer) object;
					if (target.getAITarget() == player) { player.removePotionEffect(Potion.digSlowdown.id); PotionEffect regeneration = player.getActivePotionEffect(Potion.regeneration); player.addPotionEffect(new PotionEffect(Potion.regeneration.id, Math.min(2400, 100 + (regeneration == null ? 0 : regeneration.getDuration())), 0)); }
				}
			}
		}
		return true;
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (isWet()) moisture = 6000; else if (--moisture <= 0 && ticksExisted % 20 == 0) attackEntityFrom(DamageSource.drown, 2);
		if (attackCooldown > 0) --attackCooldown; if (huntCooldown > 0) --huntCooldown;
		if (playDeadTicks > 0) { if (--playDeadTicks == 0 || !isInWater()) { playDeadTicks = 0; dataWatcher.updateObject(21, (byte) 0); } return; }
		if (!isChild() && getAttackTarget() == null && ticksExisted % 40 == 0 && isInWater()) {
			double closest = 64;
			for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(8, 4, 8))) {
				EntityLivingBase target = (EntityLivingBase) object;
				if ((target instanceof EntityGuardian || huntCooldown == 0 && (target instanceof EntityFish || target instanceof net.minecraft.entity.passive.EntitySquid)) && target.isEntityAlive() && target.isInWater() && getDistanceSqToEntity(target) < closest && canEntityBeSeen(target)) { setAttackTarget(target); closest = getDistanceSqToEntity(target); }
			}
		}
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Variant", getVariant()); tag.setBoolean("FromBucket", fromBucket); tag.setInteger("Moisture", moisture); tag.setInteger("HuntCooldown", huntCooldown); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); variant(tag.getInteger("Variant")); fromBucket = tag.getBoolean("FromBucket"); moisture = tag.hasKey("Moisture") ? MathHelper.clamp_int(tag.getInteger("Moisture"), 0, 6000) : 6000; huntCooldown = MathHelper.clamp_int(tag.getInteger("HuntCooldown"), 0, 2400); }
}
