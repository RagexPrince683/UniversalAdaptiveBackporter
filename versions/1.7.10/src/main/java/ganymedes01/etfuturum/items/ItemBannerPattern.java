package ganymedes01.etfuturum.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import java.util.*;

public class ItemBannerPattern extends Item {
	private IIcon[] icons;
	public ItemBannerPattern() {
		setHasSubtypes(true); setMaxDamage(0); setMaxStackSize(1);
		setUnlocalizedName("etfuturum.banner_pattern"); setCreativeTab(EtFuturum.creativeTabItems);
	}
	public static List<EnumBannerPattern> patterns() {
		List<EnumBannerPattern> result = new ArrayList<>();
		for (EnumBannerPattern pattern : EnumBannerPattern.VALUES) if (pattern.hasCraftingStack()) result.add(pattern);
		return result;
	}
	public static EnumBannerPattern pattern(ItemStack stack) {
		if (stack == null || stack.getItem() != ModItems.BANNER_PATTERN.get()) return null;
		List<EnumBannerPattern> patterns = patterns();
		return stack.getItemDamage() < 0 || stack.getItemDamage() >= patterns.size() ? null : patterns.get(stack.getItemDamage());
	}
	@Override public String getUnlocalizedName(ItemStack stack) {
		EnumBannerPattern pattern = pattern(stack);
		return super.getUnlocalizedName() + "." + (pattern == null ? "unknown" : pattern.getPatternName());
	}
	@Override @SideOnly(Side.CLIENT) public void getSubItems(Item item, CreativeTabs tab, List items) {
		for (int i = 0; i < patterns().size(); i++) items.add(new ItemStack(item, 1, i));
	}
	@Override @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister register) {
		List<EnumBannerPattern> patterns = patterns(); icons = new IIcon[patterns.size()];
		for (int i = 0; i < icons.length; i++) icons[i] = register.registerIcon(patterns.get(i).getPatternName() + "_banner_pattern");
	}
	@Override @SideOnly(Side.CLIENT) public IIcon getIconFromDamage(int meta) { return icons[Math.max(0, Math.min(meta, icons.length - 1))]; }
}
