package ganymedes01.etfuturum;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
final class SculkCatalyst extends SculkBlock {
 static final PropertyBool BLOOM=PropertyBool.create("bloom");
 SculkCatalyst(){super(20);setDefaultState(blockState.getBaseState().withProperty(BLOOM,false));}
 @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,BLOOM);}
 @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(BLOOM,(meta&1)!=0);}@Override public int getMetaFromState(IBlockState state){return state.getValue(BLOOM)?1:0;}
 @Override public boolean hasTileEntity(IBlockState state){return true;}@Override public TileEntity createTileEntity(World world,IBlockState state){return new SculkCatalystTile();}
 @Override public void updateTick(World world,BlockPos pos,IBlockState state,java.util.Random random){world.setBlockState(pos,state.withProperty(BLOOM,false),3);}
}
