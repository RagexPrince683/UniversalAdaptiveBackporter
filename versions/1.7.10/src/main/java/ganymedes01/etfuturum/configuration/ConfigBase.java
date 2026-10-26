package ganymedes01.etfuturum.configuration;

import ganymedes01.etfuturum.configuration.configs.*;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Property;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/** Compatibility field bindings. UabConfiguration alone owns configuration state and IO. */
public abstract class ConfigBase {
 public static final String configDir = "config" + File.separator + "etfuturum" + File.separator;
 public static final ConfigBase EXPERIMENTS = new ConfigExperiments();
 public static final ConfigBase BLOCKS_ITEMS = new ConfigBlocksItems();
 public static final ConfigBase ENCHANTS_POTIONS = new ConfigEnchantsPotions();
 public static final ConfigBase FUNCTIONS = new ConfigFunctions();
 public static final ConfigBase TWEAKS = new ConfigTweaks();
 public static final ConfigBase WORLD = new ConfigWorld();
 public static final ConfigBase ENTITIES = new ConfigEntities();
 public static final ConfigBase SOUNDS = new ConfigSounds();
 public static final ConfigBase MOD_COMPAT = new ConfigModCompat();
 public static final ConfigBase MIXINS = new ConfigMixins();
 private static final List<ConfigBase> BINDINGS = Arrays.asList(EXPERIMENTS, BLOCKS_ITEMS, ENCHANTS_POTIONS, FUNCTIONS, TWEAKS, WORLD, ENTITIES, SOUNDS, MOD_COMPAT, MIXINS);
 private static boolean initialized;
 private final String source;
 protected ConfigBase(String source) { this.source = source; }
 public static synchronized void initializeConfigs() {
  if (initialized) return;
  UabConfiguration.open(new File(Launch.minecraftHome, "config"), ganymedes01.etfuturum.mixinplugin.EtFuturumEarlyMixins.side == org.spongepowered.asm.mixin.MixinEnvironment.Side.CLIENT);
  for (ConfigBase binding : BINDINGS) binding.syncConfigOptions();
  ContentAvailability.initialize();
  SpawnSettings.initialize();
  UabConfiguration.get().finish();
  initialized = true;
 }
 public static synchronized void reloadClient() {
  if (!initialized) return;
  UabConfiguration.get().reloadClient();
  for (ConfigBase binding : BINDINGS) binding.syncConfigOptions();
  UabConfiguration.get().finish();
 }
 protected abstract void syncConfigOptions();
 protected void initValues() { }
 protected void onConstructingValues() { }
 public static void init() { for (ConfigBase binding : BINDINGS) binding.initValues(); }
 public static void onConstructing() { for (ConfigBase binding : BINDINGS) binding.onConstructingValues(); }
 protected boolean getBoolean(String name, String category, boolean value, String ignored) { return UabConfiguration.get().bool(source, name, value); }
 protected int getInt(String name, String category, int value, int min, int max, String ignored) { return UabConfiguration.get().integer(source, name, value, min, max); }
 protected float getFloat(String name, String category, float value, float min, float max, String ignored) { return UabConfiguration.get().decimal(source, name, value, min, max); }
 protected String getString(String name, String category, String value, String ignored) { return get(category, name, value).getString(); }
 protected String[] getStringList(String name, String category, String[] value, String ignored) { return get(category, name, value).getStringList(); }
 protected Property get(String category, String name, String value) { return UabConfiguration.get().property(source, name, value, Property.Type.STRING); }
 protected Property get(String category, String name, int[] value) { return UabConfiguration.get().property(source, name, value, Property.Type.INTEGER); }
 protected Property get(String category, String name, String[] value) { return UabConfiguration.get().property(source, name, value, Property.Type.STRING); }
}
