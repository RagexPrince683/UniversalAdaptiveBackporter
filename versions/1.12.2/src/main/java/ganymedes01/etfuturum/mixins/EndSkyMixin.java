package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.client.ClientAtmosphere;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=RenderGlobal.class,remap=false)
public abstract class EndSkyMixin {
 @Inject(method={"renderSky","func_174976_a"},at=@At("RETURN"),remap=false)
 private void uab$flash(float partial,int pass,CallbackInfo ci){ClientAtmosphere.renderFlash(partial);}
}
