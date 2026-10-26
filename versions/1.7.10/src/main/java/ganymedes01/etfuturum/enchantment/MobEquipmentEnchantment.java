package ganymedes01.etfuturum.enchantment;
import net.minecraft.enchantment.*;
import net.minecraft.item.ItemStack;
import ganymedes01.etfuturum.items.*;
/** Native enchanting/anvil integration for the mob equipment introduced by Stage B7. */
public class MobEquipmentEnchantment extends Enchantment {
	private final int max, family;
	public MobEquipmentEnchantment(int id, String name, int levels, int family) { super(id, 2, EnumEnchantmentType.weapon); setName(name); max = levels; this.family = family; }
	@Override public int getMaxLevel() { return max; }
	@Override public int getMinEnchantability(int level) { return 5 + level * 10; }
	@Override public int getMaxEnchantability(int level) { return getMinEnchantability(level) + 30; }
	@Override public boolean canApply(ItemStack stack) { return family == 0 ? stack.getItem() instanceof ItemCrossbow : stack.getItem() instanceof ItemTrident; }
	@Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return canApply(stack); }
	@Override public boolean canApplyTogether(Enchantment other) { if (!super.canApplyTogether(other)) return false; String a = getName(), b = other.getName(); return !(a.endsWith("riptide") && (b.endsWith("loyalty") || b.endsWith("channeling")) || b.endsWith("riptide") && (a.endsWith("loyalty") || a.endsWith("channeling")) || a.endsWith("multishot") && b.endsWith("piercing") || b.endsWith("multishot") && a.endsWith("piercing")); }
}
