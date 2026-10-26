package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class EntityAxolotl extends EntityAquaticAnimal {
    private static final DataParameter<Integer> VARIANT=EntityDataManager.createKey(EntityAxolotl.class,DataSerializers.VARINT);
    private static final DataParameter<Boolean> PLAY_DEAD=EntityDataManager.createKey(EntityAxolotl.class,DataSerializers.BOOLEAN);
    private int moisture=6000,playDeadTicks,huntCooldown,attackCooldown;private boolean fromBucket;private EntityAxolotl courting;
    public EntityAxolotl(World world){super(world);setSize(.75F,.42F);}
    @Override protected void initEntityAI(){tasks.addTask(1,new EntityAIMate(this,1));tasks.addTask(2,new EntityAITempt(this,1,Content.item("tropical_fish_bucket"),false));tasks.addTask(3,new EntityAIFollowParent(this,1));tasks.addTask(4,new EntityAIWander(this,.6));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(VARIANT,0);dataManager.register(PLAY_DEAD,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.15);}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override protected boolean canDespawn(){return !fromBucket&&!hasCustomName();}
    @Override public boolean isCreatureType(EnumCreatureType type,boolean count){return type==MobRegistry.AXOLOTLS;}
    public int getVariant(){return dataManager.get(VARIANT);}
    private void variant(int value){dataManager.set(VARIANT,MathHelper.clamp(value,0,4));}
    public boolean isPlayingDead(){return dataManager.get(PLAY_DEAD);}
    @Override protected boolean canSwim(){return !isPlayingDead();}
    @Override public boolean getCanSpawnHere(){return MobRegistry.enabled(getClass())&&wetSpawn(1,62)&&!world.canSeeSky(new BlockPos(this));}
    @Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,IEntityLivingData data){IEntityLivingData result=super.onInitialSpawn(difficulty,data);variant(rand.nextInt(4));return result;}
    @Override public boolean isBreedingItem(ItemStack stack){return !stack.isEmpty()&&stack.getItem()==Content.item("tropical_fish_bucket");}
    @Override public EntityAgeable createChild(EntityAgeable other){EntityAxolotl child=new EntityAxolotl(world);child.variant(rand.nextInt(1200)==0?4:rand.nextBoolean()?getVariant():((EntityAxolotl)other).getVariant());return child;}
    public void readBucket(NBTTagCompound tag){fromBucket=true;enablePersistence();variant(tag.getInteger("Variant"));setGrowingAge(MathHelper.clamp(tag.getInteger("Age"),-24000,6000));if(tag.hasKey("Health"))setHealth(MathHelper.clamp(tag.getFloat("Health"),1,getMaxHealth()));}
    public ItemStack bucket(){ItemStack bucket=Content.stack("axolotl_bucket");NBTTagCompound tag=new NBTTagCompound();tag.setInteger("Variant",getVariant());tag.setInteger("Age",getGrowingAge());tag.setFloat("Health",getHealth());bucket.setTagCompound(tag);if(hasCustomName())bucket.setStackDisplayName(getCustomNameTag());return bucket;}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){
        ItemStack held=player.getHeldItem(hand);
        if(held.getItem()==Items.WATER_BUCKET&&isEntityAlive()&&Features.enabled("content.axolotl_buckets")){
            if(!world.isRemote){ItemStack bucket=bucket();if(!player.capabilities.isCreativeMode&&held.getCount()==1)player.setHeldItem(hand,bucket);else{if(!player.capabilities.isCreativeMode)held.shrink(1);if(!player.inventory.addItemStackToInventory(bucket))player.dropItem(bucket,false);}setDead();}return true;
        }
        boolean food=isBreedingItem(held),consumed=super.processInteract(player,hand);if(food&&consumed&&!world.isRemote&&!player.capabilities.isCreativeMode){ItemStack empty=new ItemStack(Items.WATER_BUCKET);if(held.isEmpty())player.setHeldItem(hand,empty);else if(!player.inventory.addItemStackToInventory(empty))player.dropItem(empty,false);}return consumed;
    }
    @Override public boolean attackEntityFrom(DamageSource source,float amount){boolean hit=super.attackEntityFrom(source,amount);if(hit&&!world.isRemote&&isInWater()&&source.getTrueSource() instanceof EntityLivingBase&&rand.nextInt(3)==0&&(getHealth()<getMaxHealth()/2||amount>rand.nextInt(3))){playDeadTicks=200;dataManager.set(PLAY_DEAD,true);setAttackTarget(null);navigator.clearPath();addPotionEffect(new PotionEffect(MobEffects.REGENERATION,200));}return hit;}
    @Override protected boolean swimDestination(){
        if(isInLove()){if(ticksExisted%20==0){courting=null;for(EntityAxolotl mate:world.getEntitiesWithinAABB(EntityAxolotl.class,getEntityBoundingBox().grow(8,4,8)))if(canMateWith(mate)){courting=mate;break;}}if(courting!=null&&courting.isEntityAlive()){swimX=courting.posX;swimY=courting.posY;swimZ=courting.posZ;return true;}}
        EntityLivingBase target=getAttackTarget();if(target==null||!target.isEntityAlive()||getDistanceSq(target)>256||!target.isInWater()){setAttackTarget(null);return false;}swimX=target.posX;swimY=target.posY+target.height/2;swimZ=target.posZ;
        if(attackCooldown==0&&getDistanceSq(target)<2.5&&canEntityBeSeen(target)){
            EntityLivingBase assisted=target.getRevengeTarget();attackCooldown=20;target.attackEntityFrom(DamageSource.causeMobDamage(this),2);
            if(!target.isEntityAlive()){huntCooldown=2400;for(EntityPlayer player:world.getEntitiesWithinAABB(EntityPlayer.class,getEntityBoundingBox().grow(20,20,20)))if(assisted==player){player.removePotionEffect(MobEffects.MINING_FATIGUE);PotionEffect effect=player.getActivePotionEffect(MobEffects.REGENERATION);player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION,Math.min(2400,100+(effect==null?0:effect.getDuration()))));}}
        }return true;
    }
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();if(world.isRemote)return;if(isWet())moisture=6000;else if(--moisture<=0&&ticksExisted%20==0)attackEntityFrom(DamageSource.DROWN,2);
        if(attackCooldown>0)--attackCooldown;if(huntCooldown>0)--huntCooldown;
        if(playDeadTicks>0){if(--playDeadTicks==0||!isInWater()){playDeadTicks=0;dataManager.set(PLAY_DEAD,false);}return;}
        if(!isAIDisabled()&&!isChild()&&getAttackTarget()==null&&ticksExisted%40==0&&isInWater()){double closest=64;for(EntityLivingBase target:world.getEntitiesWithinAABB(EntityLivingBase.class,getEntityBoundingBox().grow(8,4,8)))if((target instanceof EntityGuardian||huntCooldown==0&&(target instanceof EntityFish||target instanceof EntitySquid))&&target.isEntityAlive()&&target.isInWater()&&getDistanceSq(target)<closest&&canEntityBeSeen(target)){setAttackTarget(target);closest=getDistanceSq(target);}}
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Variant",getVariant());tag.setBoolean("FromBucket",fromBucket);tag.setInteger("Moisture",moisture);tag.setInteger("HuntCooldown",huntCooldown);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);variant(tag.getInteger("Variant"));fromBucket=tag.getBoolean("FromBucket");moisture=tag.hasKey("Moisture")?MathHelper.clamp(tag.getInteger("Moisture"),0,6000):6000;huntCooldown=MathHelper.clamp(tag.getInteger("HuntCooldown"),0,2400);}
}
