package ganymedes01.etfuturum.mixins.early.configuration;

import ganymedes01.etfuturum.ModEnchantments;
import net.minecraft.enchantment.*;
import net.minecraft.item.ItemStack;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Retain saved enchantment IDs without offering disabled enchantments in new generated equipment/books. */
@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentAvailability {
	@Inject(method = "mapEnchantmentData", at = @At("RETURN"))
	private static void uab$selection(int level, ItemStack stack, CallbackInfoReturnable<Map<Integer, EnchantmentData>> callback) {
		Map<Integer, EnchantmentData> result = callback.getReturnValue();
		if (result == null) return;
		result.values().removeIf(data -> !enabled(data.enchantmentobj));
	}
	private static boolean enabled(Enchantment enchantment) {
		if (enchantment == ModEnchantments.windBurst) return false; // Ominous-vault treasure, never table rolls (including books).
		return ModEnchantments.available(enchantment);
	}
}
