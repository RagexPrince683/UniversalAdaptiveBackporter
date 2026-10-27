package ganymedes01.etfuturum.entities;

import com.google.common.collect.Lists;
import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.api.crops.IBeeGrowable;



import ganymedes01.etfuturum.core.utils.EntityVectorUtils;

import net.minecraft.util.math.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.pathfinding.PathNavigateFlying;



import ganymedes01.etfuturum.tileentities.TileEntityBeeHive;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;


import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;
import ganymedes01.etfuturum.api.tags.helpers.ItemTags;


import java.util.*;
import java.util.function.Predicate;

/**
 * Credit to thedarkcolour for permission for thedarkcolour to see how FutureMC bees work in 1.12 to see what makes bees work without 1.15 mechanics present, and what part of 1.15 mechanics need to be changed or cut.
 * FutureMC was only referenced; no code was actually used from it; It is ARR but visible source.
 */
public class EntityBee extends EntityAnimal {
	private static final DataParameter<Byte> DATA_FLAGS_ID=EntityDataManager.createKey(EntityBee.class,DataSerializers.BYTE);
	private static final DataParameter<Integer> ANGER_TIME=EntityDataManager.createKey(EntityBee.class,DataSerializers.VARINT);
	private UUID lastHurtBy;
	private float rollAmount;
	private float rollAmountO;
	private int timeSinceSting;
	private int ticksWithoutNectarSinceExitingHive;
	private int stayOutOfHiveCountdown;
	private int numCropsGrownSincePollination;
	private int remainingCooldownBeforeLocatingNewHive = 0;
	private int remainingCooldownBeforeLocatingNewFlower = 0;
	private BlockPos savedFlowerPos = null;
	private BlockPos hivePos = null;
	private EntityBee.PollinateGoal pollinateGoal;
	private EntityBee.FindBeehiveGoal findBeehiveGoal;
	private EntityBee.FindFlowerGoal findFlowerGoal;
	private int underWaterTicks;

	private float moveVertical;

	public EntityBee(World worldIn) {
		super(worldIn);
		setSize(0.7F, 0.7F);

		navigator = new PathNavigateFlying(this, worldIn) {
            @Override public void onUpdateNavigation(){if(pollinateGoal==null||!pollinateGoal.isRunning())super.onUpdateNavigation();}
        };

		moveHelper = new BeeMoveHelper(this);
		addTasks();
		((PathNavigateFlying)getNavigator()).setCanOpenDoors(false);
		((PathNavigateFlying)getNavigator()).setCanEnterDoors(true);
		((PathNavigateFlying)getNavigator()).setCanFloat(false);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(DATA_FLAGS_ID,(byte)0);
		dataManager.register(ANGER_TIME,0);
		setNoGravity(true);
	}

	@Override
	public float getBlockPathWeight(BlockPos pos) {
		IBlockState state=world.getBlockState(pos);Block block=state.getBlock();
		if (state.getMaterial() == Material.AIR) {
			return 10.0F;
		}
		if (state.getMaterial().isLiquid() || block instanceof BlockFire || block instanceof BlockCocoa || block instanceof BlockFence || block instanceof BlockWall || block instanceof net.minecraft.block.BlockMagma) {
			return -1F;
		}
		return 0.0F;
	}


	protected void addTasks() {
		tasks.addTask(0, new EntityBee.StingGoal(this, EntityLivingBase.class, 1.4D, true));
		tasks.addTask(1, new EntityBee.EnterBeehiveGoal());
		tasks.addTask(2, new EntityAIMate(this, 1.0D));
		tasks.addTask(3, new TemptBeeWithFlowerGoal(this, 1.25D, false));
		this.pollinateGoal = new EntityBee.PollinateGoal();
		tasks.addTask(4, this.pollinateGoal);
		tasks.addTask(5, new EntityAIFollowParent(this, 1.25D));
		tasks.addTask(5, new EntityBee.UpdateBeehiveGoal());
		this.findBeehiveGoal = new EntityBee.FindBeehiveGoal();
		tasks.addTask(5, this.findBeehiveGoal);
		this.findFlowerGoal = new EntityBee.FindFlowerGoal();
		tasks.addTask(6, this.findFlowerGoal);
		tasks.addTask(7, new EntityBee.FindPollinationTargetGoal());
		tasks.addTask(8, new EntityBee.WanderGoal());
		tasks.addTask(9, new EntityAISwimming(this));
		targetTasks.addTask(1, (new AngerGoal(this))/*.setCallsForHelp(new Class[0])*/);
		targetTasks.addTask(2, new EntityBee.AttackPlayerGoal(this));
		setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER,-1);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("HasNoGravity", this.hasNoGravity());
		if (this.hasHive()) {
			compound.setTag("HivePos", net.minecraft.nbt.NBTUtil.createPosTag(this.getHivePos()));
		}

		if (this.hasFlower()) {
			compound.setTag("FlowerPos", net.minecraft.nbt.NBTUtil.createPosTag(this.getFlowerPos()));
		}

