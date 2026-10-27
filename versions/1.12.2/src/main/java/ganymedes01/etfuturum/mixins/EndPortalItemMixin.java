package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=BlockEndPortal.class,remap=false)
public abstract class EndPortalItemMixin {
 @Inject(method={"getItem","func_185473_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$pick(World world,BlockPos pos,IBlockState state,CallbackInfoReturnable<ItemStack> ci){if(Features.enabled("mixins.endPortalFix"))ci.setReturnValue(new ItemStack(Blocks.END_PORTAL));}
}
