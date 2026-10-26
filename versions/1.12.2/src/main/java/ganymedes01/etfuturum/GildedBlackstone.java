package ganymedes01.etfuturum;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
final class GildedBlackstone extends Block {
    GildedBlackstone(){super(Material.ROCK);setHardness(1.5F);setResistance(6);setHarvestLevel("pickaxe",0);}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return random.nextInt(10-Math.min(3,fortune)*3)==0?Items.GOLD_NUGGET:Item.getItemFromBlock(this);}
    @Override public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops,net.minecraft.world.IBlockAccess world,net.minecraft.util.math.BlockPos pos,IBlockState state,int fortune){Random random=world instanceof net.minecraft.world.World?((net.minecraft.world.World)world).rand:RANDOM;if(random.nextInt(10-Math.min(3,fortune)*3)==0)drops.add(new net.minecraft.item.ItemStack(Items.GOLD_NUGGET,2+random.nextInt(4)));else drops.add(new net.minecraft.item.ItemStack(this));}
}
