package ganymedes01.etfuturum;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.util.math.BlockPos;
public final class TrialSpawnerBlock extends Block {
    public enum Phase implements IStringSerializable{INACTIVE("inactive"),ACTIVE("active"),REWARD("ejecting_reward");private final String name;Phase(String name){this.name=name;}public String getName(){return name;}}
    public static final PropertyEnum<Phase> PHASE=PropertyEnum.create("trial_spawner_state",Phase.class);
    public static final PropertyBool OMINOUS=PropertyBool.create("ominous"),BOGGED=PropertyBool.create("bogged");
    TrialSpawnerBlock(){super(Material.IRON);setHardness(50);setResistance(2000);setLightOpacity(0);setDefaultState(blockState.getBaseState().withProperty(PHASE,Phase.INACTIVE).withProperty(OMINOUS,false).withProperty(BOGGED,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,PHASE,OMINOUS,BOGGED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(PHASE,Phase.values()[Math.min(2,meta>>1&3)]).withProperty(OMINOUS,(meta&8)!=0).withProperty(BOGGED,(meta&1)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(PHASE).ordinal()<<1|(state.getValue(OMINOUS)?8:0)|(state.getValue(BOGGED)?1:0);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new TrialSpawnerTile();}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Items.AIR;}
    @Override protected boolean canSilkHarvest(){return false;}
}
