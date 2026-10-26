package ganymedes01.etfuturum.command;

import ganymedes01.etfuturum.world.end.dimension.ChunkProviderEFREnd;
import ganymedes01.etfuturum.world.end.gen.MapGenEndCity;
import net.minecraft.command.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import java.util.Random;

/** Keeps the legacy command's wider search; the unified locator uses the bounded overload. */
public class CommandLocateEndCity extends CommandBase {
	@Override public String getCommandName() { return "locate_endcity"; }
	@Override public int getRequiredPermissionLevel() { return 2; }
	@Override public String getCommandUsage(ICommandSender sender) { return "/locate_endcity"; }
	@Override public void processCommand(ICommandSender sender, String[] args) {
		if (args.length != 0) throw new WrongUsageException(getCommandUsage(sender));
		World world = sender.getEntityWorld();
		if (world.provider.dimensionId != 1) throw new CommandException("You must be in the End to locate an End City.");
		ChunkCoordinates origin = sender.getPlayerCoordinates();
		ChunkCoordinates point = nearestCandidate(world, origin.posX, origin.posZ, 50);
		sender.addChatMessage(new ChatComponentText(point == null ? "No End City candidate found within the search radius; check the active End provider."
				: "End City candidate at X: " + point.posX + ", Z: " + point.posZ + "."));
	}
	public static ChunkCoordinates nearestCandidate(World world, int x, int z) {
		return nearestCandidate(world, x, z, 2);
	}
	private static ChunkCoordinates nearestCandidate(World world, int x, int z, int radius) {
		IChunkProvider provider = world.getChunkProvider();
		if (provider instanceof ChunkProviderServer) provider = ((ChunkProviderServer) provider).currentChunkProvider;
		if (!(provider instanceof ChunkProviderEFREnd)) return null;
		ChunkProviderEFREnd end = (ChunkProviderEFREnd) provider;
		int gx = Math.floorDiv(x >> 4, 20), gz = Math.floorDiv(z >> 4, 20);
		double best = Double.MAX_VALUE; ChunkCoordinates result = null;
		for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
			int rx = gx + dx, rz = gz + dz;
			Random random = new Random(rx * 341873128712L + rz * 132897987541L + world.getSeed() + 10387313L);
			int cx = rx * 20 + (random.nextInt(9) + random.nextInt(9)) / 2, cz = rz * 20 + (random.nextInt(9) + random.nextInt(9)) / 2;
			if (!MapGenEndCity.isValidCityAt(end, cx, cz, world.getSeed())) continue;
			int px = cx * 16 + 8, pz = cz * 16 + 8;
			double distance = ((double) px - x) * ((double) px - x) + ((double) pz - z) * ((double) pz - z);
			if (distance < best) { best = distance; result = new ChunkCoordinates(px, -1, pz); }
		}
		return result;
	}
}
