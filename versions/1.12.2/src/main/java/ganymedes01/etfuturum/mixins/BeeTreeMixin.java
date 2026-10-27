package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.BeeNests;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value={WorldGenTrees.class,WorldGenBirchTree.class,WorldGenBigTree.class},remap=false)
public class BeeTreeMixin {
    @Inject(method={"generate","func_180709_b"},at=@At("RETURN"),remap=false)
    private void etfu$nest(World world,java.util.Random random,BlockPos pos,CallbackInfoReturnable<Boolean> result){if(result.getReturnValueZ())BeeNests.afterTree(world,random,pos);}
}
