package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.configuration.*;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.*;

@Mod.EventBusSubscriber(modid = UAB.ID)
public final class MobRegistry {
    public static EnumCreatureType FISH, DROWNED, DOLPHINS, AXOLOTLS, GLOW_SQUID,STRIDERS;
    public static EntityLiving.SpawnPlacementType LAVA;
    private static int turtleWeight;
    private static final Map<Class<?>, String> FEATURES = new LinkedHashMap<>();
    private static final Map<String, Class<? extends EntityLiving>> SPECIES = new LinkedHashMap<>();
    private static final Map<String, int[]> EGGS = new LinkedHashMap<>();
    private static final Map<Class<? extends EntityLiving>,Biome.SpawnListEntry> NATURAL=new LinkedHashMap<>();
    private MobRegistry() { }
    private static void add(String name, Class<? extends EntityLiving> type, int number, int primary, int secondary) {
        SPECIES.put(name, type); FEATURES.put(type, name.equals("creaking") ? "experiments.enablePaleGarden" : name.equals("brown_mooshroom") ? "entities.enableBrownMooshroom" : name.equals("drowned") || name.equals("phantom") ? "entities.enable" + Character.toUpperCase(name.charAt(0)) + name.substring(1) + "Spawning" : "mobs." + name);
        EGGS.put(name, new int[]{number, primary, secondary});
        Features.enabled(FEATURES.get(type));
    }
    public static boolean implemented(String name) { return SPECIES.containsKey(name); }
    public static boolean enabled(Class<?> type) { return !FEATURES.containsKey(type) || Features.enabled(FEATURES.get(type)); }
    private static void natural(Class<? extends EntityLiving> type,int weight,int min,int max){Biome.SpawnListEntry entry=SpawnSettings.entry(type,weight,min,max);if(entry!=null)NATURAL.put(type,entry);}
    private static void append(List<Biome.SpawnListEntry> list,Class<? extends EntityLiving> type){Biome.SpawnListEntry entry=NATURAL.get(type);if(entry!=null&&!list.contains(entry))list.add(entry);}
    public static void initialize() {
        add("cod", EntityCod.class, 35, 0xBEA085, 0xC5BCA2); add("salmon", EntitySalmon.class, 36, 0xA00F10, 0x0E8474);
        add("tropical_fish", EntityTropicalFish.class, 37, 0xEF6915, 0xFFF9F2); add("pufferfish", EntityPufferfish.class, 38, 0xF6B31B, 0x37C3F2);
        add("drowned", EntityDrowned.class, 41, 0x8FF1D7, 0x799C65); add("phantom", EntityPhantom.class, 42, 0x435990, 0x88FF00);
        add("dolphin", EntityDolphin.class, 39, 0x223B4D, 0xF9F9F9);add("turtle", EntityTurtle.class, 40, 0xE7E7E7, 0x00AFAF);
        add("axolotl",EntityAxolotl.class,43,0xFBC1E3,0xA62D74);add("glow_squid",EntityGlowSquid.class,44,0x095656,0x85F1BC);
        add("piglin",EntityPiglin.class,26,0x995F40,0xF9F3A4);add("piglin_brute",EntityPiglinBrute.class,27,0x592A10,0xF9F3A4);add("hoglin",EntityHoglin.class,28,0xC66E55,0x5F6464);add("zoglin",EntityZoglin.class,29,0xC66E55,0xE6E6E6);add("strider",EntityStrider.class,30,0x9C3436,0x4D494D);
        add("cat",EntityCat.class,57,0xEFC88E,0x957256);add("panda",EntityPanda.class,58,0xE7E7E7,0x1B1B22);
        add("trader_llama",EntityTraderLlama.class,60,0xEAA430,0x456296);add("wandering_trader",EntityWanderingTrader.class,61,0x456296,0xEAA430);
        add("creaking",EntityCreaking.class,31,0x5C584D,0xF9D147);
        add("warden",EntityWarden.class,63,0x0F4649,0x39D6E0);
        add("allay",EntityAllay.class,62,0x22BCF3,0x78F0FF);add("sniffer",EntitySniffer.class,66,0x871E0F,0x25AB70);add("armadillo",EntityArmadillo.class,67,0xAD716D,0x824E46);
        add("bogged",EntityBogged.class,53,0x8A9C72,0x314D1B);add("parched",EntityParched.class,54,0xC4AD7A,0x695D3E);add("breeze",EntityBreeze.class,56,0xAF94DF,0xA0B8E1);
        add("pillager",EntityPillager.class,46,0x532F36,0x959B9B);add("ravager",EntityRavager.class,51,0x757470,0x5B5049);
        add("brown_mooshroom",EntityBrownMooshroom.class,12,0x9B704B,0xB7B7B7);
        STRIDERS=EnumHelper.addCreatureType("EFR_STRIDER",EntityStrider.class,15,Material.LAVA,true,true);
        LAVA=EnumHelper.addEnum(EntityLiving.SpawnPlacementType.class,"EFR_LAVA",new Class<?>[0]);EntitySpawnPlacementRegistry.setPlacementType(EntityStrider.class,LAVA);
        FISH = EnumHelper.addCreatureType("EFR_FISH", EntityFish.class, UabConfiguration.get().integer("entities", "fishPopulation", 20, 0, 1000), Material.WATER, true, false);
        DROWNED = EnumHelper.addCreatureType("EFR_DROWNED", EntityDrowned.class, UabConfiguration.get().integer("entities", "drownedPopulation", 15, 0, 1000), Material.WATER, false, false);
        DOLPHINS = EnumHelper.addCreatureType("EFR_DOLPHIN",EntityDolphin.class,UabConfiguration.get().integer("entities","dolphinPopulation",5,0,1000),Material.WATER,true,false);
        AXOLOTLS=EnumHelper.addCreatureType("EFR_AXOLOTL",EntityAxolotl.class,UabConfiguration.get().integer("entities","axolotlPopulation",5,0,1000),Material.WATER,true,false);
        GLOW_SQUID=EnumHelper.addCreatureType("EFR_GLOW_SQUID",EntityGlowSquid.class,UabConfiguration.get().integer("entities","glowSquidPopulation",5,0,1000),Material.WATER,true,false);
        turtleWeight=UabConfiguration.get().integer("entities","turtleSpawnWeight",5,0,10000);
        Features.enabled("entities.enableAquaticSpawning");
        SpawnSettings.initialize();
        // WorldServer selects an entry, then validates List.contains(entry) on a second query.
        // The configured entries must retain identity across both native calls.
        NATURAL.clear();natural(EntityCat.class,3,1,2);natural(EntityPanda.class,2,1,2);natural(EntityArmadillo.class,6,2,3);natural(EntityBogged.class,15,1,3);natural(EntityParched.class,15,1,3);natural(EntityCod.class,15,3,6);natural(EntitySalmon.class,15,1,5);natural(EntityTropicalFish.class,25,4,8);natural(EntityPufferfish.class,5,1,3);
        natural(EntityPiglin.class,15,2,4);natural(EntityStrider.class,60,1,2);NetherBiomes.spawns();
        natural(EntityDrowned.class,1,1,2);natural(EntityDolphin.class,2,1,5);natural(EntityTurtle.class,turtleWeight,2,5);natural(EntityAxolotl.class,1,1,3);natural(EntityGlowSquid.class,1,2,4);
        for (Class<? extends EntityLiving> type : SPECIES.values()) if (EntityFish.class.isAssignableFrom(type) || type == EntityDrowned.class || type == EntityDolphin.class || type==EntityAxolotl.class || type==EntityGlowSquid.class) EntitySpawnPlacementRegistry.setPlacementType(type, EntityLiving.SpawnPlacementType.IN_WATER);
    }
    @SubscribeEvent public static void register(RegistryEvent.Register<EntityEntry> event) {
        for (Map.Entry<String, Class<? extends EntityLiving>> entry : SPECIES.entrySet()) {
            int[] properties = EGGS.get(entry.getKey());
            event.getRegistry().register(EntityEntryBuilder.create().entity(entry.getValue()).id(new ResourceLocation(UAB.ID, entry.getKey()), properties[0]).name(entry.getKey()).tracker(80, 3, true).egg(properties[1], properties[2]).build());
        }
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityTrident.class).id(new ResourceLocation(UAB.ID, "trident"), 45).name("trident").tracker(64, 1, true).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityCrossbowBolt.class).id(new ResourceLocation(UAB.ID,"crossbow_bolt"),75).name("crossbow_bolt").tracker(64,1,true).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityWindCharge.class).id(new ResourceLocation(UAB.ID,"wind_charge"),55).name("wind_charge").tracker(64,1,true).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityFallingDripstone.class).id(new ResourceLocation(UAB.ID,"falling_dripstone"),18).name("falling_dripstone").tracker(64,1,true).build());
    }
    @SubscribeEvent public static void potentialSpawns(WorldEvent.PotentialSpawns event) {
        if(event.getType()==STRIDERS){event.getList().clear();if(event.getWorld().provider.isNether())append(event.getList(),EntityStrider.class);return;}
        if(event.getType()==EnumCreatureType.MONSTER&&event.getWorld().getBiome(event.getPos())==net.minecraft.init.Biomes.HELL){for(Biome.SpawnListEntry entry:event.getList())if(entry.entityClass==net.minecraft.entity.monster.EntityBlaze.class||entry.entityClass==net.minecraft.entity.monster.EntityWitherSkeleton.class)return;append(event.getList(),EntityPiglin.class);return;}
        if(event.getWorld().provider.getDimension()==0){Biome habitat=event.getWorld().getBiome(event.getPos());if(event.getType()==EnumCreatureType.MONSTER){if(BiomeDictionary.hasType(habitat,BiomeDictionary.Type.SWAMP))append(event.getList(),EntityBogged.class);if(BiomeDictionary.hasType(habitat,BiomeDictionary.Type.SANDY)&&!BiomeDictionary.hasType(habitat,BiomeDictionary.Type.COLD))append(event.getList(),EntityParched.class);}else if(event.getType()==EnumCreatureType.CREATURE){if(BiomeDictionary.hasType(habitat,BiomeDictionary.Type.SWAMP)||BiomeDictionary.hasType(habitat,BiomeDictionary.Type.PLAINS)||BiomeDictionary.hasType(habitat,BiomeDictionary.Type.SANDY)||BiomeDictionary.hasType(habitat,BiomeDictionary.Type.CONIFEROUS))append(event.getList(),EntityCat.class);if(Content.available("bamboo")&&BiomeDictionary.hasType(habitat,BiomeDictionary.Type.JUNGLE))append(event.getList(),EntityPanda.class);if(BiomeDictionary.hasType(habitat,BiomeDictionary.Type.SAVANNA)||BiomeDictionary.hasType(habitat,BiomeDictionary.Type.MESA))append(event.getList(),EntityArmadillo.class);}}
        if (event.getType() != FISH && event.getType() != DROWNED && event.getType()!=DOLPHINS && event.getType()!=AXOLOTLS && event.getType()!=GLOW_SQUID && event.getType()!=EnumCreatureType.CREATURE) return;
        if(event.getType()!=EnumCreatureType.CREATURE)event.getList().clear();
        if (event.getWorld().provider.getDimension() != 0) return;
        Biome biome = event.getWorld().getBiome(event.getPos());
        boolean ocean = BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN), river = BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER);
        if(event.getType()==EnumCreatureType.CREATURE){if(Features.enabled("entities.enableAquaticSpawning")&&BiomeDictionary.hasType(biome,BiomeDictionary.Type.BEACH)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.COLD))append(event.getList(),EntityTurtle.class);return;}
        if (event.getType() == DROWNED) { if (ocean || river) append(event.getList(), EntityDrowned.class); return; }
        if (!Features.enabled("entities.enableAquaticSpawning")) return;
        if(event.getType()==DOLPHINS){if(ocean&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.SNOWY))append(event.getList(),EntityDolphin.class);return;}
        if(event.getType()==AXOLOTLS){append(event.getList(),EntityAxolotl.class);return;}
        if(event.getType()==GLOW_SQUID){append(event.getList(),EntityGlowSquid.class);return;}
        if (river || ocean && biome.getDefaultTemperature() < .3F) append(event.getList(), EntitySalmon.class);
        else if (ocean && biome.getDefaultTemperature() > .8F) { append(event.getList(), EntityTropicalFish.class);append(event.getList(), EntityPufferfish.class); }
        else if (ocean) append(event.getList(), EntityCod.class);
    }
}
