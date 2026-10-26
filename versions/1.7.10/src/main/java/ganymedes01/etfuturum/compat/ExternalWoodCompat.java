package ganymedes01.etfuturum.compat;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.api.StrippedLogRegistry;
import ganymedes01.etfuturum.blocks.BaseDoor;
import ganymedes01.etfuturum.blocks.BaseTrapdoor;
import ganymedes01.etfuturum.blocks.BlockWoodButton;
import ganymedes01.etfuturum.blocks.BlockWoodPressurePlate;
import ganymedes01.etfuturum.blocks.compat.BlockExternalWoodFence;
import ganymedes01.etfuturum.blocks.compat.BlockExternalWoodLog;
import ganymedes01.etfuturum.blocks.itemblocks.ItemBlockNewDoor;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.core.utils.Logger;
import ganymedes01.etfuturum.items.ItemNewBoat;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

/** Explicit, registry-only compatibility for the 1.7.10 BOP and Witchery wood sets. */
public final class ExternalWoodCompat {

	private static final List<ExternalWoodFamily> FAMILIES = new ArrayList<>();
	private static boolean contentInitialized;

	private ExternalWoodCompat() {
	}

	public static void initContent() {
		if (contentInitialized) return;
		contentInitialized = true;
		if (ModsList.BIOMES_O_PLENTY.isLoaded()) initBop();
		if (ModsList.WITCHERY.isLoaded()) initWitchery();
	}

	private static void initBop() {
		Block planks = requiredBlock("BiomesOPlenty", "planks", "BOP plank families");
		Block[] logs = new Block[4];
		for (int i = 0; i < logs.length; i++) logs[i] = requiredBlock("BiomesOPlenty", "logs" + (i + 1), "BOP log group " + (i + 1));
		String[] names = {"sacred_oak", "cherry", "dark", "fir", "ethereal", "magic", "mangrove", "palm", "redwood", "willow", "bamboo", "pine", "hellbark", "jacaranda", "mahogany"};
		for (int meta = 0; meta < names.length; meta++) {
			Block log = meta == 10 ? null : logs[meta < 10 ? meta / 4 : 3];
			int logMeta = meta < 10 ? meta % 4 : meta - 11;
			addFamily("bop", "biomesoplenty", names[meta], planks, meta, log, logMeta, meta == 10);
		}
		// BOP exposes these as logs but has no corresponding plank species.
		addFamily("bop", "biomesoplenty", "dead", null, -1, logs[2], 2, false);
		addFamily("bop", "biomesoplenty", "big_flower_stem", null, -1, logs[2], 3, false);
	}

	private static void initWitchery() {
		Block logs = requiredBlock("witchery", "witchlog", "Witchery logs");
		Block planks = requiredBlock("witchery", "witchwood", "Witchery planks");
		String[] names = {"rowan", "alder", "hawthorn"};
		for (int meta = 0; meta < names.length; meta++) {
			addFamily("witchery", "witchery", names[meta], planks, meta, logs, meta, false);
		}
	}

	private static Block requiredBlock(String mod, String name, String feature) {
		Block block = GameRegistry.findBlock(mod, name);
		if (block == null) Logger.warn("Skipping " + feature + ": missing registry block " + mod + ':' + name);
		return block;
	}

