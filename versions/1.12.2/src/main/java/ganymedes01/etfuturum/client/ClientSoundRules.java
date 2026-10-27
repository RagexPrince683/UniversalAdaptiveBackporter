package ganymedes01.etfuturum.client;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
public final class ClientSoundRules {
 private ClientSoundRules(){}
 public static String instrument(Block block){if(block==Blocks.SOUL_SAND)return "block.note_block.cow_bell";if(block==Blocks.HAY_BLOCK)return "block.note_block.banjo";if(block==Blocks.PUMPKIN||block==Blocks.LIT_PUMPKIN)return "block.note_block.didgeridoo";if(block==Blocks.EMERALD_BLOCK)return "block.note_block.bit";if(block==Blocks.IRON_BLOCK)return "block.note_block.iron_xylophone";ItemStack stack=new ItemStack(block);if(!stack.isEmpty())for(int tag:OreDictionary.getOreIDs(stack)){String name=OreDictionary.getOreName(tag);if(name.equals("blockEmerald"))return "block.note_block.bit";if(name.equals("blockIron"))return "block.note_block.iron_xylophone";}return null;}
}
