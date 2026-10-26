package ganymedes01.etfuturum.world.structure.surface;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.core.utils.structurenbt.*;
import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.tileentities.TileEntityBrushable;
import ganymedes01.etfuturum.world.structure.underground.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;
import java.io.*;
import java.util.*;

/** Released templates; mirror precedes rotation, with explicit native tile and entity translation. */
final class SurfaceTemplate {
	final int sx, sy, sz;
	final NBTTagList blocks;
	final List<String> names = new ArrayList<>();
	final List<Map<String, String>> properties = new ArrayList<>();
	final List<NBTTagCompound> connectors = new ArrayList<>();
	private final BlockStateContainer[][] states;
	SurfaceTemplate(String id) {
		NBTTagCompound tag;
		try (InputStream stream = getClass().getResourceAsStream("/data/structure/" + id + ".nbt")) {
			if (stream == null) throw new IllegalArgumentException("Missing surface template " + id);
			tag = CompressedStreamTools.readCompressed(stream);
		} catch (IOException e) { throw new IllegalStateException(id, e); }
		BlockPos size = NBTStructure.getPosFromTagList(tag.getTagList("size", 3)); sx = size.getX(); sy = size.getY(); sz = size.getZ();
		blocks = tag.getTagList("blocks", 10); NBTTagList palette = tag.getTagList("palette", 10);
		if (palette.tagCount() == 0) palette = (NBTTagList) tag.getTagList("palettes", 9).tagList.get(0);
		for (int i = 0; i < palette.tagCount(); i++) { NBTTagCompound p = palette.getCompoundTagAt(i); names.add(p.getString("Name")); properties.add(NBTStructure.getProperties(p)); }
		for (int i = 0; i < blocks.tagCount(); i++) if (names.get(blocks.getCompoundTagAt(i).getInteger("state")).equals("minecraft:jigsaw")) connectors.add(blocks.getCompoundTagAt(i));
		states = new BlockStateContainer[12][names.size()];
		for (int m = 0; m < 3; m++) for (int r = 0; r < 4; r++) for (int i = 0; i < names.size(); i++) states[m * 4 + r][i] = state(names.get(i), transform(properties.get(i), r, m));
	}
	static BlockPos rotate(int x, int y, int z, int r, int mirror) {
		if (mirror == 1) z = -z; else if (mirror == 2) x = -x;
		switch (r & 3) { case 1: return new BlockPos(-z, y, x); case 2: return new BlockPos(-x, y, -z); case 3: return new BlockPos(z, y, -x); default: return new BlockPos(x, y, z); }
	}
	static BlockPos position(NBTTagCompound entry, int r, int mirror) { BlockPos p = NBTStructure.getPosFromTagList(entry.getTagList("pos", 3)); return rotate(p.getX(), p.getY(), p.getZ(), r, mirror); }
	BlockPos direction(NBTTagCompound connector, int rotation, boolean top) {
		String[] orientation = properties.get(connector.getInteger("state")).get("orientation").split("_");
		String dir = orientation[top ? 1 : 0]; return dir.equals("up") ? new BlockPos(0, 1, 0) : dir.equals("down") ? new BlockPos(0, -1, 0) : rotate(dir.equals("east") ? 1 : dir.equals("west") ? -1 : 0, 0, dir.equals("south") ? 1 : dir.equals("north") ? -1 : 0, rotation, 0);
	}
	StructureBoundingBox box(int x, int y, int z, int r, int mirror) {
		BlockPos end = rotate(sx - 1, sy - 1, sz - 1, r, mirror);
		return new StructureBoundingBox(x + Math.min(0, end.getX()), y, z + Math.min(0, end.getZ()), x + Math.max(0, end.getX()), y + sy - 1, z + Math.max(0, end.getZ()));
	}
	private static String facing(String dir, int r, int mirror) {
		List<String> order = Arrays.asList("north", "east", "south", "west"); int i = order.indexOf(dir); if (i < 0) return dir;
		if (mirror == 1 && (i & 1) == 0 || mirror == 2 && (i & 1) == 1) i = (i + 2) & 3;
		return order.get((i + r) & 3);
	}
	private static Map<String, String> transform(Map<String, String> original, int r, int mirror) {
		Map<String, String> p = new HashMap<>(original);
		if (p.containsKey("facing")) p.put("facing", facing(p.get("facing"), r, mirror));
		for (String dir : new String[]{"north", "east", "south", "west"}) if (original.containsKey(dir)) p.put(facing(dir, r, mirror), original.get(dir));
		if ((r & 1) != 0 && ("x".equals(p.get("axis")) || "z".equals(p.get("axis")))) p.put("axis", "x".equals(p.get("axis")) ? "z" : "x");
		if (mirror != 0 && p.containsKey("hinge")) p.put("hinge", "left".equals(p.get("hinge")) ? "right" : "left");
		return p;
	}
	static BlockStateContainer state(String id, Map<String, String> p) {
		String name = id.replace("minecraft:", ""); Block block = null; int meta = 0;
		if (name.equals("structure_block") || name.equals("jigsaw") || name.equals("structure_void")) return new BlockStateContainer(Blocks.air);
		if (name.equals("mud_bricks")) return new BlockStateContainer(ModBlocks.PACKED_MUD.get(), 1);
		if (name.equals("damaged_anvil")) return new BlockStateContainer(Blocks.anvil, 8 + EFRBlockStateConverter.INSTANCE.getMetaFromState(id, p, ForgeDirection.NORTH));
		if (name.equals("brick_wall")) return new BlockStateContainer(ModBlocks.STONE_WALL.get(), 3);
		if (name.equals("dark_oak_sapling")) return new BlockStateContainer(Blocks.sapling, 5 | ("1".equals(p.get("stage")) ? 8 : 0));
		if (name.equals("dark_oak_door")) return new BlockStateContainer(ModBlocks.DOOR_DARK_OAK.get(), EFRBlockStateConverter.INSTANCE.getMetaFromState(id, p, ForgeDirection.NORTH));
		if (name.equals("birch_fence")) block = ModBlocks.FENCE_BIRCH.get();
		else if (name.equals("dark_oak_fence_gate")) block = ModBlocks.FENCE_GATE_DARK_OAK.get();
		else if (name.equals("attached_melon_stem")) { block = Blocks.melon_stem; meta = 7; }
		else if (name.equals("attached_pumpkin_stem")) { block = Blocks.pumpkin_stem; meta = 7; }
		else if (name.equals("water_cauldron")) { block = Blocks.cauldron; meta = Integer.parseInt(p.get("level")); }
		else if (name.equals("campfire") || name.equals("grindstone")) return new BlockStateContainer(Blocks.air); // unavailable workstation decorations are omitted, never replaced with unrelated blocks
		if (block == null) {
			BlockStateContainer translated = UndergroundTemplate.state(id, p, 0);
			if (name.startsWith("potted_") || name.endsWith("banner")) return EFRBlockStateConverter.INSTANCE.createBlockStateContainer(id, translated.getBlock(), p, ForgeDirection.NORTH);
			return translated;
		}
		if (name.endsWith("gate")) meta = EFRBlockStateConverter.INSTANCE.getMetaFromStateWithSubtypeAdditions(id, p, ForgeDirection.NORTH);
		return new BlockStateContainer(block, meta);
	}
	void place(World world, SurfaceStructures.Piece piece, StructureBoundingBox clip, long seed) {
		TrailProcessors.Result processed = null;
		if (piece.kind == 1) { if (piece.processed == null) piece.processed = TrailProcessors.prepare(this, piece, seed); processed = piece.processed; }
		for (int pass = 0; pass < 2; pass++) for (int i = 0; i < blocks.tagCount(); i++) {
			NBTTagCompound entry = blocks.getCompoundTagAt(i), nbt = entry.getCompoundTag("nbt"); int index = entry.getInteger("state"); String name = names.get(index);
			boolean marker = name.equals("minecraft:structure_block");
			BlockStateContainer state = states[piece.mirror * 4 + piece.rotation][index]; Block block = state.getBlock();
			boolean attached = marker || block == Blocks.torch || block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.flower_pot || block == Blocks.ladder || name.endsWith("banner") || block == Blocks.vine;
			if (attached != (pass == 1)) continue;
			if (!ganymedes01.etfuturum.configuration.ContentAvailability.available(block)) continue;
			BlockPos pos = position(entry, piece.rotation, piece.mirror); int x = piece.x + pos.getX(), y = piece.y + pos.getY(), z = piece.z + pos.getZ();
			if (!clip.isVecInside(x, y, z) || world.getBlock(x, y, z) == Blocks.bedrock) continue;
			Random random = new Random(seed ^ (long) x * 3129871 ^ (long) z * 116129781L ^ y);
			if (marker) { marker(world, nbt.getString("metadata"), x, y, z, piece, random); continue; }
			if (piece.kind != 0 && (block == Blocks.air || name.equals("minecraft:jigsaw"))) continue;
			String loot = processed == null ? null : processed.loot[i];
			if (processed != null && processed.states[i] != null) state = processed.states[i];
			world.setBlock(x, y, z, state.getBlock(), state.getMeta(), 2);
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile != null && state.getCompound() != null) { NBTTagCompound tag = (NBTTagCompound) state.getCompound().copy(); tag.setInteger("x", x); tag.setInteger("y", y); tag.setInteger("z", z); tile.readFromNBT(tag); }
			if (tile instanceof TileEntityBrushable) ((TileEntityBrushable) tile).configure(loot == null ? "" : loot, random.nextLong());
			if (tile instanceof TileEntityMobSpawner) ((TileEntityMobSpawner) tile).func_145881_a().setEntityName("Spider");
			if (tile instanceof IInventory) {
				IInventory inventory = (IInventory) tile;
				if (nbt.hasKey("LootTable")) UndergroundLoot.fill(inventory, nbt.getString("LootTable"), random);
				else { NBTTagList items = nbt.getTagList("Items", 10); for (int j = 0; j < items.tagCount(); j++) { NBTTagCompound data = items.getCompoundTagAt(j); ItemStack stack = UndergroundLoot.item(data.getString("id")); int slot = data.getByte("Slot") & 255; if (stack != null && slot < inventory.getSizeInventory()) { stack.stackSize = Math.max(1, data.getInteger("count")); inventory.setInventorySlotContents(slot, stack); } } }
			}
			if (tile != null) tile.markDirty();
			if (piece.kind == 0 && pos.getY() == 0 && piece.y <= piece.foundation && block != Blocks.air) for (int yy = y - 1; yy > 1 && !world.getBlock(x, yy, z).getMaterial().isSolid(); yy--) world.setBlock(x, yy, z, Blocks.cobblestone, 0, 2);
		}
	}
	private static void marker(World world, String marker, int x, int y, int z, SurfaceStructures.Piece piece, Random random) {
		if (marker.startsWith("Chest")) {
			String dir = marker.startsWith("Chest") ? marker.substring(5).toLowerCase(Locale.ROOT) : "north"; dir = facing(dir, piece.rotation, piece.mirror);
			int meta = dir.equals("north") ? 2 : dir.equals("south") ? 3 : dir.equals("west") ? 4 : 5;
			world.setBlock(x, y, z, Blocks.chest, meta, 2);
			String table = "chests/woodland_mansion";
			TileEntity tile = world.getTileEntity(x, y, z); if (tile instanceof IInventory) UndergroundLoot.fill((IInventory) tile, "minecraft:" + table, random); return;
		}
		int count = marker.equals("Group of Allays") ? 1 + random.nextInt(3) : 1;
		for (int i = 0; i < count; i++) {
			EntityLiving mob = marker.equals("Mage") ? new EntityEvoker(world) : marker.equals("Warrior") ? new EntityVindicator(world) : marker.equals("Group of Allays") ? new EntityAllay(world) : null;
			if (mob == null || !ganymedes01.etfuturum.configuration.ContentAvailability.mobEnabled(mob.getClass())) return;
			mob.setLocationAndAngles(x + .5, y, z + .5, random.nextFloat() * 360, 0); mob.onSpawnWithEgg(null); mob.func_110163_bv(); world.spawnEntityInWorld(mob);
		}
		if (piece.kind == 0) world.setBlockToAir(x, y, z);
	}
}
