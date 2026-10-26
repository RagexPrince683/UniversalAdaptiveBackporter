package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class EntityPanda extends EntityAnimal {
    private static final DataParameter<Byte> MAIN=EntityDataManager.createKey(EntityPanda.class,DataSerializers.BYTE),HIDDEN=EntityDataManager.createKey(EntityPanda.class,DataSerializers.BYTE);
    private static final DataParameter<Boolean> EATING=EntityDataManager.createKey(EntityPanda.class,DataSerializers.BOOLEAN);
    private int eatTicks;
    public EntityPanda(World world){super(world);setSize(1.3F,1.25F);tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIPanic(this,1.2));tasks.addTask(2,new EntityAIMate(this,1));tasks.addTask(3,new EntityAITempt(this,1,Content.item("bamboo"),false));tasks.addTask(4,new EntityAIFollowParent(this,1));tasks.addTask(5,new EntityAIAttackMelee(this,1.2,true));tasks.addTask(6,new EntityAIWanderAvoidWater(this,.6));tasks.addTask(7,new EntityAIWatchClosest(this,EntityPlayer.class,6));targetTasks.addTask(1,new EntityAIHurtByTarget(this,false));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(MAIN,(byte)0);dataManager.register(HIDDEN,(byte)0);dataManager.register(EATING,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.15);}
    public int getGene(){int main=dataManager.get(MAIN);return main==4||main==5?main==dataManager.get(HIDDEN)?main:0:main;}
    private int randomGene(){int roll=rand.nextInt(16);return roll<5?0:roll<7?1:roll<9?2:roll<11?3:roll==11?4:roll<15?5:6;}
    private void genesChanged(){getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getGene()==5?10:20);setHealth(Math.min(getHealth(),getMaxHealth()));getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(getGene()==1?.07:.15);}
    @Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,IEntityLivingData data){data=super.onInitialSpawn(difficulty,data);dataManager.set(MAIN,(byte)randomGene());dataManager.set(HIDDEN,(byte)randomGene());genesChanged();return data;}
    @Override public EntityAgeable createChild(EntityAgeable other){EntityPanda mate=(EntityPanda)other,child=new EntityPanda(world);child.dataManager.set(MAIN,dataManager.get(rand.nextBoolean()?MAIN:HIDDEN));child.dataManager.set(HIDDEN,mate.dataManager.get(rand.nextBoolean()?MAIN:HIDDEN));if(rand.nextInt(32)==0)child.dataManager.set(MAIN,(byte)randomGene());child.genesChanged();return child;}
    @Override public boolean isBreedingItem(ItemStack stack){return Content.available("bamboo")&&stack.getItem()==Content.item("bamboo");}
    private boolean hasBamboo(){BlockPos origin=new BlockPos(this);int count=0;for(BlockPos pos:BlockPos.getAllInBox(origin.add(-3,0,-3),origin.add(3,2,3)))if(world.isBlockLoaded(pos)&&world.getBlockState(pos).getBlock()==Content.block("bamboo")&&++count>=8)return true;return false;}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(isBreedingItem(held)||held.getItem()==Items.CAKE){if(!world.isRemote){if(isBreedingItem(held)&&!isChild()&&getGrowingAge()==0&&hasBamboo())return super.processInteract(player,hand);if(eatTicks==0){eatTicks=80;dataManager.set(EATING,true);if(!player.capabilities.isCreativeMode)held.shrink(1);}}return true;}return super.processInteract(player,hand);}
    @Override public boolean attackEntityAsMob(Entity target){boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(this),6);if(hit&&getGene()!=6)setAttackTarget(null);return hit;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isAIDisabled())return;if(eatTicks>0){getNavigator().clearPath();if(--eatTicks==0){heal(1);dataManager.set(EATING,false);}}if(getGene()==2&&world.isThundering()){getNavigator().clearPath();setAttackTarget(null);}if(isChild()&&rand.nextInt(getGene()==5?500:6000)==0){world.setEntityState(this,(byte)8);if(rand.nextInt(700)==0&&world.getGameRules().getBoolean("doMobLoot"))dropItem(Items.SLIME_BALL,1);}if(getGene()==3&&onGround&&rand.nextInt(600)==0){jump();motionX+=.2*Math.cos(rotationYaw);motionZ+=.2*Math.sin(rotationYaw);}}
    public boolean isEating(){return dataManager.get(EATING);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("MainGene",dataManager.get(MAIN));tag.setInteger("HiddenGene",dataManager.get(HIDDEN));}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);dataManager.set(MAIN,(byte)MathHelper.clamp(tag.getInteger("MainGene"),0,6));dataManager.set(HIDDEN,(byte)MathHelper.clamp(tag.getInteger("HiddenGene"),0,6));genesChanged();}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.panda.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.panda.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.panda.death");}
}
