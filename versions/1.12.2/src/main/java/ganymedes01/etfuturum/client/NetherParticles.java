package ganymedes01.etfuturum.client;
import java.util.Random;
import ganymedes01.etfuturum.NetherBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSuspend;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
final class NetherParticles {
 private NetherParticles(){}
 static void tick(Minecraft mc){if(mc.gameSettings.particleSetting>=2)return;Random random=mc.world.rand;NetherBiomes.Kind current=NetherBiomes.kind(mc.world.getBiome(mc.player.getPosition()));int count=mc.gameSettings.particleSetting==0?200:current==NetherBiomes.Kind.CRIMSON?100:current==NetherBiomes.Kind.WARPED?75:current==NetherBiomes.Kind.SOUL?1:current==NetherBiomes.Kind.BASALT?200:0;
  for(int i=0;i<count;i++){double x=mc.player.posX+(random.nextDouble()*2-1)*16,y=mc.player.posY+(random.nextDouble()*2-1)*16,z=mc.player.posZ+(random.nextDouble()*2-1)*16;BlockPos pos=new BlockPos(x,y,z);if(y<0||y>255||!mc.world.isBlockLoaded(pos)||!mc.world.isAirBlock(pos))continue;NetherBiomes.Kind kind=mc.gameSettings.particleSetting==0?NetherBiomes.kind(mc.world.getBiome(pos)):current;if(kind==null||mc.gameSettings.particleSetting==0&&(kind==NetherBiomes.Kind.CRIMSON&&!random.nextBoolean()||kind==NetherBiomes.Kind.WARPED&&random.nextFloat()>.375F||kind==NetherBiomes.Kind.SOUL&&random.nextFloat()>.005F))continue;
   boolean crimson=kind==NetherBiomes.Kind.CRIMSON,warped=kind==NetherBiomes.Kind.WARPED;double dx,dz,dy;float size;if(crimson||warped){dx=random.nextGaussian()*.05;dz=random.nextGaussian()*.05;dy=random.nextFloat()*.5*random.nextFloat()*.1*5;size=crimson?.02F:.001F;}else{dx=random.nextFloat()*-1.9*random.nextFloat()*.1;dz=random.nextFloat()*-1.9*random.nextFloat()*.1;dy=random.nextFloat()*-.5*random.nextFloat()*.1*5;size=.001F+random.nextFloat()*.019F;}
   Spore particle=new Spore(mc.world,x,y,z,dx,dy,dz,size);particle.setRBGColorF(crimson?.9F:warped?.1F:.7294118F,crimson?.4F:warped?.1F:.69411767F,crimson?.5F:warped?.3F:.7607843F);mc.effectRenderer.addEffect(particle);
  }
 }
 private static final class Spore extends ParticleSuspend {Spore(World world,double x,double y,double z,double dx,double dy,double dz,float size){super(world,x,y,z,0,0,0);particleMaxAge=(int)(16/(rand.nextFloat()*.8+.2));particleGravity=.01F;motionX=dx;motionY=dy;motionZ=dz;setSize(size,size);}}
}
