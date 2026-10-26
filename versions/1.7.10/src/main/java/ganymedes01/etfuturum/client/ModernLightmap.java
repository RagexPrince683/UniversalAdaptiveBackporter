package ganymedes01.etfuturum.client;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

/** Shared, allocation-free pieces of the modern lightmap calculation. */
public final class ModernLightmap {
	public static final float BLOCK_FACTOR = 1.4F;
	public static final float FLICKER_FACTOR = 0.1F;
	public static final float BIAS = 0.96F;
	public static final float[] END_AMBIENT = {0.28F, 0.16F, 0.28F};
	public static final float[] NIGHT_VISION = {0.7F, 0.7F, 0.7F};

	private static final float[] VANILLA_BRIGHTNESS = brightnessTable(0.0F);
	private static final float[] NETHER_BRIGHTNESS = brightnessTable(0.1F);
	private static final float[][] BLOCK_TINT = new float[16][3];
	private static final Map<WorldProvider, Boolean> SUPPORTED_PROVIDERS = new WeakHashMap<>();

	static {
		for (int i = 0; i < BLOCK_TINT.length; i++) {
			float light = i / 15.0F;
			float red = light;
			float green = light * ((light * 0.6F + 0.4F) * 0.6F + 0.4F);
			float blue = light * (light * light * 0.6F + 0.4F);
			BLOCK_TINT[i][0] = red * BLOCK_FACTOR;
			BLOCK_TINT[i][1] = green * BLOCK_FACTOR;
			BLOCK_TINT[i][2] = blue * BLOCK_FACTOR;
		}
	}

	private ModernLightmap() {}

	private static float[] brightnessTable(float ambient) {
		float[] table = new float[16];
		for (int i = 0; i < table.length; i++) {
			float darkness = 1.0F - i / 15.0F;
			table[i] = (1.0F - darkness) / (darkness * 3.0F + 1.0F) * (1.0F - ambient) + ambient;
		}
		return table;
	}

	/**
	 * Provider classes identify vanilla dimensions; matching the complete brightness table also permits
	 * vanilla-like modded providers without treating every custom dimension as the Overworld.
	 */
	public static boolean supports(WorldProvider provider) {
		if (provider == null) return false;
		synchronized (SUPPORTED_PROVIDERS) {
			Boolean cached = SUPPORTED_PROVIDERS.get(provider);
			if (cached != null) return cached;
			boolean supported = provider instanceof WorldProviderSurface || provider instanceof WorldProviderHell
					|| provider instanceof WorldProviderEnd || matches(provider.lightBrightnessTable, VANILLA_BRIGHTNESS)
					|| matches(provider.lightBrightnessTable, NETHER_BRIGHTNESS);
			SUPPORTED_PROVIDERS.put(provider, supported);
			return supported;
		}
	}

	public static boolean isEnd(WorldProvider provider) {
		return provider instanceof WorldProviderEnd;
	}

	private static boolean matches(float[] actual, float[] expected) {
		if (actual == expected || Arrays.equals(actual, expected)) return true;
		if (actual == null || actual.length != expected.length) return false;
		for (int i = 0; i < expected.length; i++) if (Math.abs(actual[i] - expected[i]) > 0.00001F) return false;
		return true;
	}

	public static float blockTint(int level, int channel, float flicker) {
		return BLOCK_TINT[level & 15][channel] * (1.0F + flicker * FLICKER_FACTOR);
	}

	public static float modernGamma(float color, float gamma) {
		float inverse = 1.0F - color;
		inverse = 1.0F - inverse * inverse * inverse * inverse;
		return color * (1.0F - gamma) + inverse * gamma;
	}

	public static float nightVisionFloor(float color, int channel, float strength) {
		return color + (Math.max(color, NIGHT_VISION[channel]) - color) * strength;
	}

	public static float clamp(float value) {
		return value < 0.0F ? 0.0F : Math.min(value, 1.0F);
	}
}
