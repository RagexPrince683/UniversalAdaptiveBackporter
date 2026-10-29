package ganymedes01.etfuturum.configuration;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import java.util.*;

/** Availability controls acquisition. Registry identities and already saved entities are retained. */
public final class ContentAvailability {
 private static final Map<String, String> MOBS = new HashMap<>();
 private static final Map<Object, Boolean> CONTENT = new IdentityHashMap<>();
 static {
		MOBS.put("EntityCod", "mobs.cod");
		MOBS.put("EntitySalmon", "mobs.salmon");
		MOBS.put("EntityTropicalFish", "mobs.tropical_fish");
		MOBS.put("EntityPufferfish", "mobs.pufferfish");
		MOBS.put("EntityDolphin", "mobs.dolphin");
		MOBS.put("EntityTurtle", "mobs.turtle");
		MOBS.put("EntityAxolotl", "mobs.axolotl");
		MOBS.put("EntityGlowSquid", "mobs.glow_squid");
		MOBS.put("EntityNautilus", "mobs.nautilus");
		MOBS.put("EntityZombieNautilus", "mobs.zombie_nautilus");
		MOBS.put("EntityPiglin", "mobs.piglin");
		MOBS.put("EntityPiglinBrute", "mobs.piglin_brute");
		MOBS.put("EntityHoglin", "mobs.hoglin");
		MOBS.put("EntityZoglin", "mobs.zoglin");
		MOBS.put("EntityStrider", "mobs.strider");
		MOBS.put("EntityPillager", "mobs.pillager");
		MOBS.put("EntityVindicator", "mobs.vindicator");
		MOBS.put("EntityEvoker", "mobs.evoker");
		MOBS.put("EntityVex", "mobs.vex");
		MOBS.put("EntityIllusioner", "mobs.illusioner");
		MOBS.put("EntityRavager", "mobs.ravager");
		MOBS.put("EntityBogged", "mobs.bogged");
		MOBS.put("EntityParched", "mobs.parched");
		MOBS.put("EntityBreeze", "mobs.breeze");
		MOBS.put("EntityCat", "mobs.cat");
		MOBS.put("EntityPanda", "mobs.panda");
		MOBS.put("EntityLlama", "mobs.llama");
		MOBS.put("EntityTraderLlama", "mobs.trader_llama");
		MOBS.put("EntityWanderingTrader", "mobs.wandering_trader");
		MOBS.put("EntityAllay", "mobs.allay");
		MOBS.put("EntityWarden", "mobs.warden");
		MOBS.put("EntityCamel", "mobs.camel");
		MOBS.put("EntityCamelHusk", "mobs.camel_husk");
		MOBS.put("EntitySniffer", "mobs.sniffer");
		MOBS.put("EntityArmadillo", "mobs.armadillo");
		MOBS.put("EntityCopperGolem", "mobs.copper_golem");
		MOBS.put("EntityHappyGhast", "mobs.happy_ghast");
		MOBS.put("EntityDrowned", "entities.enableDrownedSpawning");
		MOBS.put("EntityPhantom", "entities.enablePhantomSpawning");
		MOBS.put("EntityGuardian", "entities.enableGuardians");
		MOBS.put("EntityElderGuardian", "entities.enableGuardians");
		MOBS.put("EntityParrot", "mobs.parrot");
		MOBS.put("EntityFrog", "entities.enableFrogs");
		MOBS.put("EntityTadpole", "entities.enableFrogs");
		MOBS.put("EntityRabbit", "entities.enableRabbits");
		MOBS.put("EntityFox", "entities.enableFoxes");
		MOBS.put("EntityGoat", "entities.enableGoats");
		MOBS.put("EntityPolarBear", "entities.enablePolarBears");
		MOBS.put("EntityBee", "entities.enableBees");
		MOBS.put("EntityHusk", "entities.enableHusks");
		MOBS.put("EntityStray", "entities.enableStrays");
		MOBS.put("EntityShulker", "entities.enableShulker");
		MOBS.put("EntityEndermite", "entities.enableEndermite");
		MOBS.put("EntityZombieVillager", "entities.enableZombieVillager");
		MOBS.put("EntityBrownMooshroom", "entities.enableBrownMooshroom");
		MOBS.put("EntityCreaking", "experiments.enablePaleGarden");
 }
 private ContentAvailability() { }
 public static void initialize() {
  for (FeatureRegistry.Option option : FeatureRegistry.options())
   if (option.feature && (option.source.equals("content") || option.source.equals("mobs"))) UabConfiguration.get().bool(option.source, option.legacyKey, true);
 }
 public static boolean enabled(String key) { return UabConfiguration.get().bool("content", key, true); }
 public static void register(Object object, boolean enabled) { CONTENT.put(object, enabled); }
 public static boolean mobEnabled(Class<?> type) {
  String alias = MOBS.get(type.getSimpleName());
  if (alias == null || !type.getName().startsWith("ganymedes01.etfuturum.entities.")) return true;
  int dot = alias.indexOf('.');
  return UabConfiguration.get().bool(alias.substring(0, dot), alias.substring(dot + 1), true);
 }
 public static void indexContent() {
  for (ModBlocks block : ModBlocks.values()) {
   CONTENT.put(block.get(), block.isEnabled());
   Item item = block.getItem(); if (item != null) CONTENT.put(item, block.isEnabled());
   if (!block.isEnabled()) { block.get().setCreativeTab(null); if (item != null) item.setCreativeTab(null); }
  }
  for (ModItems item : ModItems.values()) { CONTENT.put(item.get(), item.isEnabled()); if (!item.isEnabled()) item.get().setCreativeTab(null); }
 }
 public static boolean available(Object object) {
  if (object instanceof ItemStack) object = ((ItemStack) object).getItem();
  return !Boolean.FALSE.equals(CONTENT.get(object));
 }
 public static net.minecraft.entity.item.EntityItem drop(Entity source, ItemStack stack, float offset) {
  return stack != null && available(stack) ? source.entityDropItem(stack, offset) : null;
 }
}
