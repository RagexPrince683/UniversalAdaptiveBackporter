package ganymedes01.etfuturum.mixins.early.nethermobs;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityLivingBase.class)
public class MixinNautilusBreath {
	@Inject(method = "decreaseAirSupply", at = @At("HEAD"), cancellable = true)
	private void etfuturum$pauseBreath(int air, CallbackInfoReturnable<Integer> cir) { EntityLivingBase entity = (EntityLivingBase) (Object) this; if (ganymedes01.etfuturum.potion.ModPotions.nautilusBreath != null && entity.isPotionActive(ganymedes01.etfuturum.potion.ModPotions.nautilusBreath)) cir.setReturnValue(air); }
}
