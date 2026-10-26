package ganymedes01.etfuturum.client.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.client.gui.inventory.GuiEditWoodSign;
import ganymedes01.etfuturum.client.particle.CustomParticles;
import ganymedes01.etfuturum.configuration.configs.ConfigFunctions;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import ganymedes01.etfuturum.network.AttackYawMessage;
import ganymedes01.etfuturum.network.BlackHeartParticlesMessage;
import ganymedes01.etfuturum.network.WoodSignOpenMessage;
import ganymedes01.etfuturum.tileentities.TileEntityWoodSign;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;

/** Loaded only through ClientProxy; even lambda methods must stay off the server class path. */
public final class ClientPacketHandler {

	private ClientPacketHandler() {
	}

	public static void handle(IMessage packet, MessageContext context) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.func_152344_a(() -> {
			WorldClient world = mc.theWorld;
			if (world == null || mc.getNetHandler() != context.netHandler) return;
			if (packet instanceof AttackYawMessage message) {
				if (ConfigFunctions.enableAttackedAtYawFix && mc.thePlayer != null && Float.isFinite(message.attackedAtYaw)) {
					mc.thePlayer.attackedAtYaw = message.attackedAtYaw;
				}
			} else if (packet instanceof BlackHeartParticlesMessage message) {
				if (ConfigWorld.enableDmgIndicator && Double.isFinite(message.x)
						&& Double.isFinite(message.y) && Double.isFinite(message.z)) {
					CustomParticles.spawnBlackHeartParticle(world, message.x, message.y, message.z);
				}
			} else if (packet instanceof WoodSignOpenMessage message && world.blockExists(message.tileX, message.tileY, message.tileZ)) {
				TileEntity tile = world.getTileEntity(message.tileX, message.tileY, message.tileZ);
				if (!(tile instanceof TileEntityWoodSign)) {
					tile = new TileEntityWoodSign();
					tile.blockType = Block.getBlockById(message.id);
					tile.setWorldObj(world);
					tile.xCoord = message.tileX;
					tile.yCoord = message.tileY;
					tile.zCoord = message.tileZ;
				}
				tile.markDirty();
				mc.displayGuiScreen(new GuiEditWoodSign((TileEntityWoodSign) tile));
			}
		});
	}
}
