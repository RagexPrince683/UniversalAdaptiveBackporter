package ganymedes01.etfuturum.mixins.early.spectator.client;

import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import ganymedes01.etfuturum.core.handlers.client.SpectatorEventHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Shadow public WorldSettings.GameType currentGameType;

    @Inject(method = "setGameType", at = @At("HEAD"))
    private void restoreRendererAfterSpectator(WorldSettings.GameType gameType, CallbackInfo ci) {
        if (currentGameType == SpectatorUtils.SPECTATOR_GAMETYPE
                && gameType != SpectatorUtils.SPECTATOR_GAMETYPE
                && Minecraft.getMinecraft().thePlayer != null) {
            // RenderPlayer reuses its ModelBiped, whose spectator-hidden parts otherwise survive the mode change.
            SpectatorEventHandlerClient.restorePlayerModel(Minecraft.getMinecraft().thePlayer);
        }
    }
}
