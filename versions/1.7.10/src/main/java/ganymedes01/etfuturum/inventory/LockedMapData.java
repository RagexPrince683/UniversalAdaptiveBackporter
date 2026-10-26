package ganymedes01.etfuturum.inventory;

/** Additive saved-map state, shared by all copies of the same map identity. */
public interface LockedMapData {
	boolean etfu$isLocked();
	void etfu$setLocked(boolean locked);
}
