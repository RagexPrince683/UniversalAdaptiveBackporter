package ganymedes01.etfuturum.world;

import java.util.Random;

/** Seed arithmetic has no dependence on chunk visitation order or mutable world state. */
public final class GenerationSeeds {
    private GenerationSeeds() { }
    public static Random chunk(long seed, int x, int z, long salt) {
        return new Random(seed + x * 341873128712L + z * 132897987541L + salt);
    }
    public static int[] candidate(long seed, int x, int z, int spacing, int separation, long salt) {
        if (spacing <= separation || separation < 0) throw new IllegalArgumentException("Invalid structure spacing");
        int gx = Math.floorDiv(x, spacing), gz = Math.floorDiv(z, spacing);
        Random random = chunk(seed, gx, gz, salt);
        return new int[]{gx * spacing + random.nextInt(spacing - separation), gz * spacing + random.nextInt(spacing - separation)};
    }
}
