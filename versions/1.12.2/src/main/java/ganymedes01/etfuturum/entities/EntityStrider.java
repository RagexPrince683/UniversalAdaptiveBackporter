package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public final class EntityStrider extends EntityAnimal {
    private static final DataParameter<Byte> FLAGS=EntityDataManager.createKey(EntityStrider.class,DataSerializers.BYTE);
    private int boostTicks;
    public EntityStrider(World world){super(world);setSize(.9F,1.7F);isImmuneToFire=true;setPathPriority(PathNodeType.LAVA,0);setPathPriority(PathNodeType.WATER,-1);tasks.addTask(1,new EntityAIMate(this,1));tasks.addTask(2,new EntityAITempt(this,1.2,Content.item("warped_fungus"),false));tasks.addTask(3,new EntityAIFollowParent(this,1));tasks.addTask(4,new EntityAIWanderAvoidWater(this,.7));tasks.addTask(5,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(6,new EntityAILookIdle(this));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(FLAGS,(byte)0);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.175);}
    @Override public boolean isCreatureType(EnumCreatureType type,boolean count){return type==MobRegistry.STRIDERS;}
    public boolean isSaddled(){return (dataManager.get(FLAGS)&1)!=0;}
    public boolean isCold(){return (dataManager.get(FLAGS)&2)!=0;}
    public boolean boost(){if(boostTicks>0)return false;boostTicks=140;return true;}
    @Override public EntityAgeable createChild(EntityAgeable other){return new EntityStrider(world);}
    @Override public boolean isBreedingItem(ItemStack stack){return Content.available("warped_fungus")&&stack.getItem()==Content.item("warped_fungus");}
    @Override public Entity getControllingPassenger(){return getPassengers().isEmpty()?null:getPassengers().get(0);}
    @Override public boolean canBeSteered(){Entity passenger=getControllingPassenger();if(!(passenger instanceof EntityPlayer))return false;EntityPlayer player=(EntityPlayer)passenger;return player.getHeldItemMainhand().getItem()==Content.item("warped_fungus_on_a_stick")||player.getHeldItemOffhand().getItem()==Content.item("warped_fungus_on_a_stick");}
    @Override public double getMountedYOffset(){return height*.75;}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(isBreedingItem(held))return super.processInteract(player,hand);if(!isChild()&&!isSaddled()&&held.getItem()==Items.SADDLE){if(!world.isRemote){dataManager.set(FLAGS,(byte)(dataManager.get(FLAGS)|1));if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}if(isSaddled()&&!isBeingRidden()&&!player.isSneaking()){if(!world.isRemote)player.startRiding(this);return true;}return super.processInteract(player,hand);}
    private boolean lava(){BlockPos below=new BlockPos(posX,getEntityBoundingBox().minY-.1,posZ);return world.isBlockLoaded(below)&&world.getBlockState(below).getMaterial()==Material.LAVA;}
    @Override public void travel(float strafe,float vertical,float forward){
        boolean lava=lava();Entity passenger=getControllingPassenger();
        if(passenger instanceof EntityPlayer&&isSaddled()){
            EntityPlayer player=(EntityPlayer)passenger;if(Float.isFinite(player.rotationYaw))rotationYaw=player.rotationYaw;renderYawOffset=rotationYaw;rotationYawHead=rotationYaw;if(Float.isFinite(player.rotationPitch))rotationPitch=MathHelper.clamp(player.rotationPitch,-90,90)*.5F;
            strafe=canBeSteered()&&Float.isFinite(player.moveStrafing)?MathHelper.clamp(player.moveStrafing,-1,1)*.5F:0;forward=canBeSteered()&&Float.isFinite(player.moveForward)?MathHelper.clamp(player.moveForward,-1,1):0;
            if(!world.isRemote){setAIMoveSpeed((lava?.35F:.175F)*(boostTicks>0?1.5F:1));super.travel(strafe,0,forward);}else limbSwingAmount=0;
        }else super.travel(strafe,vertical,forward);
        if(lava){fallDistance=0;BlockPos inside=new BlockPos(posX,getEntityBoundingBox().minY,posZ);boolean submerged=world.getBlockState(inside).getMaterial()==Material.LAVA;motionY=submerged?.08:Math.max(0,motionY);onGround=!submerged;}
    }
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote)return;if((isWet()||isInWater())&&ticksExisted%10==0)attackEntityFrom(DamageSource.DROWN,1);if(boostTicks>0)boostTicks--;if(ticksExisted%5==0)dataManager.set(FLAGS,(byte)((dataManager.get(FLAGS)&1)|(lava()?0:2)));}
    @Override public void fall(float distance,float damage){ }
    @Override public boolean getCanSpawnHere(){return MobRegistry.enabled(getClass())&&world.provider.isNether()&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&lava();}
    @Override protected void dropFewItems(boolean player,int looting){if(!isChild())dropItem(Items.STRING,2+rand.nextInt(4)+rand.nextInt(looting+1));if(isSaddled())dropItem(Items.SADDLE,1);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("Saddle",isSaddled());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);dataManager.set(FLAGS,(byte)(tag.getBoolean("Saddle")?1:0));}
}
