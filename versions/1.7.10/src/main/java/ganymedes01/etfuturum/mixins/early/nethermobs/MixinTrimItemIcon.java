package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.client.renderer.ArmorTrimRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinTrimItemIcon {
	@Shadow public float zLevel;
	// Forge adds this overload; its name is not present in vanilla's obfuscation mappings.
	@Inject(method = "renderItemIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/item/ItemStack;IIZ)V", at = @At("RETURN"), remap = false)
	private void etfu$trimIcon(FontRenderer font, TextureManager textures, ItemStack stack, int x, int y, boolean effect, CallbackInfo ci) { ArmorTrimRenderer.renderIcon(stack, x, y, zLevel); }
}
