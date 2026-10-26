package ganymedes01.etfuturum.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.entities.EntityNewBoatWithChest;

public class ChestBoatOpenInventoryHandler implements IMessageHandler<ChestBoatOpenInventoryMessage, IMessage> {
	@Override
	public IMessage onMessage(ChestBoatOpenInventoryMessage message, MessageContext ctx) {
		ServerPacketExecutor.execute(ctx, player -> {
			if (player.ridingEntity instanceof EntityNewBoatWithChest boat && boat.isUseableByPlayer(player))
				player.displayGUIChest(boat);
		});
		return null;
	}
}
