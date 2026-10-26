package ganymedes01.etfuturum.compat;

import net.minecraft.entity.Entity;

public final class CompatCustomNPCs {

	private static final String NPC_BASE_CLASS = "noppes.npcs.entity.EntityNPCInterface";

	private CompatCustomNPCs() {
	}

	/**
	 * Identifies actual CustomNPC entities without linking EFR against the optional mod.
	 */
	public static boolean isCustomNpc(Entity entity) {
		if (entity == null || !ModsList.CUSTOM_NPCS.isLoaded()) {
			return false;
		}

		Class<?> type = entity.getClass();
		while (type != null) {
			if (NPC_BASE_CLASS.equals(type.getName())) {
				return true;
			}
			type = type.getSuperclass();
		}
		return false;
	}
}
