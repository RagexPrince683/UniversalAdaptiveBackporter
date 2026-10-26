package ganymedes01.etfuturum;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
class SculkBlock extends Block {
    private final int experience;
    SculkBlock(int experience){super(Material.GROUND);this.experience=experience;setHardness(experience==1?.6F:3).setResistance(experience==1?.2F:3);}
    @Override public int getExpDrop(IBlockState state,IBlockAccess world,BlockPos pos,int fortune){return experience;}
    @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){return Items.AIR;}
    @Override public boolean canSilkHarvest(World world,BlockPos pos,IBlockState state,EntityPlayer player){return true;}
}
