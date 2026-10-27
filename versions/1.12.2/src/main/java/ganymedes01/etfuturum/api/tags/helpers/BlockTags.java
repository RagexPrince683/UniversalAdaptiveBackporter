package ganymedes01.etfuturum.api.tags.helpers;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import ganymedes01.etfuturum.api.blocksanditems.utils.BlockMetaPair;
import java.util.*;
public final class BlockTags extends ganymedes01.etfuturum.api.tags.containers.TagContainerMeta<Block,BlockMetaPair> {
 public static final String CONTAINER_ID="minecraft:blocks";
 private static final TagIndex<Block> INDEX=new TagIndex<>();
 private final Block object;
 public BlockTags(Block object){this.object=Objects.requireNonNull(object);}
 public void addTags(int meta,String...tags){addTags(object,meta,tags);}
 public void removeTags(int meta,String...tags){removeTags(object,meta,tags);}
 public Set<String> getTags(int meta){return getTags(object,meta);}
 public Set<String> getBaseTags(int meta){return INDEX.base(object,meta);}
 public Set<String> getWildcardTags(){return getBaseTags(OreDictionary.WILDCARD_VALUE);}
 public void clearCaches(){INDEX.clear();}
 public static void addTags(Block object,int meta,String...tags){INDEX.add(object,meta,tags);}
 public static void addTags(Block object,String...tags){addTags(object,OreDictionary.WILDCARD_VALUE,tags);}
 public static void addTagsByID(String namespace,String name,int meta,String...tags){ResourceLocation key=new ResourceLocation(namespace,name);if(ForgeRegistries.BLOCKS.containsKey(key))addTags(ForgeRegistries.BLOCKS.getValue(key),meta,tags);}
 public static void addTagsByID(String namespace,String name,String...tags){addTagsByID(namespace,name,OreDictionary.WILDCARD_VALUE,tags);}
 public static void removeTags(Block object,int meta,String...tags){INDEX.remove(object,meta,tags);}
 public static void removeTags(Block object,String...tags){removeTags(object,OreDictionary.WILDCARD_VALUE,tags);}
 public static Set<String> getTags(Block object,int meta){return INDEX.tags(object,meta);}
 public static Set<String> getTags(Block object){return getTags(object,OreDictionary.WILDCARD_VALUE);}
 public static boolean hasTag(Block object,int meta,String tag){return getTags(object,meta).contains(tag);}
 public static boolean hasTag(Block object,String tag){return hasTag(object,OreDictionary.WILDCARD_VALUE,tag);}
 public static Set<BlockMetaPair> getInTag(String tag){Set<BlockMetaPair> result=new LinkedHashSet<>();for(Map.Entry<Block,Set<Integer>> entry:INDEX.members(tag).entrySet())for(int meta:entry.getValue())result.add(BlockMetaPair.intern(entry.getKey(),meta));return Collections.unmodifiableSet(result);}
 public static void addInheritors(String child,String...parents){INDEX.addParents(child,parents);}
 public static void removeInheritors(String child,String...parents){INDEX.removeParents(child,parents);}
 public static Set<String> getInheritors(String child){return INDEX.parents(child);}
}