		compound.setBoolean("HasNectar", this.hasNectar());
		compound.setBoolean("HasStung", this.hasStung());
		compound.setInteger("TicksSincePollination", this.ticksWithoutNectarSinceExitingHive);
		compound.setInteger("CannotEnterHiveTicks", this.stayOutOfHiveCountdown);
		compound.setInteger("CropsGrownSincePollination", this.numCropsGrownSincePollination);
		compound.setInteger("Anger", this.getAnger());
		if (this.lastHurtBy != null) {
			compound.setString("HurtBy", this.lastHurtBy.toString());
		} else {
			compound.setString("HurtBy", "");
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		this.hivePos = null;
		if (compound.hasKey("HivePos")) {
			this.hivePos = net.minecraft.nbt.NBTUtil.getPosFromTag(compound.getCompoundTag("HivePos"));
		}

		this.savedFlowerPos = null;
		if (compound.hasKey("FlowerPos")) {
			setFlowerPos(net.minecraft.nbt.NBTUtil.getPosFromTag(compound.getCompoundTag("FlowerPos")));
		}

		super.readEntityFromNBT(compound);
		setNoGravity(compound.getBoolean("HasNoGravity"));
		this.setHasNectar(compound.getBoolean("HasNectar"));
		this.setHasStung(compound.getBoolean("HasStung"));
		this.setAnger(compound.getInteger("Anger"));
		this.ticksWithoutNectarSinceExitingHive = compound.getInteger("TicksSincePollination");
		this.stayOutOfHiveCountdown = compound.getInteger("CannotEnterHiveTicks");
		this.numCropsGrownSincePollination = compound.getInteger("CropsGrownSincePollination");
		String s = compound.getString("HurtBy");
		if (!s.isEmpty()) {
			try{this.lastHurtBy=UUID.fromString(s);}catch(IllegalArgumentException invalid){this.lastHurtBy=null;}
			EntityPlayer playerentity = null;
			for (EntityPlayer player : world.playerEntities) {
				if (player.getUniqueID().equals(lastHurtBy)) {
					playerentity = player;
					break;
				}
			}
			this.setRevengeTarget(playerentity);
			if (playerentity != null) {
				this.attackingPlayer = playerentity;
				this.recentlyHit = 100;
			}
		}
	}

    @Override public void travel(float strafe,float vertical,float forward){
        float drag=isInWater()?.8F:isInLava()?.5F:.91F;
        moveRelative(strafe,moveVertical,forward,isInWater()?.04F:isInLava()?.02F:onGround?getAIMoveSpeed()*.16277136F/(drag*drag*drag):jumpMovementFactor);
        move(MoverType.SELF,motionX,motionY,motionZ);
        if(!hasNoGravity())motionY-=isInWater()||isInLava()?.02:.08;
        motionX*=drag;motionZ*=drag;motionY*=isInWater()||isInLava()?drag:moveVertical!=0?.98:.5;
        prevLimbSwingAmount=limbSwingAmount;float amount=Math.min(1,(float)Math.sqrt((posX-prevPosX)*(posX-prevPosX)+(posZ-prevPosZ)*(posZ-prevPosZ))*4);
        limbSwingAmount+=(amount-limbSwingAmount)*.4F;limbSwing+=limbSwingAmount;
    }

