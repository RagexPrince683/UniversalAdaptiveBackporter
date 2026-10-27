package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.FlowerPots;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=BlockFlowerPot.class,remap=false)
public abstract class FlowerPotMixin {
 @Inject(method={"canBePotted","func_190951_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$plant(ItemStack stack,CallbackInfoReturnable<Boolean> ci){if(FlowerPots.accepts(stack))ci.setReturnValue(true);}
}
