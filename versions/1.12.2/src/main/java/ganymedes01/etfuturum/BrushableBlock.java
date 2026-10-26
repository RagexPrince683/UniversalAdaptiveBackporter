package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
public final class BrushableBlock extends BlockFalling {
    public static final PropertyInteger DUSTED=PropertyInteger.create("dusted",0,3);final Block base;
    BrushableBlock(Block base){this.base=base;setHardness(.25F);setSoundType(SoundType.SAND);setDefaultState(blockState.getBaseState().withProperty(DUSTED,0));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,DUSTED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(DUSTED,meta&3);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(DUSTED);}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new BrushableTile();}
    @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){return Items.AIR;}
    @Override public boolean canSilkHarvest(World world,BlockPos pos,IBlockState state,net.minecraft.entity.player.EntityPlayer player){return false;}
}
