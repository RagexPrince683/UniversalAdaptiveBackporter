package ganymedes01.etfuturum;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
final class ResinClump extends BlockVine {
    ResinClump(){setTickRandomly(false);setHardness(0);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,java.util.Random random){}
    @Override public boolean isLadder(IBlockState state,IBlockAccess world,BlockPos pos,EntityLivingBase entity){return false;}
    @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){drops.add(Content.stack("resin_clump"));}
    @Override public void harvestBlock(World world,EntityPlayer player,BlockPos pos,IBlockState state,TileEntity tile,ItemStack tool){if(!world.isRemote)dropBlockAsItem(world,pos,state,0);}
}
