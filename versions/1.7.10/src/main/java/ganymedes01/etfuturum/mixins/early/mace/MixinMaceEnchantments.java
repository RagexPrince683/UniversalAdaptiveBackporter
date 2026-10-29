package ganymedes01.etfuturum.mixins.early.mace;
import ganymedes01.etfuturum.combat.MaceCombat;
import ganymedes01.etfuturum.items.ItemMace;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class MixinMaceEnchantments {
    @Inject(method="canApply",at=@At("HEAD"),cancellable=true)
    private void etfu$mace(ItemStack stack,CallbackInfoReturnable<Boolean> result){if(stack!=null&&stack.getItem() instanceof ItemMace)result.setReturnValue(MaceCombat.allowed((Enchantment)(Object)this));}
}
