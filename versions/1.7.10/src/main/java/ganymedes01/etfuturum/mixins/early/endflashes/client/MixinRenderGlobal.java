package ganymedes01.etfuturum.mixins.early.endflashes.client;

import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.api.client.EndFlashAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProviderEnd;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {
	@Unique private static final ResourceLocation etfuturum$END_FLASH =
			new ResourceLocation("etfuturum:textures/environment/end_flash.png");
	@Shadow private Minecraft mc;
	@Shadow private WorldClient theWorld;

	@Inject(method = "renderSky", at = @At("TAIL"), require=0,expect=0)
	private void etfuturum$renderEndFlash(float partialTicks, CallbackInfo ci) {
		if (theWorld == null || !(theWorld.provider instanceof WorldProviderEnd)) return;
		float intensity = EndFlashAPI.getIntensity(partialTicks);
		if (intensity <= 0.01F) return;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		try {
			GL11.glDisable(GL11.GL_FOG);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			mc.getTextureManager().bindTexture(etfuturum$END_FLASH);
			GL11.glRotatef(EndFlashAPI.getYAngle(), 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(EndFlashAPI.getXAngle(), 1.0F, 0.0F, 0.0F);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, intensity);
			tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, 0.0D, 0.0D);
			tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, 0.0D, 1.0D);
			tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, 1.0D, 1.0D);
			tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, 1.0D, 0.0D);
			tessellator.draw();
		} finally {
			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
	}
}
