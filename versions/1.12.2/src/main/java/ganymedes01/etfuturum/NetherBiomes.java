package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.*;
import net.minecraft.init.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.layer.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Registry identities survive configuration changes; the maintained weighted layer owns new terrain. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class NetherBiomes {
    public enum Kind { CRIMSON, WARPED, SOUL, BASALT }
    private static final List<Nether> BIOMES=new ArrayList<>();
    private static int wastesWeight;private static boolean enabled;
    private NetherBiomes() { }
    static void initialize(){
        enabled=Features.enabled("experiments.netherDimensionProvider");
        wastesWeight=Features.integer("netherWastesWeight",30,1,127);
        add("crimson_forest","Crimson Forest",Kind.CRIMSON,"content.generate_crimson_forest","crimsonForestWeight",0xAD0808);
        add("warped_forest","Warped Forest",Kind.WARPED,"content.generate_warped_forest","warpedForestWeight",0x5700AF);
        add("soul_sand_valley","Soul Sand Valley",Kind.SOUL,"content.generate_soul_sand_valley","soulSandValleyWeight",0x00BACD);
        add("basalt_deltas","Basalt Deltas",Kind.BASALT,"content.generate_basalt_deltas","basaltDeltasWeight",0x534868);
    }
    private static void add(String id,String name,Kind kind,String flag,String weight,int fog){Nether biome=new Nether(name,kind,Features.enabled(flag)?Features.integer(weight,10,1,127):0,fog);biome.setRegistryName(UAB.ID,id);BIOMES.add(biome);}
    @SubscribeEvent public static void register(RegistryEvent.Register<Biome> event){for(Nether biome:BIOMES){event.getRegistry().register(biome);BiomeDictionary.addTypes(biome,BiomeDictionary.Type.NETHER,BiomeDictionary.Type.HOT,BiomeDictionary.Type.DRY);}}
    public static BiomeProvider provider(World world,BiomeProvider original){return enabled&&original!=null&&original.getClass()==BiomeProviderSingle.class?new Provider(world.getSeed()):original;}
    public static Kind kind(Biome biome){return biome instanceof Nether?((Nether)biome).kind:null;}
    static void spawns(){for(Nether biome:BIOMES)if(biome.kind==Kind.CRIMSON){ganymedes01.etfuturum.configuration.SpawnSettings.add(biome.spawnList(),ganymedes01.etfuturum.entities.EntityPiglin.class,5,3,4);ganymedes01.etfuturum.configuration.SpawnSettings.add(biome.spawnList(),ganymedes01.etfuturum.entities.EntityHoglin.class,9,3,4);}}
    public static int fog(Biome biome){return biome instanceof Nether?((Nether)biome).fog:-1;}
    public static void surface(World world,int cx,int cz,ChunkPrimer primer){
        if(!(world.getBiomeProvider() instanceof Provider))return;
        // Query the provider directly: World.getBiome would request the chunk being constructed.
        Biome[] biomes=world.getBiomeProvider().getBiomes(null,cx<<4,cz<<4,16,16);
        Random random=new Random(world.getSeed()^(long)cx*341873128712L^(long)cz*132897987541L);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){
            Kind kind=kind(biomes[x+z*16]);if(kind==null)continue;
            for(int y=5;y<123;y++){
                Block old=primer.getBlockState(x,y,z).getBlock();
                if(old!=Blocks.NETHERRACK&&old!=Blocks.SOUL_SAND&&old!=Blocks.GRAVEL)continue;
                boolean exposed=primer.getBlockState(x,y+1,z).getMaterial()==net.minecraft.block.material.Material.AIR;
                if(kind==Kind.CRIMSON||kind==Kind.WARPED){if(y>31&&exposed)primer.setBlockState(x,y,z,Content.block(kind==Kind.CRIMSON?"crimson_nylium":"warped_nylium").getDefaultState());}
                else if(kind==Kind.SOUL){if(exposed&&y>30){IBlockState soil=Content.available("soul_soil")&&random.nextBoolean()?Content.block("soul_soil").getDefaultState():Blocks.SOUL_SAND.getDefaultState();primer.setBlockState(x,y,z,soil);for(int d=1;d<=2&&y-d>4&&primer.getBlockState(x,y-d,z).getBlock()==Blocks.NETHERRACK;d++)primer.setBlockState(x,y-d,z,soil);}}
                else if(exposed||primer.getBlockState(x,y-1,z).getMaterial()==net.minecraft.block.material.Material.AIR){primer.setBlockState(x,y,z,Content.block(random.nextInt(5)==0?"blackstone":"basalt").getDefaultState());}
            }
        }
    }
    private static final class Nether extends Biome {
        final Kind kind;final int weight,fog;
        Nether(String name,Kind kind,int weight,int fog){
            super(new BiomeProperties(name).setTemperature(2).setRainfall(0).setRainDisabled());this.kind=kind;this.weight=weight;this.fog=fog;
            topBlock=kind==Kind.CRIMSON?Content.block("crimson_nylium").getDefaultState():kind==Kind.WARPED?Content.block("warped_nylium").getDefaultState():kind==Kind.SOUL?Blocks.SOUL_SAND.getDefaultState():Content.block("blackstone").getDefaultState();fillerBlock=kind==Kind.BASALT?Content.block("basalt").getDefaultState():kind==Kind.SOUL?Blocks.SOUL_SAND.getDefaultState():Blocks.NETHERRACK.getDefaultState();
            spawnableCreatureList.clear();spawnableWaterCreatureList.clear();spawnableCaveCreatureList.clear();spawnableMonsterList.clear();
            if(kind==Kind.CRIMSON)spawnableMonsterList.add(new SpawnListEntry(EntityPigZombie.class,1,2,4));
            else if(kind==Kind.WARPED)spawnableMonsterList.add(new SpawnListEntry(EntityEnderman.class,1,1,4));
            else if(kind==Kind.SOUL){spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class,20,1,5));spawnableMonsterList.add(new SpawnListEntry(EntityGhast.class,50,1,4));spawnableMonsterList.add(new SpawnListEntry(EntityEnderman.class,1,1,4));spawnableMonsterList.add(new SpawnListEntry(EntityMagmaCube.class,2,4,4));}
            else {spawnableMonsterList.add(new SpawnListEntry(EntityGhast.class,40,1,1));spawnableMonsterList.add(new SpawnListEntry(EntityMagmaCube.class,100,2,5));}
        }
        @Override public float getSpawningChance(){return kind==Kind.WARPED?.035F:kind==Kind.SOUL?.075F:super.getSpawningChance();}
        List<SpawnListEntry> spawnList(){return spawnableMonsterList;}
    }
    private static final class Provider extends BiomeProvider {
        Provider(long seed){
            GenLayer layer=new Weighted();layer=new GenLayerZoom(1000,layer);layer=new GenLayerZoom(1001,layer);layer=GenLayerZoom.magnify(1000,layer,4);
            GenLayer voronoi=new GenLayerVoronoiZoom(10,layer);layer.initWorldGenSeed(seed);voronoi.initWorldGenSeed(seed);
            ReflectionHelper.setPrivateValue(BiomeProvider.class,this,layer,"genBiomes","field_76944_d");ReflectionHelper.setPrivateValue(BiomeProvider.class,this,voronoi,"biomeIndexLayer","field_76945_e");getBiomesToSpawnIn().clear();
        }
    }
    private static final class Weighted extends GenLayer {
        final int total;
        Weighted(){super(200);int total=wastesWeight;for(Nether biome:BIOMES)total+=biome.weight;this.total=total;}
        @Override public int[] getInts(int x,int z,int width,int height){int[] result=IntCache.getIntCache(width*height);for(int dz=0;dz<height;dz++)for(int dx=0;dx<width;dx++){initChunkSeed(x+dx,z+dz);int value=nextInt(total)-wastesWeight,id=Biome.getIdForBiome(Biomes.HELL);if(value>=0)for(Nether biome:BIOMES){value-=biome.weight;if(value<0){id=Biome.getIdForBiome(biome);break;}}result[dx+dz*width]=id;}return result;}
    }
}
