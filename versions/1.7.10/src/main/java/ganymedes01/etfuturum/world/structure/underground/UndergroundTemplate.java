package ganymedes01.etfuturum.world.structure.underground;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.core.utils.structurenbt.*;
import ganymedes01.etfuturum.tileentities.TileEntityTrialSpawner;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;
import java.io.*;
import java.util.*;

/** Immutable released template, translated at its native block-state boundary. */
public final class UndergroundTemplate {
	final int sx, sy, sz;
	final List<NBTTagCompound> connectors = new ArrayList<>();
	private final List<String> names = new ArrayList<>();
	private final List<Map<String, String>> properties = new ArrayList<>();
	private final NBTTagList blocks;
	private final BlockStateContainer[][] states;
	UndergroundTemplate(String id) {
		NBTTagCompound data;
		try (InputStream in = getClass().getResourceAsStream("/data/structure/" + id + ".nbt")) {
			if (in == null) throw new IllegalArgumentException("Missing underground template " + id);
			data = CompressedStreamTools.readCompressed(in);
		} catch (IOException e) { throw new IllegalStateException(id, e); }
		BlockPos size = NBTStructure.getPosFromTagList(data.getTagList("size", 3)); sx = size.getX(); sy = size.getY(); sz = size.getZ();
		NBTTagList palette = data.getTagList("palette", 10);
		for (int i = 0; i < palette.tagCount(); i++) { NBTTagCompound entry = palette.getCompoundTagAt(i); names.add(entry.getString("Name")); properties.add(NBTStructure.getProperties(entry)); }
		blocks = data.getTagList("blocks", 10);
		for (int i = 0; i < blocks.tagCount(); i++) { NBTTagCompound entry = blocks.getCompoundTagAt(i); if (names.get(entry.getInteger("state")).equals("minecraft:jigsaw")) connectors.add(entry); }
		states = new BlockStateContainer[4][names.size()];
		for (int r = 0; r < 4; r++) for (int i = 0; i < names.size(); i++) states[r][i] = state(names.get(i), properties.get(i), r);
		for (NBTTagCompound connector : connectors) {
			String finalState = connector.getCompoundTag("nbt").getString("final_state");
			if (!finalState.isEmpty() && !finalState.equals("minecraft:structure_void")) for (int r = 0; r < 4; r++) state(finalState, Collections.emptyMap(), r);
		}
	}
	static BlockPos rotate(int x, int y, int z, int r) { switch (r & 3) { case 1: return new BlockPos(-z, y, x); case 2: return new BlockPos(-x, y, -z); case 3: return new BlockPos(z, y, -x); default: return new BlockPos(x, y, z); } }
	static BlockPos position(NBTTagCompound entry, int r) { BlockPos p = NBTStructure.getPosFromTagList(entry.getTagList("pos", 3)); return rotate(p.getX(), p.getY(), p.getZ(), r); }
	BlockPos direction(NBTTagCompound connector, int rotation, boolean top) {
		String[] orientation = properties.get(connector.getInteger("state")).get("orientation").split("_");
		switch (orientation[top ? 1 : 0]) { case "up": return new BlockPos(0, 1, 0); case "down": return new BlockPos(0, -1, 0); case "north": return rotate(0, 0, -1, rotation); case "south": return rotate(0, 0, 1, rotation); case "east": return rotate(1, 0, 0, rotation); default: return rotate(-1, 0, 0, rotation); }
	}
	StructureBoundingBox box(int x, int y, int z, int rotation) {
		BlockPos end = rotate(sx - 1, sy - 1, sz - 1, rotation);
		return new StructureBoundingBox(x + Math.min(0, end.getX()), y, z + Math.min(0, end.getZ()), x + Math.max(0, end.getX()), y + sy - 1, z + Math.max(0, end.getZ()));
	}
	public static BlockStateContainer state(String id, Map<String, String> original, int rotation) {
		String name = id.replace("minecraft:", ""); Map<String, String> props = new HashMap<>(original);
		int bracket = name.indexOf('[');
		if (bracket >= 0) { for (String part : name.substring(bracket + 1, name.length() - 1).split(",")) { String[] pair = part.split("="); props.put(pair[0], pair[1]); } name = name.substring(0, bracket); }
		String[] cardinal = {"north", "east", "south", "west"};
		if (props.containsKey("facing")) for (int i = 0; i < 4; i++) if (cardinal[i].equals(props.get("facing"))) { props.put("facing", cardinal[(i + rotation) & 3]); break; }
		if ((rotation & 1) != 0 && ("x".equals(props.get("axis")) || "z".equals(props.get("axis")))) props.put("axis", "x".equals(props.get("axis")) ? "z" : "x");
		Block block = null; int subtype = 0; boolean direct = false;
		if (name.equals("air") || name.equals("cave_air") || name.equals("jigsaw") || name.equals("structure_block")) return new BlockStateContainer(Blocks.air);
		if (name.equals("dark_oak_fence")) block = enabled(ModBlocks.FENCE_DARK_OAK);
		else if (name.equals("magma_block")) block = enabled(ModBlocks.MAGMA);
		else if (name.equals("mangrove_leaves")) { block = enabled(ModBlocks.LEAVES); subtype = 4; direct = true; }
		else if (name.equals("mangrove_wood")) { block = enabled(ModBlocks.MANGROVE_LOG); subtype = 1; }
		else if (name.equals("polished_andesite")) { block = enabled(ModBlocks.STONE); subtype = 6; direct = true; }
		else if (name.equals("red_concrete") || name.equals("white_concrete")) { block = enabled(ModBlocks.CONCRETE); subtype = name.startsWith("red_") ? 14 : 0; direct = true; }
		else if (name.equals("reinforced_deepslate")) block = enabled(ModBlocks.REINFORCED_DEEPSLATE);
		else if (name.equals("sculk_sensor")) { block = enabled(ModBlocks.SCULK_SENSOR); direct = true; }
		else if (name.equals("trial_spawner")) { block = enabled(ModBlocks.TRIAL_SPAWNER); direct = true; }
		else if (name.equals("vault")) { block = enabled(ModBlocks.VAULT); direct = true; subtype = directionIndex(props.get("facing")) | (Boolean.parseBoolean(props.get("ominous")) ? 4 : 0); }
		else if (name.endsWith("candle")) { block = enabled(ModBlocks.CANDLE); direct = true; subtype = Boolean.parseBoolean(props.get("lit")) ? 4 : 0; }
		else if (name.equals("decorated_pot")) { block = enabled(ModBlocks.DECORATED_POT); direct = true; subtype = directionIndex(props.get("facing")); }
		else if (name.equals("torch")) { block = Blocks.torch; subtype = 5; direct = true; }
		else if (name.equals("wall_torch")) { block = Blocks.torch; subtype = "east".equals(props.get("facing")) ? 1 : "west".equals(props.get("facing")) ? 2 : "south".equals(props.get("facing")) ? 3 : 4; direct = true; }
		else if (name.equals("chiseled_deepslate")) { block = enabled(ModBlocks.DEEPSLATE_BRICKS); subtype = 4; }
		else if (name.equals("cracked_deepslate_bricks")) { block = enabled(ModBlocks.DEEPSLATE_BRICKS); subtype = 1; }
		else if (name.equals("deepslate_tiles")) { block = enabled(ModBlocks.DEEPSLATE_BRICKS); subtype = 2; }
		else if (name.equals("cracked_deepslate_tiles")) { block = enabled(ModBlocks.DEEPSLATE_BRICKS); subtype = 3; }
		else if (name.equals("cobbled_deepslate_slab") || name.equals("polished_deepslate_slab")) { block = slab(props, ModBlocks.DEEPSLATE_SLAB, ModBlocks.DOUBLE_DEEPSLATE_SLAB); subtype = name.startsWith("polished") ? 1 : 0; }
		else if (name.equals("deepslate_brick_slab") || name.equals("deepslate_tile_slab")) { block = slab(props, ModBlocks.DEEPSLATE_BRICK_SLAB, ModBlocks.DOUBLE_DEEPSLATE_BRICK_SLAB); subtype = name.contains("tile") ? 1 : 0; }
		else if (name.equals("cobbled_deepslate_wall") || name.equals("polished_deepslate_wall")) { block = enabled(ModBlocks.DEEPSLATE_WALL); subtype = name.startsWith("polished") ? 1 : 0; }
		else if (name.equals("deepslate_tile_wall")) { block = enabled(ModBlocks.DEEPSLATE_BRICK_WALL); subtype = 1; }
		else if (name.equals("polished_tuff") || name.equals("tuff_bricks") || name.equals("chiseled_tuff") || name.equals("chiseled_tuff_bricks")) { block = enabled(ModBlocks.TUFF); subtype = name.equals("polished_tuff") ? 1 : name.equals("tuff_bricks") ? 2 : name.equals("chiseled_tuff") ? 3 : 4; }
		else if (name.equals("polished_tuff_slab")) { block = slab(props, ModBlocks.TUFF_SLAB, ModBlocks.DOUBLE_TUFF_SLAB); subtype = 1; }
		else if (name.equals("polished_basalt")) { block = enabled(ModBlocks.BASALT); subtype = 1; }
		else if (name.equals("smooth_basalt")) block = enabled(ModBlocks.SMOOTH_BASALT);
		else if (name.contains("copper")) {
			boolean wax = name.startsWith("waxed_"); String clean = name.replace("waxed_", ""); int age = clean.contains("oxidized") ? 3 : clean.contains("weathered") ? 2 : clean.contains("exposed") ? 1 : 0;
			if (clean.endsWith("stairs") || clean.endsWith("door")) { block = GameRegistry.findBlock("etfuturum", name); }
			else if (clean.endsWith("slab")) { block = slab(props, ModBlocks.CUT_COPPER_SLAB, ModBlocks.DOUBLE_CUT_COPPER_SLAB); subtype = age + (wax ? 4 : 0); }
			else if (clean.endsWith("bulb")) { block = Boolean.parseBoolean(props.get("powered")) ? ModBlocks.POWERED_COPPER_BULB.get() : ModBlocks.COPPER_BULB.get(); subtype = age + (wax ? 8 : 0) + (Boolean.parseBoolean(props.get("lit")) ? 4 : 0); direct = true; }
			else if (clean.contains("chiseled")) { block = ModBlocks.CHISELED_COPPER.get(); subtype = age + (wax ? 4 : 0); }
			else if (clean.endsWith("grate")) { block = ModBlocks.COPPER_GRATE.get(); subtype = age + (wax ? 4 : 0); }
			else { block = ModBlocks.COPPER_BLOCK.get(); subtype = age + (wax ? 8 : 0) + (clean.contains("cut_") ? 4 : 0); }
		}
		else if (name.equals("powder_snow")) block = Blocks.snow; // native solid snow adaptation
		else if (name.equals("lectern")) block = Blocks.bookshelf; // decorative; no book UI in legacy
		else if (name.equals("campfire")) block = Blocks.air; // preserve city clearance; no campfire smoke/fuel backport
		else if (name.equals("target")) block = enabled(ModBlocks.TARGET);
		else if (name.equals("soul_fire")) block = ganymedes01.etfuturum.configuration.configs.ConfigMixins.soulFire ? Blocks.fire : Blocks.air;
		else if (name.equals("iron_chain")) { block = enabled(ModBlocks.CHAIN); subtype = "x".equals(props.get("axis")) ? 1 : "z".equals(props.get("axis")) ? 2 : 0; direct = true; }
		else if (name.equals("soul_lantern")) { block = enabled(ModBlocks.SOUL_LANTERN); subtype = Boolean.parseBoolean(props.get("hanging")) ? 1 : 0; direct = true; }
		if (block == null) { try { block = enabled(ModBlocks.valueOf(name.toUpperCase(java.util.Locale.ROOT))); } catch (IllegalArgumentException ignored) { } }
		if (block == null) block = EFRBlockStateConverter.INSTANCE.getBlockFromNamespace("minecraft:" + name, props);
		if (block == null || block == Blocks.stone && !name.equals("stone")) throw new IllegalArgumentException("Unmapped underground template block " + name);
		int meta = direct ? subtype : EFRBlockStateConverter.INSTANCE.getMetaFromStateWithSubtypeAdditions("minecraft:" + name, props, ForgeDirection.NORTH) + subtype;
		return new BlockStateContainer(block, meta & 15);
	}
	private static int directionIndex(String facing) { return "east".equals(facing) ? 1 : "south".equals(facing) ? 2 : "west".equals(facing) ? 3 : 0; }
	private static Block enabled(ModBlocks block) { return block.isEnabled() ? block.get() : Blocks.air; }
	private static Block slab(Map<String, String> p, ModBlocks single, ModBlocks both) { return enabled("double".equals(p.get("type")) ? both : single); }
	void place(World world, int x, int y, int z, int rotation, StructureBoundingBox clip, long seed, String processors) {
		boolean previous = UndergroundStructures.beginTemplate();
		try {
			for (int i = 0; i < blocks.tagCount(); i++) {
				NBTTagCompound entry = blocks.getCompoundTagAt(i); BlockPos p = position(entry, rotation);
				int wx = x + p.getX(), wy = y + p.getY(), wz = z + p.getZ();
				if (!clip.isVecInside(wx, wy, wz)) continue;
				int index = entry.getInteger("state"); String name = names.get(index); NBTTagCompound nbt = entry.getCompoundTag("nbt");
				if (name.equals("minecraft:structure_block") || name.equals("minecraft:structure_void") || name.equals("minecraft:jigsaw") && nbt.getString("final_state").equals("minecraft:structure_void")) continue;
				BlockStateContainer s = name.equals("minecraft:jigsaw") ? state(nbt.getString("final_state").isEmpty() ? "minecraft:air" : nbt.getString("final_state"), Collections.emptyMap(), rotation) : states[rotation][index];
				if (world.getBlock(wx, wy, wz) == Blocks.bedrock) continue;
				Random random = new Random(seed ^ (long) wx * 3129871 ^ (long) wz * 116129781L ^ wy);
				s = UndergroundProcessors.apply(processors, name, s, rotation, random); if (s == null) continue;
				world.setBlock(wx, wy, wz, s.getBlock(), s.getMeta(), 2);
				TileEntity tile = world.getTileEntity(wx, wy, wz);
				if (tile instanceof IInventory inventory) {
					if (nbt.hasKey("LootTable")) UndergroundLoot.fill(inventory, nbt.getString("LootTable"), random);
					else {
						NBTTagList items = nbt.getTagList("Items", 10);
						for (int j = 0; j < items.tagCount(); j++) { NBTTagCompound item = items.getCompoundTagAt(j); ItemStack stack = UndergroundLoot.item(item.getString("id")); int slot = item.getByte("Slot") & 255; if (stack != null && slot < inventory.getSizeInventory()) { stack.stackSize = Math.max(1, item.getInteger("count")); inventory.setInventorySlotContents(slot, stack); } }
					}
				}
				if (tile instanceof ganymedes01.etfuturum.tileentities.TileEntityDecoratedPot pot) { pot.readDesigns(nbt); pot.markDirty(); world.markBlockForUpdate(wx, wy, wz); }
				if (tile instanceof TileEntityTrialSpawner trial && nbt.hasKey("normal_config")) trial.configure(nbt.getString("normal_config"));
			}
		} finally { UndergroundStructures.endTemplate(previous); }
	}
}
