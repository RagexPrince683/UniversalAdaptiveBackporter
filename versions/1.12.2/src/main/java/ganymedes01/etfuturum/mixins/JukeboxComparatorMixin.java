package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.ModernRecord;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=BlockJukebox.class,remap=false)
public abstract class JukeboxComparatorMixin {
 @Inject(method={"getComparatorInputOverride","func_180641_l"},at=@At("HEAD"),cancellable=true,remap=false)
 private void etfu$record(IBlockState state,World world,BlockPos pos,CallbackInfoReturnable<Integer> callback){TileEntity tile=world.getTileEntity(pos);if(tile instanceof BlockJukebox.TileEntityJukebox){ItemStack record=((BlockJukebox.TileEntityJukebox)tile).getRecord();if(record.getItem() instanceof ModernRecord)callback.setReturnValue(((ModernRecord)record.getItem()).comparator);}}
}
