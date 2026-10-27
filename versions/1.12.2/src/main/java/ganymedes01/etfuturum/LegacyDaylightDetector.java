package ganymedes01.etfuturum;
import java.util.Random;
import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
/** Saved old-base blocks retain native power/tile behavior and drop the canonical native detector. */
public final class LegacyDaylightDetector extends BlockDaylightDetector {
 public LegacyDaylightDetector(){super(false);setHardness(.2F);setSoundType(net.minecraft.block.SoundType.WOOD);}
 @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);}
 @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return new ItemStack(Blocks.DAYLIGHT_DETECTOR);}
}
