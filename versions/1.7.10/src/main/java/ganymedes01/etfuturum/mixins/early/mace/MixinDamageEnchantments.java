package ganymedes01.etfuturum.mixins.early.mace;
import ganymedes01.etfuturum.ModEnchantments;
import net.minecraft.enchantment.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Legacy anvils and enchantment rolls ask compatibility in only one direction. */
@Mixin(EnchantmentDamage.class)
public class MixinDamageEnchantments {
    @Inject(method="canApplyTogether",at=@At("HEAD"),cancellable=true)
    private void etfu$exclusive(Enchantment other,CallbackInfoReturnable<Boolean> result){if(other==ModEnchantments.density||other==ModEnchantments.breach)result.setReturnValue(false);}
}
