package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.AquaticEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Equal base/Depth Strider drag targets avoid depending on compiler-local variable slots. */
@Mixin(value=EntityLivingBase.class,remap=false)
public class DolphinGraceMixin {
    private float etfu$drag(float original){EntityLivingBase living=(EntityLivingBase)(Object)this;return living instanceof EntityPlayer&&living.isPotionActive(AquaticEffects.DOLPHINS_GRACE)?.96F:original;}
    @Inject(method={"getWaterSlowDown","func_189749_co"},at=@At("HEAD"),cancellable=true,remap=false)
    private void etfu$baseDrag(CallbackInfoReturnable<Float> result){EntityLivingBase living=(EntityLivingBase)(Object)this;if(living instanceof EntityPlayer&&living.isPotionActive(AquaticEffects.DOLPHINS_GRACE))result.setReturnValue(.96F);}
    @ModifyConstant(method={"travel","func_191986_a"},constant=@Constant(floatValue=.54600006F),remap=false)
    private float etfu$depthStriderDrag(float drag){return etfu$drag(drag);}
}
