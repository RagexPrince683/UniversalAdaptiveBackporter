package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.Content;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Barrier;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
final class LightMarkerParticle extends Barrier {
    LightMarkerParticle(World world,double x,double y,double z,int level){super(world,x,y,z,Content.item("light"));setParticleTexture(Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(Content.item("light"),1,level),world,null).getParticleTexture());}
}
