package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public final class EntityDolphin extends EntityAquaticAnimal {
    private int moisture=2400,attackTicks,playTicks,treasureTicks;
    private EntityPlayer following;private EntityItem toy;private BlockPos treasure;
    public EntityDolphin(World world){super(world);setSize(.9F,.6F);setAir(4800);targetTasks.addTask(1,new EntityAIHurtByTarget(this,true));}
    @Override public boolean isCreatureType(EnumCreatureType type,boolean count){return type==MobRegistry.DOLPHINS;}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.2);}
    @Override public EntityAgeable createChild(EntityAgeable other){return null;}
    @Override protected boolean canDespawn(){return !hasCustomName();}
    @Override public boolean isBreedingItem(ItemStack stack){return false;}
    @Override public boolean getCanSpawnHere(){return MobRegistry.enabled(getClass())&&wetSpawn(45,63);}
    @Override protected double swimAcceleration(){return .035;}
    @Override public int getMaxSpawnedInChunk(){return 5;}
    @Override public void fall(float distance,float multiplier){ }
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(!held.isEmpty()&&held.getItem()==Items.FISH&&held.getMetadata()<2){if(!world.isRemote){if(!player.capabilities.isCreativeMode)held.shrink(1);treasureTicks=1200;treasure=OceanGuidance.nearest(world,new BlockPos(this));}return true;}return super.processInteract(player,hand);}
    @Override protected boolean swimDestination(){
        EntityLivingBase target=getAttackTarget();if(target!=null&&target.isEntityAlive()&&target.isInWater()&&getDistanceSq(target)<256){swimX=target.posX;swimY=target.posY;swimZ=target.posZ;if(attackTicks==0&&getDistanceSq(target)<3&&canEntityBeSeen(target)){attackTicks=20;target.attackEntityFrom(DamageSource.causeMobDamage(this),3);}return true;}setAttackTarget(null);
        if(getAir()<200){swimX=posX;swimY=posY+2;swimZ=posZ;return true;}
        if(treasureTicks>0&&treasure!=null){double dx=treasure.getX()-posX,dz=treasure.getZ()-posZ,length=Math.sqrt(dx*dx+dz*dz);if(length<5){treasureTicks=0;return false;}swimX=posX+dx/length*6;swimY=Math.min(61,posY);swimZ=posZ+dz/length*6;return true;}
        if(toy!=null&&toy.isEntityAlive()&&getDistanceSq(toy)<64&&toy.isInWater()){swimX=toy.posX;swimY=toy.posY;swimZ=toy.posZ;if(getDistanceSq(toy)<2&&++playTicks>=20){Vec3d look=getLookVec();toy.addVelocity(look.x*.3,.2,look.z*.3);toy.setPickupDelay(20);playTicks=0;toy=null;}return true;}toy=null;
        if(following!=null&&following.isEntityAlive()&&following.isInWater()&&getDistanceSq(following)<256&&!following.capabilities.disableDamage){if(getDistanceSq(following)<6)return false;swimX=following.posX;swimY=following.posY;swimZ=following.posZ;return true;}return false;
    }
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();if(world.isRemote)return;
        if(!isInWater())setAir(4800);if(isWet())moisture=2400;else if(--moisture<=0&&ticksExisted%20==0)attackEntityFrom(DamageSource.DROWN,1);
        if(!isInWater()&&onGround&&ticksExisted%20==0){motionX+=(rand.nextDouble()-.5)*.4;motionZ+=(rand.nextDouble()-.5)*.4;motionY=.5;onGround=false;}
        if(attackTicks>0)--attackTicks;if(treasureTicks>0)--treasureTicks;
        if(ticksExisted%40==0){following=world.getNearestAttackablePlayer(this,16,16);if(following!=null&&following.isInWater()&&getDistanceSq(following)<64)following.addPotionEffect(new PotionEffect(AquaticEffects.DOLPHINS_GRACE,100));if(toy==null)for(EntityItem item:world.getEntitiesWithinAABB(EntityItem.class,getEntityBoundingBox().grow(6,3,6)))if(item.isInWater()&&!item.cannotPickup()){toy=item;break;}}
    }
    @Override protected void dropLoot(boolean player,int looting,DamageSource source){if(rand.nextInt(3)==0)entityDropItem(new ItemStack(isBurning()?Items.COOKED_FISH:Items.FISH),0);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Moistness",moisture);tag.setInteger("TreasureTicks",treasureTicks);tag.setBoolean("TreasureKnown",treasure!=null);if(treasure!=null){tag.setInteger("TreasureX",treasure.getX());tag.setInteger("TreasureY",treasure.getY());tag.setInteger("TreasureZ",treasure.getZ());}}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);moisture=tag.hasKey("Moistness")?MathHelper.clamp(tag.getInteger("Moistness"),0,2400):2400;treasureTicks=MathHelper.clamp(tag.getInteger("TreasureTicks"),0,1200);treasure=tag.getBoolean("TreasureKnown")?new BlockPos(tag.getInteger("TreasureX"),tag.getInteger("TreasureY"),tag.getInteger("TreasureZ")):null;}
}
