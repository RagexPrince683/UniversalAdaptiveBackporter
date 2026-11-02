package ganymedes01.etfuturum.configuration;

/** Startup parameters shared by the independent native world generators. */
public final class GenerationSettings {
    public final int dripstoneChance, dripstoneColumns, dripstoneMaximum, dripstoneLength;
    public final int kelpChance, kelpAttempts, kelpHeight;
    public GenerationSettings() {
        UabConfiguration config = UabConfiguration.get();
        dripstoneChance = config.integer("world", "dripstoneChunkChance", 4, 0, 10000);
        dripstoneColumns = config.integer("world", "dripstoneColumnChance", 4, 1, 256);
        dripstoneMaximum = config.integer("world", "dripstoneMaxPlacements", 32, 0, 4096);
        dripstoneLength = config.integer("world", "dripstoneMaxLength", 4, 1, 16);
        kelpChance = config.integer("world", "kelpChunkChance", 1, 0, 10000);
        kelpAttempts = config.integer("world", "kelpAttempts", 12, 0, 256);
        kelpHeight = config.integer("world", "kelpMaxHeight", 10, 1, 64);
    }
}
