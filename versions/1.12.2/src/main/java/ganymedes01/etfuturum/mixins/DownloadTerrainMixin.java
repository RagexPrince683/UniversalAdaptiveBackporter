package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import ganymedes01.etfuturum.client.TerrainLoading;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=GuiDownloadTerrain.class,remap=false)
public abstract class DownloadTerrainMixin extends GuiScreen {
 @Inject(method={"drawScreen","func_73863_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$terrain(int x,int y,float partial,CallbackInfo ci){if(Features.enabled("mixins.modernLoadingScreen")){TerrainLoading.render(width,height,I18n.format("multiplayer.downloadingTerrain"),"",-1,true);ci.cancel();}}
}
