package ganymedes01.etfuturum.mixins.early.spring.client;
import java.util.Random;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.block.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BlockLeaves.class)
public class MixinLeaves {
    @Inject(method="randomDisplayTick",at=@At("RETURN"))private void uab$leaves(World world,int x,int y,int z,Random random,CallbackInfo info){if(SpringAnimals.enabled()&&random.nextInt(35)==0&&world.isAirBlock(x,y-1,z))ganymedes01.etfuturum.client.particle.FallingLeafParticle.spawn(world,x,y,z,(Block)(Object)this);}
}
