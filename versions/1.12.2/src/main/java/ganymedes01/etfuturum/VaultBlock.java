package ganymedes01.etfuturum;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class VaultBlock extends Block {
    public enum Phase implements IStringSerializable{INACTIVE,ACTIVE;public String getName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public static final PropertyBool OMINOUS=PropertyBool.create("ominous");
    public static final PropertyEnum<Phase> PHASE=PropertyEnum.create("vault_state",Phase.class);
    VaultBlock(){super(Material.IRON);setHardness(50);setResistance(2000);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(OMINOUS,false).withProperty(PHASE,Phase.INACTIVE));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,OMINOUS,PHASE);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3)).withProperty(OMINOUS,(meta&4)!=0).withProperty(PHASE,(meta&8)!=0?Phase.ACTIVE:Phase.INACTIVE);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|(state.getValue(OMINOUS)?4:0)|(state.getValue(PHASE)==Phase.ACTIVE?8:0);}
    @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public IBlockState withMirror(IBlockState state,Mirror mirror){return withRotation(state,mirror.toRotation(state.getValue(FACING)));}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase placer){return getStateFromMeta(meta).withProperty(FACING,placer.getHorizontalFacing().getOpposite());}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new VaultTile();}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){TileEntity tile=world.getTileEntity(pos);return world.isRemote||tile instanceof VaultTile&&((VaultTile)tile).unlock(player,hand);}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Items.AIR;}
    @Override protected boolean canSilkHarvest(){return false;}
}
