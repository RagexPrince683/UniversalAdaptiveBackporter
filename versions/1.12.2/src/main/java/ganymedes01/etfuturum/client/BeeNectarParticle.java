package ganymedes01.etfuturum.client;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
final class BeeNectarParticle extends Particle {
    BeeNectarParticle(World world,double x,double y,double z){super(world,x,y,z,0,0,0);motionX=motionY=motionZ=0;setRBGColorF(0xE7/255F,0xC5/255F,0xB5/255F);setParticleTextureIndex(112);setSize(.01F,.01F);particleGravity=.15F;particleMaxAge=60;}
    @Override public void onUpdate(){super.onUpdate();if(onGround)setExpired();}
}
