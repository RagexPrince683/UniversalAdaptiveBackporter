package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.StorageRules;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=TileEntityShulkerBox.class,remap=false)
public abstract class ShulkerStorageMixin {
 @Inject(method={"canInsertItem","func_180462_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$ban(int slot,ItemStack stack,EnumFacing side,CallbackInfoReturnable<Boolean> callback){if(StorageRules.prohibited(stack))callback.setReturnValue(false);}
}
