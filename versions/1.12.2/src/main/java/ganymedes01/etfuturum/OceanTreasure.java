package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.BiomeDictionary;

/** Chunk-local beach treasure retains the maintained saved locations and once-only placement. */
public final class OceanTreasure extends WorldSavedData {
    private static final String ID = "etfuturum_ocean_treasure";
    private final Map<Long, BlockPos> positions = new HashMap<>();
    public OceanTreasure() { super(ID); }
    public OceanTreasure(String name) { super(name); }
    private static OceanTreasure data(World world) {
        OceanTreasure value = (OceanTreasure) world.getPerWorldStorage().getOrLoadData(OceanTreasure.class, ID);
        if (value == null) { value = new OceanTreasure(); world.getPerWorldStorage().setData(ID, value); } return value;
    }
    static void generate(World world, int cx, int cz) {
        if (world.isRemote || world.provider.getDimension() != 0 || !world.getWorldInfo().isMapFeaturesEnabled() || !Features.enabled("content.buried_treasure")) return;
        Random random = new Random(world.getSeed() + cx * 341873128712L + cz * 132897987541L + 10387320);
        BlockPos center = new BlockPos((cx << 4) + 9, 0, (cz << 4) + 9);
        if (random.nextInt(100) == 0 && BiomeDictionary.hasType(world.getBiome(center), BiomeDictionary.Type.BEACH)) place(world, center.getX(), center.getZ(), random);
    }
    public static boolean place(World world, int x, int z, Random random) {
        if (world.isRemote || !world.isBlockLoaded(new BlockPos(x, 63, z)) || !Features.enabled("content.buried_treasure")) return false;
        OceanTreasure value = data(world); long key = ChunkPos.asLong(x >> 4, z >> 4);
        if (value.positions.containsKey(key)) return false;
        for (int y = Math.min(70, world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() - 2); y >= 40; y--) {
            BlockPos pos = new BlockPos(x, y, z); Block block = world.getBlockState(pos).getBlock();
            if ((block != Blocks.SAND && block != Blocks.GRAVEL) || world.getTileEntity(pos) != null) continue;
            StructureBoundingBox box = new StructureBoundingBox(x, y, z, x, y, z);
            if (StructureReservations.vanilla(world, box) || OceanStructures.reserved(world, box)) return false;
            world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 18);
            if (!(world.getTileEntity(pos) instanceof TileEntityChest)) return false;
            ((TileEntityChest) world.getTileEntity(pos)).setLootTable(new ResourceLocation(UAB.ID, "chests/buried_treasure"), random.nextLong());
            value.positions.put(key, pos); value.markDirty(); return true;
        }
        return false;
    }
    static BlockPos nearest(World world, BlockPos origin) {
        BlockPos best = null; double distance = Double.MAX_VALUE;
        for (BlockPos pos : data(world).positions.values()) if (pos.distanceSq(origin) < distance) { best = pos; distance = pos.distanceSq(origin); } return best;
    }
    static boolean reserved(World world, StructureBoundingBox box) { for (BlockPos pos : data(world).positions.values()) if (box.isVecInside(pos)) return true; return false; }
    @Override public void readFromNBT(NBTTagCompound tag) {
        positions.clear(); NBTTagList list = tag.getTagList("Locations", 10);
        for (int i = 0; i < list.tagCount(); i++) { int[] p = list.getCompoundTagAt(i).getIntArray("Position"); if (p.length == 3) positions.put(ChunkPos.asLong(p[0] >> 4, p[2] >> 4), new BlockPos(p[0], p[1], p[2])); }
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (BlockPos p : positions.values()) { NBTTagCompound entry = new NBTTagCompound(); entry.setIntArray("Position", new int[]{p.getX(), p.getY(), p.getZ()}); list.appendTag(entry); }
        tag.setTag("Locations", list); return tag;
    }
}
