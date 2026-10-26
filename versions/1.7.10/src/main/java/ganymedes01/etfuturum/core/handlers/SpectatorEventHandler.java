package ganymedes01.etfuturum.core.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

public class SpectatorEventHandler {
	public static final SpectatorEventHandler INSTANCE = new SpectatorEventHandler();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (SpectatorUtils.isSpectator(event.entityPlayer)) {
            if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                event.setCanceled(true);
            } else {
                TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
                if (!SpectatorUtils.canSpectatorSelectTileEntity(te)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlace(BlockEvent.PlaceEvent event) {
        if (SpectatorUtils.isSpectator(event.player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityItemPickupEvent event) {
        if (SpectatorUtils.isSpectator(event.entityPlayer)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void itemToss(ItemTossEvent event) {
        if (SpectatorUtils.isSpectator(event.player) && event.player.inventory.addItemStackToInventory(event.entityItem.getEntityItem())) {
            // Cancels any attempt to throw away items, unless they don't fit in the inventory.
            event.setCanceled(true);
        }
    }
}
