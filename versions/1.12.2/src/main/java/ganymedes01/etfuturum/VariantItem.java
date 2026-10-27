package ganymedes01.etfuturum;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
/** Keeps the maintained single registry identity and metadata variants. */
public class VariantItem extends Item {
    public final String[] variants;
    VariantItem(String...variants){this.variants=variants.clone();setHasSubtypes(true);setMaxStackSize(1);}
    public boolean available(int meta){String own=getRegistryName().getPath();return Content.available(own)&&(!own.equals("nautilus_armor")||meta!=0||Content.available("copper_ingot"))&&(!own.equals("nautilus_armor")||meta!=4||Content.available("netherite_ingot"));}
    @Override public String getTranslationKey(ItemStack stack){return "item.etfuturum."+variants[MathHelper.clamp(stack.getMetadata(),0,variants.length-1)];}
    @Override public void getSubItems(CreativeTabs tab,NonNullList<ItemStack> items){if(isInCreativeTab(tab))for(int meta=0;meta<variants.length;meta++)if(available(meta))items.add(new ItemStack(this,1,meta));}
    public static String model(String name){for(Item item:Content.ITEMS.values())if(item instanceof VariantItem){VariantItem variants=(VariantItem)item;if(item.getRegistryName().getPath().equals(name))return variants.variants[0];for(String variant:variants.variants)if(variant.equals(name))return variant;}return null;}
    public static ItemStack stack(String variant){for(Item item:Content.ITEMS.values())if(item instanceof VariantItem){VariantItem type=(VariantItem)item;for(int i=0;i<type.variants.length;i++)if(type.variants[i].equals(variant))return type.available(i)?new ItemStack(item,1,i):ItemStack.EMPTY;}return ItemStack.EMPTY;}
}
