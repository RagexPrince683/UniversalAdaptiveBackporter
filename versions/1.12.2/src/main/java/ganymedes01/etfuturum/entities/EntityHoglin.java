package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class EntityHoglin extends EntityAnimal implements IMob {
    private int unsafeTicks;private boolean repelled;
    public EntityHoglin(World world){super(world);setSize(1.4F,1.4F);tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIAttackMelee(this,1,true));tasks.addTask(2,new EntityAIMate(this,1));tasks.addTask(3,new EntityAITempt(this,1,Content.item("crimson_fungus"),false));tasks.addTask(4,new EntityAIFollowParent(this,1));tasks.addTask(5,new EntityAIWanderAvoidWater(this,.7));tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,8));targetTasks.addTask(1,new EntityAIHurtByTarget(this,true));targetTasks.addTask(2,new EntityAINearestAttackableTarget<EntityPlayer>(this,EntityPlayer.class,true){@Override public boolean shouldExecute(){return !isChild()&&!isInLove()&&!repelled&&super.shouldExecute();}});}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.3);getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6);getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(.6);}
    @Override public boolean isCreatureType(EnumCreatureType type,boolean count){return type==EnumCreatureType.MONSTER;}
    @Override public EntityAgeable createChild(EntityAgeable other){return new EntityHoglin(world);}
    @Override public boolean isBreedingItem(ItemStack stack){return Content.available("crimson_fungus")&&stack.getItem()==Content.item("crimson_fungus");}
    static boolean strike(EntityLivingBase hog,Entity target){boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(hog),hog.isChild()?.5F:3+hog.getRNG().nextInt(6));if(hit&&!hog.isChild()){double yaw=hog.rotationYaw*Math.PI/180;target.addVelocity(-Math.sin(yaw)*.5,.25,Math.cos(yaw)*.5);target.velocityChanged=true;}return hit;}
    @Override public boolean attackEntityAsMob(Entity target){return strike(this,target);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote)return;
        if(!isAIDisabled()&&ticksExisted%40==0){repelled=false;BlockPos center=getPosition();search:for(BlockPos pos:BlockPos.getAllInBox(center.add(-3,-1,-3),center.add(3,1,3)))if(world.isBlockLoaded(pos)&&Content.available("warped_fungus")&&world.getBlockState(pos).getBlock()==Content.block("warped_fungus")){repelled=true;setAttackTarget(null);getNavigator().tryMoveToXYZ(posX+(pos.getX()<=posX?4:-4),posY,posZ+(pos.getZ()<=posZ?4:-4),1.3);break search;}}
        if(!isAIDisabled()&&!isChild()&&!repelled&&ticksExisted%100==0){int adults=0;EntityPiglin closest=null;for(EntityPiglin piglin:world.getEntitiesWithinAABB(EntityPiglin.class,getEntityBoundingBox().grow(12,6,12)))if(!piglin.isChild()){adults++;closest=piglin;}if(closest!=null&&adults>world.getEntitiesWithinAABB(EntityHoglin.class,getEntityBoundingBox().grow(12,6,12)).size()){setAttackTarget(null);getNavigator().tryMoveToXYZ(posX+posX-closest.posX,posY,posZ+posZ-closest.posZ,1.3);}}
        if(repelled)setAttackTarget(null);unsafeTicks=world.provider.isNether()?0:unsafeTicks+1;
        if(unsafeTicks>=300&&MobRegistry.enabled(EntityZoglin.class)){EntityZoglin zombie=new EntityZoglin(world);zombie.copyLocationAndAnglesFrom(this);zombie.setGrowingAge(getGrowingAge());EntityPiglin.copyEquipment(this,zombie);if(isNoDespawnRequired())zombie.enablePersistence();if(world.spawnEntity(zombie))setDead();}
    }
    @Override public boolean getCanSpawnHere(){return MobRegistry.enabled(getClass())&&world.provider.isNether()&&world.getDifficulty()!=EnumDifficulty.PEACEFUL&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&!world.containsAnyLiquid(getEntityBoundingBox());}
    @Override protected void dropFewItems(boolean player,int looting){if(!isChild()){dropItem(isBurning()?Items.COOKED_PORKCHOP:Items.PORKCHOP,2+rand.nextInt(3)+rand.nextInt(looting+1));int leather=rand.nextInt(2)+rand.nextInt(looting+1);if(leather>0)dropItem(Items.LEATHER,leather);}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("UnsafeTicks",unsafeTicks);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);unsafeTicks=MathHelper.clamp(tag.getInteger("UnsafeTicks"),0,300);}
}
