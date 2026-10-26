package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.world.structure.MonumentSpawning;
import ganymedes01.etfuturum.world.structure.MapGenOceanMonument;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/** Native aquatic hostile AI, with a server-owned charged beam and saved elder state. */
public class EntityGuardian extends EntityMob {
	private int swimTicks;
	private double swimX, swimY, swimZ;
	public EntityGuardian(World world) {
		super(world); setSize(.85F, .85F); experienceValue = 10;
		tasks.addTask(1, new BeamAttack());
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true));
		targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntitySquid.class, 10, true));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(16, (byte) 0); dataWatcher.addObject(17, 0); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.5);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16);
	}
	public boolean isElder() { return (dataWatcher.getWatchableObjectByte(16) & 1) != 0; }
	public boolean isSwimming() { return (dataWatcher.getWatchableObjectByte(16) & 2) != 0; }
	public int beamTarget() { return dataWatcher.getWatchableObjectInt(17); }
	public void setElder(boolean elder) {
		dataWatcher.updateObject(16, (byte) ((dataWatcher.getWatchableObjectByte(16) & ~1) | (elder ? 1 : 0)));
		setSize(elder ? 1.9975F : .85F, elder ? 1.9975F : .85F);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(elder ? 80 : 30);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(elder ? 8 : 6);
		if (elder) { setHealth(80); func_110163_bv(); }
	}
	@Override public boolean canBreatheUnderwater() { return true; }
	@Override protected void fall(float distance) { }
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == MonumentSpawning.GUARDIANS; }
	@Override public boolean getCanSpawnHere() {
		int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
		return worldObj.difficultySetting != EnumDifficulty.PEACEFUL && worldObj.getBlock(x, y, z).getMaterial() == net.minecraft.block.material.Material.water
				&& worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
				&& MapGenOceanMonument.contains(worldObj, x, y, z) && (rand.nextInt(20) == 0 || !worldObj.canBlockSeeTheSky(x, y, z));
	}
	@Override public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!worldObj.isRemote && !isSwimming() && !source.isMagicDamage() && !source.isExplosion()
				&& !"thorns".equals(source.getDamageType()) && source.getSourceOfDamage() instanceof EntityLivingBase)
			source.getSourceOfDamage().attackEntityFrom(DamageSource.causeThornsDamage(this), 2);
		return super.attackEntityFrom(source, amount);
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); setAir(300);
		if (!worldObj.isRemote) {
			if (!isInWater() && onGround) { motionY = .5; motionX += (rand.nextFloat() - .5) * .4; motionZ += (rand.nextFloat() - .5) * .4; rotationYaw = rand.nextFloat() * 360; onGround = false; }
			if (isElder() && (ticksExisted + getEntityId()) % 1200 == 0) {
				for (Object object : worldObj.playerEntities) {
					EntityPlayer player = (EntityPlayer) object;
					if (player.capabilities.disableDamage || getDistanceSqToEntity(player) >= 2500) continue;
					PotionEffect effect = player.getActivePotionEffect(Potion.digSlowdown);
					if (effect == null || effect.getAmplifier() < 2 || effect.getDuration() < 1200) player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 6000, 2));
				}
			}
		}
		if (worldObj.isRemote && isElder() && width < 1) setSize(1.9975F, 1.9975F);
	}
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (!worldObj.isRemote && isInWater()) {
			EntityLivingBase target = getAttackTarget();
			boolean moving = beamTarget() == 0;
			if (moving) {
				if (--swimTicks <= 0 || isCollidedHorizontally) {
					swimTicks = 40 + rand.nextInt(40);
					swimX = posX + (rand.nextDouble() - .5) * 12;
					swimY = posY + (rand.nextDouble() - .5) * 6;
					swimZ = posZ + (rand.nextDouble() - .5) * 12;
				}
				if (target != null && getDistanceSqToEntity(target) > 64) { swimX = target.posX; swimY = target.posY + target.getEyeHeight(); swimZ = target.posZ; }
				int x = MathHelper.floor_double(swimX), y = MathHelper.floor_double(swimY), z = MathHelper.floor_double(swimZ);
				if (worldObj.blockExists(x, y, z) && worldObj.getBlock(x, y, z).getMaterial() == net.minecraft.block.material.Material.water) {
					double dx = swimX - posX, dy = swimY - posY, dz = swimZ - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (length > .5) { double acceleration = isElder() ? .012 : .025; motionX += dx / length * acceleration; motionY += dy / length * acceleration; motionZ += dz / length * acceleration; rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; }
				} else swimTicks = 0;
			}
			dataWatcher.updateObject(16, (byte) ((isElder() ? 1 : 0) | (moving ? 2 : 0)));
			moveEntity(motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9;
		} else super.moveEntityWithHeading(strafe, forward);
	}
	@Override protected void dropFewItems(boolean player, int looting) {
		if (isElder() && rand.nextInt(5) == 0 && ModItems.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE.isEnabled()) entityDropItem(ModItems.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE.newItemStack(), 0);
		int shards = rand.nextInt(3) + rand.nextInt(looting + 1);
		if (shards > 0 && ModItems.PRISMARINE_SHARD.isEnabled()) entityDropItem(ModItems.PRISMARINE_SHARD.newItemStack(shards), 0);
		int bonus = rand.nextInt(5);
		if (bonus < 2) entityDropItem(new ItemStack(isBurning() ? Items.cooked_fished : Items.fish), 0);
		else if (bonus < 4 && ModItems.PRISMARINE_CRYSTALS.isEnabled()) entityDropItem(ModItems.PRISMARINE_CRYSTALS.newItemStack(), 0);
		if (isElder() && player && ModBlocks.SPONGE.isEnabled()) entityDropItem(ModBlocks.SPONGE.newItemStack(1, 1), 0);
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("Elder", isElder()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); float health = getHealth(); setElder(tag.getBoolean("Elder")); setHealth(health); }
	private class BeamAttack extends EntityAIBase {
		private int charge;
		BeamAttack() { setMutexBits(3); }
		@Override public boolean shouldExecute() { EntityLivingBase target = getAttackTarget(); return target != null && target.isEntityAlive() && (isElder() || getDistanceSqToEntity(target) > 9); }
		@Override public boolean continueExecuting() { return shouldExecute() && getDistanceSqToEntity(getAttackTarget()) <= 256; }
		@Override public void startExecuting() { charge = -10; getNavigator().clearPathEntity(); }
		@Override public void resetTask() { dataWatcher.updateObject(17, 0); setAttackTarget(null); }
		@Override public void updateTask() {
			EntityLivingBase target = getAttackTarget();
			if (target == null || !canEntityBeSeen(target)) { setAttackTarget(null); return; }
			getLookHelper().setLookPositionWithEntity(target, 90, 90);
			if (++charge == 0) dataWatcher.updateObject(17, target.getEntityId());
			if (charge >= (isElder() ? 60 : 80)) {
				target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(EntityGuardian.this, EntityGuardian.this), (worldObj.difficultySetting == EnumDifficulty.HARD ? 3 : 1) + (isElder() ? 2 : 0));
				target.attackEntityFrom(DamageSource.causeMobDamage(EntityGuardian.this), (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
				setAttackTarget(null);
			}
		}
	}

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.guardian.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.guardian.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.guardian.death"; }
}
