package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public final class EntityGlowSquid extends EntitySquid {
    private static final DataParameter<Integer> DARK=EntityDataManager.createKey(EntityGlowSquid.class,DataSerializers.VARINT);
    public EntityGlowSquid(World world){super(world);}
    @Override protected void entityInit(){super.entityInit();dataManager.register(DARK,0);}
    public boolean isLuminous(){return dataManager.get(DARK)==0;}
    @Override public boolean isCreatureType(EnumCreatureType type,boolean count){return type==MobRegistry.GLOW_SQUID;}
    @Override public int getBrightnessForRender(){return isLuminous()?15728880:super.getBrightnessForRender();}
    @Override public float getBrightness(){return isLuminous()?1:super.getBrightness();}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){boolean hit=super.attackEntityFrom(source,amount);if(hit&&!world.isRemote)dataManager.set(DARK,100);return hit;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!world.isRemote&&!isLuminous())dataManager.set(DARK,dataManager.get(DARK)-1);}
    @Override public boolean getCanSpawnHere(){BlockPos pos=new BlockPos(this);return MobRegistry.enabled(getClass())&&world.provider.getDimension()==0&&pos.getY()<30&&isInWater()&&!world.canSeeSky(pos)&&world.getLightFromNeighbors(pos)==0&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty();}
    @Override protected void dropLoot(boolean player,int looting,DamageSource source){if(Content.available("glow_ink_sac")){net.minecraft.item.ItemStack drop=Content.stack("glow_ink_sac");drop.setCount(1+rand.nextInt(3)+rand.nextInt(looting+1));entityDropItem(drop,0);}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("DarkTicksRemaining",dataManager.get(DARK));}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);dataManager.set(DARK,MathHelper.clamp(tag.getInteger("DarkTicksRemaining"),0,100));}
}
