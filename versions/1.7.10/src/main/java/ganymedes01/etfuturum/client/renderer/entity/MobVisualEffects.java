package ganymedes01.etfuturum.client.renderer.entity;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.potion.ModPotions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.lwjgl.opengl.GL11;
public class MobVisualEffects {
	@SubscribeEvent public void darkness(EntityViewRenderEvent.FogDensity event) { if (ModPotions.darkness != null && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.isPotionActive(ModPotions.darkness)) { GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP); event.density = .055F + (float) Math.sin((Minecraft.getMinecraft().thePlayer.ticksExisted + event.renderPartialTicks) * .04) * .015F; event.setCanceled(true); } }
	@SubscribeEvent public void darknessColor(EntityViewRenderEvent.FogColors event) { if (ModPotions.darkness != null && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.isPotionActive(ModPotions.darkness)) { event.red *= .1F; event.green *= .1F; event.blue *= .1F; } }
}
