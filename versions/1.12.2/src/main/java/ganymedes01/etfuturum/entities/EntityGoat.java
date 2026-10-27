package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.Content;
import ganymedes01.etfuturum.ModernSounds;
import ganymedes01.etfuturum.MobRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.*;
import net.minecraft.util.*;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;

import java.util.List;

public class EntityGoat extends EntityAnimal {

	private static final DataParameter<Byte> GOAT_FLAGS=EntityDataManager.createKey(EntityGoat.class,DataSerializers.BYTE);
	private static final int SCREAMING_FLAG = 1;
	private static final int LEFT_HORN_FLAG = 2;
	private static final int RIGHT_HORN_FLAG = 4;
	private static final byte LOWER_HEAD_EVENT = 58;
	private static final byte RAISE_HEAD_EVENT = 59;
	private static final int RAM_MIN_DISTANCE = 4;
	private static final int RAM_MAX_DISTANCE = 7;
	private static final int RAM_PREPARE_TICKS = 20;

	private boolean loweringHead;
	private int lowerHeadTick;
	private boolean wasChild;
	private int longJumpCooldown;
	private int ramCooldown;

	public EntityGoat(World world) {
		super(world);
		setSize(0.9F, 1.3F);
		setPathPriority(PathNodeType.WATER,-1);
		longJumpCooldown = nextLongJumpCooldown();
		ramCooldown = nextRamCooldown();

		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityGoat.AIRamTarget());
		tasks.addTask(2, new EntityGoat.AILongJump());
		tasks.addTask(3, new EntityAIPanic(this, 2.0D));
		tasks.addTask(4, new EntityAIMate(this, 1.0D));
		tasks.addTask(5, new EntityAITempt(this, 1.25D, Items.WHEAT, false));
		tasks.addTask(6, new EntityAIFollowParent(this, 1.25D));
		tasks.addTask(7, new EntityAIWander(this, 1.0D));
		tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		tasks.addTask(9, new EntityAILookIdle(this));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(GOAT_FLAGS, (byte) (LEFT_HORN_FLAG | RIGHT_HORN_FLAG));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
	}

	@Override
	public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty,IEntityLivingData livingData) {
		livingData = super.onInitialSpawn(difficulty,livingData);
		setScreamingGoat(rand.nextDouble() < 0.02D);
		updateHornsForAge(true);

		if (!isChild() && rand.nextFloat() < 0.1F) {
			if (rand.nextBoolean()) {
				setLeftHorn(false);
			} else {
				setRightHorn(false);
			}
		}

		wasChild = isChild();
		return livingData;
	}

	@Override
	public EntityAgeable createChild(EntityAgeable mate) {
		EntityGoat goat = new EntityGoat(world);
		EntityAgeable parent = rand.nextBoolean() ? this : mate;
		goat.setScreamingGoat(parent instanceof EntityGoat && ((EntityGoat) parent).isScreamingGoat() || rand.nextDouble() < 0.02D);
		goat.removeHorns();
		return goat;
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack != null && stack.getItem() == Items.WHEAT;
	}

	@Override
	public boolean processInteract(EntityPlayer player,EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack != null && stack.getItem() == Items.BUCKET && !isChild()) {
			playSound(getMilkingSound(), 1.0F, 1.0F);
			if (!player.capabilities.isCreativeMode) {
				stack.shrink(1);
				ItemStack milk = new ItemStack(Items.MILK_BUCKET);
				if (stack.isEmpty()) {
					player.setHeldItem(hand,milk);
				} else if (!player.inventory.addItemStackToInventory(milk)) {
					player.dropItem(milk,false);
				}
			}
			return true;
		}

		boolean eating = isBreedingItem(stack);
		boolean result = super.processInteract(player,hand);
		if (result && eating) {
			playSound(getEatSound(), 1.0F, 0.8F + rand.nextFloat() * 0.4F);
		}
		return result;
	}

	@Override
	public void onLivingUpdate() {
		if (loweringHead) {
			++lowerHeadTick;
		} else {
			lowerHeadTick -= 2;
		}
		lowerHeadTick = MathHelper.clamp(lowerHeadTick, 0, 20);

		if (!world.isRemote) {
			if (longJumpCooldown > 0) {
				--longJumpCooldown;
			}
			if (ramCooldown > 0) {
				--ramCooldown;
			}
			if (wasChild != isChild()) {
				updateHornsForAge(true);
				wasChild = isChild();
			}
		}

		super.onLivingUpdate();
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == LOWER_HEAD_EVENT) {
			loweringHead = true;
		} else if (id == RAISE_HEAD_EVENT) {
			loweringHead = false;
		} else {
			super.handleStatusUpdate(id);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setBoolean("IsScreamingGoat", isScreamingGoat());
		nbt.setBoolean("HasLeftHorn", hasLeftHorn());
		nbt.setBoolean("HasRightHorn", hasRightHorn());
		nbt.setInteger("LongJumpCooldown", longJumpCooldown);
		nbt.setInteger("RamCooldown", ramCooldown);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		setScreamingGoat(nbt.getBoolean("IsScreamingGoat"));
		setLeftHorn(nbt.getBoolean("HasLeftHorn"));
		setRightHorn(nbt.getBoolean("HasRightHorn"));
		longJumpCooldown = nbt.hasKey("LongJumpCooldown") ? nbt.getInteger("LongJumpCooldown") : nextLongJumpCooldown();
		ramCooldown = nbt.hasKey("RamCooldown") ? nbt.getInteger("RamCooldown") : nextRamCooldown();
		wasChild = isChild();
		setAttackDamageForAge();
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		return entity.attackEntityFrom(DamageSource.causeMobDamage(this), (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
	}

	@Override
	public void fall(float distance,float multiplier) {
		super.fall(Math.max(0.0F, distance - 10.0F),multiplier);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.ambient":"entity.goat.ambient");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.hurt":"entity.goat.hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.death":"entity.goat.death");
	}

	@Override
	protected void playStepSound(BlockPos pos,Block block) {
		playSound(ModernSounds.get("entity.goat.step"), 0.15F, 1.0F);
	}


	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEFINED;
	}

	@Override
	public void setRotationYawHead(float yaw) {
		float diff = MathHelper.wrapDegrees(yaw - renderYawOffset);
		diff = MathHelper.clamp(diff, -15.0F, 15.0F);
		super.setRotationYawHead(renderYawOffset + diff);
	}

	@Override
	public boolean getCanSpawnHere() {
		int x = MathHelper.floor(posX);
		int y = MathHelper.floor(getEntityBoundingBox().minY);
		int z = MathHelper.floor(posZ);
		Block block = world.getBlockState(new BlockPos(x,y-1,z)).getBlock();
		int meta = world.getBlockState(new BlockPos(x,y-1,z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x,y-1,z)));
		return MobRegistry.enabled(getClass()) && world.getLight(new BlockPos(x,y,z)) > 8
				&& isGoatSpawnableOn(block, meta)
				&& world.checkNoEntityCollision(getEntityBoundingBox())
				&& world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty()
				&& !world.containsAnyLiquid(getEntityBoundingBox());
	}

	private SoundEvent getEatSound() {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.eat":"entity.goat.eat");
	}

	private SoundEvent getMilkingSound() {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.milk":"entity.goat.milk");
	}

	private SoundEvent getLongJumpSound() {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.long_jump":"entity.goat.long_jump");
	}

	private SoundEvent getPrepareRamSound() {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.prepare_ram":"entity.goat.prepare_ram");
	}

	private SoundEvent getRamImpactSound() {
		return ModernSounds.get(isScreamingGoat()?"entity.goat.screaming.ram_impact":"entity.goat.ram_impact");
	}

	public boolean isScreamingGoat() {
		return getGoatFlag(SCREAMING_FLAG);
	}

	public void setScreamingGoat(boolean screaming) {
		setGoatFlag(SCREAMING_FLAG, screaming);
	}

	public boolean hasLeftHorn() {
		return getGoatFlag(LEFT_HORN_FLAG);
	}

	private void setLeftHorn(boolean hasHorn) {
		setGoatFlag(LEFT_HORN_FLAG, hasHorn);
	}

	public boolean hasRightHorn() {
		return getGoatFlag(RIGHT_HORN_FLAG);
	}

	private void setRightHorn(boolean hasHorn) {
		setGoatFlag(RIGHT_HORN_FLAG, hasHorn);
	}

	public void addHorns() {
		setLeftHorn(true);
		setRightHorn(true);
	}

	public void removeHorns() {
		setLeftHorn(false);
		setRightHorn(false);
	}

	public boolean dropHorn() {
        if(world.isRemote||!Content.available("goat_horn"))return false;
		boolean left = hasLeftHorn();
		boolean right = hasRightHorn();
		if (!left && !right) {
			return false;
		}


		EntityItem item = new EntityItem(world, posX, posY + 0.5D, posZ, createHorn());
		item.motionX = MathHelper.nextDouble(rand, -0.2D, 0.2D);
		item.motionY = MathHelper.nextDouble(rand, 0.3D, 0.7D);
		item.motionZ = MathHelper.nextDouble(rand, -0.2D, 0.2D);
		if(!world.spawnEntity(item))return false;
		if (!left) {
			setRightHorn(false);
		} else if (!right) {
			setLeftHorn(false);
		} else if (rand.nextBoolean()) {
			setLeftHorn(false);
		} else {
			setRightHorn(false);
		}

		return true;
	}

	public ItemStack createHorn() {
		int groupOffset = isScreamingGoat() ? 4 : 0;
		int variant = (getUniqueID().hashCode() & Integer.MAX_VALUE) % 4;
		return new ItemStack(Content.item("goat_horn"),1,groupOffset+variant);
	}

	public float getRammingXHeadRot() {
		return (float) lowerHeadTick / 20.0F * 30.0F * (float) Math.PI / 180.0F;
	}

	private boolean getGoatFlag(int flag) {
		return (dataManager.get(GOAT_FLAGS) & flag) != 0;
	}

	private void setGoatFlag(int flag, boolean value) {
		byte flags = dataManager.get(GOAT_FLAGS);
		if (value) {
			flags = (byte) (flags | flag);
		} else {
			flags = (byte) (flags & ~flag);
		}
		dataManager.set(GOAT_FLAGS, flags);
	}

	private void setLoweringHead(boolean lowering) {
		if (loweringHead != lowering) {
			loweringHead = lowering;
			world.setEntityState(this, lowering ? LOWER_HEAD_EVENT : RAISE_HEAD_EVENT);
		}
	}

	private void updateHornsForAge(boolean resetAdultHorns) {
		setAttackDamageForAge();
		if (isChild()) {
			removeHorns();
		} else if (resetAdultHorns) {
			addHorns();
		}
	}

	private void setAttackDamageForAge() {
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(isChild() ? 1.0D : 2.0D);
	}

	private int nextLongJumpCooldown() {
		return 600 + rand.nextInt(601);
	}

	private int nextRamCooldown() {
		return isScreamingGoat() ? 100 + rand.nextInt(201) : 600 + rand.nextInt(5401);
	}

	private int nextFailedRamCooldown() {
		return isScreamingGoat() ? 100 : 600;
	}

	private static boolean isGoatSpawnableOn(Block block, int meta) {
		return BlockTags.hasTag(block, meta, "minecraft:goats_spawnable_on")
				|| block == Blocks.GRASS || block == Blocks.STONE || block == Blocks.SNOW || block == Blocks.SNOW_LAYER || block == Blocks.PACKED_ICE || block == Blocks.GRAVEL;
	}

	private static boolean snapsGoatHorn(World world, int x, int y, int z) {
        if(!world.isBlockLoaded(new BlockPos(x,y,z)))return false;
		Block block = world.getBlockState(new BlockPos(x,y,z)).getBlock();
		int meta = world.getBlockState(new BlockPos(x,y,z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x,y,z)));
		return BlockTags.hasTag(block, meta, "minecraft:snaps_goat_horn")
				|| block == Blocks.LOG || block == Blocks.LOG2 || block == Blocks.STONE || block == Blocks.PACKED_ICE
				|| block == Blocks.IRON_ORE || block == Blocks.COAL_ORE || block == Blocks.EMERALD_ORE || block == Content.block("copper_ore");
	}

	private class AILongJump extends EntityAIBase {

		private int targetX;
		private int targetY;
		private int targetZ;
		private int prepareTicks;
		private boolean jumped;

		private AILongJump() {
			setMutexBits(3);
		}

		@Override
		public boolean shouldExecute() {
			if (longJumpCooldown > 0 || !onGround || isInWater() || getAttackTarget() != null || isInLove()) {
				return false;
			}
			return chooseJumpTarget();
		}

		@Override
		public boolean shouldContinueExecuting() {
			return !jumped && prepareTicks < 45 || jumped && !onGround;
		}

		@Override
		public void startExecuting() {
			prepareTicks = 0;
			jumped = false;
			getNavigator().clearPath();
		}

		@Override
		public void resetTask() {
			longJumpCooldown = nextLongJumpCooldown();
		}

		@Override
		public void updateTask() {
			getLookHelper().setLookPosition(targetX + 0.5D, targetY + 0.5D, targetZ + 0.5D, 30.0F, 30.0F);
			if (++prepareTicks >= 40 && !jumped) {
				double dx = targetX + 0.5D - posX;
				double dz = targetZ + 0.5D - posZ;
				double horizontal = MathHelper.sqrt(dx * dx + dz * dz);
				if (horizontal > 0.001D) {
					double dy = targetY - posY;
					motionX = dx / horizontal * Math.min(1.2D, 0.45D + horizontal * 0.08D);
					motionZ = dz / horizontal * Math.min(1.2D, 0.45D + horizontal * 0.08D);
					motionY = MathHelper.clamp(0.55D + dy * 0.12D, 0.35D, 1.0D);
					isAirBorne = true;
					playSound(getLongJumpSound(), 1.0F, 1.0F);
				}
				jumped = true;
			}
		}

		private boolean chooseJumpTarget() {
			int originX = MathHelper.floor(posX);
			int originY = MathHelper.floor(getEntityBoundingBox().minY);
			int originZ = MathHelper.floor(posZ);

			for (int i = 0; i < 20; ++i) {
				int x = originX + rand.nextInt(11) - 5;
				int y = originY + rand.nextInt(11) - 5;
				int z = originZ + rand.nextInt(11) - 5;
				if ((x != originX || z != originZ) && isValidLanding(x, y, z)) {
					targetX = x;
					targetY = y;
					targetZ = z;
					return true;
				}
			}

			longJumpCooldown = nextLongJumpCooldown() / 2;
			return false;
		}

		private boolean isValidLanding(int x, int y, int z) {
            if(!world.isBlockLoaded(new BlockPos(x,y+1,z))||y<1||y>=254)return false;
			return world.isBlockLoaded(new BlockPos(x,y+1,z)) && world.getBlockState(new BlockPos(x,y-1,z)).isSideSolid(world,new BlockPos(x,y-1,z),EnumFacing.UP)
					&& world.isAirBlock(new BlockPos(x,y,z))
					&& world.isAirBlock(new BlockPos(x,y+1,z))
					&& world.getBlockState(new BlockPos(x,y,z)).getMaterial() != Material.WATER;
		}
	}

	private class AIRamTarget extends EntityAIBase {

		private EntityLivingBase target;
		private int startX;
		private int startY;
		private int startZ;
		private int targetX;
		private int targetY;
		private int targetZ;
		private double ramTargetX;
		private double ramTargetZ;
		private int prepareTicks;
		private int timeout;
		private boolean charging;
		private double chargeDirX;
		private double chargeDirZ;

		private AIRamTarget() {
			setMutexBits(3);
		}

		@Override
		public boolean shouldExecute() {
			if (ramCooldown > 0 || isInLove()) {
				return false;
			}
			if (findRamCandidate()) {
				return true;
			}
			ramCooldown = nextFailedRamCooldown();
			return false;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return target != null && target.isEntityAlive() && timeout < (charging ? 200 : 160);
		}

		@Override
		public void startExecuting() {
			prepareTicks = 0;
			timeout = 0;
			charging = false;
			setLoweringHead(false);
			getNavigator().tryMoveToXYZ(startX + 0.5D, startY, startZ + 0.5D, 1.25D);
		}

		@Override
		public void resetTask() {
			if (target != null) {
				finishRam(false);
			}
		}

		@Override
		public void updateTask() {
			++timeout;
			if (target == null) {
				return;
			}

			getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
			if (!charging) {
				if (hasTargetMovedBlock()) {
					if (!chooseStartFor(target)) {
						finishRam(true);
						return;
					}
					prepareTicks = 0;
					setLoweringHead(false);
					getNavigator().tryMoveToXYZ(startX + 0.5D, startY, startZ + 0.5D, 1.25D);
					return;
				}

				if (getDistanceSq(startX + 0.5D, startY, startZ + 0.5D) <= 1.0D) {
					getNavigator().clearPath();
					setLoweringHead(true);
					if (++prepareTicks >= RAM_PREPARE_TICKS) {
						beginCharge();
					}
				} else if (getNavigator().noPath()) {
					finishRam(true);
				}
				return;
			}

			getNavigator().clearPath();
			faceChargeDirection();
			motionX += chargeDirX * 0.12D;
			motionZ += chargeDirZ * 0.12D;
			limitHorizontalChargeSpeed();

			if (getDistanceSq(target) <= 2.25D || getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D).intersects(target.getEntityBoundingBox())) {
				hitTarget(target);
			} else if (hasRammedHornBreakingBlock()) {
				playSound(getRamImpactSound(), 1.0F, 1.0F);
				if (dropHorn()) {
					playSound(ModernSounds.get("entity.goat.horn_break"), 1.0F, 1.0F);
				}
				finishRam(false);
			} else if (hasPassedTarget()) {
				finishRam(false);
			}
		}

		private void beginCharge() {
			charging = true;
			playSound(getPrepareRamSound(), 1.0F, getSoundPitch());
			double dx = ramTargetX - posX;
			double dz = ramTargetZ - posZ;
			double length = MathHelper.sqrt(dx * dx + dz * dz);
			if (length < 0.001D) {
				finishRam(true);
				return;
			}
			chargeDirX = dx / length;
			chargeDirZ = dz / length;
			getNavigator().clearPath();
			faceChargeDirection();
			motionX = chargeDirX * 0.65D;
			motionZ = chargeDirZ * 0.65D;
		}

		private void faceChargeDirection() {
			float yaw = (float) (Math.atan2(chargeDirZ, chargeDirX) * 180.0D / Math.PI) - 90.0F;
			rotationYaw = yaw;
			renderYawOffset = yaw;
			rotationYawHead = yaw;
		}

		private void limitHorizontalChargeSpeed() {
			double speed = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
			if (speed > 0.9D) {
				motionX = motionX / speed * 0.9D;
				motionZ = motionZ / speed * 0.9D;
			}
		}

		private boolean hasPassedTarget() {
			double dx = ramTargetX - posX;
			double dz = ramTargetZ - posZ;
			return dx * chargeDirX + dz * chargeDirZ < -0.5D;
		}

		private boolean hasTargetMovedBlock() {
			return MathHelper.floor(target.posX) != targetX
					|| MathHelper.floor(target.getEntityBoundingBox().minY) != targetY
					|| MathHelper.floor(target.posZ) != targetZ;
		}

		private void hitTarget(EntityLivingBase living) {
			living.attackEntityFrom(DamageSource.causeMobDamage(EntityGoat.this), isChild() ? 1.0F : 2.0F);
			double force = isChild() ? 0.5D : 1.25D;
			living.addVelocity(chargeDirX * force, 0.35D, chargeDirZ * force);
			playSound(getRamImpactSound(), 1.0F, 1.0F);
			finishRam(false);
		}

		private void finishRam(boolean failed) {
			setLoweringHead(false);
			getNavigator().clearPath();
			ramCooldown = failed ? nextFailedRamCooldown() : nextRamCooldown();
			target = null;
		}

		private boolean findRamCandidate() {
			EntityLivingBase nearest = null;
			double nearestDistance = Double.MAX_VALUE;
			List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(10.0D, 4.0D, 10.0D));
			for (EntityLivingBase living : list) {
				if (!canRam(living)) {
					continue;
				}
				double distance = getDistanceSq(living);
				if (distance < nearestDistance && chooseStartFor(living)) {
					nearest = living;
					nearestDistance = distance;
				}
			}

			target = nearest;
			return target != null;
		}

		private boolean canRam(EntityLivingBase living) {
			if (living == EntityGoat.this || living instanceof EntityGoat || living instanceof EntityArmorStand || !living.isEntityAlive()) {
				return false;
			}
			if (living instanceof EntityPlayer && ((EntityPlayer) living).capabilities.isCreativeMode) {
				return false;
			}
			return getEntitySenses().canSee(living);
		}

		private boolean chooseStartFor(EntityLivingBase living) {
			int tx = MathHelper.floor(living.posX);
			int ty = MathHelper.floor(living.getEntityBoundingBox().minY);
			int tz = MathHelper.floor(living.posZ);
			int bestX = 0;
			int bestY = 0;
			int bestZ = 0;
			double bestDistance = Double.MAX_VALUE;
			int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

			for (int[] dir : dirs) {
				int sx = tx;
				int sz = tz;
				int lastValidX = tx;
				int lastValidZ = tz;
				int distance = 0;
				for (int i = 0; i < RAM_MAX_DISTANCE; ++i) {
					sx += dir[0];
					sz += dir[1];
					if (!isWalkable(sx, ty, sz)) {
						break;
					}
					lastValidX = sx;
					lastValidZ = sz;
					++distance;
				}

				if (distance >= RAM_MIN_DISTANCE) {
					double distanceToStart = getDistanceSq(lastValidX + 0.5D, ty, lastValidZ + 0.5D);
					Path path = getNavigator().getPathToPos(new BlockPos(lastValidX,ty,lastValidZ));
					if (path != null && distanceToStart < bestDistance) {
						bestX = lastValidX;
						bestY = ty;
						bestZ = lastValidZ;
						bestDistance = distanceToStart;
					}
				}
			}

			if (bestDistance < Double.MAX_VALUE) {
				startX = bestX;
				startY = bestY;
				startZ = bestZ;
				targetX = tx;
				targetY = ty;
				targetZ = tz;
				setRamTargetEdge();
				return true;
			}
			return false;
		}

		private void setRamTargetEdge() {
			ramTargetX = targetX + 0.5D + 0.5D * Math.signum(targetX - startX);
			ramTargetZ = targetZ + 0.5D + 0.5D * Math.signum(targetZ - startZ);
		}

		private boolean isWalkable(int x, int y, int z) {
			return world.isBlockLoaded(new BlockPos(x,y+1,z)) && world.getBlockState(new BlockPos(x,y-1,z)).isSideSolid(world,new BlockPos(x,y-1,z),EnumFacing.UP)
					&& world.isAirBlock(new BlockPos(x,y,z))
					&& world.isAirBlock(new BlockPos(x,y+1,z));
		}

		private boolean hasRammedHornBreakingBlock() {
			double horizontal = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
			double dirX = horizontal < 0.001D ? chargeDirX : motionX / horizontal;
			double dirZ = horizontal < 0.001D ? chargeDirZ : motionZ / horizontal;
			int x = MathHelper.floor(posX + dirX * 0.8D);
			int y = MathHelper.floor(getEntityBoundingBox().minY + 0.5D);
			int z = MathHelper.floor(posZ + dirZ * 0.8D);
			return snapsGoatHorn(world, x, y, z) || snapsGoatHorn(world, x, y + 1, z);
		}
	}
}
