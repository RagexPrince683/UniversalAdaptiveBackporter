package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;
public final class ItemPhysics {
 private static boolean flammable;
 private ItemPhysics(){}
 static void initialize(){flammable=ganymedes01.etfuturum.configuration.UabConfiguration.get().bool("functions","enableNetheriteFlammable",false);}
 public static boolean fireproof(ItemStack stack){if(stack.isEmpty()||flammable||!Features.enabled("mixins.fireproofItems"))return false;if(stack.getItem().getTranslationKey(stack).toLowerCase(java.util.Locale.ROOT).contains("netherite"))return true;for(int id:OreDictionary.getOreIDs(stack)){String tag=OreDictionary.getOreName(id);if(tag.equals("oreDebris")||tag.equals("scrapDebris")||tag.equals("ingotNetherite")||tag.equals("blockNetherite"))return true;}return false;}
 public static void floatItem(EntityItem item){if(!Features.enabled("mixins.liquidItemFloat")||item.hasNoGravity()||item.motionY<=-.15||Math.max(Math.abs(item.motionX),Math.abs(item.motionZ))>=.015)return;BlockPos pos=new BlockPos(item.posX,item.posY,item.posZ);if(!item.world.isBlockLoaded(pos))return;IBlockState state=item.world.getBlockState(pos);if(!(state.getBlock() instanceof BlockLiquid))return;BlockPos up=pos.up();IBlockState above=item.world.isBlockLoaded(up)?item.world.getBlockState(up):net.minecraft.init.Blocks.AIR.getDefaultState();boolean covered=above.getMaterial().isLiquid()&&above.getBlock() instanceof BlockLiquid&&above.getValue(BlockLiquid.LEVEL)==0;double height=covered?1.1:.9-BlockLiquid.getLiquidHeightPercent(state.getValue(BlockLiquid.LEVEL));if(item.posY<pos.getY()+height){item.motionY=.05;item.motionX*=.4;item.motionZ*=.4;}else item.motionY=.03999999910593033D;}
}
