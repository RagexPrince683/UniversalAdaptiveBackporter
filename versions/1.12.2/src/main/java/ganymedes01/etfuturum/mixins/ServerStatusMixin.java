package ganymedes01.etfuturum.mixins;

import com.google.gson.JsonObject;
import ganymedes01.etfuturum.ConnectionSettings;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Server-list compatibility uses the same immutable settings as the login handshake. */
@Mixin(value = FMLNetworkHandler.class, remap = false)
public abstract class ServerStatusMixin {
    @Inject(method = "enhanceStatusQuery", at = @At("RETURN"), remap = false)
    private static void uab$settings(JsonObject response, CallbackInfo callback) { ConnectionSettings.advertiseStatus(response); }
}
