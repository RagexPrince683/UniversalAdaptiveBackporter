package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.ModernSounds;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
public class EntityRavager extends EntityIllager {
    private int stunTicks,roarTicks;
    public EntityRavager(World world){super(world);setSize(1.95F,2.2F);stepHeight=1;tasks.addTask(2,new EntityAIAttackMelee(this,1,true));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100);getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12);getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(.75);}
    @Override public boolean attackEntityAsMob(Entity target){if(stunTicks>0||roarTicks>0)return false;if(target instanceof EntityPlayer&&((EntityPlayer)target).isActiveItemStackBlocking()&&rand.nextBoolean()){stunTicks=40;return false;}boolean hit=super.attackEntityAsMob(target);if(hit){target.addVelocity(-MathHelper.sin(rotationYaw*.0174533F),.5,MathHelper.cos(rotationYaw*.0174533F));target.velocityChanged=true;}return hit;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isAIDisabled())return;if(stunTicks>0){getNavigator().clearPath();if(--stunTicks==0)roarTicks=20;}if(roarTicks>0&&--roarTicks==10){playSound(ModernSounds.get("entity.ravager.roar"),1,1);for(EntityLivingBase target:world.getEntitiesWithinAABB(EntityLivingBase.class,getEntityBoundingBox().grow(4,3,4)))if(target!=this&&!(target instanceof EntityIllager)&&!(target instanceof AbstractIllager)){target.attackEntityFrom(DamageSource.causeMobDamage(this),6);double dx=target.posX-posX,dz=target.posZ-posZ,length=Math.max(.1,Math.sqrt(dx*dx+dz*dz));target.addVelocity(dx/length,.4,dz/length);target.velocityChanged=true;}}}
    @Override protected void dropFewItems(boolean player,int looting){dropItem(Items.SADDLE,1);}
    @Override public double getMountedYOffset(){return 2.1;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("StunTick",stunTicks);tag.setInteger("RoarTick",roarTicks);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);stunTicks=MathHelper.clamp(tag.getInteger("StunTick"),0,40);roarTicks=MathHelper.clamp(tag.getInteger("RoarTick"),0,20);}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.ravager.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.ravager.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.ravager.death");}
}
