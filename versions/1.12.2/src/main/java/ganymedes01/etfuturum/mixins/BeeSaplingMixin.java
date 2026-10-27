package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.BeeNests;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=BlockSapling.class,remap=false)
public class BeeSaplingMixin {
    @Inject(method={"generateTree","func_176476_e"},at=@At("HEAD"),remap=false)
    private void etfu$begin(World world,BlockPos pos,IBlockState state,java.util.Random random,CallbackInfo ci){BeeNests.beginSapling(world,pos,random);}
    @Inject(method={"generateTree","func_176476_e"},at=@At("RETURN"),remap=false)
    private void etfu$end(World world,BlockPos pos,IBlockState state,java.util.Random random,CallbackInfo ci){BeeNests.endSapling();}
}
