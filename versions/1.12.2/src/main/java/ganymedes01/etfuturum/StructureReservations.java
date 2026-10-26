package ganymedes01.etfuturum;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;

/** Consult persisted native starts without asking a structure generator to search terrain. */
final class StructureReservations {
    private static final String[] NATIVE = {"Mineshaft", "Village", "Stronghold", "Temple", "Monument", "Fortress", "EndCity", "Mansion"};
    static boolean vanilla(World world, StructureBoundingBox box) {
        for (String name : NATIVE) {
            MapGenStructureData data = (MapGenStructureData) world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class, name);
            if (data == null) continue;
            NBTTagCompound starts = data.getTagCompound();
            for (String key : starts.getKeySet()) {
                int[] bounds = starts.getCompoundTag(key).getIntArray("BB");
                if (bounds.length == 6 && new StructureBoundingBox(bounds).intersectsWith(box)) return true;
            }
        }
        return false;
    }
}
