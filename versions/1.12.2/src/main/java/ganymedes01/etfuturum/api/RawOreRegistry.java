package ganymedes01.etfuturum.api;
import java.util.*;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraftforge.common.config.Property;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import ganymedes01.etfuturum.api.mappings.RawOreDropMapping;
/** Canonical ore-tag to raw-item mapping. Register during initialization or on the owning game thread. */
public final class RawOreRegistry {
 public static final Map<String,RawOreDropMapping> rawOreRegistry=new LinkedHashMap<>();
 private RawOreRegistry(){}
 public static void addOre(String name,Item item){addOre(name,item,0);}
 public static void addOre(String name,Item item,int meta){Objects.requireNonNull(name);if(item==null||item==Items.AIR||meta<0)throw new IllegalArgumentException("A raw drop needs a real item and metadata");RawOreDropMapping mapping=new RawOreDropMapping(item,meta);for(String extra:UabConfiguration.get().property("functions","extraDropRawOres",new String[]{"oreCopper","oreTin"},Property.Type.STRING).getStringList())if(extra.equals(name.replace("oreNether","ore")))mapping.setDropsExtra(true);rawOreRegistry.put(name,mapping);}
 public static boolean hasOre(String name){return rawOreRegistry.containsKey(name);}
 public static RawOreDropMapping getOre(String name){return rawOreRegistry.get(name);}
 public static Map<String,RawOreDropMapping> getOreMap(){return Collections.unmodifiableMap(rawOreRegistry);}
 public static void init(){ganymedes01.etfuturum.OreCompatibility.initialize();}
}