	@Override
	public void knockBack(Entity p_70653_1_, float p_70653_2_, double p_70653_3_, double p_70653_5_) {
		if (this.rand.nextDouble() >= this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
			this.isAirBorne = true;
			float f1 = MathHelper.sqrt(p_70653_3_ * p_70653_3_ + p_70653_5_ * p_70653_5_);
			float f2 = 0.4F;
			this.motionX /= 2.0D;
			this.motionZ /= 2.0D;
			this.motionX -= p_70653_3_ / (double) f1 * (double) f2;
			this.motionZ -= p_70653_5_ / (double) f1 * (double) f2;

			if (!hasNoGravity()) {
				this.motionY /= 2.0D;
				this.motionY += f2;
				if (this.motionY > 0.4000000059604645D) {
					this.motionY = 0.4000000059604645D;
				}
			} else {
				motionY += .1D;
			}
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = entityIn.attackEntityFrom(new EntityDamageSource("sting", this), (float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
		if (flag) {
//          this.applyEnchantments(this, entityIn);
			if (entityIn instanceof EntityLivingBase) {
//              ((EntityLiving)entityIn).setBeeStingCount(((EntityLiving)entityIn).getBeeStingCount() + 1);
				int i = 0;
				if (this.world.getDifficulty() == EnumDifficulty.NORMAL) {
					i = 10;
				} else if (this.world.getDifficulty() == EnumDifficulty.HARD) {
					i = 18;
				}

				if (i > 0) {
					((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 20, 0));
				}
			}

			this.setHasStung(true);
			this.setAttackTarget(null);
			this.playSound(ModernSounds.get("entity.bee.sting"), 1.0F, 1.0F);
		}

		return flag;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (world.isRemote && this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.rand.nextFloat() < 0.05F) {
			for (int i = 0; i < this.rand.nextInt(2) + 1; ++i) {
				addParticle(this.world, this.posX - (double) 0.3F, this.posX + (double) 0.3F, this.posZ - (double) 0.3F, posZ + (double) 0.3F, posY + this.height * 0.5F);
			}
		}
		updateBodyPitch();
	}

	private void addParticle(World worldIn, double p_226397_2_, double p_226397_4_, double p_226397_6_, double p_226397_8_, double posY) {
		UAB.proxy.beeNectar(worldIn,p_226397_2_+worldIn.rand.nextDouble()*(p_226397_4_-p_226397_2_),posY,p_226397_6_+worldIn.rand.nextDouble()*(p_226397_8_-p_226397_6_));
	}

	private boolean isBreedingFlower(Item item, int meta) {
		return ItemTags.hasTag(item, meta, "minecraft:bee_food");
	}

	//TODO: Maybe fix [https://bugs.mojang.com/browse/MC-168267](MC-168267) and evaluate the contents of flower pots?
	//For breeding the ItemBlock will have different metas, this is an issue for double plants so we handle them separately.
    public static boolean isValidFlower(World world,int x,int y,int z){BlockPos pos=new BlockPos(x,y,z);if(!world.isBlockLoaded(pos))return false;IBlockState state=world.getBlockState(pos);if(state.getBlock() instanceof BlockDoublePlant&&state.getValue(BlockDoublePlant.HALF)==BlockDoublePlant.EnumBlockHalf.UPPER)state=world.getBlockState(pos.down());return BlockTags.hasTag(state.getBlock(),state.getBlock().getMetaFromState(state),"minecraft:bee_attractive");}

	public BlockPos getFlowerPos() {
		return this.savedFlowerPos;
	}

	public boolean hasFlower() {
		return this.savedFlowerPos != null;
	}

	public void setFlowerPos(BlockPos pos) {
		this.savedFlowerPos = pos;
	}

	private boolean failedPollinatingTooLong() {
		return this.ticksWithoutNectarSinceExitingHive > 3600;
	}

	private boolean canEnterHive() {
		if (this.stayOutOfHiveCountdown <= 0 && !this.pollinateGoal.isRunning() && !this.hasStung()) {
			boolean flag = this.failedPollinatingTooLong() || this.world.isRaining() || !this.world.isDaytime() || this.hasNectar();
			return flag && !this.isHiveNearFire();
		} else {
			return false;
		}
	}

	public void setStayOutOfHiveCountdown(int p_226450_1_) {
		this.stayOutOfHiveCountdown = p_226450_1_;
	}

	public float getBodyPitch(float p_226455_1_) {
		return rollAmountO+p_226455_1_*(rollAmount-rollAmountO);
	}

	private void updateBodyPitch() {
		this.rollAmountO = this.rollAmount;
		if (this.isNearTarget()) {
			this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
		} else {
			this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
		}
	}

	@Override
	public void setRevengeTarget(EntityLivingBase livingBase) {
		super.setRevengeTarget(livingBase);
		if (livingBase != null) {
			this.lastHurtBy = livingBase.getUniqueID();
		}

	}

	@Override
	protected void updateAITasks() {
        super.updateAITasks();
		if (this.isInWater()) {
			++this.underWaterTicks;
		} else {
			this.underWaterTicks = 0;
		}

		if (this.underWaterTicks > 20) {
			this.attackEntityFrom(DamageSource.DROWN, 1.0F);
		}

		if (hasStung()) {
			++this.timeSinceSting;
			if (this.timeSinceSting % 5 == 0 && this.rand.nextInt(MathHelper.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
				this.attackEntityFrom(DamageSource.GENERIC, this.getHealth()); //Kills the bee after it's been a while when it stings someone
			}
		}

		if (this.isAngry()) {
			int i = this.getAnger();
			this.setAnger(i - 1);
			if (i == 0 && this.getAttackTarget() != null) {
				this.setBeeAttacker(this.getAttackTarget());
			}
		}

		if (!this.hasNectar()) {
			++this.ticksWithoutNectarSinceExitingHive;
		}
	}

	public void resetTicksWithoutNectar() {
		this.ticksWithoutNectarSinceExitingHive = 0;
	}

	private boolean isHiveNearFire() {
		if (this.hivePos == null) {
			return false;
		} else {
			TileEntity tileentity = tileAt(hivePos);
			return tileentity instanceof TileEntityBeeHive && ((TileEntityBeeHive) tileentity).isNearFire();
		}
	}

	public boolean isAngry() {
		return this.getAnger() > 0;
	}

	private int getAnger() {
		return this.dataManager.get(ANGER_TIME);
	}

	private void setAnger(int angerTime) {
		this.dataManager.set(ANGER_TIME, angerTime);
	}

	private boolean doesHiveHaveSpace(BlockPos pos) {
		TileEntity tileentity = tileAt(pos);
		if (tileentity instanceof TileEntityBeeHive) {
			return !((TileEntityBeeHive) tileentity).isFullOfBees();
		} else {
			return false;
		}
	}

    public void setHivePos(BlockPos pos){hivePos=pos==null?null:pos.toImmutable();}

	public boolean hasHive() {
		return this.hivePos != null;
	}

	public BlockPos getHivePos() {
		return this.hivePos;
	}

//  protected void sendDebugPackets() {
//      super.sendDebugPackets();
//      DebugPacketSender.func_229749_a_(this);
//  }

	private int getCropsGrownSincePollination() {
		return this.numCropsGrownSincePollination;
	}

	private void resetCropCounter() {
		this.numCropsGrownSincePollination = 0;
	}

	private void addCropCounter() {
		++this.numCropsGrownSincePollination;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.world.isRemote) {
			if (this.stayOutOfHiveCountdown > 0) {
				--this.stayOutOfHiveCountdown;
			}

			if (this.remainingCooldownBeforeLocatingNewHive > 0) {
				--this.remainingCooldownBeforeLocatingNewHive;
			}

			if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
				--this.remainingCooldownBeforeLocatingNewFlower;
			}
		}

	}

	private boolean isHiveValid() {
		if (!this.hasHive()) {
			return false;
		} else {
			return tileAt(hivePos) instanceof TileEntityBeeHive;
		}
	}

	public boolean hasNectar() {
		return this.getBeeFlag(8);
	}

	private void setHasNectar(boolean p_226447_1_) {
		if (p_226447_1_) {
			this.resetTicksWithoutNectar();
		}

		this.setBeeFlag(8, p_226447_1_);
	}

	public boolean hasStung() {
		return this.getBeeFlag(4);
	}

	private void setHasStung(boolean p_226449_1_) {
		this.setBeeFlag(4, p_226449_1_);
	}

	private boolean isNearTarget() {
		return this.getBeeFlag(2);
	}

    private void setNearTarget(boolean p_226452_1_) {
        this.setBeeFlag(2, p_226452_1_);
    }

	private boolean isTooFar(BlockPos pos) {
		return !this.isWithinDistance(pos, 48);
	}

	private void setBeeFlag(int flagId, boolean p_226404_2_) {
		if (p_226404_2_) {
			this.dataManager.set(DATA_FLAGS_ID, (byte) (this.dataManager.get(DATA_FLAGS_ID) | flagId));
		} else {
			this.dataManager.set(DATA_FLAGS_ID, (byte) (this.dataManager.get(DATA_FLAGS_ID) & ~flagId));
		}

	}

	private boolean getBeeFlag(int flagId) {
		return (this.dataManager.get(DATA_FLAGS_ID) & flagId) != 0;
	}

	public boolean isPollinating() {
		return getBeeFlag(0x1);
	}

	public void setPollinating(boolean isPollinating) {
		setBeeFlag(0x1, isPollinating);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.6000000238418579D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return !stack.isEmpty()&&isBreedingFlower(stack.getItem(),stack.getMetadata());
	}

	/**
	 * MCP name: {@code playStepSound}
	 */
	@Override
	protected void playStepSound(BlockPos pos,Block block) {
	}

	@Override
	public int getTalkInterval() {
		return 0;
	}

	@Override
	public void playLivingSound() {
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModernSounds.get("entity.bee.hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModernSounds.get("entity.bee.death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public EntityBee createChild(EntityAgeable entity) {
		return new EntityBee(entity.world);
	}

	@Override
	public float getEyeHeight() {
		return this.height * 0.5F;
	}

	@Override
	public void fall(float distance,float multiplier) {
	}

	@Override
	protected void updateFallState(double distanceFallenThisTick,boolean isOnGround,IBlockState state,BlockPos pos) {
	}

	public void onHoneyDelivered() {
		this.setHasNectar(false);
		this.resetCropCounter();
	}

	public boolean setBeeAttacker(Entity attacker) {
		this.setAnger(400 + this.rand.nextInt(400));
		if (attacker instanceof EntityLivingBase) {
			this.setRevengeTarget((EntityLivingBase) attacker);
		}

		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		} else {
			Entity entity = source.getTrueSource();
			if (!this.world.isRemote && entity instanceof EntityLivingBase && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode) && canEntityBeSeen(entity)) {
				this.pollinateGoal.cancel();
				List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(32.0D,32.0D,32.0D));
				for (Entity entity1 : list) {
					if (entity1 instanceof EntityBee) {
						((EntityBee) entity1).setBeeAttacker(entity);
					}
				}
			}

			return super.attackEntityFrom(source, amount);
		}
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

//  protected void handleFluidJump(Tag<Fluid> fluidTag) {
//      this.setMotion(this.getMotion().add(0.0D, 0.01D, 0.0D));
//  }

	private boolean isWithinDistance(BlockPos pos, int distance) {
		return getPositionVector().squareDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= distance * distance;
	}


	static class AngerGoal extends EntityAIHurtByTarget {
		AngerGoal(EntityBee beeIn) {
			super(beeIn, true);
		}

		@Override
		protected boolean isSuitableTarget(EntityLivingBase targetIn, boolean p_75296_2_) {
			if (targetIn != null && this.taskOwner.canEntityBeSeen(targetIn) && (!(targetIn instanceof EntityPlayer) || !((EntityPlayer) targetIn).capabilities.isCreativeMode) && ((EntityBee) taskOwner).setBeeAttacker(targetIn)) {
				taskOwner.setAttackTarget(targetIn);
				return true;
			}
			return false;
		}
	}

	static class AttackPlayerGoal extends EntityAINearestAttackableTarget<EntityPlayer> {
		AttackPlayerGoal(EntityBee beeIn) {
			super(beeIn,EntityPlayer.class,10,true,false,null);
		}

		@Override
		public boolean shouldExecute() {
			return this.canSting() && super.shouldExecute();
		}

		@Override
		public boolean shouldContinueExecuting() {
			boolean flag = this.canSting();
			if (flag && taskOwner.getAttackTarget() != null) {
				return super.shouldContinueExecuting();
			} else {
				targetEntity = null;
				return false;
			}
		}

		private boolean canSting() {
			EntityBee beeentity = (EntityBee) this.taskOwner;
			return beeentity.isAngry() && !beeentity.hasStung();
		}
	}


	private void startMovingTo(BlockPos pos) {
		Vec3d vec3d = new Vec3d(pos);
		int i = 0;
		BlockPos blockpos = new BlockPos(this);
		int j = (int) vec3d.y - blockpos.getY();
		if (j > 2) {
			i = 4;
		} else if (j < -2) {
			i = -4;
		}

		int k = 6;
		int l = 8;
		int i1 = Math.abs(blockpos.getX()-pos.getX())+Math.abs(blockpos.getY()-pos.getY())+Math.abs(blockpos.getZ()-pos.getZ());
		if (i1 < 15) {
			k = i1 / 2;
			l = i1 / 2;
		}

		Vec3d vec3d1 = EntityVectorUtils.func_226344_b_(this, k, l, i, vec3d, (float) Math.PI / 10F);
		if (vec3d1 != null) {
//          this.navigator.setRangeMultiplier(0.5F);
			this.getNavigator().tryMoveToXYZ(vec3d1.x, vec3d1.y, vec3d1.z, 1.0D);
		}
	}

	private List<BlockPos> getBlocksInRange(Predicate<BlockPos> predicate, int range, boolean shuffle) {
		final BlockPos beePos = new BlockPos(EntityBee.this);
		final List<BlockPos> posList = Lists.newArrayList();

		for (int x1 = -range; x1 <= range; x1++) {
			for (int y1 = -range; y1 <= range; y1++) {
				for (int z1 = -range; z1 <= range; z1++) {
					BlockPos pos = beePos.add(x1, y1, z1);
					if (world.isBlockLoaded(pos)&&predicate.test(pos)) {
						posList.add(pos);
					}
				}
			}
		}
		if (shuffle) {
			Collections.shuffle(posList);
		} else {
			posList.sort(Comparator.comparingDouble(pos -> pos.distanceSq(beePos)));
		}
		return posList;
	}

	class EnterBeehiveGoal extends EntityBee.PassiveGoal {
		private EnterBeehiveGoal() {
		}

		@Override
		public boolean canBeeStart() {
			if (EntityBee.this.hasHive() && EntityBee.this.canEnterHive() && EntityBee.this.hivePos.distanceSq(new BlockPos(EntityBee.this))<4) {
				TileEntity tileentity = tileAt(getHivePos());
				if (tileentity instanceof TileEntityBeeHive) {
					if (!((TileEntityBeeHive)tileentity).isFullOfBees()) {
						return true;
					}

					EntityBee.this.hivePos = null;
				}
			}

			return false;
		}

		@Override
		public boolean canBeeContinue() {
			return false;
		}

		@Override
		public void startExecuting() {
			TileEntity tileentity = tileAt(getHivePos());
			if (tileentity instanceof TileEntityBeeHive) {
				((TileEntityBeeHive)tileentity).tryEnterHive(EntityBee.this, EntityBee.this.hasNectar());
			}

		}
	}

	public class FindBeehiveGoal extends EntityBee.PassiveGoal {
		private int ticks = EntityBee.this.world.rand.nextInt(10);
		private final List<BlockPos> possibleHives = Lists.newArrayList();
		private Path path = null;

		FindBeehiveGoal() {
			setMutexBits(0);
		}

		@Override
		public boolean canBeeStart() {
			return EntityBee.this.hivePos != null && !EntityBee.this.hasHome() && EntityBee.this.canEnterHive() && !this.isCloseEnough(EntityBee.this.hivePos) && world.isBlockLoaded(getHivePos())&&world.getBlockState(getHivePos()).getBlock() instanceof BeeHiveBlock;
		}

		@Override
		public boolean canBeeContinue() {
			return this.canBeeStart();
		}

		@Override
		public void startExecuting() {
			this.ticks = 0;
			super.startExecuting();
		}

		@Override
		public void resetTask() {
			this.ticks = 0;
			EntityBee.this.getNavigator().clearPath();
//          EntityBee.this.getNavigator().resetRangeMultiplier();
		}

		@Override
		public void updateTask() {
			if (EntityBee.this.hivePos != null) {
				++this.ticks;
				if (this.ticks > 600) {
					this.makeChosenHivePossibleHive();
				} else if (getNavigator().noPath()) {
					if (!EntityBee.this.isWithinDistance(EntityBee.this.hivePos, 16)) {
						if (EntityBee.this.isTooFar(EntityBee.this.hivePos)) {
							this.reset();
						} else {
							EntityBee.this.startMovingTo(EntityBee.this.hivePos);
						}
					} else {
						boolean flag = this.startMovingToFar(EntityBee.this.hivePos);
						if (!flag) {
							this.makeChosenHivePossibleHive();
						} else if (this.path != null && EntityBee.this.getNavigator().getPath().isSamePath(this.path)) {
							this.reset();
						} else {
							this.path = EntityBee.this.getNavigator().getPath();
						}

					}
				}
			}
		}

		private boolean startMovingToFar(BlockPos pos) {
//          EntityBee.this.getNavigator().setRangeMultiplier(10.0F);
			EntityBee.this.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0D);
			return EntityBee.this.getNavigator().getPath() != null /*&& EntityBee.this.getNavigator().getPath().reachesTarget()*/;
		}

		private boolean isPossibleHive(BlockPos pos) {
			return this.possibleHives.contains(pos);
		}

		private void addPossibleHives(BlockPos pos) {
			this.possibleHives.add(pos);

			while (this.possibleHives.size() > 3) {
				this.possibleHives.remove(0);
			}

		}

		private void clearPossibleHives() {
			this.possibleHives.clear();
		}

		private void makeChosenHivePossibleHive() {
			if (EntityBee.this.hivePos != null) {
				this.addPossibleHives(EntityBee.this.hivePos);
			}

			this.reset();
		}

		private void reset() {
			EntityBee.this.hivePos = null;
			EntityBee.this.remainingCooldownBeforeLocatingNewHive = 200;
		}

		private boolean isCloseEnough(BlockPos pos) {
			if (EntityBee.this.isWithinDistance(pos, 2)) {
				return true;
			} else {
				Path path = EntityBee.this.getNavigator().getPath();
				return path != null && path.getFinalPathPoint()!=null&&path.getFinalPathPoint().x==pos.getX()&&path.getFinalPathPoint().z==pos.getZ()/* && path.reachesTarget() */ && path.isFinished();
			}
		}
	}

	public class FindFlowerGoal extends EntityBee.PassiveGoal {
		private int ticks = EntityBee.this.world.rand.nextInt(10);

		FindFlowerGoal() {
			setMutexBits(0);
		}

		@Override
		public boolean canBeeStart() {
			return EntityBee.this.savedFlowerPos != null && !EntityBee.this.hasHome() && this.shouldMoveToFlower() && isValidFlower(world, savedFlowerPos.getX(), savedFlowerPos.getY(), savedFlowerPos.getZ())
					&& !EntityBee.this.isWithinDistance(EntityBee.this.savedFlowerPos, 2);
		}

		@Override
		public boolean canBeeContinue() {
			return this.canBeeStart();
		}

		@Override
		public void startExecuting() {
			this.ticks = 0;
			super.startExecuting();
		}

		@Override
		public void resetTask() {
			this.ticks = 0;
			EntityBee.this.getNavigator().clearPath();
//          EntityBee.this.getNavigator().resetRangeMultiplier();
		}

		@Override
		public void updateTask() {
			if (EntityBee.this.savedFlowerPos != null) {
				++this.ticks;
				if (this.ticks > 600) {
					EntityBee.this.savedFlowerPos = null;
				} else if (getNavigator().noPath()) {
					if (EntityBee.this.isTooFar(EntityBee.this.savedFlowerPos)) {
						EntityBee.this.savedFlowerPos = null;
					} else {
						EntityBee.this.startMovingTo(EntityBee.this.savedFlowerPos);
					}
				}
			}
		}

		private boolean shouldMoveToFlower() {
			return EntityBee.this.ticksWithoutNectarSinceExitingHive > 2400;
		}
	}

	class FindPollinationTargetGoal extends EntityBee.PassiveGoal {
		private FindPollinationTargetGoal() {
		}

		@Override
		public boolean canBeeStart() {
			if (EntityBee.this.getCropsGrownSincePollination() >= 10) {
				return false;
			} else if (EntityBee.this.rand.nextFloat() < 0.3F) {
				return false;
			} else {
				return EntityBee.this.hasNectar() && EntityBee.this.isHiveValid();
			}
		}

		@Override
		public boolean canBeeContinue() {
			return this.canBeeStart();
		}

		@Override
		public void updateTask() {
			if (EntityBee.this.rand.nextInt(30) == 0) {
				for (int i = 1; i <= 2; ++i) {
					int x = (int) posX;
					int y = (int) posY - i;
					int z = (int) posZ;
                    BlockPos cropPos=new BlockPos(x,y,z);if(!world.isBlockLoaded(cropPos))continue;IBlockState state=world.getBlockState(cropPos);Block block=state.getBlock();
                    if(BlockTags.hasTag(block,block.getMetaFromState(state),"minecraft:bee_growables")){
                        if(block instanceof IBeeGrowable){IBeeGrowable crop=(IBeeGrowable)block;if(crop.canBeeGrow(world,rand,x,y,z)&&crop.onBeeGrow(world,rand,x,y,z))addCropCounter();}
                        else if(block instanceof BlockCrops){BlockCrops crop=(BlockCrops)block;if(!crop.isMaxAge(state)){world.setBlockState(cropPos,crop.withAge(crop.getMetaFromState(state)+1),2);addCropCounter();}}
                        else if(block instanceof BlockStem&&state.getValue(BlockStem.AGE)<7){world.setBlockState(cropPos,state.withProperty(BlockStem.AGE,state.getValue(BlockStem.AGE)+1),2);addCropCounter();}
                        else if(block instanceof BerryBush&&state.getValue(BerryBush.AGE)<3){world.setBlockState(cropPos,state.withProperty(BerryBush.AGE,state.getValue(BerryBush.AGE)+1),2);addCropCounter();}
                        else if(block instanceof IGrowable&&((IGrowable)block).canGrow(world,cropPos,state,false)&&((IGrowable)block).canUseBonemeal(world,rand,cropPos,state)){IBlockState grown=block.getStateFromMeta(block.getMetaFromState(state)+1);if(grown!=state&&world.setBlockState(cropPos,grown,2))addCropCounter();}
                    }
				}
			}
		}
	}

	abstract class PassiveGoal extends EntityAIBase {
		private PassiveGoal() {
		}

		public abstract boolean canBeeStart();

		public abstract boolean canBeeContinue();

		@Override
		public boolean shouldExecute() {
			return this.canBeeStart() && !EntityBee.this.isAngry();
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.canBeeContinue() && !EntityBee.this.isAngry();
		}
	}

	class PollinateGoal extends EntityBee.PassiveGoal {
		private int pollinationTicks = 0;
		private int lastPollinationTick = 0;
		private boolean running;
		private Vec3d nextTarget;
		private int ticks = 0;

		PollinateGoal() {
			this.setMutexBits(0);
		}

		@Override
		public boolean canBeeStart() {
			if (EntityBee.this.remainingCooldownBeforeLocatingNewFlower > 0) {
				return false;
			} else if (EntityBee.this.hasNectar()) {
				return false;
			} else if (EntityBee.this.world.isRaining()) {
				return false;
			} else if (EntityBee.this.rand.nextFloat() < 0.7F) {
				return false;
			} else {
				Optional<BlockPos> optional = this.getFlower();
				if (optional.isPresent()) {
					EntityBee.this.savedFlowerPos = optional.get();
					EntityBee.this.getNavigator().tryMoveToXYZ((double) EntityBee.this.savedFlowerPos.getX() + 0.5D, (double) EntityBee.this.savedFlowerPos.getY() + 0.5D, (double) EntityBee.this.savedFlowerPos.getZ() + 0.5D, 1.2F);
					return true;
				} else {
					return false;
				}
			}
		}

		@Override
		public boolean canBeeContinue() {
			if (!this.running) {
				return false;
			} else if (!EntityBee.this.hasFlower()) {
				return false;
			} else if (EntityBee.this.world.isRaining()) {
				return false;
			} else if (this.completedPollination()) {
				return EntityBee.this.rand.nextFloat() < 0.2F;
			} else if (EntityBee.this.ticksExisted % 20 == 0 && !isValidFlower(world, savedFlowerPos.getX(), savedFlowerPos.getY(), savedFlowerPos.getZ())) {
				EntityBee.this.savedFlowerPos = null;
				return false;
			} else {
				return true;
			}
		}

		private boolean completedPollination() {
			return this.pollinationTicks > 400;
		}

		private boolean isRunning() {
			return this.running;
		}

		private void cancel() {
			this.running = false;
		}

		@Override
		public void startExecuting() {
			this.pollinationTicks = 0;
			this.ticks = 0;
			this.lastPollinationTick = 0;
			this.running = true;
			EntityBee.this.resetTicksWithoutNectar();
		}

		@Override
		public void resetTask() {
			if (this.completedPollination()) {
				EntityBee.this.setHasNectar(true);
			}

			this.running = false;
			EntityBee.this.getNavigator().clearPath();
			EntityBee.this.remainingCooldownBeforeLocatingNewFlower = 200;
		}

		@Override
		public void updateTask() {
			BlockPos beePos = new BlockPos(EntityBee.this);
			++this.ticks;
			if (this.ticks > 600) {
				EntityBee.this.savedFlowerPos = null;
			} else {
				Vec3d vec3d = new Vec3d(EntityBee.this.savedFlowerPos).add(.5,.6,.5);
				if (vec3d.squareDistanceTo(posX, posY, posZ) > 1.0D) {
					this.nextTarget = vec3d;
					this.moveToNextTarget();
				} else {
					if (this.nextTarget == null) {
						this.nextTarget = vec3d;
					}

					boolean flag = getPositionVector().squareDistanceTo(this.nextTarget) <= 0.1D;
					boolean flag1 = true;
					if (!flag && this.ticks > 600) {
						EntityBee.this.savedFlowerPos = null;
					} else {
						if (flag) {
							boolean flag2 = EntityBee.this.rand.nextInt(100) == 0;
							if (flag2) {
								this.nextTarget = new Vec3d(vec3d.x + (double) this.getRandomOffset(), vec3d.y, vec3d.z + (double) this.getRandomOffset());
								EntityBee.this.getNavigator().clearPath();
							} else {
								flag1 = false;
							}
						}

						if (flag1) {
							this.moveToNextTarget();
						}

						++this.pollinationTicks;
						if (EntityBee.this.rand.nextFloat() < 0.05F && this.pollinationTicks > this.lastPollinationTick + 60) {
							this.lastPollinationTick = this.pollinationTicks;
							EntityBee.this.playSound(ModernSounds.get("entity.bee.pollinate"), 1.0F, 1.0F);
						}
					}
				}
			}
		}

		private void moveToNextTarget() {
			EntityBee.this.getMoveHelper().setMoveTo(this.nextTarget.x, this.nextTarget.y, this.nextTarget.z, 0.35F);
		}

		private float getRandomOffset() {
			return (EntityBee.this.rand.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
		}

		private Optional<BlockPos> getFlower() {
			return this.findFlower(5);
		}

		private Optional<BlockPos> findFlower(int distance) {
			List<BlockPos> list = getBlocksInRange(pos -> isValidFlower(world, pos.getX(), pos.getY(), pos.getZ()), distance, true);
			return list.isEmpty() || list.get(0) == null ? Optional.empty() : Optional.of(list.get(0));
		}
	}

	class StingGoal extends EntityAIAttackMelee {
		StingGoal(EntityCreature creatureIn, Class<? extends Entity> classIn, double speedIn, boolean useLongMemory) {
			super(creatureIn,speedIn,useLongMemory);
		}

		@Override
		public boolean shouldExecute() {
			return super.shouldExecute() && EntityBee.this.isAngry() && !EntityBee.this.hasStung();
		}

		@Override
		public boolean shouldContinueExecuting() {
			return super.shouldContinueExecuting() && EntityBee.this.isAngry() && !EntityBee.this.hasStung();
		}
	}

	class UpdateBeehiveGoal extends EntityBee.PassiveGoal {
		private UpdateBeehiveGoal() {
		}

		@Override
		public boolean canBeeStart() {
			return EntityBee.this.remainingCooldownBeforeLocatingNewHive == 0 && !EntityBee.this.hasHive() && EntityBee.this.canEnterHive();
		}

		@Override
		public boolean canBeeContinue() {
			return false;
		}

		@Override
		public void startExecuting() {
			EntityBee.this.remainingCooldownBeforeLocatingNewHive = 200;
			List<BlockPos> list=TileEntityBeeHive.nearby(world,new BlockPos(EntityBee.this),20);list.removeIf(candidate->!doesHiveHaveSpace(candidate));
			if (!list.isEmpty()) {
				for (BlockPos blockpos : list) {
					if (!EntityBee.this.findBeehiveGoal.isPossibleHive(blockpos)) {
						EntityBee.this.hivePos = blockpos;
						return;
					}
				}

				EntityBee.this.findBeehiveGoal.clearPossibleHives();
				EntityBee.this.hivePos = list.get(0);
			}
		}
	}

	class WanderGoal extends EntityAIBase {
		WanderGoal() {
			this.setMutexBits(0);
		}

		@Override
		public boolean shouldExecute() {
			return EntityBee.this.getNavigator().noPath() && EntityBee.this.rand.nextInt(10) == 0;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return !getNavigator().noPath();
		}

		@Override
		public void startExecuting() {
			Vec3d vec3d = this.getRandomLocation();
			if (vec3d != null) {
				EntityBee.this.getNavigator().setPath(EntityBee.this.getNavigator().getPathToPos(new BlockPos(vec3d)), 1.0D);
			}
		}

		
		private Vec3d getRandomLocation() {
			Vec3d vec3d;
			if (isHiveValid() && !isWithinDistance(hivePos, 40)) {
				Vec3d vec3d1 = new Vec3d(hivePos);
				vec3d = vec3d1.subtract(getPositionVector()).normalize();
			} else {
				vec3d = getLook(0.0F);
			}

			Vec3d vec3d2 = EntityVectorUtils.findAirTarget(EntityBee.this, 8, 7, vec3d, ((float) Math.PI / 2F), 2, 1);
			return vec3d2 != null ? vec3d2 : EntityVectorUtils.findGroundTarget(EntityBee.this, 8, 4, -2, vec3d, (float) Math.PI / 2F);
		}
	}

    class TemptBeeWithFlowerGoal extends EntityAIBase {
        private final double speed;private EntityPlayer player;private int delay;
        TemptBeeWithFlowerGoal(EntityCreature bee,double speed,boolean scared){this.speed=speed;setMutexBits(3);}
        @Override public boolean shouldExecute(){if(delay>0){delay--;return false;}player=world.getClosestPlayerToEntity(EntityBee.this,getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).getAttributeValue());return player!=null&&(isBreedingItem(player.getHeldItemMainhand())||isBreedingItem(player.getHeldItemOffhand()));}
        @Override public boolean shouldContinueExecuting(){return shouldExecute();}
        @Override public void resetTask(){player=null;getNavigator().clearPath();delay=100;}
        @Override public void updateTask(){getLookHelper().setLookPositionWithEntity(player,getHorizontalFaceSpeed()+20,getVerticalFaceSpeed());if(getDistanceSq(player)<6.25)getNavigator().clearPath();else getNavigator().tryMoveToEntityLiving(player,speed);}
    }
    private TileEntity tileAt(BlockPos pos){return world.isBlockLoaded(pos)?world.getTileEntity(pos):null;}
    private final class BeeMoveHelper extends EntityMoveHelper {
        BeeMoveHelper(EntityLiving entity){super(entity);}
        @Override public void onUpdateMoveHelper(){
            if(action==Action.MOVE_TO){action=Action.WAIT;setNoGravity(true);double dx=posX-EntityBee.this.posX,dy=posY-EntityBee.this.posY,dz=posZ-EntityBee.this.posZ;
                if(dx*dx+dy*dy+dz*dz<2.5E-7){moveVertical=0;setMoveForward(0);return;}
                rotationYaw=limitAngle(rotationYaw,(float)(Math.atan2(dz,dx)*180/Math.PI)-90,90);
                float value=(float)(speed*getEntityAttribute(onGround?SharedMonsterAttributes.MOVEMENT_SPEED:SharedMonsterAttributes.FLYING_SPEED).getAttributeValue());
                setAIMoveSpeed(value);rotationPitch=limitAngle(rotationPitch,(float)(-Math.atan2(dy,Math.sqrt(dx*dx+dz*dz))*180/Math.PI),10);moveVertical=dy>0?value:-value;
            }else{moveVertical=0;setMoveForward(0);}
        }
    }
}
