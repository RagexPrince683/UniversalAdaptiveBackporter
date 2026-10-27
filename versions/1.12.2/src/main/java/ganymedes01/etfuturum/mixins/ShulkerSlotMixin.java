package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.StorageRules;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.SlotShulkerBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=SlotShulkerBox.class,remap=false)
public abstract class ShulkerSlotMixin {
 @Inject(method={"isItemValid","func_75214_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$ban(ItemStack stack,CallbackInfoReturnable<Boolean> callback){if(StorageRules.prohibited(stack))callback.setReturnValue(false);}
}
