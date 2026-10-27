package ganymedes01.etfuturum.client;
import java.util.List;
import ganymedes01.etfuturum.StorageTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
public final class ShulkerTooltip {
 private ShulkerTooltip(){}
 public static int limit(){return ganymedes01.etfuturum.configuration.UabConfiguration.get().integer("functions","shulkerBoxTooltipLines",5,0,127);}
 public static boolean tiered(ItemStack stack,List<String> tooltip){
  if(!stack.hasTagCompound())return false;NBTTagCompound tag=stack.getTagCompound().getCompoundTag("BlockEntityTag");StorageTier tier=StorageTier.shulker(tag.getByte("Type"));if(tier==StorageTier.WOOD)return false;
  tooltip.add(TextFormatting.GRAY+I18n.format("tile.etfuturum."+tier.id()+"_shulker_box.name")+" ("+tier.size+")");
  if(tag.hasKey("LootTable",8)){tooltip.add("???????");return true;}
  NonNullList<ItemStack> items=NonNullList.withSize(tier.size,ItemStack.EMPTY);ItemStackHelper.loadAllItems(tag,items);int count=0;
  for(ItemStack item:items)if(!item.isEmpty()){if(count<limit())tooltip.add(item.getDisplayName()+" x"+item.getCount());count++;}
  if(count>limit())tooltip.add(TextFormatting.ITALIC+I18n.format("container.shulkerBox.more",count-limit()));return true;
 }
}
