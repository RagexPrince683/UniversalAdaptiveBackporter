package ganymedes01.etfuturum.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.configuration.configs.ConfigMixins;
import ganymedes01.etfuturum.elytra.IElytraPlayer;
import ganymedes01.etfuturum.items.equipment.ItemArmorElytra;
import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import net.minecraft.item.ItemStack;

public class StartElytraFlyingHandler implements IMessageHandler<StartElytraFlyingMessage, IMessage> {
	@Override
	public IMessage onMessage(StartElytraFlyingMessage message, MessageContext ctx) {
		ServerPacketExecutor.execute(ctx, player -> {
			if (!ConfigMixins.enableElytra || !(player instanceof IElytraPlayer elytraPlayer) || SpectatorUtils.isSpectator(player)
					|| !player.isEntityAlive() || player.isRiding() || player.capabilities.isFlying || player.handleLavaMovement()) return;
			if (!player.onGround && (ConfigMixins.enableNewElytraTakeoffLogic || player.motionY < 0.0D) && !elytraPlayer.etfu$isElytraFlying() && !player.isInWater()) {
				ItemStack itemstack = ItemArmorElytra.getElytra(player);

				if (itemstack != null && itemstack.getItem() instanceof ItemArmorElytra && !ItemArmorElytra.isBroken(itemstack)) {
					elytraPlayer.etfu$setElytraFlying(true);
				}
			} else {
				elytraPlayer.etfu$setElytraFlying(false);
			}
		});
		return null;
	}
}
