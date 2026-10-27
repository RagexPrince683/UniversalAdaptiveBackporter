package ganymedes01.etfuturum.api;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.*;
import net.minecraftforge.oredict.OreDictionary;
import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.api.blocksanditems.utils.BlockMetaPair;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;
/** Native metadata-indexed replacement authority; generation queries allocate no mapping keys. */
public final class DeepslateOreRegistry {
 private static final Map<Block,BlockMetaPair[]> ORES=new IdentityHashMap<>();
 private DeepslateOreRegistry(){}
 public static void addOre(Block from,Block to){addOre(from,0,to,0);}
 public static void addOre(Block from,int fromMeta,Block to,int toMeta){addOre(from,fromMeta,to,toMeta,false);}
 public static void addOre(Block from,int fromMeta,Block to,int toMeta,boolean onlyIfAbsent){if(from==null||to==null||fromMeta<0||fromMeta>15||toMeta<0||toMeta>15)throw new IllegalArgumentException("Native block metadata must be between 0 and 15");if(from.hasTileEntity(from.getStateFromMeta(fromMeta))||to.hasTileEntity(to.getStateFromMeta(toMeta)))throw new IllegalArgumentException("Tile entities cannot be replaced by the ore registry");BlockMetaPair[] variants=ORES.computeIfAbsent(from,key->new BlockMetaPair[16]);if(!onlyIfAbsent||variants[fromMeta]==null)variants[fromMeta]=BlockMetaPair.intern(to,toMeta);}
 public static void addOreByOreDict(String name,Block to){addOreByOreDict(name,to,0);}
 public static void addOreByOreDict(String name,Block to,int meta){for(ItemStack stack:OreDictionary.getOres(name,false))if(stack.getItem() instanceof ItemBlock){Block from=Block.getBlockFromItem(stack.getItem());if(from==to)continue;if(stack.getMetadata()==OreDictionary.WILDCARD_VALUE){for(IBlockState state:from.getBlockState().getValidStates())if(!from.hasTileEntity(state))addOre(from,from.getMetaFromState(state),to,meta,true);}else if(stack.getMetadata()>=0&&stack.getMetadata()<16&&!from.hasTileEntity(from.getStateFromMeta(stack.getMetadata())))addOre(from,stack.getMetadata(),to,meta,true);}}
 public static boolean hasOre(Block block){return hasOre(block,0);}
 public static boolean hasOre(Block block,int meta){return getOre(block,meta)!=null;}
 public static BlockMetaPair getOre(Block block){return getOre(block,0);}
 public static BlockMetaPair getOre(Block block,int meta){BlockMetaPair[] variants=ORES.get(block);return variants==null||meta<0||meta>15?null:variants[meta];}
 public static Map<BlockMetaPair,BlockMetaPair> getOreMap(){Map<BlockMetaPair,BlockMetaPair> result=new LinkedHashMap<>();for(Map.Entry<Block,BlockMetaPair[]> entry:ORES.entrySet())for(int meta=0;meta<16;meta++)if(entry.getValue()[meta]!=null)result.put(BlockMetaPair.intern(entry.getKey(),meta),entry.getValue()[meta]);return Collections.unmodifiableMap(result);}
 public static void init(){OreCompatibility.initialize();}
 public static boolean setBlockDeepslate(Chunk chunk,int x,int y,int z,Block input,int inputMeta){World world=chunk.getWorld();if(x<0||x>15||z<0||z>15||y<0||y>=world.getHeight())return false;BlockPos pos=new BlockPos((chunk.x<<4)+x,y,(chunk.z<<4)+z);IBlockState current=chunk.getBlockState(pos);Block block=current.getBlock();if(current.getMaterial()==Material.AIR||block.hasTileEntity(current)||BlockTags.hasTag(block,block.getMetaFromState(current),"minecraft:deepslate_ore_replaceables")||ModernStructures.occupied(world,pos))return false;BlockMetaPair replacement=getOre(block,block.getMetaFromState(current));IBlockState target;if(replacement!=null)target=replacement.get()==Content.block("deepslate")?input.getStateFromMeta(inputMeta):replacement.get().getStateFromMeta(replacement.getMeta());else if(block.isReplaceableOreGen(current,world,pos,state->state.getBlock()==Blocks.STONE))target=input.getStateFromMeta(inputMeta);else return false;return chunk.setBlockState(pos,target)!=null;}
 public static boolean doesChunkSupportLayerDeepslate(IChunkProvider provider,int dimension){return OreCompatibility.layerSupported(provider,dimension);}
 public static int getDeepslateHeight(World world){return OreCompatibility.height(world);}
}
