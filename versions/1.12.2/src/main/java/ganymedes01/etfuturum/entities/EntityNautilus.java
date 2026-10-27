package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public class EntityNautilus extends EntityRideableMob {
    private static final DataParameter<Boolean> TAMED=EntityDataManager.createKey(EntityNautilus.class,DataSerializers.BOOLEAN);
    private int dryTicks,attackCooldown;
    private EntityLivingBase hunting;
    public EntityNautilus(World world){super(world);setSize(1.2F,1.1F);}
    @Override protected void entityInit(){super.entityInit();dataManager.register(TAMED,false);}
    public boolean isTamed(){return dataManager.get(TAMED);}
    protected void setTamed(boolean value){dataManager.set(TAMED,value);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15);}
    @Override public int passengerCount(){return 1;}
    @Override public double getMountedYOffset(){return 1.05;}
    @Override protected boolean aquaticMount(){return true;}
    @Override public boolean isCreatureType(EnumCreatureType type,boolean count){return type==MobRegistry.NAUTILUS;}
    @Override public boolean getCanSpawnHere(){BlockPos pos=new BlockPos(this);return MobRegistry.enabled(getClass())&&world.provider.getDimension()==0&&pos.getY()>20&&pos.getY()<62&&world.isBlockLoaded(pos.up())&&world.getBlockState(pos).getMaterial()==Material.WATER&&world.getBlockState(pos.up()).getMaterial()==Material.WATER&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&world.checkNoEntityCollision(getEntityBoundingBox())&&(!(this instanceof EntityZombieNautilus)||world.getDifficulty()!=EnumDifficulty.PEACEFUL&&!world.isDaytime());}
    @Override protected boolean canDespawn(){return !isTamed()&&!hasCustomName();}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override protected boolean mayRide(EntityPlayer player){return isTamed()&&super.mayRide(player);}
    @Override public boolean isBreedingItem(ItemStack stack){return isTamed()&&stack.getItem()==Items.FISH&&stack.getMetadata()==3;}
    @Override public EntityAgeable createChild(EntityAgeable mate){EntityNautilus child=new EntityNautilus(world);child.setTamed(true);return child;}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(!isTamed()&&held.getItem()==Items.FISH&&held.getMetadata()==3){if(!world.isRemote){consume(player,held);if(rand.nextInt(3)==0&&!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this,player)){setTamed(true);enablePersistence();world.setEntityState(this,(byte)7);}}return true;}if(isTamed()&&Content.available("nautilus_armor")&&held.getItem()==Content.item("nautilus_armor")){if(!world.isRemote){if(!getBodyEquipment().isEmpty()&&!ItemDelivery.drop(this,getBodyEquipment(),0))return true;ItemStack armor=held.copy();armor.setCount(1);bodyEquipment(armor);consume(player,held);}return true;}if(isTamed()&&player.isSneaking()&&held.isEmpty()&&!getBodyEquipment().isEmpty()){if(!world.isRemote&&ItemDelivery.drop(this,getBodyEquipment(),0))bodyEquipment(ItemStack.EMPTY);return true;}return super.processInteract(player,hand);}
    @Override public int getTotalArmorValue(){ItemStack armor=getBodyEquipment();return armor.isEmpty()?0:new int[]{3,5,7,11,15}[MathHelper.clamp(armor.getMetadata(),0,4)];}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){boolean hit=super.attackEntityFrom(source,amount);if(hit&&!world.isRemote&&source.getTrueSource() instanceof EntityLivingBase)hunting=(EntityLivingBase)source.getTrueSource();return hit;}
    @Override public void travel(float strafe,float vertical,float forward){if(!world.isRemote&&!isAIDisabled()&&!isBeingRidden()&&isInWater()&&!isChild()){if(attackCooldown>0)--attackCooldown;if(hunting==null&&ticksExisted%80==0)for(EntityPufferfish fish:world.getEntitiesWithinAABB(EntityPufferfish.class,getEntityBoundingBox().grow(12,6,12))){hunting=fish;break;}if(hunting!=null&&(!hunting.isEntityAlive()||getDistanceSq(hunting)>256||!hunting.isInWater()))hunting=null;if(hunting!=null&&canEntityBeSeen(hunting)){double dx=hunting.posX-posX,dy=hunting.posY-posY,dz=hunting.posZ-posZ,length=Math.max(1,Math.sqrt(dx*dx+dy*dy+dz*dz));motionX+=dx/length*.03;motionY+=dy/length*.03;motionZ+=dz/length*.03;if(length<2&&attackCooldown==0){hunting.attackEntityFrom(DamageSource.causeMobDamage(this),4);attackCooldown=40;}move(MoverType.SELF,motionX,motionY,motionZ);motionX*=.9;motionY*=.9;motionZ*=.9;return;}}super.travel(strafe,vertical,forward);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote)return;dryTicks=isWet()?0:Math.min(301,dryTicks+1);if(dryTicks>300&&ticksExisted%20==0)attackEntityFrom(DamageSource.DROWN,1);Entity passenger=getControllingPassenger();if(passenger instanceof EntityPlayer&&ticksExisted%20==0){EntityPlayer rider=(EntityPlayer)passenger;PotionEffect effect=rider.getActivePotionEffect(AquaticEffects.NAUTILUS_BREATH);if(effect==null||effect.getAmplifier()==0&&effect.getDuration()<60)rider.addPotionEffect(new PotionEffect(AquaticEffects.NAUTILUS_BREATH,80));}}
    @Override protected void dropFewItems(boolean player,int looting){super.dropFewItems(player,looting);if(this instanceof EntityZombieNautilus){int count=rand.nextInt(4)+rand.nextInt(looting+1);if(count>0)dropItem(Items.ROTTEN_FLESH,count);}else if(player&&Content.available("nautilus_shell")&&rand.nextFloat()<.05F+.01F*looting)entityDropItem(Content.stack("nautilus_shell"),0);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("Tame",isTamed());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setTamed(tag.getBoolean("Tame"));}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.nautilus.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.nautilus.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.nautilus.death");}
}
