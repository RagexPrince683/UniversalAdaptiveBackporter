package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** The maintained ten surface biomes use Forge identities and the native 1.12 climate layer. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class SurfaceBiomes {
    enum Kind { CHERRY, MANGROVE, PALE, MEADOW, GROVE, SLOPES, JAGGED, FROZEN, STONY, BAMBOO }
    private static final List<Land> LAND=new ArrayList<>();
    private SurfaceBiomes() { }
    static void initialize(){
        add("cherry_grove","Cherry Grove",Kind.CHERRY,"generateCherryGrove",BiomeManager.BiomeType.COOL,BiomeDictionary.Type.FOREST,BiomeDictionary.Type.MOUNTAIN);
        add("mangrove_swamp","Mangrove Swamp",Kind.MANGROVE,"generateMangroveSwamp",BiomeManager.BiomeType.WARM,BiomeDictionary.Type.SWAMP,BiomeDictionary.Type.WET,BiomeDictionary.Type.HOT);
        add("pale_garden","Pale Garden",Kind.PALE,"generatePaleGarden",BiomeManager.BiomeType.WARM,BiomeDictionary.Type.FOREST,BiomeDictionary.Type.DENSE);
        add("meadow","Meadow",Kind.MEADOW,"generateMeadow",BiomeManager.BiomeType.COOL,BiomeDictionary.Type.PLAINS,BiomeDictionary.Type.MOUNTAIN);
        add("grove","Grove",Kind.GROVE,"generateGrove",BiomeManager.BiomeType.ICY,BiomeDictionary.Type.MOUNTAIN,BiomeDictionary.Type.CONIFEROUS,BiomeDictionary.Type.FOREST,BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.COLD);
        add("snowy_slopes","Snowy Slopes",Kind.SLOPES,"generateSnowySlopes",BiomeManager.BiomeType.ICY,BiomeDictionary.Type.MOUNTAIN,BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.COLD);
        add("jagged_peaks","Jagged Peaks",Kind.JAGGED,"generateJaggedPeaks",BiomeManager.BiomeType.ICY,BiomeDictionary.Type.MOUNTAIN,BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.COLD);
        add("frozen_peaks","Frozen Peaks",Kind.FROZEN,"generateFrozenPeaks",BiomeManager.BiomeType.ICY,BiomeDictionary.Type.MOUNTAIN,BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.COLD);
        add("stony_peaks","Stony Peaks",Kind.STONY,"generateStonyPeaks",BiomeManager.BiomeType.WARM,BiomeDictionary.Type.MOUNTAIN);
        add("bamboo_jungle","Bamboo Jungle",Kind.BAMBOO,"generateBambooJungle",BiomeManager.BiomeType.WARM,BiomeDictionary.Type.JUNGLE,BiomeDictionary.Type.HOT,BiomeDictionary.Type.WET);
    }
    private static void add(String id,String name,Kind kind,String flag,BiomeManager.BiomeType climate,BiomeDictionary.Type...tags){Land biome=new Land(name,kind,flag,climate,tags);biome.setRegistryName(UAB.ID,id);LAND.add(biome);}
    @SubscribeEvent public static void register(RegistryEvent.Register<Biome> event){for(Land biome:LAND){event.getRegistry().register(biome);BiomeDictionary.addTypes(biome,biome.tags);if(Features.enabled("world.enableSurfaceBiomes")&&biome.generate)BiomeManager.addBiome(biome.climate,new BiomeManager.BiomeEntry(biome,3));}}
    private static Biome.BiomeProperties properties(String name,Kind kind){
        Biome.BiomeProperties p=new Biome.BiomeProperties(name).setTemperature(.5F).setRainfall(.7F).setBaseHeight(.8F).setHeightVariation(.3F);
        switch(kind){case CHERRY:return p.setRainfall(.8F);case MANGROVE:return p.setBaseHeight(-.15F).setHeightVariation(.1F).setTemperature(.8F).setRainfall(.9F).setWaterColor(0x3A7A6A);case PALE:return p.setBaseHeight(.1F).setHeightVariation(.2F).setTemperature(.7F).setRainfall(.8F);case MEADOW:return p;case BAMBOO:return p.setBaseHeight(.1F).setHeightVariation(.2F).setTemperature(.95F).setRainfall(.9F);default:p.setBaseHeight(kind==Kind.SLOPES?1:1.3F).setHeightVariation(kind==Kind.GROVE?.3F:.5F).setRainfall(.4F);return kind==Kind.STONY?p.setTemperature(1):p.setTemperature(-.2F).setSnowEnabled();}
    }
    static void decorate(World world,Random random,int cx,int cz){
        BlockPos center=new BlockPos((cx<<4)+8,0,(cz<<4)+8);Biome biome=world.getBiome(center);if(!(biome instanceof Land))return;Land land=(Land)biome;
        int count=land.kind==Kind.PALE?8:land.kind==Kind.MANGROVE?7:land.kind==Kind.CHERRY?5:land.kind==Kind.GROVE||land.kind==Kind.BAMBOO?6:land.kind==Kind.MEADOW&&random.nextInt(100)==0?1:0;
        if(!net.minecraftforge.event.terraingen.TerrainGen.decorate(world,random,new BlockPos(cx<<4,0,cz<<4),net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.TREE))count=0;
        for(int i=0;i<count;i++){
            // The complete native tree footprint must already be loaded; never generate its neighbors.
            BlockPos pos=world.getHeight(center.add(random.nextInt(9)-4,0,random.nextInt(9)-4));if(world.getBiome(pos)!=biome)continue;
            net.minecraft.world.gen.structure.StructureBoundingBox area=new net.minecraft.world.gen.structure.StructureBoundingBox(pos.add(-8,-6,-8),pos.add(8,15,8));
            if(!world.isAreaLoaded(pos.add(-8,-6,-8),pos.add(8,15,8))||StructureReservations.vanilla(world,area)||OceanStructures.reserved(world,area)||OceanTreasure.reserved(world,area))continue;
            if(land.kind==Kind.CHERRY||land.kind==Kind.MANGROVE||land.kind==Kind.PALE){ModernTreeGenerator.generate(world,random,pos,land.kind==Kind.CHERRY?"cherry":land.kind==Kind.MANGROVE?"mangrove":"pale_oak",false);}
            else land.tree.generate(world,random,pos);
        }
        if(land.kind==Kind.PALE&&Content.available("pale_moss_block"))for(int i=0;i<64;i++){BlockPos pos=world.getHeight(new BlockPos((cx<<4)+random.nextInt(16),0,(cz<<4)+random.nextInt(16))).down();if(world.getBiome(pos)!=biome||world.getTileEntity(pos)!=null)continue;Block old=world.getBlockState(pos).getBlock();if(old!=Blocks.GRASS&&old!=Blocks.DIRT)continue;world.setBlockState(pos,Content.block("pale_moss_block").getDefaultState(),18);if(world.isAirBlock(pos.up())&&random.nextInt(3)==0)world.setBlockState(pos.up(),Content.block(Content.available("eyeblossom")&&random.nextInt(5)==0?"eyeblossom":"pale_moss_carpet").getDefaultState(),18);}
    }
    private static final class Land extends Biome {
        final Kind kind;final boolean generate;final BiomeManager.BiomeType climate;final BiomeDictionary.Type[] tags;final WorldGenAbstractTree tree;
        Land(String name,Kind kind,String flag,BiomeManager.BiomeType climate,BiomeDictionary.Type[] tags){
            super(properties(name,kind));this.kind=kind;generate=Features.enabled("world."+flag);this.climate=climate;this.tags=tags;decorator.treesPerChunk=-999;decorator.grassPerChunk=kind==Kind.PALE?0:5;decorator.flowersPerChunk=kind==Kind.MEADOW?12:0;
            tree=kind==Kind.BAMBOO?new WorldGenMegaJungle(false,10,20,Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT,BlockPlanks.EnumType.JUNGLE),Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT,BlockPlanks.EnumType.JUNGLE)):kind==Kind.MEADOW?new WorldGenTrees(false):new WorldGenTaiga2(false);
            if(kind==Kind.MANGROVE){topBlock=Content.block("mud").getDefaultState();fillerBlock=Blocks.DIRT.getDefaultState();}
            if(kind==Kind.JAGGED||kind==Kind.STONY||kind==Kind.FROZEN)topBlock=fillerBlock=(kind==Kind.FROZEN?Blocks.PACKED_ICE:Blocks.STONE).getDefaultState();
            spawnableCreatureList.clear();spawnableWaterCreatureList.clear();
            switch(kind){case CHERRY:spawnableCreatureList.add(new SpawnListEntry(EntityPig.class,1,1,2));spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class,1,1,2));rabbit();break;case MEADOW:spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class,2,2,6));spawnableCreatureList.add(new SpawnListEntry(EntityHorse.class,1,1,2));rabbit();break;case GROVE:spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class,8,4,4));rabbit();break;case SLOPES:rabbit();break;case BAMBOO:spawnableCreatureList.add(new SpawnListEntry(EntityChicken.class,10,4,4));spawnableCreatureList.add(new SpawnListEntry(EntityOcelot.class,2,1,1));break;default:break;}
        }
        private void rabbit(){spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class,2,2,3));}
        @Override public int getGrassColorAtPos(BlockPos pos){return kind==Kind.PALE?0x778272:kind==Kind.CHERRY?0xB6DB61:kind==Kind.MANGROVE?0x6A7039:super.getGrassColorAtPos(pos);}
        @Override public int getFoliageColorAtPos(BlockPos pos){return kind==Kind.PALE?0x878D76:kind==Kind.MANGROVE?0x8DB127:super.getFoliageColorAtPos(pos);}
    }
}
