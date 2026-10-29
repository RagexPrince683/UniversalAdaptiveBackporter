package ganymedes01.etfuturum.enchantment;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.items.ItemMace;
import net.minecraft.enchantment.*;
import net.minecraft.item.ItemStack;

public final class MaceEnchantment extends Enchantment {
    private final int kind;
    public MaceEnchantment(int id, String name, int kind) {
        super(id, kind == 0 ? 5 : 2, EnumEnchantmentType.weapon);
        this.kind = kind; setName(name);
        if (kind != 2 && ContentAvailability.enabled("maces")) Enchantment.addToBookList(this);
    }
    @Override public int getMaxLevel() { return kind == 0 ? 5 : kind == 1 ? 4 : 3; }
    @Override public int getMinEnchantability(int level) { return kind == 0 ? 5 + (level - 1) * 8 : 15 + (level - 1) * 9; }
    @Override public int getMaxEnchantability(int level) { return getMinEnchantability(level) + (kind == 0 ? 20 : 50); }
    @Override public boolean canApply(ItemStack stack) { return stack != null && stack.getItem() instanceof ItemMace; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return kind != 2 && ContentAvailability.enabled("maces") && canApply(stack); }
    @Override public boolean isAllowedOnBooks() { return kind != 2 && ContentAvailability.enabled("maces"); }
    @Override public boolean canApplyTogether(Enchantment other) {
        if (!super.canApplyTogether(other)) return false;
        return kind == 2 || !(other instanceof EnchantmentDamage) && !(other instanceof MaceEnchantment && ((MaceEnchantment) other).kind != 2);
    }
}
