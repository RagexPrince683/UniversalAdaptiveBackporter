package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.block.BlockCactus;
import net.minecraft.world.World;
import java.util.Random;
@Mixin(value=BlockCactus.class)
public class MixinCactus {
 @Inject(method={"updateTick"},at=@At("RETURN")) private void uab$flowers(World world,int x,int y,int z,Random random,CallbackInfo info){SpringPlants.cactus(world,x,y,z,random);}
}
