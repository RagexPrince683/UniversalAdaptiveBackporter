package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.items.ItemCrossbow;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ItemRenderer.class)
public class MixinCrossbowItemRenderer {
	// Crossbows charge without the vanilla bow's stretching transform; item use and firing stay unchanged.
	@Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItemUseAction()Lnet/minecraft/item/EnumAction;"))
	private EnumAction etfu$crossbowAction(ItemStack stack) { return stack.getItem() instanceof ItemCrossbow ? EnumAction.none : stack.getItemUseAction(); }
}
