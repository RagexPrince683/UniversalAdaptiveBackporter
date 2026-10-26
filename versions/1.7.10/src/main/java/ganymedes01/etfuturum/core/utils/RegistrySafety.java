package ganymedes01.etfuturum.core.utils;

/** Check live capacities after registry-expansion coremods, without changing configured IDs. */
public final class RegistrySafety {
	private RegistrySafety() {}

	public static int requireVacant(Object[] registry, int id, String name) {
		if (id < 0 || id >= registry.length)
			throw new IllegalArgumentException(name + " ID " + id + " is outside the runtime registry (0.."
					+ (registry.length - 1) + "). Check the configuration and installed ID expansion.");
		if (registry[id] != null)
			throw new IllegalArgumentException(name + " ID " + id + " is already occupied by " + registry[id]
					+ ". Choose an unused ID explicitly; UAB will not remap saved IDs.");
		return id;
	}
}
