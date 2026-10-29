package ganymedes01.etfuturum.enchantment;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.items.ItemSpear;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

public final class Lunge extends Enchantment {
    public Lunge(int id) {
        super(id, 5, EnumEnchantmentType.weapon);
        setName("lunge");
        if (ContentAvailability.enabled("spears")) Enchantment.addToBookList(this);
    }
    @Override public int getMaxLevel() { return 3; }
    @Override public int getMinEnchantability(int level) { return 5 + (level - 1) * 8; }
    @Override public int getMaxEnchantability(int level) { return getMinEnchantability(level) + 20; }
    @Override public boolean canApply(ItemStack stack) { return stack != null && stack.getItem() instanceof ItemSpear; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return ContentAvailability.enabled("spears") && canApply(stack); }
    @Override public boolean isAllowedOnBooks() { return ContentAvailability.enabled("spears"); }
}
