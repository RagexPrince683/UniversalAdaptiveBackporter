package ganymedes01.etfuturum.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.entities.EntityNewBoat;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.server.MinecraftServer;

public class BoatMoveHandler implements IMessageHandler<BoatMoveMessage, IMessage> {
	@Override
	public IMessage onMessage(BoatMoveMessage message, MessageContext ctx) {
		ServerPacketExecutor.execute(ctx, player -> {
			if (message.dimensionId != player.dimension || !(player.ridingEntity instanceof EntityNewBoat vehicle)
					|| vehicle.getEntityId() != message.entityId || vehicle.riddenByEntity != player || vehicle.isDead) return;
			if (!Double.isFinite(message.x) || !Double.isFinite(message.y) || !Double.isFinite(message.z)
					|| !Float.isFinite(message.yaw) || !Float.isFinite(message.pitch)
					|| Math.abs(message.x) > 3.0E7D || Math.abs(message.y) > 3.0E7D || Math.abs(message.z) > 3.0E7D) {
				ctx.getServerHandler().sendPacket(new S18PacketEntityTeleport(vehicle));
				return;
			}
			double expectedDelta = vehicle.motionX * vehicle.motionX + vehicle.motionY * vehicle.motionY + vehicle.motionZ * vehicle.motionZ;
			double deltaX = message.x - vehicle.posX;
			double deltaY = message.y - vehicle.posY;
			double deltaZ = message.z - vehicle.posZ;
			double actualDelta = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
			MinecraftServer server = MinecraftServer.getServer();
			if (Math.abs(expectedDelta - actualDelta) > 100.0D && !server.isSinglePlayer()) {
				ctx.getServerHandler().sendPacket(new S18PacketEntityTeleport(vehicle));
				return;
			}
			vehicle.setPositionAndRotation(message.x, message.y, message.z, message.yaw, message.pitch);
			ctx.getServerHandler().sendPacket(new S18PacketEntityTeleport(vehicle));
		});
		return null;
	}
}
