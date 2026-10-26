package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public class EntityBreeze extends EntityMob {
    private int shotCooldown,jumpCooldown;
    public EntityBreeze(World world){super(world);setSize(.6F,1.77F);tasks.addTask(5,new EntityAIWanderAvoidWater(this,.8));targetTasks.addTask(1,new EntityAIHurtByTarget(this,false));targetTasks.addTask(2,new EntityAINearestAttackableTarget<>(this,EntityPlayer.class,true));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.63);getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24);}
    @Override public void fall(float distance,float multiplier){}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){if(source.getImmediateSource() instanceof EntityArrow){EntityArrow arrow=(EntityArrow)source.getImmediateSource();if(!world.isRemote){arrow.motionX*=-1;arrow.motionY*=-1;arrow.motionZ*=-1;arrow.shootingEntity=this;arrow.velocityChanged=true;}return false;}return super.attackEntityFrom(source,amount);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isAIDisabled())return;if(shotCooldown>0)--shotCooldown;if(jumpCooldown>0)--jumpCooldown;EntityLivingBase target=getAttackTarget();if(target==null||!target.isEntityAlive()||getDistanceSq(target)>576)return;getLookHelper().setLookPositionWithEntity(target,30,30);if(shotCooldown==0&&onGround&&canEntityBeSeen(target)){EntityWindCharge charge=new EntityWindCharge(world,this);charge.shoot(target.posX-posX,target.posY+target.height/2-charge.posY,target.posZ-posZ,1.2F,2);if(world.spawnEntity(charge))shotCooldown=60;}if(jumpCooldown==0&&onGround){double angle=rand.nextDouble()*Math.PI*2,x=target.posX+Math.cos(angle)*4,z=target.posZ+Math.sin(angle)*4;if(world.isBlockLoaded(new BlockPos(x,posY,z))){motionX=(x-posX)*.08;motionZ=(z-posZ)*.08;motionY=.8;isAirBorne=true;velocityChanged=true;jumpCooldown=100;}}}
    @Override protected void dropFewItems(boolean player,int looting){if(player&&Content.available("breeze_rod"))entityDropItem(new ItemStack(Content.item("breeze_rod"),1+rand.nextInt(2)+rand.nextInt(looting+1)),0);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("ShotCooldown",shotCooldown);tag.setInteger("JumpCooldown",jumpCooldown);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);shotCooldown=MathHelper.clamp(tag.getInteger("ShotCooldown"),0,60);jumpCooldown=MathHelper.clamp(tag.getInteger("JumpCooldown"),0,100);}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.breeze.idle_air");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.breeze.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.breeze.death");}
}
