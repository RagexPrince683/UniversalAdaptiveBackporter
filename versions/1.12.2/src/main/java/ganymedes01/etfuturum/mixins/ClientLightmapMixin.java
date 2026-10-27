package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.client.ClientLighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityRenderer.class,remap=false)
public abstract class ClientLightmapMixin {
 @Shadow(aliases="field_78531_r",remap=false) private Minecraft mc;
 @Shadow(aliases="field_78504_Q",remap=false) private int[] lightmapColors;
 @Shadow(aliases="field_78513_d",remap=false) private DynamicTexture lightmapTexture;
 @Shadow(aliases="field_78536_aa",remap=false) private boolean lightmapUpdateNeeded;
 @Shadow(aliases="field_78514_e",remap=false) private float torchFlickerX;
 @Shadow(aliases="field_82831_U",remap=false) private float bossColorModifier;
 @Shadow(aliases="field_82832_V",remap=false) private float bossColorModifierPrev;
 @Inject(method={"updateLightmap","func_78472_g"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$lightmap(float partial,CallbackInfo ci){if(lightmapUpdateNeeded&&ClientLighting.calculate(mc,lightmapColors,partial,torchFlickerX,bossColorModifier,bossColorModifierPrev)){lightmapTexture.updateDynamicTexture();lightmapUpdateNeeded=false;ci.cancel();}}
}
