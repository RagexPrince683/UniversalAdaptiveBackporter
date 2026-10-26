package ganymedes01.etfuturum.api.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.client.EndFlashState;

@SideOnly(Side.CLIENT)
public final class EndFlashAPI {
	private EndFlashAPI() {}
	public static float getIntensity(float partialTicks) { return EndFlashState.INSTANCE.getIntensity(partialTicks); }
	public static float getXAngle() { return EndFlashState.INSTANCE.getXAngle(); }
	public static float getYAngle() { return EndFlashState.INSTANCE.getYAngle(); }
}
