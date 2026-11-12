package ganymedes01.etfuturum.compat;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.api.StrippedLogRegistry;
import ganymedes01.etfuturum.blocks.BaseDoor;
import ganymedes01.etfuturum.blocks.BaseTrapdoor;
import ganymedes01.etfuturum.blocks.BlockWoodButton;
import ganymedes01.etfuturum.blocks.BlockWoodPressurePlate;
import ganymedes01.etfuturum.blocks.BlockWoodSign;
import ganymedes01.etfuturum.blocks.itemblocks.ItemBlockSign;
import ganymedes01.etfuturum.tileentities.TileEntityWoodSign;
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
        for (ExternalWoodFamily family : FAMILIES) if (!ContentCompatibility.enabled(family.prefix)) {
            for (Block block : new Block[]{family.strippedLog,family.wood,family.strippedWood,family.fence,family.button,family.plate,family.door,family.trapdoor,family.sign}) if (block != null) {
                block.setCreativeTab(null); ganymedes01.etfuturum.configuration.ContentAvailability.register(block,false);
                Item item=Item.getItemFromBlock(block);if(item!=null){item.setCreativeTab(null);ganymedes01.etfuturum.configuration.ContentAvailability.register(item,false);}
            }
            for(Item item:new Item[]{family.boat,family.chestBoat})if(item!=null){item.setCreativeTab(null);ganymedes01.etfuturum.configuration.ContentAvailability.register(item,false);}
        }

	}

    private static void initBop() { initFamilies("bop"); }
    private static void initWitchery() { initFamilies("witchery"); }
    private static void initFamilies(String provider) {
        for (ganymedes01.etfuturum.compatibility.WoodFamilies.Family family : ganymedes01.etfuturum.compatibility.WoodFamilies.forTarget("1.7.10")) {
            if (!family.provider.equals(provider)) continue;
            Block planks = resolve(family.planks), log = resolve(family.log);
            addFamily(provider, provider.equals("bop") ? "biomesoplenty" : provider, family.name,
                    planks, family.plankMeta, log, family.logMeta, family.name.equals("bamboo"));
        }
    }
    private static Block resolve(String id) {
        if (id == null) return null;
        String[] parts = id.split(":", 2); return requiredBlock(parts[0], parts[1], id);
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
		// EFR's inherited assets use the original BOP species spellings.
		String textureName = name.equals("sacred_oak") ? "sacredoak" : name.equals("big_flower_stem") ? "bigflowerstem" : name;
		String strippedTexture = textureDomain + ":stripped_" + textureName + "_log";

		if (log != null && ConfigBlocksItems.enableStrippedLogs) {
			family.strippedLog = namedLog(log, logMeta, false, id + "_stripped_log", strippedTexture);
			if (ConfigBlocksItems.enableBarkLogs) family.strippedWood = namedLog(log, logMeta, true, id + "_stripped_wood", strippedTexture);
		}
		if (log != null && ConfigBlocksItems.enableBarkLogs) family.wood = namedLog(log, logMeta, true, id + "_wood", null);
		if (planks == null) return;
		if (ConfigBlocksItems.enableNewSigns) {
			family.sign = new BlockWoodSign(TileEntityWoodSign.class, true, id, planks, plankMeta);
			BlockWoodSign wall = new BlockWoodSign(TileEntityWoodSign.class, false, id, planks, plankMeta);
			family.sign.textureDomain = wall.textureDomain = textureDomain;
			family.sign.textureType = wall.textureType = textureName;
			GameRegistry.registerBlock(family.sign, ItemBlockSign.class, id + "_sign");
			GameRegistry.registerBlock(wall, (Class<? extends net.minecraft.item.ItemBlock>) null, id + "_wall_sign");
		}

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
			family.door.setBlockTextureName(textureDomain + ':' + textureName + "_door");
			GameRegistry.registerBlock(family.door, ItemBlockNewDoor.class, id + "_door");
		}
		if (ConfigBlocksItems.enableNewTrapdoors) {
			family.trapdoor = new BaseTrapdoor(id);
			family.trapdoor.setBlockTextureName(textureDomain + ':' + textureName + "_trapdoor");
			registerBlock(family.trapdoor, id + "_trapdoor");
		}
		if (ConfigBlocksItems.enableNewBoats) {
			family.boat = new ItemNewBoat(prefix, name, () -> Item.getItemFromBlock(planks), plankMeta, false, raft, textureDomain, textureName);
			family.chestBoat = new ItemNewBoat(prefix, name, () -> Item.getItemFromBlock(planks), plankMeta, true, raft, textureDomain, textureName);
			family.boat.setUnlocalizedName("etfuturum." + id + (raft ? "_raft" : "_boat"));
			family.chestBoat.setUnlocalizedName("etfuturum." + id + (raft ? "_chest_raft" : "_chest_boat"));
			GameRegistry.registerItem(family.boat, id + (raft ? "_raft" : "_boat"));
			GameRegistry.registerItem(family.chestBoat, id + (raft ? "_chest_raft" : "_chest_boat"));
		}
	}

	private static Block namedLog(Block source, int meta, boolean allSides, String id, String strippedTexture) {
		Block block = new BlockExternalWoodLog(source, meta, allSides, strippedTexture).setBlockName("etfuturum." + id);
		registerBlock(block, id);
		return block;
	}

	private static void registerBlock(Block block, String id) {
		GameRegistry.registerBlock(block, id);
	}

	public static void initRecipesAndStripping() {
		for (ExternalWoodFamily family : FAMILIES) {
            if (!ContentCompatibility.enabled(family.prefix)) continue;
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
			if (family.sign != null) GameRegistry.addShapedRecipe(new ItemStack(family.sign, 3), "xxx", "xxx", " y ", 'x', plank, 'y', Items.stick);
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
		BlockWoodSign sign;
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
