package ganymedes01.etfuturum.entities;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;






import net.minecraft.util.math.*;
import net.minecraft.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.datasync.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.monster.EntityPolarBear;
import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.entities.ai.*;
import net.minecraft.block.Block;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;


import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants;
import ganymedes01.etfuturum.api.blocksanditems.utils.ItemMetaPair;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityFox extends EntityAnimal {

    private static final DataParameter<String> OWNER=EntityDataManager.createKey(EntityFox.class,DataSerializers.STRING);
    private static final DataParameter<String> OTHER_TRUSTED=EntityDataManager.createKey(EntityFox.class,DataSerializers.STRING);
    private static final DataParameter<Byte> TYPE=EntityDataManager.createKey(EntityFox.class,DataSerializers.BYTE);
    private static final DataParameter<Byte> FOX_FLAGS=EntityDataManager.createKey(EntityFox.class,DataSerializers.BYTE);

    private static final com.google.common.base.Predicate<EntityItem> PICKABLE_DROP_FILTER=item->!item.cannotPickup()&&item.isEntityAlive();
    private static final Predicate<EntityLivingBase> CHICKEN_AND_RABBIT_FILTER =
            entity -> entity instanceof EntityChicken || entity instanceof EntityRabbit;
    private static final Predicate<EntityLivingBase> NOTICEABLE_PLAYER_FILTER =
            entity -> !(entity instanceof EntityPlayer) || !entity.isSneaking()&&!((EntityPlayer)entity).capabilities.isCreativeMode&&!((EntityPlayer)entity).isSpectator();
    private static final Predicate<EntityLivingBase> JUST_ATTACKED_SOMETHING_FILTER =
            entity -> entity instanceof EntityLiving;

    private EntityAIBase followChickenAndRabbitTask;
    private EntityAIBase followBabyTurtleTask;
    private EntityAIBase followFishTask;

    private float headRollProgress;
    private float lastHeadRollProgress;
    private float extraRollingHeight;
    private float lastExtraRollingHeight;
    private int eatingTime;
    private EntityLivingBase friend;
    private boolean followOwner;

    public EntityFox(World world) {
        super(world);
        ((LookHelperControl)this).etfu$setLookHelper(new FoxLookHelper());
        this.moveHelper = new EntityFox.FoxMoveHelper();
        this.setSize(0.6F, 0.7F);
        this.setCanPickUpLoot(true);
        initTasks();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(OWNER, "");
        this.dataManager.register(OTHER_TRUSTED, "");
        this.dataManager.register(TYPE, (byte) 0);
        this.dataManager.register(FOX_FLAGS, (byte) 0);
    }

    protected void initTasks() {
        this.followChickenAndRabbitTask = new EntityAINearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 10, true, false,
                entity -> entity instanceof EntityChicken || entity instanceof EntityRabbit);
        this.followBabyTurtleTask = new EntityAINearestAttackableTarget<EntityTurtle>(this, EntityTurtle.class, 10, true, false,
                entity -> ((EntityTurtle) entity).isChild() && !entity.isInWater());
        this.followFishTask = new EntityAINearestAttackableTarget<EntityFish>(this, EntityFish.class, 20, true, false,
                entity -> !(entity instanceof EntityPufferfish));

        this.tasks.addTask(0, new EntityFox.AISwim());
        this.tasks.addTask(1, new EntityFox.AIStopWandering());
        this.tasks.addTask(2, new EntityFox.AIEscapeWhenNotAggressive(2.2D));

        this.tasks.addTask(3, new EntityAIAvoidEntity<EntityPlayer>(this,EntityPlayer.class,livingEntity -> NOTICEABLE_PLAYER_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUniqueID()) && !this.isAggressive(),16.0F,1.6D,1.4D));
        this.tasks.addTask(3, new EntityAIAvoidEntity<EntityWolf>(this,EntityWolf.class,livingEntity -> !((EntityWolf) livingEntity).isTamed() && !this.isAggressive(),8.0F,1.6D,1.4D));
        this.tasks.addTask(3, new EntityAIAvoidEntity<EntityPolarBear>(this,EntityPolarBear.class,livingEntity -> !this.isAggressive(),8.0F,1.6D,1.4D));

        this.tasks.addTask(4, new EntityFox.AIMoveToHunt());
        this.tasks.addTask(5, new EntityFox.AIJumpChase());
        this.tasks.addTask(5, new EntityFox.AIMate(1.0D));
        this.tasks.addTask(5, new EntityFox.AIAvoidDaylight(1.25D));
        this.tasks.addTask(6, new EntityFox.AIAttack(1.2D, true));
        this.tasks.addTask(6, new EntityFox.AIDelayedCalmDown());
        this.tasks.addTask(7, new EntityFox.AIFollowParent(this, 1.25D));
        this.tasks.addTask(8, new EntityAIMoveThroughVillage(this, 1.0D, true));
        if (Content.available("sweet_berries")||Content.available("glow_berries")) {
            this.tasks.addTask(9, new EntityFox.AIEatSweetBerries(1.2D, 12, 2));
        }
        this.tasks.addTask(9, new EntityAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(10, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(10, new EntityFox.AIPickupItem());
        this.tasks.addTask(11, new EntityFox.AILookAtEntity(this, EntityPlayer.class, 24.0F));
        this.tasks.addTask(12, new EntityFox.AISitDownAndLookAround());

        this.targetTasks.addTask(3, new EntityFox.AIDefendFriend(EntityLiving.class, false, false,
                entity -> entity != null && JUST_ATTACKED_SOMETHING_FILTER.test(entity) && !this.canTrust(entity.getUniqueID())));
    }

    @Override
    public void onLivingUpdate() {
        if (!this.world.isRemote && this.isEntityAlive()) {
            ++this.eatingTime;
            ItemStack itemStack = this.getHeldItemMainhand();
            if (this.canEat(itemStack)) {
                int eatDelay = 560;
                if (this.eatingTime > eatDelay + 40) {
                    ItemStack itemStack2 = this.eatFood(this.world, itemStack);
                    if (!itemStack2.isEmpty()) {
                        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemStack2);
                    } else {
                        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    }

                    this.eatingTime = 0;
                } else if (this.eatingTime > eatDelay && this.rand.nextFloat() < 0.1F) {
                    this.playSound(this.getEatSound(), 1.0F, 1.0F);
                    this.world.setEntityState(this, (byte) 45);
                }
            }

            EntityLivingBase livingEntity = this.getAttackTarget();
            if (livingEntity == null || !livingEntity.isEntityAlive()) {
                this.setCrouching(false);
                this.setRollingHead(false);
            }
        }

        if (this.getPetOwner() != null && this.getDistanceSq(this.getPetOwner()) > 64f * 64f) {
            followOwner = false;
        }

        if (this.isPlayerSleeping() || this.isDead) {
            this.isJumping = false;
            this.moveStrafing = 0.0F;
            this.moveForward = 0.0F;
        }

        super.onLivingUpdate();

        if (this.isAggressive() && this.rand.nextFloat() < 0.05F) {
            this.playSound(ModernSounds.get("entity.fox.aggro"), 1.0F, 1.0F);
        }
    }

    private boolean canEat(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof ItemFood
                && this.getAttackTarget() == null
                && this.onGround
                && !this.isPlayerSleeping();
    }

    protected void initEquipment(){if(rand.nextFloat()<.2F){int roll=rand.nextInt(100);Item item=roll<10?Items.RABBIT_FOOT:roll<20?Items.RABBIT_HIDE:roll<25?Items.EMERALD:roll<40?Items.EGG:roll<60?Items.WHEAT:roll<80?Items.LEATHER:Items.FEATHER;setItemStackToSlot(EntityEquipmentSlot.MAINHAND,new ItemStack(item));}}
    @Override @SideOnly(Side.CLIENT) public void handleStatusUpdate(byte status){if(status!=45){super.handleStatusUpdate(status);return;}ItemStack stack=getHeldItemMainhand();if(stack.isEmpty())return;for(int i=0;i<8;i++){Vec3d velocity=new Vec3d((rand.nextFloat()-.5)*.1,rand.nextDouble()*.1+.1,0).rotatePitch(-rotationPitch*.017453292F).rotateYaw(-rotationYaw*.017453292F);Vec3d look=getRotationVector(rotationPitch,rotationYaw);world.spawnParticle(EnumParticleTypes.ITEM_CRACK,posX+look.x/2,posY,posZ+look.z/2,velocity.x,velocity.y+.05,velocity.z,Item.getIdFromItem(stack.getItem()),stack.getMetadata());}}

    private static Vec3d getRotationVector(float pitch, float yaw) {
        float f = (float) (pitch * Math.PI / 180.0F);
        float g = (float) (-yaw * Math.PI / 180.0F);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2D);
    }

    @Override
    public EntityFox createChild(EntityAgeable passiveEntity) {
        EntityFox EntityFox = new EntityFox(this.world);
        EntityFox.setType(this.rand.nextBoolean() ? this.getFoxType() : ((EntityFox) passiveEntity).getFoxType());
        return EntityFox;
    }

    @Override
    public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty,IEntityLivingData entityData) {
        entityData = super.onInitialSpawn(difficulty,entityData);
        EntityFox.Type type = EntityFox.Type.fromBiome(world.getBiome(new BlockPos(this)));
        boolean bl = false;
        if (entityData instanceof EntityFox.FoxData) {
            type = ((EntityFox.FoxData) entityData).type;
            if (((EntityFox.FoxData) entityData).getSpawnedCount() >= 2) {
                bl = true;
            }
        } else {
            entityData = new EntityFox.FoxData(type);
        }

        this.setType(type);
        if (bl) {
            this.setGrowingAge(-24000);
        }

        if (world instanceof WorldServer) {
            this.addTypeSpecificTasks();
        }

		this.initEquipment();

        EntityFox.FoxData foxData = (EntityFox.FoxData) entityData;
        if (foxData.canSpawnBaby() && foxData.getSpawnedCount() > 0 && rand.nextFloat() <= foxData.getBabyChance()) {
            this.setGrowingAge(-24000);
        }
        foxData.countSpawned();
        return entityData;
    }

    private void addTypeSpecificTasks() {
        targetTasks.removeTask(followChickenAndRabbitTask);targetTasks.removeTask(followBabyTurtleTask);targetTasks.removeTask(followFishTask);
        if (this.getFoxType() == EntityFox.Type.RED) {
            this.targetTasks.addTask(4, this.followChickenAndRabbitTask);
            this.targetTasks.addTask(4, this.followBabyTurtleTask);
            this.targetTasks.addTask(6, this.followFishTask);
        } else {
            this.targetTasks.addTask(4, this.followFishTask);
            this.targetTasks.addTask(6, this.followChickenAndRabbitTask);
            this.targetTasks.addTask(6, this.followBabyTurtleTask);
        }
    }

    public void eat(EntityPlayer player, ItemStack stack) {
        if (this.isBreedingItem(stack)) {
            this.playSound(this.getEatSound(), 1.0F, 1.0F);
        }

        if(!player.capabilities.isCreativeMode)stack.shrink(1);
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? this.height * 0.85F : 0.4F;
    }

    public EntityFox.Type getFoxType() {
        return EntityFox.Type.fromId(dataManager.get(TYPE));
    }

    private void setType(EntityFox.Type type) {
        this.dataManager.set(TYPE, (byte) type.getId());
    }

    private List<UUID> getTrustedUuids() {
        List<UUID> list = new ArrayList<>();
        String owner = this.dataManager.get(OWNER);
        list.add(owner == null || owner.isEmpty() ? null : UUID.fromString(owner));
        String otherTrusted = this.dataManager.get(OTHER_TRUSTED);
        list.add(otherTrusted == null || otherTrusted.isEmpty() ? null : UUID.fromString(otherTrusted));
        return list;
    }

    private void addTrustedUuid(UUID uuid) {
        if (!this.dataManager.get(OWNER).isEmpty()) {
            this.dataManager.set(OTHER_TRUSTED, uuid.toString());
        } else {
            this.dataManager.set(OWNER, uuid.toString());
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        List<UUID> list = this.getTrustedUuids();
        NBTTagList listTag = new NBTTagList();

        for (UUID uUID : list) {
            if (uUID != null) {
                listTag.appendTag(new NBTTagString(uUID.toString()));
            }
        }

        tag.setTag("Trusted", listTag);
        tag.setBoolean("Sleeping", this.isPlayerSleeping());
        tag.setString("Type", this.getFoxType().getKey());
        tag.setBoolean("Sitting", this.isSitting());
        tag.setBoolean("Crouching", this.isInSneakingPose());
        if (this.followOwner) {
            tag.setBoolean("FollowOwner", true);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagList listTag = tag.getTagList("Trusted", Constants.NBT.TAG_STRING);

        dataManager.set(OWNER,"");dataManager.set(OTHER_TRUSTED,"");
        for (int i = 0; i < Math.min(2,listTag.tagCount()); ++i) {
            try{this.addTrustedUuid(UUID.fromString(listTag.getStringTagAt(i)));}catch(IllegalArgumentException ignored){}
        }

        this.setSleeping(tag.getBoolean("Sleeping"));
        this.setType(EntityFox.Type.byName(tag.getString("Type")));
        this.setSitting(false);//tag.getBoolean("Sitting")); // disabled to avoid bugs
        this.setCrouching(tag.getBoolean("Crouching"));
        if (this.world instanceof WorldServer) {
            this.addTypeSpecificTasks();
        }
        if (tag.hasKey("FollowOwner")) {
            this.followOwner = tag.getBoolean("FollowOwner");
        }
    }

    public boolean isSitting() {
        return this.getFoxFlag(1);
    }

    public void setSitting(boolean sitting) {
        this.setFoxFlag(1, sitting);
    }

    public boolean isWalking() {
        return this.getFoxFlag(64);
    }

    private void setWalking(boolean walking) {
        this.setFoxFlag(64, walking);
    }

    private boolean isAggressive() {
        return this.getFoxFlag(128);
    }

    private void setAggressive(boolean aggressive) {
        this.setFoxFlag(128, aggressive);
    }

    @Override
    public boolean isPlayerSleeping() {
        return this.getFoxFlag(32);
    }

    private void setSleeping(boolean sleeping) {
        this.setFoxFlag(32, sleeping);
    }

    private void setFoxFlag(int mask, boolean value) {
        if (value) {
            this.dataManager.set(FOX_FLAGS, (byte) (this.dataManager.get(FOX_FLAGS) | mask));
        } else {
            this.dataManager.set(FOX_FLAGS, (byte) (this.dataManager.get(FOX_FLAGS) & ~mask));
        }
    }

    private boolean getFoxFlag(int bitmask) {
        return (this.dataManager.get(FOX_FLAGS) & bitmask) != 0;
    }

    public boolean canPickupItem(ItemStack stack){ItemStack held=getHeldItemMainhand();return !stack.isEmpty()&&(held.isEmpty()||eatingTime>0&&stack.getItem() instanceof ItemFood&&!(held.getItem() instanceof ItemFood));}
    private boolean spit(ItemStack stack){if(stack.isEmpty())return true;if(world.isRemote)return false;Vec3d look=getRotationVector(rotationPitch,rotationYaw);EntityItem item=new EntityItem(world,posX+look.x,posY+1,posZ+look.z,stack.copy());item.setPickupDelay(40);item.setThrower(getUniqueID().toString());if(!world.spawnEntity(item))return false;playSound(ModernSounds.get("entity.fox.spit"),1,1);return true;}
    @Override protected void updateEquipmentIfNeeded(EntityItem item){loot(item);}
    protected void loot(EntityItem item){ItemStack stack=item.getItem();if(!canPickupItem(stack)||!spit(getHeldItemMainhand()))return;EntityPlayer dropper=item.getThrower()==null?null:world.getPlayerEntityByName(item.getThrower());if(dropper==null&&item.getThrower()!=null)try{dropper=world.getPlayerEntityByUUID(UUID.fromString(item.getThrower()));}catch(IllegalArgumentException ignored){}if(isBreedingItem(stack)&&dropper!=null&&dropper.getUniqueID().equals(getTrustedUuids().get(0)))followOwner=true;setItemStackToSlot(EntityEquipmentSlot.MAINHAND,stack.splitStack(1));setDropChance(EntityEquipmentSlot.MAINHAND,2);onItemPickup(item,1);if(stack.isEmpty())item.setDead();eatingTime=0;}

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            boolean bl = this.isInWater();
            if (bl || this.getAttackTarget() != null || this.world.isThundering()) {
                this.stopSleeping();
            }

            if (bl || this.isPlayerSleeping()) {
                this.setSitting(false);
            }

            if(isWalking()&&world.rand.nextFloat()<.2F)world.playEvent(2001,new BlockPos(this),Block.getStateId(world.getBlockState(new BlockPos(this))));
        }

        this.lastHeadRollProgress = this.headRollProgress;
        if (this.isRollingHead()) {
            this.headRollProgress += (1.0F - this.headRollProgress) * 0.4F;
        } else {
            this.headRollProgress += (0.0F - this.headRollProgress) * 0.4F;
        }

        this.lastExtraRollingHeight = this.extraRollingHeight;
        if (this.isInSneakingPose()) {
            this.extraRollingHeight += 0.2F;
            if (this.extraRollingHeight > 3.0F) {
                this.extraRollingHeight = 3.0F;
            }
        } else {
            this.extraRollingHeight = 0.0F;
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return !stack.isEmpty() && getBreedingItems().contains(stack.getItem());
    }

    private static List<Item> getBreedingItems(){List<Item> items=new ArrayList<>();if(Content.available("sweet_berries"))items.add(Content.item("sweet_berries"));if(Content.available("glow_berries"))items.add(Content.item("glow_berries"));if(items.isEmpty())items.add(Items.WHEAT);return items;}

    public boolean isChasing() {
        return this.getFoxFlag(16);
    }

    public void setChasing(boolean chasing) {
        this.setFoxFlag(16, chasing);
    }

    public boolean isFullyCrouched() {
        return this.extraRollingHeight == 3.0F;
    }

    public void setCrouching(boolean crouching) {
        this.setFoxFlag(4, crouching);
    }

    public boolean isInSneakingPose() {
        return this.getFoxFlag(4);
    }

    public void setRollingHead(boolean rollingHead) {
        this.setFoxFlag(8, rollingHead);
    }

    public boolean isRollingHead() {
        return this.getFoxFlag(8);
    }

    @SideOnly(Side.CLIENT)
    public float getHeadRoll(float tickDelta) {
        return (this.lastHeadRollProgress + tickDelta * (this.headRollProgress - this.lastHeadRollProgress)) * 0.11F * (float) Math.PI;
    }

    @SideOnly(Side.CLIENT)
    public float getBodyRotationHeightOffset(float tickDelta) {
        return this.lastExtraRollingHeight + tickDelta * (this.extraRollingHeight - this.lastExtraRollingHeight);
    }

    @Override
    public void setAttackTarget(EntityLivingBase target) {
        if (this.isAggressive() && target == null) {
            this.setAggressive(false);
        }

        super.setAttackTarget(target);
    }

    public float computeFallDistance(float fallDistance) {
        return fallDistance - 2f;
    }

    private void stopSleeping() {
        this.setSleeping(false);
    }

    @Override public void fall(float distance,float multiplier){super.fall(Math.max(0,computeFallDistance(distance)),multiplier);}

    private void stopActions() {
        this.setRollingHead(false);
        this.setCrouching(false);
        this.setSitting(false);
        this.setSleeping(false);
        this.setAggressive(false);
        this.setWalking(false);
    }

    private boolean wantsToPickupItem() {
        return !this.isPlayerSleeping() && !this.isSitting() && !this.isWalking();
    }

    @Override
    public void playLivingSound() {
        SoundEvent soundEvent=this.getAmbientSound();
        if (soundEvent==ModernSounds.get("entity.fox.screech")) {
            this.playSound(soundEvent, 2.0F, this.getSoundPitch());
        } else {
            super.playLivingSound();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isPlayerSleeping()) {
            return ModernSounds.get("entity.fox.sleep");
        } else {
            if (!this.world.isDaytime() && this.rand.nextFloat() < 0.1F) {
                List<EntityPlayer> list = this.world.getEntitiesWithinAABB(
                        EntityPlayer.class,
                        getEntityBoundingBox().grow(16.0D, 16.0D, 16.0D));
                if (list.isEmpty()) {
                    return ModernSounds.get("entity.fox.screech");
                }
            }
            return ModernSounds.get("entity.fox.ambient");
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModernSounds.get("entity.fox.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModernSounds.get("entity.fox.death");
    }

    protected SoundEvent getEatSound() {
        return ModernSounds.get("entity.fox.eat");
    }

    private boolean canTrust(UUID uuid) {
        return this.getTrustedUuids().contains(uuid);
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!this.world.isRemote) {
            ItemStack itemStack = this.getHeldItemMainhand();
            if (!itemStack.isEmpty()) {
                this.entityDropItem(itemStack, 0.0F);
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }

        super.onDeath(source);
    }

    public static boolean canJumpChase(EntityFox fox, EntityLivingBase chasedEntity) {
        double d = chasedEntity.posZ - fox.posZ;
        double e = chasedEntity.posX - fox.posX;
        for(int j=0;j<6;j++){double fraction=j/6.0;for(int k=1;k<4;k++){BlockPos pos=new BlockPos(fox.posX+e*fraction,fox.posY+k,fox.posZ+d*fraction);if(!fox.world.isBlockLoaded(pos)||!fox.world.getBlockState(pos).getMaterial().isReplaceable())return false;}}

        return true;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        float f = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int i = 0;

        if (target instanceof EntityLivingBase) {
            f += EnchantmentHelper.getModifierForCreature(getHeldItemMainhand(),((EntityLivingBase)target).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        int j = EnchantmentHelper.getFireAspectModifier(this);

        if (j > 0) {
            target.setFire(j * 4);
        }

        boolean result = target.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (result) {
            if (i > 0) {
                target.addVelocity(
                        -MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F, 0.1D,
                        MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F);
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            this.dealDamage(this, target);
            this.onAttacking(target);
            this.playSound(ModernSounds.get("entity.fox.bite"), 1.0F, 1.0F);
        }

        return result;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if ("sweetBerryBush".equals(source.damageType)) {
            return false;
        }
        return super.attackEntityFrom(source, damage);
    }

    public EntityLivingBase getPetOwner() {
        UUID uUID = EntityFox.this.getTrustedUuids().get(0);

        if (uUID != null && EntityFox.this.world instanceof WorldServer) {
            // Assuming owner is a player
            return EntityFox.this.world.getPlayerEntityByUUID(uUID); // getPlayerByUuid
        }
        return null;
    }

    public int getLootingLevel() {
        int lootingLevel = EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.LOOTING, this.getHeldItemMainhand());
        return Math.max(0, lootingLevel);
    }

    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack stack=player.getHeldItem(hand);if(isBreedingItem(stack)&&getGrowingAge()==0&&!isInLove()){if(!world.isRemote){eat(player,stack);setInLove(player);}return true;}return super.processInteract(player,hand);}

    public boolean canEat() {
        return !this.isInLove();
    }

    public ItemStack eatFood(World world,ItemStack stack){if(stack.getItem() instanceof ItemFood){playSound(getEatSound(),1,1+(rand.nextFloat()-rand.nextFloat())*.4F);if(!world.isRemote)((FoodEffectControl)stack.getItem()).etfu$applyFoodEffect(this);stack.shrink(1);}return stack;}

    public int getLookPitchSpeed() {
        return 40;
    }

    public int getBodyYawSpeed() {
        return 75;
    }

    public void dealDamage(EntityLivingBase attacker, Entity target) {
        if (target instanceof EntityLivingBase) {
            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)target, attacker);
        }
        EnchantmentHelper.applyArthropodEnchantments(attacker, target);
    }

    public void onAttacking(Entity target) {
        this.setLastAttackedEntity(target);
    }

    private class AILookAtEntity extends EntityAIWatchClosest {

        public AILookAtEntity(EntityLiving fox, Class<? extends Entity> targetType, float range) {
            super(fox, targetType, range);
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && !EntityFox.this.isWalking() && !EntityFox.this.isRollingHead();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && !EntityFox.this.isWalking() && !EntityFox.this.isRollingHead();
        }
    }

    private static class AIFollowParent extends EntityAIFollowParent {

        private final EntityFox fox;

        public AIFollowParent(EntityFox fox, double speed) {
            super(fox, speed);
            this.fox = fox;
        }

        @Override
        public boolean shouldExecute() {
            return !this.fox.isAggressive() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.fox.isAggressive() && super.shouldContinueExecuting();
        }

        @Override
        public void startExecuting() {
            this.fox.stopActions();
            super.startExecuting();
        }
    }

    private class FoxLookHelper extends ExtendedEntityLookHelper {

        public FoxLookHelper() {
            super(EntityFox.this);
        }

        @Override
        public void onUpdateLook() {
            if (!EntityFox.this.isPlayerSleeping()) {
                super.onUpdateLook();
            }
        }

        @Override
        protected boolean shouldStayHorizontal() {
            return !EntityFox.this.isChasing()
                    && !EntityFox.this.isInSneakingPose()
                    && !EntityFox.this.isRollingHead()
                    && !EntityFox.this.isWalking();
        }
    }

    private class AIJumpChase extends EntityAIBase {

        public AIJumpChase() {
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityFox.this.isFullyCrouched()) {
                return false;
            } else {
                EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
                if (livingEntity != null && livingEntity.isEntityAlive()) {
                    boolean bl = EntityFox.canJumpChase(EntityFox.this, livingEntity);
                    if (!bl) {
                        EntityFox.this.getNavigator().tryMoveToEntityLiving(livingEntity, 0);
                        EntityFox.this.setCrouching(false);
                        EntityFox.this.setRollingHead(false);
                    }
                    return bl;
                } else {
                    return false;
                }
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
            if (livingEntity != null && livingEntity.isEntityAlive()) {
                double d = EntityFox.this.motionY;
                return (d * d >= 0.05D
                        || Math.abs(EntityFox.this.rotationPitch) >= 15.0F
                        || !EntityFox.this.onGround)
                        && !EntityFox.this.isWalking();
            } else {
                return false;
            }
        }

        @Override
        public boolean isInterruptible() {
            return false;
        }

        @Override
        public void startExecuting() {
            EntityFox.this.setJumping(true);
            EntityFox.this.setChasing(true);
            EntityFox.this.setRollingHead(false);
            EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
            EntityFox.this.getLookHelper().setLookPositionWithEntity(livingEntity, 60.0F, 30.0F);
            Vec3d vec3d = new Vec3d(
                    livingEntity.posX - EntityFox.this.posX,
                    livingEntity.posY - EntityFox.this.posY,
                    livingEntity.posZ - EntityFox.this.posZ).normalize();
            EntityFox.this.motionX += vec3d.x * 0.8D;
            EntityFox.this.motionY += 0.9D;
            EntityFox.this.motionZ += vec3d.z * 0.8D;
            EntityFox.this.getNavigator().clearPath();
        }

        @Override
        public void resetTask() {
            EntityFox.this.setCrouching(false);
            EntityFox.this.extraRollingHeight = 0.0F;
            EntityFox.this.lastExtraRollingHeight = 0.0F;
            EntityFox.this.setRollingHead(false);
            EntityFox.this.setChasing(false);
        }

        @Override
        public void updateTask() {
            EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
            if (livingEntity != null) {
                EntityFox.this.getLookHelper().setLookPositionWithEntity(livingEntity, 60.0F, 30.0F);
            }

            if (!EntityFox.this.isWalking()) {
                Vec3d vec3d = new Vec3d(EntityFox.this.motionX, EntityFox.this.motionY, EntityFox.this.motionZ);
                if (vec3d.y * vec3d.y < 0.03D && EntityFox.this.rotationPitch != 0.0F) {
                    EntityFox.this.rotationPitch = lerpAngle(EntityFox.this.rotationPitch, 0.0F, 0.2F);
                } else {
                    double d = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                    double e = Math.signum(-vec3d.y) * Math.acos(d / vec3d.length()) * 57.2957763671875D;
                    EntityFox.this.rotationPitch = (float) e;
                }
            }

            if (livingEntity != null && EntityFox.this.getDistance(livingEntity) <= 2.0F) {
                EntityFox.this.attackEntityAsMob(livingEntity);
            } else if (EntityFox.this.rotationPitch > 0.0F
                    && EntityFox.this.onGround
                    && (float) EntityFox.this.motionY != 0.0F
                    && EntityFox.this.world.getBlockState(new BlockPos(
                            MathHelper.floor(posX),
                            MathHelper.floor(posY),
                            MathHelper.floor(posZ))).getBlock() == Blocks.SNOW_LAYER) {
                EntityFox.this.rotationPitch = 60.0F;
                EntityFox.this.setAttackTarget(null);
                EntityFox.this.setWalking(true);
            }
        }

        private float lerpAngle(float start, float end, float delta) {
            float f;
            for (f = end - start; f < -180.0F; f += 360.0F);
            while (f >= 180.0F) f -= 360.0F;
            return start + delta * f;
        }
    }

    private class AISwim extends EntityAISwimming {

        public AISwim() {
            super(EntityFox.this);
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            EntityFox.this.stopActions();
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute();
        }
    }

    /*
    private class GoToVillageGoal extends net.minecraft.entity.ai.goal.GoToVillageGoal {
        public GoToVillageGoal(int unused, int searchRange) {
            super(EntityFox.this, searchRange);
        }

        public void start() {
            EntityFox.this.stopActions();
            super.start();
        }

        public boolean canStart() {
            return super.canStart() && this.canGoToVillage();
        }

        public boolean shouldContinue() {
            return super.shouldContinue() && this.canGoToVillage();
        }

        private boolean canGoToVillage() {
            return !EntityFox.this.isPlayerSleeping()
                    && !EntityFox.this.isSitting()
                    && !EntityFox.this.isAggressive()
                    && EntityFox.this.getAttackTarget() == null;
        }
    }*/

    private class AIEscapeWhenNotAggressive extends EntityAIPanic {

        private int startTime;
        private static final int MAX_DURATION = 7 * 20;

        public AIEscapeWhenNotAggressive(double speed) {
            super(EntityFox.this, speed);
        }

        @Override
        public boolean shouldExecute() {
            return !EntityFox.this.isAggressive() && super.shouldExecute();
        }

        @Override
        public void startExecuting() {
            startTime = EntityFox.this.ticksExisted;
            super.startExecuting();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && !isTimedOut();
        }

        @Override
        public void resetTask() {
            if (isTimedOut()) {
                EntityFox.this.getNavigator().clearPath();
            }
            super.resetTask();
        }

        private boolean isTimedOut() {
            return EntityFox.this.ticksExisted >= startTime + MAX_DURATION;
        }
    }

    private class AIStopWandering extends EntityAIBase {

        int timer;

        public AIStopWandering() {
            setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            return EntityFox.this.isWalking();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute() && this.timer > 0;
        }

        @Override
        public void startExecuting() {
            this.timer = 40;
        }

        @Override
        public void resetTask() {
            EntityFox.this.setWalking(false);
        }

        @Override
        public void updateTask() {
            --this.timer;
        }
    }

    public static class FoxData implements IEntityLivingData {

        public final EntityFox.Type type;
        private int spawnCount;
        private final boolean babyAllowed;
        private final float babyChance;

        public FoxData(EntityFox.Type type) {
            this.type = type;
            this.babyAllowed = false;
            this.babyChance = 0.05F;
        }

        public int getSpawnedCount() {
            return this.spawnCount;
        }

        public void countSpawned() {
            ++this.spawnCount;
        }

        public boolean canSpawnBaby() {
            return this.babyAllowed;
        }

        public float getBabyChance() {
            return this.babyChance;
        }
    }

    private class AIEatSweetBerries extends EntityAIBase {

        public final double speed;
        protected int cooldown;
        protected int tryingTime;
        private int safeWaitingTime;
        protected BlockPos targetPos;
        private boolean reached;
        private final int range;
        private final int maxYDifference;
        protected int lowestY;
        protected int timer;

        public AIEatSweetBerries(double speed, int range, int maxYDifference) {
            this.targetPos = BlockPos.ORIGIN;
            this.speed = speed;
            this.range = range;
            this.lowestY = 0;
            this.maxYDifference = maxYDifference;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityFox.this.isPlayerSleeping()) return false;

            if (this.cooldown > 0) {
                --this.cooldown;
                return false;
            } else {
                this.cooldown = this.getInterval(EntityFox.this);
                return this.findTargetPos();
            }
        }

        private int getInterval(EntityCreature mob) {
            return 200 + mob.getRNG().nextInt(200);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.tryingTime >= -this.safeWaitingTime
                    && this.tryingTime <= 1200
                    && this.isTargetPos(EntityFox.this.world, this.targetPos);
        }

        @Override
        public void startExecuting() {
            this.timer = 0;
            EntityFox.this.setSitting(false);
            this.startMovingToTarget();
            this.tryingTime = 0;
            this.safeWaitingTime = EntityFox.this.getRNG().nextInt(EntityFox.this.getRNG().nextInt(1200) + 1200) + 1200;
        }

        private void startMovingToTarget() {
            EntityFox.this.getNavigator().tryMoveToXYZ(
                    (double) ((float) this.targetPos.getX()) + 0.5D,
                    this.targetPos.getY() + 1,
                    (double) ((float) this.targetPos.getZ()) + 0.5D,
                    this.speed);
        }

        private double getDesiredSquaredDistanceToTarget() {
            return 2.0D;
        }

        private BlockPos getTargetPos() {
            return this.targetPos.up();
        }

        @Override
        public void updateTask() {
            if (this.hasReached()) {
                if (this.timer >= 40) {
                    this.eatSweetBerry();
                } else {
                    ++this.timer;
                }
            } else if (!this.hasReached() && EntityFox.this.rand.nextFloat() < 0.05F) {
                EntityFox.this.playSound(ModernSounds.get("entity.fox.sniff"), 1.0F, 1.0F);
            }

            BlockPos blockPos = this.getTargetPos();
            if (EntityFox.this.getDistanceSq(blockPos.getX(), blockPos.getY(), blockPos.getZ()) >= Math.pow(this.getDesiredSquaredDistanceToTarget(), 2)) {
                this.reached = false;
                ++this.tryingTime;
                if (this.shouldResetPath()) {
                    EntityFox.this.getNavigator().tryMoveToXYZ(
                            (double) ((float) blockPos.getX()) + 0.5D,
                            blockPos.getY(),
                            (double) ((float) blockPos.getZ()) + 0.5D,
                            this.speed);
                }
            } else {
                this.reached = true;
                --this.tryingTime;
            }
        }

        private boolean shouldResetPath() {
            return this.tryingTime % 100 == 0;
        }

        private boolean hasReached() {
            return this.reached;
        }

        protected boolean findTargetPos() {
            int mobX = MathHelper.floor(EntityFox.this.posX);
            int mobY = MathHelper.floor(EntityFox.this.posY);
            int mobZ = MathHelper.floor(EntityFox.this.posZ);
            int bx, by, bz;

            for(int k = this.lowestY; k <= this.maxYDifference; k = k > 0 ? -k : 1 - k) {
                for(int l = 0; l < this.range; ++l) {
                    for(int m = 0; m <= l; m = m > 0 ? -m : 1 - m) {
                        for(int n = m < l && m > -l ? l : 0; n <= l; n = n > 0 ? -n : 1 - n) {
                            bx = mobX + m;
                            by = mobY + k - 1;
                            bz = mobZ + n;
                            if (EntityFox.this.isWithinHomeDistanceFromPosition(new BlockPos(bx,by,bz))
                                    && this.isTargetPos(EntityFox.this.world, bx, by, bz)) {
                                this.targetPos = new BlockPos(bx, by, bz);
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }

        private boolean isTargetPos(World world, BlockPos pos) {
            return this.isTargetPos(world, pos.getX(), pos.getY(), pos.getZ());
        }

        private boolean isTargetPos(World world,int x,int y,int z){BlockPos pos=new BlockPos(x,y,z);if(!world.isBlockLoaded(pos))return false;IBlockState state=world.getBlockState(pos);return state.getBlock() instanceof BerryBush&&state.getValue(BerryBush.AGE)>=2||Content.available("glow_berries")&&state.getBlock() instanceof CaveVines&&state.getValue(CaveVines.BERRIES);}
        private void eatSweetBerry(){if(!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,EntityFox.this)||!world.isBlockLoaded(targetPos))return;IBlockState state=world.getBlockState(targetPos);boolean glow=state.getBlock() instanceof CaveVines;if(!isTargetPos(world,targetPos))return;int count=glow?1:1+rand.nextInt(2)+(state.getValue(BerryBush.AGE)==3?1:0);ItemStack berries=new ItemStack(Content.item(glow?"glow_berries":"sweet_berries"),count);boolean hold=getHeldItemMainhand().isEmpty();if(hold)berries.shrink(1);if(!berries.isEmpty()&&!ItemDelivery.drop(EntityFox.this,berries,0))return;if(hold)setItemStackToSlot(EntityEquipmentSlot.MAINHAND,Content.stack(glow?"glow_berries":"sweet_berries"));world.setBlockState(targetPos,glow?state.withProperty(CaveVines.BERRIES,false):state.withProperty(BerryBush.AGE,1),2);playSound(ModernSounds.get(glow?"block.cave_vines.pick_berries":"block.sweet_berry_bush.pick_berries"),1,1);}
    }

    private class AISitDownAndLookAround extends EntityFox.AICalmDown {

        private double lookX;
        private double lookZ;
        private int timer;
        private int counter;

        public AISitDownAndLookAround() {
            super(null);
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return isIdle() && EntityFox.this.rand.nextFloat() < 0.02F;
        }

        private boolean isIdle() {
            return EntityFox.this.getAttackTarget() == null
                    && !EntityFox.this.isPlayerSleeping()
                    && EntityFox.this.getAttackTarget() == null
                    && EntityFox.this.getNavigator().noPath()
                    && !EntityFox.this.isChasing()
                    && !EntityFox.this.isInSneakingPose()
                    && EntityFox.this.getHealth() > EntityFox.this.getMaxHealth() / 4f
                    && !this.canNotCalmDown();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.counter > 0 && isIdle();
        }

        @Override
        public void startExecuting() {
            this.chooseNewAngle();
            this.counter = 2 + EntityFox.this.rand.nextInt(3);
            EntityFox.this.setSitting(true);
            EntityFox.this.getNavigator().clearPath();
        }

        @Override
        public void resetTask() {
            EntityFox.this.setSitting(false);
        }

        @Override
        public void updateTask() {
            --this.timer;
            if (this.timer <= 0) {
                --this.counter;
                this.chooseNewAngle();
            }

            EntityFox.this.getLookHelper().setLookPosition(
                    EntityFox.this.posX + this.lookX,
                    EntityFox.this.posY + EntityFox.this.getEyeHeight(),
                    EntityFox.this.posZ + this.lookZ,
                    (float) EntityFox.this.getBodyYawSpeed(),
                    (float) EntityFox.this.getLookPitchSpeed());
        }

        private void chooseNewAngle() {
            double d = Math.PI * 2.0D * EntityFox.this.rand.nextDouble();
            this.lookX = Math.cos(d);
            this.lookZ = Math.sin(d);
            this.timer = 80 + EntityFox.this.rand.nextInt(20);
        }
    }

    private class AIDelayedCalmDown extends EntityFox.AICalmDown {

        private int timer;

        public AIDelayedCalmDown() {
            super(null);
            this.timer = EntityFox.this.rand.nextInt(140);
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityFox.this.moveStrafing == 0.0F && EntityFox.this.onGround && EntityFox.this.moveForward == 0.0F) {
                return this.canCalmDown() || EntityFox.this.isPlayerSleeping();
            } else {
                return false;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.canCalmDown();
        }

        private boolean canCalmDown() {
            if (this.timer > 0) {
                --this.timer;
                return false;
            } else {
                return EntityFox.this.world.isDaytime()&&!EntityFox.this.world.isThundering() && this.isAtFavoredLocation() && !this.canNotCalmDown();
            }
        }

        @Override
        public void resetTask() {
            this.timer = EntityFox.this.rand.nextInt(140);
            EntityFox.this.stopActions();
        }

        @Override
        public void startExecuting() {
            EntityFox.this.setSitting(false);
            EntityFox.this.setCrouching(false);
            EntityFox.this.setRollingHead(false);
            EntityFox.this.setJumping(false);
            EntityFox.this.setSleeping(true);
            EntityFox.this.getNavigator().clearPath();
            EntityFox.this.getMoveHelper().setMoveTo(EntityFox.this.posX, EntityFox.this.posY, EntityFox.this.posZ, 0.0D);
        }
    }

    private abstract class AICalmDown extends EntityAIBase {

        private final TargetPredicate WORRIABLE_ENTITY_PREDICATE;

        private AICalmDown() {
            this.WORRIABLE_ENTITY_PREDICATE = (new TargetPredicate())
                    .setBaseMaxDistance(12.0D)
                    .includeHidden()
                    .setPredicate(EntityFox.this.new WorriableEntityFilter());
        }

        protected boolean isAtFavoredLocation() {
            BlockPos blockPos = new BlockPos(EntityFox.this.posX, EntityFox.this.getEntityBoundingBox().maxY, EntityFox.this.posZ);
            return !EntityFox.this.world.canSeeSky(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ())) &&
                    EntityFox.this.getBlockPathWeight(blockPos) >= 0.0F;
        }

        protected boolean canNotCalmDown() {
            List<EntityLivingBase> list = EntityFox.this.world.getEntitiesWithinAABB(
                    EntityLivingBase.class,
                    EntityFox.this.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D),
                    null);

            List<EntityLivingBase> list2 = new ArrayList<>();
            for (EntityLivingBase entityLivingBase : list) {
                if (this.WORRIABLE_ENTITY_PREDICATE.test(EntityFox.this, entityLivingBase)) {
                    list2.add(entityLivingBase);
                }
            }

            return !list2.isEmpty();
        }

        // $FF: synthetic method
        AICalmDown(Object arg) {
            this();
        }
    }

    private class WorriableEntityFilter implements Predicate<EntityLivingBase> {
        public boolean test(EntityLivingBase livingEntity) {
            if (livingEntity instanceof EntityFox) {
                return false;
            } else if (!(livingEntity instanceof EntityChicken) && !(livingEntity instanceof EntityRabbit) && !(livingEntity instanceof EntityMob)) {
                if (livingEntity instanceof EntityTameable) {
                    return !((EntityTameable) livingEntity).isTamed();
                } else if (livingEntity instanceof EntityPlayer && (((EntityPlayer)livingEntity).isSpectator()||((EntityPlayer)livingEntity).capabilities.isCreativeMode)) {
                    return false;
                } else if (EntityFox.this.canTrust(livingEntity.getUniqueID())) {
                    return false;
                } else {
                    return !livingEntity.isPlayerSleeping() && !livingEntity.isSneaking();
                }
            } else {
                return true;
            }
        }
    }

    private class AIAvoidDaylight extends EntityAIFleeSunExtended {
        private int timer = 100;

        public AIAvoidDaylight(double speed) {
            super(EntityFox.this, speed);
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityFox.this.isPlayerSleeping() && EntityFox.this.getAttackTarget() == null) {
                if (EntityFox.this.world.isThundering()) {
                    return EntityFox.this.world.canSeeSky(new BlockPos(
                            MathHelper.floor(posX),
                            MathHelper.floor(posY),
                            MathHelper.floor(posZ)))
                            // todo when points of interest are added
                            /*&& !EntityFox.this.world.isNearOccupiedPointOfInterest(blockPos)*/
                            && this.targetShadedPos();
                } else if (this.timer > 0) {
                    --this.timer;
                    return false;
                } else {
                    this.timer = 100;
                    return EntityFox.this.world.isDaytime() && EntityFox.this.world.canSeeSky(new BlockPos(
                            MathHelper.floor(posX),
                            MathHelper.floor(posY),
                            MathHelper.floor(posZ)))
                            /*&& !EntityFox.this.world.isNearOccupiedPointOfInterest(blockPos)*/
                            && this.targetShadedPos();
                }
            } else {
                return false;
            }
        }

        @Override
        public void startExecuting() {
            EntityFox.this.stopActions();
            super.startExecuting();
        }
    }

    private class AIDefendFriend extends EntityAITarget {
        private EntityLivingBase offender;private int lastAttackedTime;private final Predicate<EntityLivingBase> predicate;
        AIDefendFriend(Class<? extends Entity> type,boolean visibility,boolean nearby,Predicate<EntityLivingBase> predicate){super(EntityFox.this,visibility,nearby);this.predicate=predicate;}
        @Override public boolean shouldExecute(){if(rand.nextInt(10)!=0)return false;for(UUID uuid:getTrustedUuids())if(uuid!=null){EntityPlayer player=world.getPlayerEntityByUUID(uuid);if(player!=null){EntityLivingBase candidate=player.getRevengeTarget();int time=player.getRevengeTimer();if(time!=lastAttackedTime&&candidate!=null&&predicate.test(candidate)&&isSuitableTarget(candidate,false)){friend=player;offender=candidate;return true;}}}return false;}
        @Override public void startExecuting(){target=offender;setAttackTarget(offender);if(friend!=null)lastAttackedTime=friend.getRevengeTimer();playSound(ModernSounds.get("entity.fox.aggro"),1,1);setAggressive(true);stopSleeping();super.startExecuting();}
        @Override public void resetTask(){friend=null;super.resetTask();}
    }

    private class AIMate extends EntityAIBase {
        private final double speed;private EntityFox targetMate;private int delay;
        AIMate(double speed){this.speed=speed;setMutexBits(3);}
        @Override public boolean shouldExecute(){if(!isInLove()||getGrowingAge()!=0)return false;targetMate=null;double nearest=Double.MAX_VALUE;for(EntityFox fox:world.getEntitiesWithinAABB(EntityFox.class,getEntityBoundingBox().grow(8))){double distance=getDistanceSq(fox);if(canMateWith(fox)&&distance<nearest){nearest=distance;targetMate=fox;}}return targetMate!=null;}
        @Override public boolean shouldContinueExecuting(){return targetMate!=null&&targetMate.isEntityAlive()&&targetMate.isInLove()&&isInLove()&&delay<60;}
        @Override public void startExecuting(){stopActions();targetMate.stopActions();delay=0;}
        @Override public void resetTask(){targetMate=null;delay=0;}
        @Override public void updateTask(){getLookHelper().setLookPositionWithEntity(targetMate,10,getVerticalFaceSpeed());getNavigator().tryMoveToEntityLiving(targetMate,speed);if(++delay>=60&&getDistanceSq(targetMate)<9)spawnBaby();}
        public void spawnBaby(){EntityFox child=createChild(targetMate);EntityPlayer first=getLoveCause(),second=targetMate.getLoveCause();if(first!=null)child.addTrustedUuid(first.getUniqueID());if(second!=null&&first!=second)child.addTrustedUuid(second.getUniqueID());net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event=new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(EntityFox.this,targetMate,child);if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)){setGrowingAge(6000);targetMate.setGrowingAge(6000);resetInLove();targetMate.resetInLove();return;}EntityAgeable baby=event.getChild();if(baby==null)return;baby.setGrowingAge(-24000);baby.setLocationAndAngles(posX,posY,posZ,0,0);if(!world.spawnEntity(baby))return;setGrowingAge(6000);targetMate.setGrowingAge(6000);resetInLove();targetMate.resetInLove();world.setEntityState(EntityFox.this,(byte)18);EntityPlayer cause=first!=null?first:second;if(cause instanceof net.minecraft.entity.player.EntityPlayerMP)net.minecraft.advancements.CriteriaTriggers.BRED_ANIMALS.trigger((net.minecraft.entity.player.EntityPlayerMP)cause,EntityFox.this,targetMate,baby);if(world.getGameRules().getBoolean("doMobLoot"))world.spawnEntity(new EntityXPOrb(world,posX,posY,posZ,rand.nextInt(7)+1));}
    }

    private class AIAttack extends EntityAIAttackMelee {

        public AIAttack(double speed, boolean pauseWhenIdle) {
            super(EntityFox.this,speed,true);
        }

        @Override
        public void startExecuting() {
            EntityFox.this.setRollingHead(false);
            super.startExecuting();
        }

        @Override
        public void updateTask() {
            super.updateTask();
        }

        @Override
        public boolean shouldExecute() {
            return !EntityFox.this.isSitting()
                    && !EntityFox.this.isPlayerSleeping()
                    && !EntityFox.this.isInSneakingPose()
                    && !EntityFox.this.isWalking()
                    && super.shouldExecute();
        }
    }

    private class AIMoveToHunt extends EntityAIBase {
        public AIMoveToHunt() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityFox.this.isPlayerSleeping()) {
                return false;
            } else {
                EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
                return livingEntity != null
                        && livingEntity.isEntityAlive()
                        && EntityFox.CHICKEN_AND_RABBIT_FILTER.test(livingEntity)
                        && EntityFox.this.getDistanceSq(livingEntity) > 36.0D
                        && !EntityFox.this.isInSneakingPose()
                        && !EntityFox.this.isRollingHead()
                        && !EntityFox.this.isJumping;
            }
        }

        @Override
        public void startExecuting() {
            EntityFox.this.setSitting(false);
            EntityFox.this.setWalking(false);
        }

        @Override
        public void resetTask() {
            EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
            if (livingEntity != null && EntityFox.canJumpChase(EntityFox.this, livingEntity)) {
                EntityFox.this.setRollingHead(true);
                EntityFox.this.setCrouching(true);
                EntityFox.this.getNavigator().clearPath();
                EntityFox.this.getLookHelper().setLookPositionWithEntity(
                        livingEntity,
                        (float) EntityFox.this.getBodyYawSpeed(),
                        (float) EntityFox.this.getLookPitchSpeed());
            } else {
                EntityFox.this.setRollingHead(false);
                EntityFox.this.setCrouching(false);
            }

        }

        @Override
        public void updateTask() {
            EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
            EntityFox.this.getLookHelper().setLookPositionWithEntity(
                    livingEntity,
                    (float) EntityFox.this.getBodyYawSpeed(),
                    (float) EntityFox.this.getLookPitchSpeed());
            if (EntityFox.this.getDistanceSq(livingEntity) <= 36.0D) {
                EntityFox.this.setRollingHead(true);
                EntityFox.this.setCrouching(true);
                EntityFox.this.getNavigator().clearPath();
            } else {
                EntityFox.this.getNavigator().tryMoveToEntityLiving(livingEntity, 1.5D);
            }

        }
    }

    private class FoxMoveHelper extends EntityMoveHelper {

        public FoxMoveHelper() {
            super(EntityFox.this);
        }

        @Override
        public void onUpdateMoveHelper() {
            if (EntityFox.this.wantsToPickupItem()) {
                super.onUpdateMoveHelper();
            }
        }
    }

    private class AIPickupItem extends EntityAIBase {

        public AIPickupItem() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityFox.this.getHeldItemMainhand().isEmpty()) {
                return false;
            } else if ((EntityFox.this.getAttackTarget() == null && EntityFox.this.getRevengeTarget() == null)) {
                if (!EntityFox.this.wantsToPickupItem()) {
                    return false;
                } else if (EntityFox.this.rand.nextInt(10) != 0) {
                    return false;
                } else {
                    return canPickUpNearbyItemStack();
                }
            } else {
                return false;
            }
        }

        @Override
        public void updateTask() {
            moveToNearbyItemStack(true);
        }

        @Override
        public void startExecuting() {
            moveToNearbyItemStack(false);
        }

        protected double getFetchSpeed() {
            return 1.2D;
        }

        protected List<EntityItem> getNearbyItemStacks() {
            return EntityFox.this.world.getEntitiesWithinAABB(
                    EntityItem.class,
                    EntityFox.this.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D),
                    EntityFox.PICKABLE_DROP_FILTER);
        }

        protected void moveToNearbyItemStack(boolean onlyIfNotHoldingItem) {
            List<EntityItem> list = getNearbyItemStacks();
            if (!list.isEmpty()) {
                EntityItem ei = list.get(0);
                if (!onlyIfNotHoldingItem || EntityFox.this.getHeldItemMainhand().isEmpty()) {
                    EntityFox.this.getNavigator().tryMoveToEntityLiving(ei, getFetchSpeed());
                }
            }
        }

        protected boolean canPickUpNearbyItemStack() {
            List<EntityItem> nearbyItemStacks = getNearbyItemStacks();
            return !nearbyItemStacks.isEmpty() && EntityFox.this.canPickupItem(nearbyItemStacks.get(0).getItem());
        }
    }

    public enum Type {

        RED(0, "red"),
        SNOW(1, "snow");

        private final int id;
        private final String key;

        Type(int id, String key) {
            this.id = id;
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public int getId() {
            return this.id;
        }

        public static EntityFox.Type byName(String name) {
            if (SNOW.key.equals(name)) {
                return SNOW;
            }
            return RED;
        }

        public static EntityFox.Type fromId(int id) {
            if (id == SNOW.id) return SNOW;
            return RED;
        }

        public static EntityFox.Type fromBiome(Biome bgb) {
            return bgb != null && BiomeDictionary.hasType(bgb, BiomeDictionary.Type.SNOWY) ? SNOW : RED;
        }
    }
}
