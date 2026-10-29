package ganymedes01.etfuturum.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;
public final class CampfireSmoke extends ParticleSmokeNormal {
    private CampfireSmoke(World world,double x,double y,double z,boolean signal){super(world,x,y,z,0,.07,0,2);particleMaxAge=signal?300:120;particleRed=particleGreen=particleBlue=.8F;motionY=.07;}
    @Override public void onUpdate(){super.onUpdate();motionY=.07;}
    public static void spawn(World world,double x,double y,double z,boolean signal){Minecraft.getMinecraft().effectRenderer.addEffect(new CampfireSmoke(world,x,y,z,signal));}
}
