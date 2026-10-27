package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntityBee;
import ganymedes01.etfuturum.tileentities.TileEntityBeeHive;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import java.util.*;
public final class BeeNests {
    private BeeNests(){}
    private static final ThreadLocal<Boolean> SAPLING=new ThreadLocal<>();
    public static void beginSapling(World world,BlockPos pos,Random random){SAPLING.set(Content.available("bee_nest")&&random.nextFloat()<=.05F&&flowers(world,pos));}
    public static void endSapling(){SAPLING.remove();}
    private static boolean flowers(World world,BlockPos origin){for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if((x!=0||z!=0)&&EntityBee.isValidFlower(world,origin.getX()+x,origin.getY(),origin.getZ()+z))return true;return false;}
    public static void afterTree(World world,Random random,BlockPos origin){if(world.isRemote||world.provider.getDimension()!=0||!Content.available("bee_nest"))return;Boolean sapling=SAPLING.get();if(sapling!=null){if(sapling)place(world,origin,random,1);return;}if(!Features.enabled("mixins.postTreeGenEvent"))return;Biome biome=world.getBiome(origin);String name=biome.getRegistryName()==null?"":biome.getRegistryName().getPath();float chance=biome==Biomes.MUTATED_FOREST?.02F:name.equals("meadow")?1:name.equals("cherry_grove")||name.equals("mangrove_swamp")?.05F:!BiomeDictionary.hasType(biome,BiomeDictionary.Type.CONIFEROUS)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.SNOWY)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.SANDY)?BiomeDictionary.hasType(biome,BiomeDictionary.Type.PLAINS)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.SAVANNA)?.05F:BiomeDictionary.hasType(biome,BiomeDictionary.Type.FOREST)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.SPARSE)?.002F:0:0;if(random.nextFloat()<=chance)place(world,origin,random,3);}
    public static boolean place(World world,BlockPos origin,Random random,int minimum){if(!Content.available("bee_nest")||!world.isBlockLoaded(origin))return false;IBlockState log=world.getBlockState(origin);if(!log.getBlock().isWood(world,origin)){for(int y=1;y<=12;y++){BlockPos next=origin.up(y);if(!world.isBlockLoaded(next))return false;if(world.getBlockState(next).getBlock().isWood(world,next))return place(world,next,random,minimum);}return false;}EnumFacing[] directions={EnumFacing.SOUTH,EnumFacing.EAST,EnumFacing.WEST};EnumFacing side=directions[random.nextInt(directions.length)];BlockPos trunk=origin;if(world.isBlockLoaded(trunk.offset(side))&&world.getBlockState(trunk.offset(side)).equals(log))trunk=trunk.offset(side);for(int y=trunk.getY();y<world.getHeight()-2;y++){BlockPos at=new BlockPos(trunk.getX(),y,trunk.getZ()),nest=at.offset(side);if(!world.isAreaLoaded(nest,nest.up().south())||!world.getBlockState(at).equals(log))return false;IBlockState above=world.getBlockState(nest.up());if(above.getBlock().isLeaves(above,world,nest.up())||above.equals(log)){if(!world.isAirBlock(nest)||!world.isAirBlock(nest.south())||!world.setBlockState(nest,Content.block("bee_nest").getDefaultState().withProperty(BeeHiveBlock.FACING,EnumFacing.SOUTH),2))return false;if(!(world.getTileEntity(nest) instanceof TileEntityBeeHive))return false;TileEntityBeeHive hive=(TileEntityBeeHive)world.getTileEntity(nest);int count=minimum+random.nextInt(Math.max(1,hive.maxHiveSize()-minimum+1));for(int i=0;i<count;i++)hive.tryEnterHive(new EntityBee(world),false,random.nextInt(599));return true;}}return false;}
}
