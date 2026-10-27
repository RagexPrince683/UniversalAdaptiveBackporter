package ganymedes01.etfuturum;

import ganymedes01.etfuturum.configuration.UabConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = UAB.ID, name = "Universal Adaptive Backporter", version = BuildVersion.VERSION, useMetadata = true, acceptedMinecraftVersions = "[1.12.2]",
    guiFactory = "ganymedes01.etfuturum.client.ConfigGuiFactory",
    dependencies = "required-after:forge@[14.23.5.2859,);required-after:mixinbooter@[11.18,)")
public final class UAB {
    public static final String ID = "etfuturum";
    @Mod.Instance(ID) public static UAB instance;
    @SidedProxy(clientSide = "ganymedes01.etfuturum.client.ClientProxy", serverSide = "ganymedes01.etfuturum.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        UabConfiguration.open(event.getModConfigurationDirectory(), event.getSide().isClient(), "1.12.2");
        for(ganymedes01.etfuturum.configuration.FeatureRegistry.Option option:ganymedes01.etfuturum.configuration.FeatureRegistry.options())if(option.feature)Features.enabled(option.alias);
        BoatSettings.initialize();
        ItemPhysics.initialize();
        DamageDirection.initialize();
        ModernBlockSounds.initialize();
        StorageRules.initialize();
        Content.define();
        ModernStructures.initialize();
        TagDefaults.initialize();
        OceanBiomes.initialize();
        SurfaceBiomes.initialize();
        NetherBiomes.initialize();
        MobRegistry.initialize();
        AquaticPopulation.initialize();
        MinecraftForge.EVENT_BUS.register(new Gameplay());
        ModernLoot.initialize();
        GameRegistry.registerWorldGenerator(new WorldGeneration(), 10);
        proxy.preInit(event);
    }
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        Crafting.initialize();
        proxy.init();
    }
    @Mod.EventHandler public void postInit(FMLPostInitializationEvent event) {
        StorageRules.resolve();
        OreCompatibility.initialize();
        IronStorage.initialize();
        LegacySpawns.initialize();
        MendingCompatibility.initialize();
        TagDefaults.dynamic();
        UabConfiguration.get().finish();
        ConnectionSettings.capture();
    }
    @net.minecraftforge.fml.common.network.NetworkCheckHandler
    public boolean checkNetwork(java.util.Map<String, String> remote, net.minecraftforge.fml.relauncher.Side side) {
        return ConnectionSettings.matches(remote, side);
    }
    @Mod.EventHandler public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new LocateCommand());
    }
}
