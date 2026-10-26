package ganymedes01.etfuturum.api.utils;

import ganymedes01.etfuturum.compat.ModsList;
import net.minecraft.item.ItemDye;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;

/** Utility methods required by Et Futurum's internalized tag and metadata APIs. */
public final class GenericUtils {
    private GenericUtils() {}

    private static Integer maxMeta;
    private static Integer minMeta;

    public static int getMaxBlockMetadata() {
        if (maxMeta == null) {
            if (ModsList.NOT_ENOUGH_IDS.isLoaded() && ModsList.NOT_ENOUGH_IDS.isVersionNewerOrEqual("2.0.0")) {
                maxMeta = (int) Short.MAX_VALUE;
            } else if (ModsList.ENDLESS_IDS_BLOCKITEM.isLoaded()) {
                maxMeta = 65536;
            } else {
                maxMeta = 15;
            }
        }
        return maxMeta;
    }

    public static int getMinBlockMetadata() {
        if (minMeta == null) {
            minMeta = ModsList.NOT_ENOUGH_IDS.isLoaded()
                && ModsList.NOT_ENOUGH_IDS.isVersionNewerOrEqual("2.0.0") ? (int) Short.MIN_VALUE : 0;
        }
        return minMeta;
    }

    public static boolean isBlockMetaInBounds(int meta) {
        return meta <= getMaxBlockMetadata() && meta >= getMinBlockMetadata();
    }

    public static boolean isBlockMetaInBoundsIgnoreWildcard(int meta) {
        return meta == OreDictionary.WILDCARD_VALUE || isBlockMetaInBounds(meta);
    }

    public static BiomeGenBase getBiomeFromID(int id) {
        if (id >= 0 && id < BiomeGenBase.getBiomeGenArray().length && BiomeGenBase.getBiomeGenArray()[id] != null) {
            return BiomeGenBase.getBiomeGenArray()[id];
        }
        throw new IllegalArgumentException(id + " is not a valid Biome ID!");
    }

    public static boolean verifyFilenameIntegrity(String string) {
        return verifyFilenameIntegrity(string, (char[]) null);
    }

    public static boolean verifyFilenameIntegrity(String string, char... exceptions) {
        for (char invalid : new char[]{'<', '>', ':', '"', '/', '\\', '|', '?', '*'}) {
            boolean excepted = false;
            if (exceptions != null) {
                for (char exception : exceptions) {
                    if (exception == invalid) {
                        excepted = true;
                        break;
                    }
                }
            }
            if (!excepted && string.indexOf(invalid) >= 0) return false;
        }
        return true;
    }

    public static boolean isLowerAlphanumeric(String name) {
        return name.matches("^[a-z0-9_/]*$");
    }

    public static final class Constants {
        public static final String[] MODERN_COLORS_SNAKE_CASE;

        static {
            MODERN_COLORS_SNAKE_CASE = Arrays.copyOf(ItemDye.field_150921_b, 16);
            MODERN_COLORS_SNAKE_CASE[7] = "light_gray";
        }

        private Constants() {}
    }
}
