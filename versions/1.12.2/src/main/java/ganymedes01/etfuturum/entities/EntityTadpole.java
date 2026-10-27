package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
public class EntityTadpole extends EntityWaterMob {
    private int age;
    private boolean fromBucket;
    public EntityTadpole(World world){super(world);setSize(.3F,.3F);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6);}
    @Override protected boolean canDespawn(){return !fromBucket&&!hasCustomName();}
    public void readBucket(NBTTagCompound tag){fromBucket=true;age=MathHelper.clamp(tag.getInteger("Age"),0,24000);enablePersistence();if(tag.hasKey("Health"))setHealth(MathHelper.clamp(tag.getFloat("Health"),1,getMaxHealth()));}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||!isInWater())return;if(!isAIDisabled()&&(ticksExisted%30==0||collidedHorizontally)){motionX=(rand.nextDouble()-.5)*.12;motionZ=(rand.nextDouble()-.5)*.12;motionY=(rand.nextDouble()-.5)*.05;}age=Math.min(24000,age+1);if(age>=24000&&MobRegistry.enabled(EntityFrog.class)){EntityFrog frog=new EntityFrog(world);frog.copyLocationAndAnglesFrom(this);frog.chooseVariant();frog.enablePersistence();if(hasCustomName())frog.setCustomNameTag(getCustomNameTag());if(world.spawnEntity(frog))setDead();}}
    @Override public void travel(float strafe,float vertical,float forward){if(!world.isRemote)move(MoverType.SELF,motionX,motionY,motionZ);}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(held.getItem()==Items.SLIME_BALL){if(!world.isRemote){age=Math.min(24000,age+Math.max(20,(24000-age)/10));if(!player.capabilities.isCreativeMode)held.shrink(1);world.setEntityState(this,(byte)7);}return true;}if(held.getItem()==Items.WATER_BUCKET&&isEntityAlive()&&Content.available("tadpole_bucket")){if(!world.isRemote){ItemStack bucket=Content.stack("tadpole_bucket");NBTTagCompound tag=new NBTTagCompound();tag.setInteger("Age",age);tag.setFloat("Health",getHealth());bucket.setTagCompound(tag);if(hasCustomName())bucket.setStackDisplayName(getCustomNameTag());if(!player.capabilities.isCreativeMode&&held.getCount()==1)player.setHeldItem(hand,bucket);else{if(!player.inventory.addItemStackToInventory(bucket)&&!ItemDelivery.drop(player,bucket,.3F))return true;if(!player.capabilities.isCreativeMode)held.shrink(1);}setDead();}return true;}return super.processInteract(player,hand);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Age",age);tag.setBoolean("FromBucket",fromBucket);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);age=MathHelper.clamp(tag.getInteger("Age"),0,24000);fromBucket=tag.getBoolean("FromBucket");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.tadpole.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.tadpole.death");}
}
