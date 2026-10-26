package ganymedes01.etfuturum.mixins;

import java.util.List;
import java.util.Map;
import ganymedes01.etfuturum.ConnectionSettings;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge has no pre-handshake metadata event; extend its existing mod-list payload. */
@Mixin(value = FMLHandshakeMessage.ModList.class, remap = false)
public abstract class HandshakeModListMixin {
    @Shadow private Map<String, String> modTags;
    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"), remap = false)
    private void uab$settings(List<ModContainer> mods, CallbackInfo callback) { ConnectionSettings.advertise(modTags); }
}
