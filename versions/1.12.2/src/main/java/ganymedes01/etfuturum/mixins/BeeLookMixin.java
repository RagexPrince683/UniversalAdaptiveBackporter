package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.entities.EntityBee;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityLookHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityLookHelper.class,remap=false)
public class BeeLookMixin {
    @Shadow(aliases="field_75659_a") @Final private EntityLiving entity;
    @Inject(method={"onUpdateLook","func_75649_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private void etfu$angry(CallbackInfo ci){if(entity instanceof EntityBee&&((EntityBee)entity).isAngry())ci.cancel();}
}
