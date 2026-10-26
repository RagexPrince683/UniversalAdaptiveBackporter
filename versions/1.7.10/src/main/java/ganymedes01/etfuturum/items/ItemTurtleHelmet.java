package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

public class ItemTurtleHelmet extends ItemArmor {
	public ItemTurtleHelmet() { super(EnumHelper.addArmorMaterial("EFR_TURTLE", 25, new int[]{2, 6, 5, 2}, 9), 0, 0); setMaxDamage(275); setUnlocalizedName("etfuturum.turtle_helmet"); setTextureName("turtle_helmet"); setCreativeTab(EtFuturum.creativeTabItems); }
	@Override public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) { return "etfuturum:textures/models/armor/turtle_layer_1.png"; }
	@Override public boolean getIsRepairable(ItemStack stack, ItemStack ingredient) { return ingredient.getItem() == ganymedes01.etfuturum.ModItems.TURTLE_SCUTE.get() || super.getIsRepairable(stack, ingredient); }
}
