package ganymedes01.etfuturum.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Forge 1.7.10 invokes simple-channel handlers on Netty threads, not the server thread. */
public final class ServerPacketExecutor {

	public static final ServerPacketExecutor INSTANCE = new ServerPacketExecutor();
	private static final ArrayBlockingQueue<PendingAction> PENDING = new ArrayBlockingQueue<>(4096);
	private static final Map<NetHandlerPlayServer, Integer> PER_CONNECTION = new IdentityHashMap<>();
	private static long generation;
	private static boolean accepting;

	private ServerPacketExecutor() {
	}

	public static synchronized void start() {
		PENDING.clear();
		PER_CONNECTION.clear();
		generation++;
		accepting = true;
	}

	public static synchronized void stop() {
		accepting = false;
		PENDING.clear();
		PER_CONNECTION.clear();
		generation++;
	}

	public static synchronized void execute(MessageContext context, Consumer<EntityPlayerMP> action) {
		if (!accepting) return;
		NetHandlerPlayServer handler = context.getServerHandler();
		if (handler == null || handler.playerEntity == null || !handler.netManager.isChannelOpen()) return;
		int queued = PER_CONNECTION.getOrDefault(handler, 0);
		if (queued >= 128) return;
		EntityPlayerMP player = handler.playerEntity;
		World world = player.worldObj;
		long epoch = generation;
		// Bound retained work, and discard packets from an old connection, respawn or dimension.
		if (PENDING.offer(new PendingAction(handler, () -> {
			if (accepting && generation == epoch && handler.netManager.isChannelOpen()
					&& handler.playerEntity == player && player.playerNetServerHandler == handler
					&& player.worldObj == world && net.minecraftforge.common.DimensionManager.getWorld(player.dimension) == world
					&& !player.isDead) {
				action.accept(player);
			}
		}))) PER_CONNECTION.put(handler, queued + 1);
	}

	private static synchronized PendingAction poll() {
		PendingAction pending = PENDING.poll();
		if (pending != null) {
			int count = PER_CONNECTION.getOrDefault(pending.handler, 1) - 1;
			if (count == 0) PER_CONNECTION.remove(pending.handler);
			else PER_CONNECTION.put(pending.handler, count);
		}
		return pending;
	}

	private static final class PendingAction {
		private final NetHandlerPlayServer handler;
		private final Runnable action;
		private PendingAction(NetHandlerPlayServer handler, Runnable action) {
			this.handler = handler;
			this.action = action;
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		int count = Math.min(256, PENDING.size());
		for (int i = 0; i < count; i++) {
			PendingAction task = poll();
			if (task == null) break;
			task.action.run();
		}
	}
}
