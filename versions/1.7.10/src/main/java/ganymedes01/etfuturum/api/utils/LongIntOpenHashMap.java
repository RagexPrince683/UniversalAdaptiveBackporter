package ganymedes01.etfuturum.api.utils;

/**
 * A compact primitive map for the fixed coordinate data used by large structures.
 * This intentionally exposes only the operations needed by those loaders.
 */
public final class LongIntOpenHashMap {

	private static final float LOAD_FACTOR = 0.65F;

	private long[] keys;
	private int[] values;
	private byte[] states;
	private int mask;
	private int maxFill;
	private int size;

	public LongIntOpenHashMap(int expectedSize) {
		if (expectedSize < 0) {
			throw new IllegalArgumentException("Expected size must not be negative");
		}
		int capacity = 2;
		while (capacity < expectedSize / LOAD_FACTOR && capacity < 1 << 30) {
			capacity <<= 1;
		}
		allocate(capacity);
	}

	public void put(long key, int value) {
		int index = mix(key) & mask;
		while (states[index] != 0) {
			if (keys[index] == key) {
				values[index] = value;
				return;
			}
			index = index + 1 & mask;
		}

		states[index] = 1;
		keys[index] = key;
		values[index] = value;
		if (++size >= maxFill) {
			rehash(keys.length << 1);
		}
	}

	public int capacity() {
		return keys.length;
	}

	public boolean isOccupied(int index) {
		return states[index] != 0;
	}

	public long keyAt(int index) {
		return keys[index];
	}

	public int valueAt(int index) {
		return values[index];
	}

	private void allocate(int capacity) {
		keys = new long[capacity];
		values = new int[capacity];
		states = new byte[capacity];
		mask = capacity - 1;
		maxFill = Math.min(capacity - 1, (int) (capacity * LOAD_FACTOR));
	}

	private void rehash(int newCapacity) {
		long[] oldKeys = keys;
		int[] oldValues = values;
		byte[] oldStates = states;
		allocate(newCapacity);

		for (int oldIndex = 0; oldIndex < oldKeys.length; oldIndex++) {
			if (oldStates[oldIndex] == 0) {
				continue;
			}
			int index = mix(oldKeys[oldIndex]) & mask;
			while (states[index] != 0) {
				index = index + 1 & mask;
			}
			states[index] = 1;
			keys[index] = oldKeys[oldIndex];
			values[index] = oldValues[oldIndex];
		}
	}

	private static int mix(long key) {
		key ^= key >>> 33;
		key *= 0xff51afd7ed558ccdl;
		key ^= key >>> 33;
		key *= 0xc4ceb9fe1a85ec53l;
		key ^= key >>> 33;
		return (int) (key ^ key >>> 32);
	}
}
