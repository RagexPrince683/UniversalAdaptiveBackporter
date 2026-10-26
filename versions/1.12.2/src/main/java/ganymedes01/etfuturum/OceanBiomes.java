package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.init.*;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.layer.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Native Forge biome identities and the maintained temperature-cell ocean substitution. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class OceanBiomes {
    private static final Map<String,Biome> BIOMES=new LinkedHashMap<>();
    private static final Biome[] SHALLOW=new Biome[5],DEEP=new Biome[5];
    private static final Map<Biome,String> FLAGS=new IdentityHashMap<>();
    private OceanBiomes() { }
    public static void initialize() {
        SHALLOW[0]=ocean("warm_ocean","Warm Ocean",.9F,false,"generateWarmOcean");
        SHALLOW[1]=ocean("lukewarm_ocean","Lukewarm Ocean",.6F,false,"generateLukewarmOcean");
        SHALLOW[2]=Biomes.OCEAN;SHALLOW[3]=ocean("cold_ocean","Cold Ocean",.2F,false,"generateColdOcean");SHALLOW[4]=Biomes.FROZEN_OCEAN;
        DEEP[0]=DEEP[1]=ocean("deep_lukewarm_ocean","Deep Lukewarm Ocean",.6F,true,"generateDeepLukewarmOcean");
        DEEP[2]=Biomes.DEEP_OCEAN;DEEP[3]=ocean("deep_cold_ocean","Deep Cold Ocean",.2F,true,"generateDeepColdOcean");DEEP[4]=ocean("deep_frozen_ocean","Deep Frozen Ocean",0,true,"generateDeepFrozenOcean");
        if(Features.enabled("world.enableSurfaceBiomes"))MinecraftForge.TERRAIN_GEN_BUS.register(new Events());
    }
    private static Biome ocean(String id,String name,float temperature,boolean deep,String flag) {
        Biome.BiomeProperties properties=new Biome.BiomeProperties(name).setTemperature(temperature).setRainfall(.5F).setBaseHeight(deep?-1.8F:-1F).setHeightVariation(.1F);
        if(temperature<.15F)properties.setSnowEnabled();
        Biome biome=new BiomeOcean(properties){{topBlock=(temperature>=.6F?Blocks.SAND:Blocks.GRAVEL).getDefaultState();fillerBlock=topBlock;}};
        biome.setRegistryName(UAB.ID,id);BIOMES.put(id,biome);FLAGS.put(biome,"world."+flag);Features.enabled("world."+flag);return biome;
    }
    @SubscribeEvent public static void register(RegistryEvent.Register<Biome> event) {
        for(Biome biome:BIOMES.values()){event.getRegistry().register(biome);BiomeDictionary.addTypes(biome,BiomeDictionary.Type.OCEAN,BiomeDictionary.Type.WATER);if(biome.getDefaultTemperature()<.15F)BiomeDictionary.addTypes(biome,BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.COLD);BiomeManager.oceanBiomes.add(biome);}
    }
    public static List<Biome> monumentBiomes(List<Biome> original) {
        boolean deep=original.contains(Biomes.DEEP_OCEAN)&&!original.contains(Biomes.OCEAN);
        List<Biome> result=new ArrayList<>(original);
        for(Map.Entry<String,Biome> entry:BIOMES.entrySet())if((!deep||entry.getKey().startsWith("deep_"))&&!result.contains(entry.getValue()))result.add(entry.getValue());
        return result;
    }
    private static final class Layer extends GenLayer {
        Layer(GenLayer parent){super(712034L);this.parent=parent;}
        @Override public int[] getInts(int x,int z,int width,int height) {
            int[] source=parent.getInts(x,z,width,height),result=IntCache.getIntCache(width*height);
            for(int dz=0;dz<height;dz++)for(int dx=0;dx<width;dx++){
                int i=dx+dz*width,old=source[i];result[i]=old;
                if(old!=Biome.getIdForBiome(Biomes.OCEAN)&&old!=Biome.getIdForBiome(Biomes.DEEP_OCEAN))continue;
                initChunkSeed(Math.floorDiv(x+dx,64),Math.floorDiv(z+dz,64));Biome biome=(old==Biome.getIdForBiome(Biomes.DEEP_OCEAN)?DEEP:SHALLOW)[nextInt(5)];
                if(biome!=null&&(!FLAGS.containsKey(biome)||Features.enabled(FLAGS.get(biome))))result[i]=Biome.getIdForBiome(biome);
            }
            return result;
        }
    }
    public static final class Events {
        @SubscribeEvent public void layers(WorldTypeEvent.InitBiomeGens event) {
            WorldType type=event.getWorldType();if(type!=WorldType.DEFAULT&&type!=WorldType.LARGE_BIOMES&&type!=WorldType.AMPLIFIED)return;
            GenLayer[] layers=event.getNewBiomeGens().clone();if(layers.length<2)return;
            GenLayer ocean=new Layer(layers[0]),voronoi=new GenLayerVoronoiZoom(10L,ocean);ocean.initWorldGenSeed(event.getSeed());voronoi.initWorldGenSeed(event.getSeed());
            layers[0]=ocean;layers[1]=voronoi;if(layers.length>2)layers[2]=ocean;event.setNewBiomeGens(layers);
        }
    }
}
