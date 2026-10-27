package ganymedes01.etfuturum.client;

import java.util.Random;

public final class EndFlashState {
	public static final int SOUND_DELAY_IN_TICKS = 30;
	public static final int FLASH_INTERVAL_IN_TICKS = 600;
	public static final int MAX_FLASH_OFFSET_IN_TICKS = 200;
	public static final int MIN_FLASH_DURATION_IN_TICKS = 100;
	public static final int MAX_FLASH_DURATION_IN_TICKS = 380;

	public static final EndFlashState INSTANCE = new EndFlashState();
	private final Random random = new Random();
	private long flashStart = Long.MIN_VALUE;
	private int duration;
	private float previousIntensity;
	private float intensity;
	private float xAngle;
	private float yAngle;

	private EndFlashState() {}

	public void reset(long worldTime) {
		previousIntensity = intensity = 0.0F;
		schedule(worldTime);
	}

	private void schedule(long worldTime) {
		long interval = Math.floorDiv(worldTime, FLASH_INTERVAL_IN_TICKS) + 1L;
		random.setSeed(interval);
		flashStart = interval * FLASH_INTERVAL_IN_TICKS + random.nextInt(MAX_FLASH_OFFSET_IN_TICKS + 1);
		duration = MIN_FLASH_DURATION_IN_TICKS + random.nextInt(MAX_FLASH_DURATION_IN_TICKS - MIN_FLASH_DURATION_IN_TICKS + 1);
		xAngle = 60.0F + random.nextFloat() * 60.0F;
		yAngle = random.nextFloat() * 360.0F;
	}

	/** @return true exactly once for each newly-started flash. */
	public boolean tick(long worldTime) {
		previousIntensity = intensity;
		if (worldTime < flashStart) {
			intensity = 0.0F;
			return false;
		}
		if (worldTime >= flashStart + duration) {
			schedule(worldTime);
			intensity = 0.0F;
			return false;
		}
		float progress = (worldTime - flashStart) / (float) duration;
		intensity = (float) Math.sin(progress * Math.PI);
		intensity *= intensity;
		return worldTime == flashStart;
	}

	public float getIntensity(float partialTicks) { return previousIntensity + (intensity - previousIntensity) * partialTicks; }
	public float getXAngle() { return xAngle; }
	public float getYAngle() { return yAngle; }
}
