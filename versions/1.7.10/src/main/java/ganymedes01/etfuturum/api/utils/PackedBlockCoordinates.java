package ganymedes01.etfuturum.api.utils;

/** Minecraft's modern 26/12/26 packed block-coordinate layout. */
public final class PackedBlockCoordinates {

	private static final int X_BITS = 26;
	private static final int Y_BITS = 12;
	private static final int Z_BITS = 26;
	private static final int Z_SHIFT = Y_BITS;
	private static final int X_SHIFT = Y_BITS + Z_BITS;

	private PackedBlockCoordinates() {
	}

	public static long pack(int x, int y, int z) {
		return ((long) x & (1L << X_BITS) - 1L) << X_SHIFT
				| ((long) z & (1L << Z_BITS) - 1L) << Z_SHIFT
				| (long) y & (1L << Y_BITS) - 1L;
	}

	public static int unpackX(long packed) {
		return (int) (packed << 64 - X_SHIFT - X_BITS >> 64 - X_BITS);
	}

	public static int unpackY(long packed) {
		return (int) (packed << 64 - Y_BITS >> 64 - Y_BITS);
	}

	public static int unpackZ(long packed) {
		return (int) (packed << 64 - Z_SHIFT - Z_BITS >> 64 - Z_BITS);
	}
}
