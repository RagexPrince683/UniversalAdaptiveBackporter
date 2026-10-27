package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.AquaticEffects;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=EntityLivingBase.class,remap=false)
public abstract class NautilusBreathMixin {
    @Inject(method={"decreaseAirSupply","func_70682_h"},at=@At("HEAD"),cancellable=true,remap=false)
    private void uab$pauseOxygen(int air,CallbackInfoReturnable<Integer> callback){if(((EntityLivingBase)(Object)this).isPotionActive(AquaticEffects.NAUTILUS_BREATH))callback.setReturnValue(air);}
}
