package ganymedes01.etfuturum;

import java.io.*;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Released geometry is shared; native blockstates, NBT and loot remain target-owned. */
final class OceanTemplate {
    private static final Map<String, OceanTemplate> CACHE = new HashMap<>();
    final int sx, sy, sz;
    private final NBTTagList entries;
    private final String[][] names;
    private final IBlockState[][] states;
    private boolean supported = true;
    static OceanTemplate get(String name) { return CACHE.computeIfAbsent(name, OceanTemplate::new); }
    private OceanTemplate(String name) {
        NBTTagCompound data;
        try (InputStream in = OceanTemplate.class.getResourceAsStream("/data/structure/" + name + ".nbt")) {
            if (in == null) throw new IOException("Missing structure " + name);
            data = CompressedStreamTools.readCompressed(in);
        } catch (IOException e) { throw new IllegalStateException(e); }
        NBTTagList size = data.getTagList("size", 3); sx = size.getIntAt(0); sy = size.getIntAt(1); sz = size.getIntAt(2);
        entries = data.getTagList("blocks", 10);
        NBTTagList variants = data.getTagList("palettes", 9);
        int count = variants.tagCount() == 0 ? 1 : variants.tagCount();
        states = new IBlockState[count][]; names = new String[count][];
        for (int variant = 0; variant < count; variant++) {
            NBTTagList palette = variants.tagCount() == 0 ? data.getTagList("palette", 10) : (NBTTagList) variants.get(variant);
            states[variant] = new IBlockState[palette.tagCount()]; names[variant] = new String[palette.tagCount()];
            for (int i = 0; i < palette.tagCount(); i++) {
                NBTTagCompound entry = palette.getCompoundTagAt(i); names[variant][i] = entry.getString("Name");
                states[variant][i] = ModernStates.read(entry);
                if (states[variant][i] == null) supported = false;
            }
        }
    }
    boolean available() { return supported; }
    int width(int rotation) { return (rotation & 1) == 0 ? sx : sz; }
    int depth(int rotation) { return (rotation & 1) == 0 ? sz : sx; }
    private BlockPos position(NBTTagCompound entry, BlockPos origin, int rotation) {
        NBTTagList p = entry.getTagList("pos", 3); int x = p.getIntAt(0), y = p.getIntAt(1), z = p.getIntAt(2);
        switch (rotation) {
            case 1: return origin.add(sz - 1 - z, y, x);
            case 2: return origin.add(sx - 1 - x, y, sz - 1 - z);
            case 3: return origin.add(z, y, sx - 1 - x);
            default: return origin.add(x, y, z);
        }
    }
    void place(World world, String name, BlockPos origin, int rotation, int cx, int cz, long seed, float integrity) {
        Rotation turn = Rotation.values()[rotation & 3];
        int palette = new Random(seed).nextInt(states.length);
        IBlockState[] selected = states[palette]; String[] selectedNames = names[palette];
        for (int i = 0; i < entries.tagCount(); i++) {
            NBTTagCompound entry = entries.getCompoundTagAt(i); BlockPos p = position(entry, origin, rotation);
            if ((p.getX() >> 4) != cx || (p.getZ() >> 4) != cz) continue;
            int index = entry.getInteger("state");
            if (selectedNames[index].equals("minecraft:structure_block") || selected[index].getBlock() == Blocks.AIR || integrity < .5F && selected[index].getBlock() == Blocks.CHEST || world.getTileEntity(p) != null || world.getBlockState(p).getBlock() == Blocks.BEDROCK) continue;
            Random random = new Random(seed ^ p.getX() * 3129871L ^ p.getZ() * 116129781L ^ p.getY());
            if (random.nextFloat() > integrity) continue;
            world.setBlockState(p, selected[index].withRotation(turn), 18);
            TileEntity tile = world.getTileEntity(p);
            if (tile != null && entry.hasKey("nbt")) {
                NBTTagCompound nbt = entry.getCompoundTag("nbt").copy();
                nbt.setInteger("x", p.getX()); nbt.setInteger("y", p.getY()); nbt.setInteger("z", p.getZ());
                // Modern loot-table IDs are converted below through structure markers.
                nbt.removeTag("LootTable"); tile.readFromNBT(nbt); tile.markDirty();
            }
        }
        if (integrity < .5F) return; // Masonry overlays never repeat primary loot or mobs.
        for (int i = 0; i < entries.tagCount(); i++) {
            NBTTagCompound entry = entries.getCompoundTagAt(i);
            if (!selectedNames[entry.getInteger("state")].equals("minecraft:structure_block")) continue;
            BlockPos p = position(entry, origin, rotation);
            if ((p.getX() >> 4) != cx || (p.getZ() >> 4) != cz) continue;
            String marker = entry.getCompoundTag("nbt").getString("metadata");
            if (marker.equals("drowned")) {
                if (world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL && MobRegistry.enabled(ganymedes01.etfuturum.entities.EntityDrowned.class)) {
                    ganymedes01.etfuturum.entities.EntityDrowned drowned = new ganymedes01.etfuturum.entities.EntityDrowned(world);
                    drowned.setLocationAndAngles(p.getX() + .5, p.getY(), p.getZ() + .5, 0, 0);
                    drowned.onInitialSpawn(world.getDifficultyForLocation(p), null); drowned.enablePersistence(); world.spawnEntity(drowned);
                }
                continue;
            }
            if (!marker.endsWith("chest")) continue;
            boolean ruin = name.startsWith("underwater_ruin/") && marker.equals("chest");
            BlockPos chest = ruin ? p : p.down();
            if (ruin && world.getTileEntity(chest) == null) world.setBlockState(chest, Blocks.CHEST.getDefaultState().withRotation(turn), 18);
            TileEntity tile = world.getTileEntity(chest);
            if (tile instanceof TileEntityChest) {
                String table = marker.contains("supply") ? "shipwreck_supply" : marker.contains("map") ? "shipwreck_map" : marker.contains("treasure") ? "shipwreck_treasure" : name.contains("big_") ? "underwater_ruin_big" : "underwater_ruin_small";
                ((TileEntityChest) tile).setLootTable(new ResourceLocation(UAB.ID, "chests/" + table), seed ^ p.toLong());
            }
        }
    }
}