	private static void addFamily(String prefix, String textureDomain, String name, Block planks, int plankMeta, Block log, int logMeta, boolean raft) {
		if (planks == null && log == null) return;
		ExternalWoodFamily family = new ExternalWoodFamily(prefix, textureDomain, name, planks, plankMeta, log, logMeta, raft);
		FAMILIES.add(family);
		String id = prefix + '_' + name;

		if (log != null && ConfigBlocksItems.enableStrippedLogs) {
			family.strippedLog = namedLog(log, logMeta, false, id + "_stripped_log");
			if (ConfigBlocksItems.enableBarkLogs) family.strippedWood = namedLog(log, logMeta, true, id + "_stripped_wood");
		}
		if (log != null && ConfigBlocksItems.enableBarkLogs) family.wood = namedLog(log, logMeta, true, id + "_wood");
		if (planks == null) return;

		if (ConfigBlocksItems.enableNewFences) {
			family.fence = new BlockExternalWoodFence(planks, plankMeta);
			registerBlock(family.fence, id + "_fence");
		}
		// Mod woods are post-1.7 completeness content, so the new-wood redstone toggle is authoritative.
		if (ConfigBlocksItems.enableNewWoodRedstone) {
			family.button = new BlockWoodButton(id, planks, plankMeta, true);
			family.plate = new BlockWoodPressurePlate(id, planks, plankMeta, true);
			registerBlock(family.button, id + "_button");
			registerBlock(family.plate, id + "_pressure_plate");
		}
		if (ConfigBlocksItems.enableNewDoors) {
			family.door = new BaseDoor(id);
			family.door.setBlockTextureName(textureDomain + ':' + id + "_door");
			GameRegistry.registerBlock(family.door, ItemBlockNewDoor.class, id + "_door");
		}
		if (ConfigBlocksItems.enableNewTrapdoors) {
			family.trapdoor = new BaseTrapdoor(id);
			family.trapdoor.setBlockTextureName(textureDomain + ':' + id + "_trapdoor");
			registerBlock(family.trapdoor, id + "_trapdoor");
		}
		if (ConfigBlocksItems.enableNewBoats) {
			family.boat = new ItemNewBoat(prefix, name, () -> Item.getItemFromBlock(planks), plankMeta, false, raft);
			family.chestBoat = new ItemNewBoat(prefix, name, () -> Item.getItemFromBlock(planks), plankMeta, true, raft);
			family.boat.setUnlocalizedName("etfuturum." + id + (raft ? "_raft" : "_boat"));
			family.chestBoat.setUnlocalizedName("etfuturum." + id + (raft ? "_chest_raft" : "_chest_boat"));
			GameRegistry.registerItem(family.boat, id + (raft ? "_raft" : "_boat"));
			GameRegistry.registerItem(family.chestBoat, id + (raft ? "_chest_raft" : "_chest_boat"));
		}
	}

	private static Block namedLog(Block source, int meta, boolean allSides, String id) {
		Block block = new BlockExternalWoodLog(source, meta, allSides).setBlockName("etfuturum." + id);
		registerBlock(block, id);
		return block;
	}

	private static void registerBlock(Block block, String id) {
		GameRegistry.registerBlock(block, id);
	}

	public static void initRecipesAndStripping() {
		for (ExternalWoodFamily family : FAMILIES) {
			if (family.strippedLog != null) {
				StrippedLogRegistry.addLog(family.log, family.logMeta, family.strippedLog, 0);
				OreDictionary.registerOre("logWood", new ItemStack(family.strippedLog, 1, OreDictionary.WILDCARD_VALUE));
			}
			if (family.wood != null) {
				OreDictionary.registerOre("logWood", new ItemStack(family.wood, 1, OreDictionary.WILDCARD_VALUE));
				GameRegistry.addShapedRecipe(new ItemStack(family.wood, 3), "xx", "xx", 'x', new ItemStack(family.log, 1, family.logMeta));
			}
			if (family.strippedWood != null) {
				StrippedLogRegistry.addLog(family.wood, 0, family.strippedWood, 0);
				OreDictionary.registerOre("logWood", new ItemStack(family.strippedWood, 1, OreDictionary.WILDCARD_VALUE));
			}
			if (family.planks == null) continue;
			ItemStack plank = new ItemStack(family.planks, 1, family.plankMeta);
			if (family.fence != null) GameRegistry.addShapedRecipe(new ItemStack(family.fence, 3), "xyx", "xyx", 'x', plank, 'y', Items.stick);
			if (family.button != null) ganymedes01.etfuturum.api.utils.RecipeHelper.addShapelessRecipe(new ItemStack(family.button), plank);
			if (family.plate != null) GameRegistry.addShapedRecipe(new ItemStack(family.plate), "xx", 'x', plank);
			if (family.door != null) GameRegistry.addShapedRecipe(new ItemStack(family.door, 3), "xx", "xx", "xx", 'x', plank);
			if (family.trapdoor != null) GameRegistry.addShapedRecipe(new ItemStack(family.trapdoor, 2), "xxx", "xxx", 'x', plank);
			if (family.boat != null) {
				GameRegistry.addShapedRecipe(new ItemStack(family.boat), "x x", "xxx", 'x', plank);
				ganymedes01.etfuturum.api.utils.RecipeHelper.addShapelessRecipe(new ItemStack(family.chestBoat), Blocks.chest, family.boat);
			}
		}
	}

	private static final class ExternalWoodFamily {
		final String prefix, textureDomain, name;
		final Block planks, log;
		final int plankMeta, logMeta;
		final boolean raft;
		Block strippedLog, wood, strippedWood, fence, button, plate, door, trapdoor;
		Item boat, chestBoat;

		ExternalWoodFamily(String prefix, String textureDomain, String name, Block planks, int plankMeta, Block log, int logMeta, boolean raft) {
			this.prefix = prefix;
			this.textureDomain = textureDomain;
			this.name = name;
			this.planks = planks;
			this.plankMeta = plankMeta;
			this.log = log;
			this.logMeta = logMeta;
			this.raft = raft;
		}
	}
}
