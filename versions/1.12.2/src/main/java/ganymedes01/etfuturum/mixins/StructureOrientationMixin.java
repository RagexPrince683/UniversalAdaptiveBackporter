package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.ModernStructures;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Prepared states own orientation while a clipped template is placed. */
@Mixin(value={BlockChest.class,BlockTorch.class},remap=false)
public abstract class StructureOrientationMixin {
 @Inject(method={"onBlockAdded","func_176213_c"},at=@At("HEAD"),cancellable=true,remap=false)
 private void etfu$prepared(World world,BlockPos pos,IBlockState state,CallbackInfo callback){if(ModernStructures.placing())callback.cancel();}
}
