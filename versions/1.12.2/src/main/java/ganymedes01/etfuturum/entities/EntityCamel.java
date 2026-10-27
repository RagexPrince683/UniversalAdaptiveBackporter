package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public class EntityCamel extends EntityRideableMob {
    public EntityCamel(World world){super(world);setSize(1.7F,2.375F);stepHeight=1.5F;tasks.addTask(2,new EntityAITempt(this,1,Item.getItemFromBlock(Blocks.CACTUS),false));}
    @Override public boolean getCanSpawnHere(){BlockPos pos=new BlockPos(this);return MobRegistry.enabled(getClass())&&world.provider.getDimension()==0&&pos.getY()>60&&world.getBlockState(pos.down()).getMaterial().isSolid()&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&!world.containsAnyLiquid(getEntityBoundingBox())&&(!(this instanceof EntityCamelHusk)||!world.isDaytime()&&world.getLight(pos)<=7&&world.getDifficulty()!=EnumDifficulty.PEACEFUL);}
    public boolean isSitting(){return flag(2);}
    @Override public int passengerCount(){return 2;}
    @Override public double getMountedYOffset(){return isSitting()?.9:2.2;}
    @Override public EntityAgeable createChild(EntityAgeable mate){return new EntityCamel(world);}
    @Override public boolean isBreedingItem(ItemStack stack){return stack.getItem()==Item.getItemFromBlock(Blocks.CACTUS);}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){if(player.isSneaking()&&player.getHeldItem(hand).isEmpty()&&!isBeingRidden()){if(!world.isRemote)flag(2,!isSitting());return true;}if(!world.isRemote)flag(2,false);return super.processInteract(player,hand);}
    @Override public void travel(float strafe,float vertical,float forward){if(isSitting()&&!isBeingRidden()){move(MoverType.SELF,0,motionY,0);return;}super.travel(strafe,vertical,forward);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!world.isRemote&&!isAIDisabled()&&!isBeingRidden()&&getNavigator().noPath()&&ticksExisted%200==0&&rand.nextInt(3)==0)flag(2,!isSitting());}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("Sitting",isSitting());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);flag(2,tag.getBoolean("Sitting"));}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.camel.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.camel.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.camel.death");}
}
