package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Removed items resolve to vanilla; saved numeric block IDs have a hidden loading adapter. */
@Mod.EventBusSubscriber(modid = UAB.ID)
public final class NativeContentMigration {
    private static Block slab, doubled;
    private NativeContentMigration() { }

    @SubscribeEvent public static void registerBlocks(RegistryEvent.Register<Block> event) {
        // Forge cannot map two saved block IDs onto an already registered vanilla block.
        // Retain only these block identities, without ItemBlocks, recipes or creative entries.
        NativeSlab[] pair = NativeSlab.pair(Content.block("smooth_stone").getDefaultState());
        slab = pair[0].setRegistryName(UAB.ID, "smooth_stone_slab");
        doubled = pair[1].setRegistryName(UAB.ID, "smooth_stone_double_slab");
        event.getRegistry().register(slab);
        event.getRegistry().register(doubled);
    }

    @SubscribeEvent public static void items(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings()) {
            if (!mapping.key.getNamespace().equals(UAB.ID)) continue;
            String name = mapping.key.getPath();
            if (name.equals("smooth_stone_slab")) mapping.remap(Item.getItemFromBlock(Blocks.STONE_SLAB));
            else if (name.endsWith("_boat")) {
                String wood = name.substring(0, name.length() - 5);
                if (BoatContent.legacy(wood) != null) mapping.remap(BoatContent.nativeBoat(wood).getItem());
            }
        }
    }

    @SubscribeEvent public static void loadChunk(ChunkDataEvent.Load event) {
        if (event.getWorld().isRemote || slab == null) return;
        NBTTagCompound data = event.getData();
        if (data.hasKey("Level", 10)) data = data.getCompoundTag("Level");
        NBTTagList sections = data.getTagList("Sections", 10);
        ExtendedBlockStorage[] storage = event.getChunk().getBlockStorageArray();
        int singleId = Block.getIdFromBlock(slab), doubleId = Block.getIdFromBlock(doubled);
        boolean changed = false;
        for (int s = 0; s < sections.tagCount(); s++) {
            NBTTagCompound section = sections.getCompoundTagAt(s);
            int y = section.getByte("Y") & 255;
            byte[] blocks = section.getByteArray("Blocks"), extra = section.getByteArray("Add"), metadata = section.getByteArray("Data");
            if (y >= storage.length || blocks.length != 4096 || metadata.length != 2048) continue;
            boolean sectionChanged = false;
            for (int i = 0; i < blocks.length; i++) {
                int shift = (i & 1) * 4;
                int id = (blocks[i] & 255) | (extra.length == 2048 ? (extra[i >> 1] >> shift & 15) << 8 : 0);
                if (id != singleId && id != doubleId) continue;
                if (storage[y] == null) storage[y] = new ExtendedBlockStorage(y << 4, event.getWorld().provider.hasSkyLight());
                int meta = metadata[i >> 1] >> shift & 15;
                // The chunk is still owned by its loader. Do not publish world/neighbor updates here.
                storage[y].set(i & 15, i >> 8, i >> 4 & 15, (id == singleId ? Blocks.STONE_SLAB : Blocks.DOUBLE_STONE_SLAB).getStateFromMeta(meta));
                sectionChanged = changed = true;
            }
            if (sectionChanged) storage[y].recalculateRefCounts();
        }
        if (changed) event.getChunk().markDirty();
    }
}
