package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public class SculkSensor extends Block {
    public enum Phase implements IStringSerializable {INACTIVE,ACTIVE,COOLDOWN;public String getName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final PropertyEnum<Phase> PHASE=PropertyEnum.create("sculk_sensor_phase",Phase.class);
    SculkSensor(){super(net.minecraft.block.material.Material.ROCK);setHardness(1.5F);setDefaultState(blockState.getBaseState().withProperty(PHASE,Phase.INACTIVE));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,PHASE);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(PHASE,(meta&4)!=0?Phase.ACTIVE:(meta&8)!=0?Phase.COOLDOWN:Phase.INACTIVE);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(PHASE)==Phase.ACTIVE?4:state.getValue(PHASE)==Phase.COOLDOWN?8:0;}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new SculkSensorTile();}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,0,0,1,.5,1);}
    @Override public boolean canProvidePower(IBlockState state){return true;}
    @Override public int getWeakPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){TileEntity tile=world.getTileEntity(pos);return state.getValue(PHASE)==Phase.ACTIVE&&tile instanceof SculkSensorTile?((SculkSensorTile)tile).power:0;}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);return state.getValue(PHASE)==Phase.ACTIVE&&tile instanceof SculkSensorTile?((SculkSensorTile)tile).frequency:0;}
    @Override public int damageDropped(IBlockState state){return 0;}
}
