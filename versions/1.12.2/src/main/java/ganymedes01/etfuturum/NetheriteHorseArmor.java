package ganymedes01.etfuturum;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

/** Keeps native horse inventory, armor calculations and ArmorItem persistence authoritative. */
final class NetheriteHorseArmor extends Item {
    private static final String TEXTURE = "etfuturum:textures/entity/horse/armor/horse_armor_netherite.png";
    private static final HorseArmorType TYPE = EnumHelper.addHorseArmor("UAB_NETHERITE", TEXTURE, 19);
    NetheriteHorseArmor() { setMaxStackSize(1); }
    @Override public HorseArmorType getHorseArmorType(ItemStack stack) { return TYPE; }
    @Override public String getHorseArmorTexture(EntityLiving horse, ItemStack stack) { return TEXTURE; }
}
