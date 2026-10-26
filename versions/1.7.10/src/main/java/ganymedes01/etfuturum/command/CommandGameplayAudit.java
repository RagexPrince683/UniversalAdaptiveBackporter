package ganymedes01.etfuturum.command;

import ganymedes01.etfuturum.configuration.configs.ConfigExperiments;
import ganymedes01.etfuturum.world.AquaticPopulation;
import ganymedes01.etfuturum.world.nether.dimension.NetherWorldChunkManager;
import net.minecraft.command.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.biome.*;
import java.util.*;

/** Operator-only, bounded sampling of the real configured biome pipeline without generating chunks. */
public final class CommandGameplayAudit extends CommandBase {
	private static final Map<World, Sampling> JOBS = new IdentityHashMap<>();
	@Override public String getCommandName() { return "uab_audit"; }
	@Override public java.util.List<String> getCommandAliases() { return java.util.Collections.singletonList("efr_audit"); }
	@Override public int getRequiredPermissionLevel() { return 2; }
	@Override public String getCommandUsage(ICommandSender sender) { return "/uab_audit aquatic [reset] | biomes <surface|nether> <seed> <radius:256..16384> [stride:4..64] | cancel"; }
	@Override public void processCommand(ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();
		if (args.length >= 1 && args[0].equals("aquatic")) {
			for (String line : AquaticPopulation.get(world).report(args.length > 1 && args[1].equals("reset"))) output(sender, line);
			return;
		}
		if (args.length == 1 && args[0].equals("cancel")) { JOBS.remove(world); output(sender, "Biome sampling cancelled."); return; }
		if (args.length < 4 || !args[0].equals("biomes") || !args[1].equals("surface") && !args[1].equals("nether")) throw new WrongUsageException(getCommandUsage(sender));
		if (JOBS.containsKey(world)) throw new CommandException("A biome sample is already running in this world; use /uab_audit cancel first.");
		long seed;
		try { seed = Long.parseLong(args[2]); } catch (NumberFormatException exception) { throw new NumberInvalidException("commands.generic.num.invalid", args[2]); }
		int radius = parseIntBounded(sender, args[3], 256, 16384), stride = args.length > 4 ? parseIntBounded(sender, args[4], 4, 64) : 16;
		WorldChunkManager manager = args[1].equals("surface") ? new WorldChunkManager(seed, world.getWorldInfo().getTerrainType())
				: ConfigExperiments.netherDimensionProvider && !ganymedes01.etfuturum.compat.ModsList.NETHERLICIOUS.isLoaded() ? new NetherWorldChunkManager(seed, world.getWorldInfo().getTerrainType()) : new WorldChunkManagerHell(BiomeGenBase.hell, 0);
		JOBS.put(world, new Sampling(sender, manager, seed, radius, stride, args[1]));
		output(sender, "Sampling " + args[1] + " seed=" + seed + " radius=" + radius + " stride=" + stride + " manager=" + manager.getClass().getSimpleName() + "; one row per server tick; no chunks are loaded.");
	}
	public static void unload(World world) { JOBS.remove(world); }
	public static void tick(World world) {
		Sampling job = JOBS.get(world); if (job == null) return;
		try { if (job.row()) { JOBS.remove(world); job.finish(); } }
		catch (RuntimeException exception) { JOBS.remove(world); output(job.sender, "Biome sampling failed: " + exception); org.apache.logging.log4j.LogManager.getLogger("UAB gameplay audit").error("Biome sampling failed", exception); }
	}
	private static void output(ICommandSender sender, String line) { sender.addChatMessage(new ChatComponentText(line)); org.apache.logging.log4j.LogManager.getLogger("UAB gameplay audit").info("[Gameplay audit] " + line); }
	private static final class Sampling {
		final ICommandSender sender;
		final WorldChunkManager manager;
		final long seed;
		final int radius, stride;
		final String dimension;
		final Map<Integer, Long> counts = new TreeMap<>();
		int z;
		long total;
		Sampling(ICommandSender sender, WorldChunkManager manager, long seed, int radius, int stride, String dimension) { this.sender = sender; this.manager = manager; this.seed = seed; this.radius = radius; this.stride = stride; this.dimension = dimension; z = -radius; }
		boolean row() {
			// Query contiguous strips at block resolution so Voronoi/coastline results match actual worlds.
			BiomeGenBase[] row = manager.getBiomeGenAt(null, -radius, z, radius * 2, 1, false);
			for (int x = 0; x < radius * 2; x += stride) { int id = row[x].biomeID; Long previous = counts.get(id); counts.put(id, previous == null ? 1 : previous + 1); total++; }
			z += stride; return z >= radius;
		}
		void finish() {
			output(sender, "Biome result " + dimension + " seed=" + seed + " rectangle=[" + -radius + "," + radius + ") squared, area=" + (4L * radius * radius) + " blocks2, samples=" + total + ", stride=" + stride + ". Adjacent samples are spatially correlated; binomial significance tests are invalid.");
			for (Map.Entry<Integer, Long> count : counts.entrySet()) output(sender, String.format(Locale.ROOT, "%d %s: %d (%.4f%%)", count.getKey(), BiomeGenBase.getBiome(count.getKey()).biomeName, count.getValue(), count.getValue() * 100.0 / total));
			if (manager instanceof NetherWorldChunkManager) output(sender, "Nether expected selection weights: wastes=" + ganymedes01.etfuturum.configuration.configs.ConfigWorld.netherWastesWeight + " crimson=" + ganymedes01.etfuturum.configuration.configs.ConfigWorld.crimsonForestWeight + " warped=" + ganymedes01.etfuturum.configuration.configs.ConfigWorld.warpedForestWeight + " soul=" + ganymedes01.etfuturum.configuration.configs.ConfigWorld.soulSandValleyWeight + " basalt=" + ganymedes01.etfuturum.configuration.configs.ConfigWorld.basaltDeltasWeight + "; only registered biomes participate.");
			else if (dimension.equals("surface")) output(sender, "Surface modern selection weight=3 within the registered climate pool, before native special climates, oceans, rivers, hills and shores; these are not global area probabilities.");
		}
	}
}
