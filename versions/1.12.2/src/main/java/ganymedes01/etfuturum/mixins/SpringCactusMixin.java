package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.block.BlockCactus;
import net.minecraft.world.World;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
@Mixin(value=BlockCactus.class,remap=false)
public class SpringCactusMixin {
 @Inject(method={"updateTick","func_180650_b"},at=@At("RETURN"),remap=false) private void uab$flowers(World world,BlockPos pos,IBlockState state,Random random,CallbackInfo info){SpringPlants.cactus(world,pos,random);}
}
