package ganymedes01.etfuturum.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.entities.EntityArmourStand;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import net.minecraft.entity.Entity;

public class ArmourStandInteractHandler implements IMessageHandler<ArmourStandInteractMessage, IMessage> {

	@Override
	public IMessage onMessage(ArmourStandInteractMessage message, MessageContext ctx) {
		ServerPacketExecutor.execute(ctx, player -> {
			if (!ConfigBlocksItems.enableArmourStand || SpectatorUtils.isSpectator(player)
					|| message.dimID != player.dimension || message.playerID != player.getEntityId()
					|| message.hitPos == null || !Double.isFinite(message.hitPos.xCoord)
					|| !Double.isFinite(message.hitPos.yCoord) || !Double.isFinite(message.hitPos.zCoord)) return;
			Entity entity = player.worldObj.getEntityByID(message.standID);
			if (!(entity instanceof EntityArmourStand stand) || stand.isDead) return;
			double reachSquared = player.canEntityBeSeen(stand) ? 36.0D : 9.0D;
			if (player.getDistanceSqToEntity(stand) >= reachSquared
					|| Math.abs(message.hitPos.xCoord) > stand.width / 2.0D + 0.25D
					|| Math.abs(message.hitPos.zCoord) > stand.width / 2.0D + 0.25D
					|| message.hitPos.yCoord < -0.25D || message.hitPos.yCoord > stand.height + 0.25D) return;
			stand.interact(player, message.hitPos);
		});
		return null;
	}
}
