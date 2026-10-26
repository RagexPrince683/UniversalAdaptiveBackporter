package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.world.World;

public final class EntityZoglin extends EntityMob {
    private static final DataParameter<Integer> AGE=EntityDataManager.createKey(EntityZoglin.class,DataSerializers.VARINT);
    public EntityZoglin(World world){super(world);setSize(1.4F,1.4F);isImmuneToFire=true;tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIAttackMelee(this,1,true));tasks.addTask(5,new EntityAIWanderAvoidWater(this,.7));targetTasks.addTask(1,new EntityAIHurtByTarget(this,true));targetTasks.addTask(2,new EntityAINearestAttackableTarget<>(this,EntityLivingBase.class,10,true,false,entity->!(entity instanceof EntityZoglin)&&!(entity instanceof EntityCreeper)));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(AGE,0);}
    public void setGrowingAge(int age){dataManager.set(AGE,Math.min(0,age));setSize(age<0?.7F:1.4F,age<0?.7F:1.4F);}
    @Override public boolean isChild(){return dataManager.get(AGE)<0;}
    @Override public void notifyDataManagerChange(DataParameter<?> key){super.notifyDataManagerChange(key);if(AGE.equals(key))setSize(isChild()?.7F:1.4F,isChild()?.7F:1.4F);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.3);getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6);getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(.6);}
    @Override public EnumCreatureAttribute getCreatureAttribute(){return EnumCreatureAttribute.UNDEAD;}
    @Override public boolean attackEntityAsMob(Entity target){return EntityHoglin.strike(this,target);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!world.isRemote&&isChild())setGrowingAge(dataManager.get(AGE)+1);}
    @Override protected void dropFewItems(boolean player,int looting){int count=rand.nextInt(2)+rand.nextInt(looting+1);if(count>0)dropItem(Items.ROTTEN_FLESH,count);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Age",dataManager.get(AGE));}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setGrowingAge(tag.getInteger("Age"));}
}
