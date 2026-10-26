package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
public class EntityNautilus extends EntityRideableMob {
	public boolean isTamed() { return dataWatcher.getWatchableObjectByte(23) != 0; }
	protected void setTamed(boolean value) { dataWatcher.updateObject(23, (byte) (value ? 1 : 0)); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(23, (byte) 0); }
	private int dryTicks;
	private EntityLivingBase hunting;
	private int attackCooldown;
	public EntityNautilus(World world) { super(world); setSize(1.2F, 1.1F); }
	@Override public int passengerCount() { return 1; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15); }
	@Override public double getMountedYOffset() { return 1.05; }
	@Override protected boolean aquaticMount() { return true; }
	@Override public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) { return type == ganymedes01.etfuturum.world.MobSpawning.NAUTILUS; }
	@Override public boolean getCanSpawnHere() {
		int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
		// Natural admission precedes the entity's first water-movement update.
		return worldObj.provider.dimensionId == 0 && y > 20 && y < 62 && worldObj.blockExists(x, y + 1, z)
				&& worldObj.getBlock(x, y, z).getMaterial() == net.minecraft.block.material.Material.water && worldObj.getBlock(x, y + 1, z).getMaterial() == net.minecraft.block.material.Material.water
				&& worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty() && worldObj.checkNoEntityCollision(boundingBox)
				&& (!(this instanceof EntityZombieNautilus) || ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableModernHostileSpawning && worldObj.difficultySetting != net.minecraft.world.EnumDifficulty.PEACEFUL && !worldObj.isDaytime());
	}
	@Override protected boolean canDespawn() { return !isTamed() && !hasCustomNameTag(); }
	@Override public boolean canBreatheUnderwater() { return true; }
	@Override protected boolean mayRide(EntityPlayer player) { return isTamed() && super.mayRide(player); }
	@Override public boolean isBreedingItem(ItemStack stack) { return isTamed() && stack != null && stack.getItem() == Items.fish && stack.getItemDamage() == 3; }
	@Override public EntityAgeable createChild(EntityAgeable mate) { EntityNautilus child = new EntityNautilus(worldObj); child.setTamed(true); return child; }
	@Override public boolean interact(EntityPlayer player) { ItemStack held = player.getHeldItem(); if (!isTamed() && held != null && held.getItem() == Items.fish && held.getItemDamage() == 3) { if (!worldObj.isRemote) { consume(player, held); if (rand.nextInt(3) == 0) { setTamed(true); func_110163_bv(); worldObj.setEntityState(this, (byte) 7); } } return true; } if (isTamed() && held != null && held.getItem() == ModItems.NAUTILUS_ARMOR.get()) { if (!worldObj.isRemote) { if (getBodyEquipment() != null) entityDropItem(getBodyEquipment(), 0); ItemStack armor = held.copy(); armor.stackSize = 1; bodyEquipment(armor); consume(player, held); } return true; } if (isTamed() && player.isSneaking() && held == null && getBodyEquipment() != null) { if (!worldObj.isRemote) { entityDropItem(getBodyEquipment(), 0); bodyEquipment(null); } return true; } return super.interact(player); }
	@Override public int getTotalArmorValue() { ItemStack armor = getBodyEquipment(); return armor == null ? 0 : new int[]{3, 5, 7, 11, 15}[MathHelper.clamp_int(armor.getItemDamage(), 0, 4)]; }
	@Override public boolean attackEntityFrom(net.minecraft.util.DamageSource source, float amount) { boolean hit = super.attackEntityFrom(source, amount); if (hit && !worldObj.isRemote && source.getEntity() instanceof EntityLivingBase target) hunting = target; return hit; }
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (!worldObj.isRemote && riddenByEntity == null && isInWater() && !isChild()) {
			if (attackCooldown > 0) --attackCooldown;
			if (hunting == null && ticksExisted % 80 == 0) for (Object object : worldObj.getEntitiesWithinAABB(EntityPufferfish.class, boundingBox.expand(12, 6, 12))) { hunting = (EntityPufferfish) object; break; }
			if (hunting != null && (!hunting.isEntityAlive() || getDistanceSqToEntity(hunting) > 256 || !hunting.isInWater())) hunting = null;
			if (hunting != null && canEntityBeSeen(hunting)) { double dx = hunting.posX - posX, dy = hunting.posY - posY, dz = hunting.posZ - posZ, length = Math.max(1, Math.sqrt(dx * dx + dy * dy + dz * dz)); motionX += dx / length * .03; motionY += dy / length * .03; motionZ += dz / length * .03; if (length < 2 && attackCooldown == 0) { hunting.attackEntityFrom(net.minecraft.util.DamageSource.causeMobDamage(this), 4); attackCooldown = 40; } moveEntity(motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9; return; }
		}
		super.moveEntityWithHeading(strafe, forward);
	}
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote) { dryTicks = isWet() ? 0 : dryTicks + 1; if (dryTicks > 300 && ticksExisted % 20 == 0) attackEntityFrom(net.minecraft.util.DamageSource.drown, 1); if (riddenByEntity instanceof EntityPlayer rider && ticksExisted % 20 == 0) { net.minecraft.potion.PotionEffect effect = rider.getActivePotionEffect(ganymedes01.etfuturum.potion.ModPotions.nautilusBreath); if (effect == null || effect.getAmplifier() == 0 && effect.getDuration() < 60) rider.addPotionEffect(new net.minecraft.potion.PotionEffect(ganymedes01.etfuturum.potion.ModPotions.nautilusBreath.id, 80, 0)); } } }
	@Override protected void dropFewItems(boolean player, int looting) { super.dropFewItems(player, looting); if (this instanceof EntityZombieNautilus) { int count = rand.nextInt(4) + rand.nextInt(looting + 1); if (count > 0) dropItem(Items.rotten_flesh, count); } else if (player && rand.nextFloat() < .05F + .01F * looting) entityDropItem(ModItems.NAUTILUS_SHELL.newItemStack(), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("Tame", isTamed()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); setTamed(tag.getBoolean("Tame")); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.nautilus.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.nautilus.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.nautilus.death"; }
}
