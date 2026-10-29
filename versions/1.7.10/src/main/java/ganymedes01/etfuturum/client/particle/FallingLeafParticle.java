package ganymedes01.etfuturum.client.particle;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.world.World;
public final class FallingLeafParticle extends EntityDiggingFX {
    private FallingLeafParticle(World world,int x,int y,int z,Block block){super(world,x+world.rand.nextDouble(),y-.05,z+world.rand.nextDouble(),0,-.02,0,block,world.getBlockMetadata(x,y,z));particleMaxAge=80;particleGravity=.02F;particleScale=.5F;motionY=-.02;applyColourMultiplier(x,y,z);}
    @Override public void onUpdate(){super.onUpdate();motionX=Math.sin(particleAge*.2)*.01;motionZ=.005;}
    public static void spawn(World world,int x,int y,int z,Block block){Minecraft.getMinecraft().effectRenderer.addEffect(new FallingLeafParticle(world,x,y,z,block));}
}
