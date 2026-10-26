package ganymedes01.etfuturum.world;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.core.utils.structurenbt.*;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.*;
import net.minecraftforge.common.util.ForgeDirection;
import java.io.InputStream;
import java.util.*;

/** Released tower and bounded surrounding pieces, each placed only during its own chunk's first population. */
public final class OutpostStructures extends WorldSavedData {
	private static final String[] NAMES = {"watchtower", "feature_cage_with_allays", "feature_tent1", "feature_logs", "feature_targets"};
	private static final Template[] TEMPLATES = new Template[NAMES.length];
	private final Map<Long, Piece> pending = new HashMap<>();
	public OutpostStructures(String name) { super(name); }
	private static OutpostStructures get(World world) {
		OutpostStructures data = (OutpostStructures) world.perWorldStorage.loadData(OutpostStructures.class, "etfuturum_outpost_pieces");
		if (data == null) { data = new OutpostStructures("etfuturum_outpost_pieces"); world.perWorldStorage.setData("etfuturum_outpost_pieces", data); }
		return data;
	}
	private static Template template(int type) { if (TEMPLATES[type] == null) TEMPLATES[type] = new Template(NAMES[type]); return TEMPLATES[type]; }
	public static void populatePending(World world, Random random, int cx, int cz) {
		OutpostStructures data = get(world); Piece piece = data.pending.remove(ChunkCoordIntPair.chunkXZ2Int(cx, cz));
		if (piece != null) { place(world, new Random(piece.seed), cx, cz, piece.type, piece.rotation); data.markDirty(); }
	}
	public static boolean tower(World world, Random random, int cx, int cz) {
		if (RaidProgress.get(world).isOutpost(cx * 16, cz * 16)) return false;
		int rotation = random.nextInt(4);
		if (!place(world, random, cx, cz, 0, rotation)) return false;
		RaidProgress.get(world).addOutpost(cx, cz);
		OutpostStructures data = get(world);
		int[][] offsets = {{2,0},{0,2},{-2,0},{0,-2}};
		for (int i = 0; i < offsets.length; i++) {
			int direction = (i + rotation) & 3, x = cx + offsets[direction][0], z = cz + offsets[direction][1];
			// Previously populated neighbors are never changed, even if an outpost is discovered beside them.
			if (world.getChunkProvider().chunkExists(x, z) && world.getChunkFromChunkCoords(x, z).isTerrainPopulated) continue;
			long key = ChunkCoordIntPair.chunkXZ2Int(x, z);
			if (!data.pending.containsKey(key)) data.pending.put(key, new Piece(i + 1, random.nextInt(4), random.nextLong()));
		}
		data.markDirty(); return true;
	}
	private static boolean place(World world, Random random, int cx, int cz, int type, int rotation) {
		Template template = template(type); int width = (rotation & 1) == 0 ? template.sx : template.sz, depth = (rotation & 1) == 0 ? template.sz : template.sx;
		int x = cx * 16 + (16 - width) / 2, z = cz * 16 + (16 - depth) / 2;
		if (!world.getChunkProvider().chunkExists(cx, cz)) return false;
		int y = world.getTopSolidOrLiquidBlock(x + width / 2, z + depth / 2) - 1;
		if (y < 59 || y + template.sy >= world.getActualHeight()) return false;
		for (int dx = 0; dx < width; dx++) for (int dz = 0; dz < depth; dz++) {
			int ground = world.getTopSolidOrLiquidBlock(x + dx, z + dz) - 1;
			if (Math.abs(ground - y) > 3 || world.getBlock(x + dx, ground, z + dz).getMaterial().isLiquid()) return false;
		}
		template.place(world, random, x, y, z, rotation);
		if (type == 0) for (int i = 0; i < 3; i++) { EntityPillager pillager = new EntityPillager(world); pillager.setCaptain(i == 0); mob(world, pillager, x + width / 2 + i - 1, y + 14, z + depth / 2); }
		if (type == 1) for (int i = 0; i < 2; i++) mob(world, new EntityAllay(world), x + width / 2, y + 1, z + depth / 2);
		return true;
	}
	private static void mob(World world, EntityLiving mob, int x, int y, int z) {
		mob.setLocationAndAngles(x + .5, y, z + .5, world.rand.nextFloat() * 360, 0);
		if (!world.getCollidingBoundingBoxes(mob, mob.boundingBox).isEmpty()) return;
		mob.onSpawnWithEgg(null); mob.func_110163_bv(); world.spawnEntityInWorld(mob);
	}
	@Override public void writeToNBT(NBTTagCompound tag) {
		NBTTagList pieces = new NBTTagList();
		for (Map.Entry<Long, Piece> entry : pending.entrySet()) { NBTTagCompound piece = new NBTTagCompound(); piece.setLong("Chunk", entry.getKey()); piece.setInteger("Type", entry.getValue().type); piece.setInteger("Rotation", entry.getValue().rotation); piece.setLong("Seed", entry.getValue().seed); pieces.appendTag(piece); }
		tag.setTag("Pieces", pieces);
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		pending.clear(); NBTTagList pieces = tag.getTagList("Pieces", 10);
		for (int i = 0; i < pieces.tagCount(); i++) { NBTTagCompound piece = pieces.getCompoundTagAt(i); int type = piece.getInteger("Type"); if (type > 0 && type < NAMES.length) pending.put(piece.getLong("Chunk"), new Piece(type, piece.getInteger("Rotation") & 3, piece.getLong("Seed"))); }
	}
	private static final class Piece { final int type, rotation; final long seed; Piece(int type, int rotation, long seed) { this.type = type; this.rotation = rotation; this.seed = seed; } }
	private static final class Template {
		final int sx, sy, sz;
		final NBTTagList blocks;
		final BlockStateContainer[][] states;
		Template(String name) {
			NBTTagCompound data;
			try (InputStream stream = OutpostStructures.class.getResourceAsStream("/data/structure/pillager_outpost/" + name + ".nbt")) { if (stream == null) throw new IllegalStateException("Missing outpost " + name); data = CompressedStreamTools.readCompressed(stream); }
			catch (Exception exception) { throw new IllegalStateException("Cannot read outpost " + name, exception); }
			ganymedes01.etfuturum.core.utils.helpers.BlockPos size = NBTStructure.getPosFromTagList(data.getTagList("size", 3)); sx = size.getX(); sy = size.getY(); sz = size.getZ();
			if (sx > 16 || sz > 16 || sy > 32) throw new IllegalStateException("Outpost piece exceeds bounded footprint " + name);
			blocks = data.getTagList("blocks", 10); NBTTagList palette = data.getTagList("palette", 10); states = new BlockStateContainer[4][palette.tagCount()];
			for (int r = 0; r < 4; r++) for (int i = 0; i < palette.tagCount(); i++) {
				NBTTagCompound state = palette.getCompoundTagAt(i); String blockName = state.getString("Name"); Map<String, String> properties = new HashMap<>(NBTStructure.getProperties(state));
				String facing = properties.get("facing"); if (facing != null && Arrays.asList("north", "east", "south", "west").contains(facing)) properties.put("facing", new String[]{"north", "east", "south", "west"}[(Arrays.asList("north", "east", "south", "west").indexOf(facing) + r) & 3]);
				if ((r & 1) != 0 && ("x".equals(properties.get("axis")) || "z".equals(properties.get("axis")))) properties.put("axis", "x".equals(properties.get("axis")) ? "z" : "x");
				Block block = blockName.equals("minecraft:jigsaw") ? Blocks.air : blockName.equals("minecraft:dark_oak_fence") ? ModBlocks.FENCE_DARK_OAK.isEnabled() ? ModBlocks.FENCE_DARK_OAK.get() : Blocks.fence : EFRBlockStateConverter.INSTANCE.getBlockFromNamespace(blockName, properties);
				if (block == null || block == Blocks.stone && !blockName.equals("minecraft:stone")) throw new IllegalStateException("Unresolved outpost palette " + blockName);
				states[r][i] = new BlockStateContainer(block, EFRBlockStateConverter.INSTANCE.getMetaFromStateWithSubtypeAdditions(blockName, properties, ForgeDirection.NORTH));
			}
		}
		void place(World world, Random random, int x, int y, int z, int rotation) {
			// Place support and air before attached blocks; no shared NBT or modern entity data is applied.
			for (int pass = 0; pass < 2; pass++) for (int i = 0; i < blocks.tagCount(); i++) {
				NBTTagCompound entry = blocks.getCompoundTagAt(i); BlockStateContainer state = states[rotation][entry.getInteger("state")]; Block block = state.getBlock();
				boolean attached = block == Blocks.torch || block == Blocks.chest || block == ModBlocks.BANNER.get(); if (attached != (pass == 1)) continue;
				ganymedes01.etfuturum.core.utils.helpers.BlockPos pos = NBTStructure.getPosFromTagList(entry.getTagList("pos", 3)); int px = pos.getX(), py = pos.getY(), pz = pos.getZ();
				int rx = rotation == 1 ? sz - 1 - pz : rotation == 2 ? sx - 1 - px : rotation == 3 ? pz : px;
				int rz = rotation == 1 ? px : rotation == 2 ? sz - 1 - pz : rotation == 3 ? sx - 1 - px : pz;
				int wx = x + rx, wy = y + py, wz = z + rz;
				if (py == 0 && block != Blocks.air) for (int dy = 1; dy <= 4 && !world.getBlock(wx, wy - dy, wz).getMaterial().isSolid(); dy++) world.setBlock(wx, wy - dy, wz, Blocks.cobblestone, 0, 2);
				world.setBlock(wx, wy, wz, block, state.getMeta(), 2);
				if (block == Blocks.chest && world.getTileEntity(wx, wy, wz) instanceof TileEntityChest chest) loot(chest, random);
				if (block == ModBlocks.BANNER.get() && world.getTileEntity(wx, wy, wz) instanceof ganymedes01.etfuturum.tileentities.TileEntityBanner banner) {
					NBTTagCompound tag = new NBTTagCompound(); banner.writeToNBT(tag); tag.setInteger("Base", 0); tag.setBoolean("IsStanding", false);
					NBTTagList patterns = new NBTTagList(); String[] names = {"mr","bs","cs","bo","ms","hh","mc","bo"}; int[] colors = {9,8,7,8,15,8,8,15};
					for (int p = 0; p < names.length; p++) { NBTTagCompound pattern = new NBTTagCompound(); pattern.setString("Pattern", names[p]); pattern.setInteger("Color", colors[p]); patterns.appendTag(pattern); } tag.setTag("Patterns", patterns); banner.readFromNBT(tag); banner.markDirty();
				}
			}
		}
		private void loot(TileEntityChest chest, Random random) {
			for (int i = 0, rolls = 3 + random.nextInt(4); i < rolls; i++) {
				ItemStack stack = switch (random.nextInt(8)) {
					case 0 -> new ItemStack(Items.arrow, 2 + random.nextInt(6)); case 1 -> new ItemStack(Items.wheat, 3 + random.nextInt(3));
					case 2 -> new ItemStack(Items.potato, 2 + random.nextInt(4)); case 3 -> new ItemStack(Items.carrot, 3 + random.nextInt(3));
					case 4 -> new ItemStack(Items.string, 1 + random.nextInt(6)); case 5 -> new ItemStack(Items.iron_ingot, 1 + random.nextInt(3));
					case 6 -> new ItemStack(Blocks.log2, 2 + random.nextInt(2), 1); default -> ModItems.CROSSBOW.newItemStack();
				};
				int slot = random.nextInt(chest.getSizeInventory()); for (int tries = 0; tries < chest.getSizeInventory() && chest.getStackInSlot(slot) != null; tries++) slot = (slot + 1) % chest.getSizeInventory(); chest.setInventorySlotContents(slot, stack);
			}
			if (random.nextInt(4) == 0) ganymedes01.etfuturum.world.structure.ExplorationLoot.put(chest, ModItems.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 2, random);
			chest.markDirty();
		}
	}
}
