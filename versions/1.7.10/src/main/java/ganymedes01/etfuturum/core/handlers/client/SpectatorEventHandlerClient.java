package ganymedes01.etfuturum.core.handlers.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.*;

public final class SpectatorEventHandlerClient {
	public static final SpectatorEventHandlerClient INSTANCE = new SpectatorEventHandlerClient();

	private SpectatorEventHandlerClient() {
	}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderPlayerArmor(RenderPlayerEvent.Specials.Pre event) {
        if (SpectatorUtils.isSpectator(event.entityPlayer)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderLivingEvent.Pre event) {
        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        Entity following = SpectatorUtils.getSpectatingEntity(player);
        if (SpectatorUtils.isSpectator(player) && event.entity.equals(following) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onOverlayRenderPre(RenderGameOverlayEvent.Pre event) {
        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        if (SpectatorUtils.isSpectator(player)) {
            if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
                event.setCanceled(true);
            } else if(event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && SpectatorUtils.getSpectatingEntity(player) == null) {
                MovingObjectPosition mop = FMLClientHandler.instance().getClient().objectMouseOver;
                if (mop == null) {
                    event.setCanceled(true);
                    return;
                }
                if(mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
                    if (!SpectatorUtils.canSpectatorSelectTileEntity(te)) {
                        event.setCanceled(true);
                    }
                } else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    if(mop.entityHit == null || SpectatorUtils.getSpectatingEntity(player) != null) {
                        event.setCanceled(true);
                    }
                } else {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onHandRender(RenderHandEvent event) {
        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        if (SpectatorUtils.isSpectator(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFireRender(RenderBlockOverlayEvent event) {
        if (SpectatorUtils.isSpectator(event.player)) {
            event.setCanceled(true);
        }
    }

    /* TODO look into increasing the distance instead of outright disabling it */
    @SubscribeEvent
    public void onRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (SpectatorUtils.isSpectator(event.entity) && event.block.getMaterial().isLiquid()) {
            event.setCanceled(true);
            event.density = 0;
        }
    }

    @SubscribeEvent
    public void onBlockHighlight(DrawBlockHighlightEvent event) {
        if (SpectatorUtils.isSpectator(event.player) && event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(event.target.blockX, event.target.blockY, event.target.blockZ);
            if (!SpectatorUtils.canSpectatorSelectTileEntity(te)) {
                event.setCanceled(true);
            }
        }
    }

    private static void toggleVisibility(ModelBiped biped, boolean visible) {
        biped.bipedBody.showModel = visible;
        biped.bipedRightArm.showModel = visible;
        biped.bipedLeftArm.showModel = visible;
        biped.bipedRightLeg.showModel = visible;
        biped.bipedLeftLeg.showModel = visible;
    }

    public static void restorePlayerModel(EntityPlayer player) {
        if (RenderManager.instance.getEntityRenderObject(player) instanceof RenderPlayer renderer) {
            toggleVisibility(renderer.modelBipedMain, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if(SpectatorUtils.isSpectator(event.entityPlayer)) {
            if(SpectatorUtils.getSpectatingEntity(event.entityPlayer) != null) {
                event.setCanceled(true);
            } else {
                toggleVisibility(event.renderer.modelBipedMain, false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if(SpectatorUtils.isSpectator(event.entityPlayer)) {
            toggleVisibility(event.renderer.modelBipedMain, true);
        }
    }
}
