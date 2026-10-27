package ganymedes01.etfuturum.api.tags.helpers;
import ganymedes01.etfuturum.api.tags.containers.TagContainerBasic;
import net.minecraft.world.biome.Biome;
import java.util.*;
public final class BiomeTags extends TagContainerBasic<Biome> {
    public static final String CONTAINER_ID="minecraft:worldgen/biome";
    private static final TagIndex<Biome> INDEX=new TagIndex<>();
    private final Biome biome;
    public BiomeTags(Biome biome){this.biome=Objects.requireNonNull(biome);}
    @Override public void addTags(String...tags){addTags(biome,tags);}
    @Override public void removeTags(String...tags){removeTags(biome,tags);}
    @Override public Set<String> getTags(){return getTags(biome);}
    @Override public Set<String> getBaseTags(){return INDEX.base(biome,0);}
    @Override public void clearCaches(){INDEX.clear();}
    public static void addTags(Biome biome,String...tags){INDEX.add(biome,0,tags);}
    public static void addTags(int id,String...tags){addTags(Objects.requireNonNull(Biome.getBiome(id),"Unknown biome "+id),tags);}
    public static void removeTags(Biome biome,String...tags){INDEX.remove(biome,0,tags);}
    public static void removeTags(int id,String...tags){removeTags(Objects.requireNonNull(Biome.getBiome(id),"Unknown biome "+id),tags);}
    public static Set<String> getTags(Biome biome){return INDEX.tags(biome,0);}
    public static Set<String> getTags(int id){return getTags(Objects.requireNonNull(Biome.getBiome(id),"Unknown biome "+id));}
    public static boolean hasTag(Biome biome,String tag){return getTags(biome).contains(tag);}
    public static Set<Biome> getInTag(String tag){return Collections.unmodifiableSet(INDEX.members(tag).keySet());}
    public static void addInheritors(String child,String...parents){INDEX.addParents(child,parents);}
    public static void removeInheritors(String child,String...parents){INDEX.removeParents(child,parents);}
    public static Set<String> getInheritors(String tag){return INDEX.parents(tag);}
}
