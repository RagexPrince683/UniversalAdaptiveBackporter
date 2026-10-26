package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.*;
import java.util.*;
final class MobVisualEffects {
    private final Set<RenderWolf> wolves=Collections.newSetFromMap(new WeakHashMap<>());
    @SubscribeEvent public void density(EntityViewRenderEvent.FogDensity event){if(Minecraft.getMinecraft().player!=null&&Minecraft.getMinecraft().player.isPotionActive(AquaticEffects.DARKNESS)){GlStateManager.setFog(GlStateManager.FogMode.EXP);event.setDensity(.055F+(float)Math.sin((Minecraft.getMinecraft().player.ticksExisted+event.getRenderPartialTicks())*.04)*.015F);event.setCanceled(true);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void color(EntityViewRenderEvent.FogColors event){if(Minecraft.getMinecraft().player!=null&&Minecraft.getMinecraft().player.isPotionActive(AquaticEffects.DARKNESS)){event.setRed(event.getRed()*.1F);event.setGreen(event.getGreen()*.1F);event.setBlue(event.getBlue()*.1F);}}
    @SubscribeEvent public void wolf(RenderLivingEvent.Pre<?> event){if(!(event.getRenderer() instanceof RenderWolf))return;RenderWolf renderer=(RenderWolf)event.getRenderer();if(!wolves.add(renderer))return;renderer.addLayer(new LayerRenderer<EntityWolf>(){
        @Override public void doRenderLayer(EntityWolf wolf,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(wolf.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()!=Content.item("wolf_armor"))return;Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(UAB.ID,"textures/entity/equipment/wolf_body/armadillo_scute.png"));GlStateManager.color(1,1,1,1);renderer.getMainModel().render(wolf,limb,amount,age,yaw,pitch,scale);}
        @Override public boolean shouldCombineTextures(){return false;}
    });}
}
