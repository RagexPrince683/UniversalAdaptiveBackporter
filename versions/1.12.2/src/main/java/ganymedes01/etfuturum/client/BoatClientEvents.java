package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.entities.EntityNewBoatWithChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
public final class BoatClientEvents {
 @SubscribeEvent public void inventory(GuiOpenEvent event){Minecraft mc=Minecraft.getMinecraft();if(event.getGui() instanceof GuiInventory&&mc.player!=null&&mc.player.getRidingEntity() instanceof EntityNewBoatWithChest){event.setCanceled(true);mc.player.connection.sendPacket(new CPacketEntityAction(mc.player,CPacketEntityAction.Action.OPEN_INVENTORY));}}
}
