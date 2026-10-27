package ganymedes01.etfuturum;
import net.minecraft.block.Block;
import net.minecraft.item.*;
/** The maintained portable flowers use the native pot tile, drops and save format. */
public final class FlowerPots {
 private FlowerPots(){}
 public static boolean accepts(ItemStack stack){if(stack.isEmpty()||!(stack.getItem() instanceof ItemBlock))return false;Block block=Block.getBlockFromItem(stack.getItem());return block instanceof ModernFlower||block instanceof NetherPlant||block instanceof AzaleaBlock||block instanceof ModernSapling||block instanceof MangroveSapling||block instanceof Eyeblossom||block.getRegistryName()!=null&&block.getRegistryName().toString().equals(UAB.ID+":torchflower");}
}
