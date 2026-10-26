package ganymedes01.etfuturum.mixins.early.potionids;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.handshake.FMLHandshakeMessage;
import ganymedes01.etfuturum.potion.ModPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/** Compare potion identities before Forge permits player data/effect packets to be sent. */
@Mixin(value = FMLHandshakeMessage.ModList.class, remap = false)
public abstract class MixinHandshakeModList {
	@Shadow private Map<String, String> modTags;

	@Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"), remap = false)
	private void etfuturum$includePotionIds(List<ModContainer> mods, CallbackInfo ci) {
		modTags.put(ModPotions.IDS.handshakeKey(), ModPotions.IDS.handshake());
		modTags.put("uab:configuration", ganymedes01.etfuturum.configuration.UabConfiguration.get().handshake());
	}
}
