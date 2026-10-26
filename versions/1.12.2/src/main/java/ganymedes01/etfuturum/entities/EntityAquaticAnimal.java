package ganymedes01.etfuturum.entities;

import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/** Loaded-water steering preserves the maintained animal behavior independently of land navigation. */
public abstract class EntityAquaticAnimal extends EntityAnimal {
    protected double swimX,swimY,swimZ;
    private int swimTicks;
    protected EntityAquaticAnimal(World world){super(world);}
    protected final boolean waterAt(int x,int y,int z){BlockPos pos=new BlockPos(x,y,z);return world.isBlockLoaded(pos)&&world.getBlockState(pos).getMaterial()==Material.WATER;}
    protected boolean swimDestination(){return false;}
    protected boolean canSwim(){return true;}
    protected double swimAcceleration(){return .02;}
    protected boolean wetSpawn(int low,int high){int x=MathHelper.floor(posX),y=MathHelper.floor(posY),z=MathHelper.floor(posZ);return world.provider.getDimension()==0&&y>=low&&y<=high&&waterAt(x,y,z)&&waterAt(x,y+1,z)&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty();}
    @Override public void travel(float strafe,float vertical,float forward){
        if(!isInWater()||isAIDisabled()){super.travel(strafe,vertical,forward);return;}if(world.isRemote)return;
        if(canSwim()){
        boolean directed=swimDestination();
        if(!directed&&(--swimTicks<=0||collidedHorizontally)){swimTicks=30+rand.nextInt(40);for(int i=0;i<5;i++){double x=posX+(rand.nextDouble()-.5)*12,y=posY+(rand.nextDouble()-.5)*6,z=posZ+(rand.nextDouble()-.5)*12;if(waterAt(MathHelper.floor(x),MathHelper.floor(y),MathHelper.floor(z))){swimX=x;swimY=y;swimZ=z;break;}}}
        double dx=swimX-posX,dy=swimY-posY,dz=swimZ-posZ,length=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(world.isBlockLoaded(new BlockPos(swimX,swimY,swimZ))&&length>.5&&world.rayTraceBlocks(new Vec3d(posX,posY+height/2,posZ),new Vec3d(swimX,swimY,swimZ),false,true,false)==null){double acceleration=swimAcceleration();motionX+=dx/length*acceleration;motionY+=dy/length*acceleration;motionZ+=dz/length*acceleration;rotationYaw=(float)(Math.atan2(dz,dx)*180/Math.PI)-90;renderYawOffset=rotationYaw;}
        else{swimTicks=0;if(collidedHorizontally)motionY+=.04;}
        }
        move(MoverType.SELF,motionX,motionY,motionZ);motionX*=.9;motionY*=.9;motionZ*=.9;
    }
}
