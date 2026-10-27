package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import ganymedes01.etfuturum.client.TerrainLoading;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=LoadingScreenRenderer.class,remap=false)
public abstract class LoadingProgressMixin {
 @Shadow(aliases="field_73727_a",remap=false) private String message;
 @Shadow(aliases="field_73726_c",remap=false) private String currentlyDisplayedText;
 @Redirect(method={"setLoadingProgress","func_73718_a"},at=@At(value="INVOKE",target="Lnet/minecraftforge/fml/client/FMLClientHandler;handleLoadingScreen(Lnet/minecraft/client/gui/ScaledResolution;)Z"),remap=false)
 private boolean uab$progress(FMLClientHandler handler,ScaledResolution resolution,int progress) throws java.io.IOException {if(handler.handleLoadingScreen(resolution))return true;if(!Features.enabled("mixins.modernLoadingScreen"))return false;TerrainLoading.render(resolution.getScaledWidth(),resolution.getScaledHeight(),currentlyDisplayedText,message,progress,false);return true;}
}
