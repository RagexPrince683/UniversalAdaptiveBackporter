package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityArmadillo extends EntityAnimal {
    private static final DataParameter<Boolean> ROLLED=EntityDataManager.createKey(EntityArmadillo.class,DataSerializers.BOOLEAN);
    private int calmTicks,scuteTicks=12000;
    public EntityArmadillo(World world){super(world);setSize(.7F,.65F);tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(2,new EntityAIMate(this,1));tasks.addTask(3,new EntityAITempt(this,1.1,Items.SPIDER_EYE,false));tasks.addTask(4,new EntityAIFollowParent(this,1.1));tasks.addTask(5,new EntityAIWanderAvoidWater(this,1));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(ROLLED,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.14);}
    public boolean isRolled(){return dataManager.get(ROLLED);}
    @Override public boolean isBreedingItem(ItemStack stack){return !isRolled()&&stack.getItem()==Items.SPIDER_EYE;}
    @Override public EntityAgeable createChild(EntityAgeable mate){return new EntityArmadillo(world);}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(Content.available("brush")&&held.getItem()==Content.item("brush")&&!isChild()&&Content.available("armadillo_scute")){if(!world.isRemote){entityDropItem(new ItemStack(Content.item("armadillo_scute")),.1F);held.damageItem(16,player);}return true;}return super.processInteract(player,hand);}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){if(isRolled()&&!source.isUnblockable())amount=Math.max(0,amount-1)*.5F;boolean hit=super.attackEntityFrom(source,amount);if(hit&&!world.isRemote)calmTicks=100;return hit;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isAIDisabled())return;if(ticksExisted%10==0&&!isInWater())for(EntityLivingBase entity:world.getEntitiesWithinAABB(EntityLivingBase.class,getEntityBoundingBox().grow(7,3,7)))if(entity.isEntityUndead()||entity instanceof EntityPlayer&&!((EntityPlayer)entity).capabilities.disableDamage&&(entity.isSprinting()||entity.isRiding())){calmTicks=100;break;}if(calmTicks>0)--calmTicks;dataManager.set(ROLLED,calmTicks>0&&!isInWater()&&!isBurning());if(isRolled()){getNavigator().clearPath();motionX=motionZ=0;}if(!isChild()&&--scuteTicks<=0){scuteTicks=6000+rand.nextInt(6000);if(Content.available("armadillo_scute")&&world.getGameRules().getBoolean("doMobLoot"))entityDropItem(new ItemStack(Content.item("armadillo_scute")),.1F);}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("ScuteTime",scuteTicks);tag.setInteger("ScaredTicks",calmTicks);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);scuteTicks=tag.hasKey("ScuteTime")?MathHelper.clamp(tag.getInteger("ScuteTime"),0,12000):12000;calmTicks=MathHelper.clamp(tag.getInteger("ScaredTicks"),0,100);}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.armadillo.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.armadillo.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.armadillo.death");}
}
