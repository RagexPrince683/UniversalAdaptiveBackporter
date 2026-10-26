package ganymedes01.etfuturum.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.EtFuturum;

public class BlackHeartParticlesHandler implements IMessageHandler<BlackHeartParticlesMessage, IMessage> {
	@Override
	public IMessage onMessage(BlackHeartParticlesMessage message, MessageContext ctx) {
		EtFuturum.proxy.handleClientPacket(message, ctx);
		return null;
	}
}
