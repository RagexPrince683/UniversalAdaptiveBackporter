package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class EntityFallingDripstone extends EntityFallingBlock {
    private static final DataParameter<Integer> COUNT=EntityDataManager.createKey(EntityFallingDripstone.class,DataSerializers.VARINT);private float hurtAmount=4;private int hurtMaximum=40;private boolean hurt=true,sourceRemoved;private int renderSegment;
    public EntityFallingDripstone(World world){super(world);}
    public EntityFallingDripstone(World world,double x,double y,double z,IBlockState state,int count){super(world,x,y,z,state);dataManager.set(COUNT,MathHelper.clamp(count,1,256));sourceRemoved=true;}
    @Override protected void entityInit(){super.entityInit();dataManager.register(COUNT,1);}
    public int getCount(){return dataManager.get(COUNT);}
    public void renderSegment(int segment){renderSegment=segment;}
    @Override public IBlockState getBlock(){IBlockState state=Content.block("pointed_dripstone").getDefaultState();return state.withProperty(PointedDripstone.THICKNESS,renderSegment==0?PointedDripstone.Thickness.TIP:renderSegment==1?PointedDripstone.Thickness.FRUSTUM:renderSegment==getCount()-1?PointedDripstone.Thickness.BASE:PointedDripstone.Thickness.MIDDLE);}
    @Override public void onUpdate(){if(!world.isRemote&&fallTime==0&&!sourceRemoved){net.minecraft.util.math.BlockPos origin=new net.minecraft.util.math.BlockPos(this);if(world.getBlockState(origin).getBlock()!=Content.block("pointed_dripstone")){setDead();return;}world.setBlockToAir(origin);sourceRemoved=true;}prevPosX=posX;prevPosY=posY;prevPosZ=posZ;fallTime++;motionY-=.04;move(MoverType.SELF,motionX,motionY,motionZ);motionX*=.98;motionY*=.98;motionZ*=.98;if(!world.isRemote&&(onGround||fallTime>600||fallTime>100&&(posY<1||posY>world.getHeight()))){setDead();if(shouldDropItem&&world.getGameRules().getBoolean("doTileDrops"))for(int left=getCount();left>0;left-=64)entityDropItem(new ItemStack(Content.block("pointed_dripstone"),Math.min(64,left)),0);}}
    @Override public void fall(float distance,float multiplier){if(world.isRemote||!hurt)return;int blocks=MathHelper.ceil(distance-1);if(blocks>0)for(Entity entity:world.getEntitiesWithinAABBExcludingEntity(this,getEntityBoundingBox()))entity.attackEntityFrom(PointedDripstone.STALACTITE,Math.min(blocks*hurtAmount*getCount(),hurtMaximum));}
    @Override protected void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Count",getCount());tag.setBoolean("SourceRemoved",sourceRemoved);tag.setBoolean("HurtEntities",hurt);tag.setFloat("FallHurtAmount",hurtAmount);tag.setInteger("FallHurtMax",hurtMaximum);}
    @Override protected void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);sourceRemoved=tag.getBoolean("SourceRemoved");dataManager.set(COUNT,MathHelper.clamp(tag.hasKey("Count")?tag.getInteger("Count"):1,1,256));hurt=!tag.hasKey("HurtEntities")||tag.getBoolean("HurtEntities");hurtAmount=tag.hasKey("FallHurtAmount")?tag.getFloat("FallHurtAmount"):4;if(!Float.isFinite(hurtAmount)||hurtAmount<0)hurtAmount=4;hurtMaximum=tag.hasKey("FallHurtMax")?Math.max(0,tag.getInteger("FallHurtMax")):40;}
}
