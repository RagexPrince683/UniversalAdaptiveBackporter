package ganymedes01.etfuturum.client;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class FallingLeafParticle extends ParticleDigging {
    private FallingLeafParticle(World world,BlockPos pos,IBlockState state){super(world,pos.getX()+world.rand.nextDouble(),pos.getY()-.05,pos.getZ()+world.rand.nextDouble(),0,-.02,0,state);setBlockPos(pos);particleMaxAge=80;particleGravity=.02F;particleScale=.5F;motionY=-.02;int color=Minecraft.getMinecraft().getBlockColors().colorMultiplier(state,world,pos,0);particleRed=(color>>16&255)/255F;particleGreen=(color>>8&255)/255F;particleBlue=(color&255)/255F;}
    @Override public void onUpdate(){super.onUpdate();motionX=Math.sin(particleAge*.2)*.01;motionZ=.005;}
    public static void spawn(World world,BlockPos pos,IBlockState state){Minecraft.getMinecraft().effectRenderer.addEffect(new FallingLeafParticle(world,pos,state));}
}
