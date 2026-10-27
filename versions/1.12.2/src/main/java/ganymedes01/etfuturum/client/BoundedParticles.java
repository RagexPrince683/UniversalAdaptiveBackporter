package ganymedes01.etfuturum.client;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public final class BoundedParticles {
 private BoundedParticles(){}
 public static void destroy(ParticleManager manager,World world,BlockPos pos,IBlockState state){
  if(state.getBlock().isAir(state,world,pos)||state.getBlock().addDestroyEffects(world,pos,manager))return;
  state=state.getActualState(world,pos);List<AxisAlignedBB> boxes=new ArrayList<>();
  try{state.addCollisionBoxToList(world,pos,new AxisAlignedBB(pos),boxes,null,false);}catch(NullPointerException incompatible){boxes.clear();}
  if(boxes.isEmpty()){AxisAlignedBB box=state.getBoundingBox(world,pos);if(box==null)return;boxes.add(box.offset(pos));}
  for(AxisAlignedBB box:boxes){double minX=Math.max(0,box.minX-pos.getX()),minY=Math.max(0,box.minY-pos.getY()),minZ=Math.max(0,box.minZ-pos.getZ());double x=Math.min(1,box.maxX-pos.getX())-minX,y=Math.min(1,box.maxY-pos.getY())-minY,z=Math.min(1,box.maxZ-pos.getZ())-minZ;if(x<=0||y<=0||z<=0)continue;
   int nx=Math.max(2,MathHelper.ceil(x*4)),ny=Math.max(2,MathHelper.ceil(y*4)),nz=Math.max(2,MathHelper.ceil(z*4));
   for(int a=0;a<nx;a++)for(int b=0;b<ny;b++)for(int c=0;c<nz;c++){double u=(a+.5)/nx,v=(b+.5)/ny,w=(c+.5)/nz;manager.addEffect(new Digging(world,pos.getX()+minX+u*x,pos.getY()+minY+v*y,pos.getZ()+minZ+w*z,u-.5,v-.5,w-.5,state).setBlockPos(pos));}
  }
 }
 private static final class Digging extends ParticleDigging {Digging(World w,double x,double y,double z,double dx,double dy,double dz,IBlockState s){super(w,x,y,z,dx,dy,dz,s);}}
}
