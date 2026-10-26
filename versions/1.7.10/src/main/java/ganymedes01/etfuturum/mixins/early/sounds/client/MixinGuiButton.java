package ganymedes01.etfuturum.mixins.early.sounds.client;

import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.configuration.configs.ConfigSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButton.class)
public class MixinGuiButton {
	@Shadow public int id;

	@Inject(method = "func_146113_a", at = @At("HEAD"), cancellable = true)
	private void etfu$pageSound(SoundHandler handler, CallbackInfo ci) {
		if (ConfigSounds.bookPageTurn && (id == 1 || id == 2) && Minecraft.getMinecraft().currentScreen instanceof GuiScreenBook) {
			handler.playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(Tags.MC_ASSET_VER + ":item.book.page_turn"), 1));
			ci.cancel();
		}
	}
}
