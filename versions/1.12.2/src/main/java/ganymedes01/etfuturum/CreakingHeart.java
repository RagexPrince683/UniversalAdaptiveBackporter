package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
public final class CreakingHeart extends BlockRotatedPillar {
    public enum HeartState implements IStringSerializable {UPROOTED,DORMANT,AWAKE;public String getName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final PropertyEnum<HeartState> HEART=PropertyEnum.create("creaking_heart_state",HeartState.class);
    CreakingHeart(){super(net.minecraft.block.material.Material.WOOD);setHardness(10);setResistance(10);setSoundType(SoundType.WOOD);setDefaultState(blockState.getBaseState().withProperty(AXIS,EnumFacing.Axis.Y).withProperty(HEART,HeartState.UPROOTED));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,AXIS,HEART);}
    @Override public IBlockState getStateFromMeta(int meta){return super.getStateFromMeta(meta).withProperty(HEART,HeartState.values()[Math.min(2,meta&3)]);}
    @Override public int getMetaFromState(IBlockState state){return super.getMetaFromState(state)|state.getValue(HEART).ordinal();}
    @Override public IBlockState getActualState(IBlockState state,IBlockAccess world,BlockPos pos){if(!(world instanceof World))return state;boolean aligned=aligned(world,pos,state.getValue(AXIS));return state.withProperty(HEART,!aligned?HeartState.UPROOTED:night((World)world)?HeartState.AWAKE:HeartState.DORMANT);}
    private static boolean night(World world){long time=Math.floorMod(world.getWorldTime(),24000L);return world.provider.hasSkyLight()&&time>=13000&&time<23000;}
    private static boolean aligned(IBlockAccess world,BlockPos pos,EnumFacing.Axis axis){EnumFacing direction=EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE,axis);for(BlockPos at:new BlockPos[]{pos.offset(direction),pos.offset(direction.getOpposite())}){if(world instanceof World&&!((World)world).isBlockLoaded(at))return false;net.minecraft.block.state.IBlockState state=world.getBlockState(at);if(state.getBlock()!=Content.block("pale_oak_log")||state.getValue(BlockLog.LOG_AXIS)!=BlockLog.EnumAxis.fromFacingAxis(axis))return false;}return true;}
    public static boolean isActive(World world,BlockPos pos){IBlockState state=world.getBlockState(pos);return state.getBlock() instanceof CreakingHeart&&night(world)&&aligned(world,pos,state.getValue(AXIS));}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new CreakingHeartTile();}
    @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){return Items.AIR;}
    @Override public boolean canSilkHarvest(World world,BlockPos pos,IBlockState state,EntityPlayer player){return true;}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public int getExpDrop(IBlockState state,IBlockAccess world,BlockPos pos,int fortune){return 20+(world instanceof World?((World)world).rand.nextInt(5):2);}
}
