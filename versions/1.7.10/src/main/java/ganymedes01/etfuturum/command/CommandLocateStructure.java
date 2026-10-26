package ganymedes01.etfuturum.command;

import ganymedes01.etfuturum.configuration.configs.*;
import ganymedes01.etfuturum.world.RaidProgress;
import ganymedes01.etfuturum.world.structure.*;
import ganymedes01.etfuturum.world.structure.surface.SurfaceStructures;
import ganymedes01.etfuturum.world.structure.underground.UndergroundStructures;
import net.minecraft.command.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.structure.MapGenStructureData;
import java.util.*;

/** Operator discovery reads saved starts first; prediction never generates terrain. */
public class CommandLocateStructure extends CommandBase {
	private static final String[] TYPES = {"ancient_city", "trial_chambers", "woodland_mansion", "trail_ruins", "ocean_monument", "shipwreck", "ocean_ruin", "buried_treasure", "pillager_outpost", "bastion_remnant", "end_city"};
	@Override public String getCommandName() { return "uab_locate"; }
	@Override public List<String> getCommandAliases() { return Collections.singletonList("efr_locate"); }
	@Override public int getRequiredPermissionLevel() { return 2; }
	@Override public String getCommandUsage(ICommandSender sender) { return "/uab_locate <structure> (Tab lists structures)"; }
	@Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) { return args.length == 1 ? getListOfStringsMatchingLastWord(args, TYPES) : null; }
	@Override public void processCommand(ICommandSender sender, String[] args) {
		if (args.length != 1 || !Arrays.asList(TYPES).contains(args[0])) throw new WrongUsageException(getCommandUsage(sender));
		String name = args[0]; World world = sender.getEntityWorld();
		int dimension = name.equals("end_city") ? 1 : name.equals("bastion_remnant") ? -1 : 0;
		if (world.provider.dimensionId != dimension) throw new CommandException("Locate " + name + " in the " + (dimension == 1 ? "End" : dimension == -1 ? "Nether" : "Overworld") + ".");
		ChunkCoordinates origin = sender.getPlayerCoordinates();
		ChunkCoordinates point = saved(world, name, origin.posX, origin.posZ);
		boolean saved = point != null;
		if (point == null && world.getWorldInfo().isMapFeaturesEnabled()) {
			if (name.equals("ancient_city") || name.equals("trial_chambers")) point = UndergroundStructures.nearest(world, origin.posX, origin.posZ, name.equals("ancient_city"));
			else if (name.equals("woodland_mansion") || name.equals("trail_ruins")) point = SurfaceStructures.nearest(world, origin.posX, origin.posZ, name.equals("woodland_mansion") ? 0 : 1);
			else if (name.equals("ocean_monument") && ConfigWorld.enableOceanMonuments && ganymedes01.etfuturum.ModBlocks.PRISMARINE_BLOCK.isEnabled() && ganymedes01.etfuturum.ModBlocks.SEA_LANTERN.isEnabled()) point = monument(world, origin.posX, origin.posZ);
			else if (name.equals("bastion_remnant") && ConfigExperiments.netherDimensionProvider) point = ganymedes01.etfuturum.world.nether.structure.MapGenBastion.nearestCandidate(world, origin.posX, origin.posZ);
			else if (name.equals("end_city") && ConfigExperiments.enableEndCities) point = CommandLocateEndCity.nearestCandidate(world, origin.posX, origin.posZ);
		}
		if (point == null) sender.addChatMessage(new ChatComponentText("No " + name + " found. Searches use saved locations and, where supported, candidates within two placement regions. Explore new terrain and check generation settings and prerequisites."));
		else sender.addChatMessage(new ChatComponentText(name + " at X: " + point.posX + (point.posY < 0 ? "" : ", Y: " + point.posY) + ", Z: " + point.posZ + (saved ? " (saved structure)." : " (new-terrain candidate; placement is subject to generation checks).")));
	}

	private static ChunkCoordinates saved(World world, String name, int x, int z) {
		String id; Class<? extends WorldSavedData> type = MapGenStructureData.class;
		switch (name) {
			case "ancient_city": case "trial_chambers": id = "EFRUnderground"; break;
			case "woodland_mansion": case "trail_ruins": id = "UABSurfaceStructures"; break;
			case "ocean_monument": id = "EFROceanMonument"; break;
			case "bastion_remnant": id = "EFRBastion"; break;
			case "end_city": id = "EndCity"; break;
			case "shipwreck": case "ocean_ruin": id = "etfuturum_ocean_sites"; type = OceanSites.class; break;
			case "buried_treasure": id = "etfuturum_ocean_treasure"; type = OceanTreasure.class; break;
			case "pillager_outpost": id = "etfuturum_raids"; type = RaidProgress.class; break;
			default: return null;
		}
		WorldSavedData data = world.perWorldStorage.loadData(type, id); if (data == null) return null;
		NBTTagCompound tag = new NBTTagCompound(); data.writeToNBT(tag);
		List<ChunkCoordinates> points = new ArrayList<>();
		if (data instanceof MapGenStructureData) {
			NBTTagCompound starts = tag.getCompoundTag("Features");
			for (Object key : starts.func_150296_c()) {
				NBTTagCompound start = starts.getCompoundTag((String) key);
				if ((name.equals("ancient_city") || name.equals("trial_chambers")) && start.getBoolean("City") != name.equals("ancient_city")) continue;
				if ((name.equals("woodland_mansion") || name.equals("trail_ruins")) && start.getInteger("Kind") != (name.equals("woodland_mansion") ? 0 : 1)) continue;
				int[] box = start.getIntArray("BB");
				if (box.length == 6) points.add(new ChunkCoordinates(start.getInteger("ChunkX") * 16 + 8, name.equals("ocean_monument") ? 58 : box[1], start.getInteger("ChunkZ") * 16 + 8));
			}
		} else if (data instanceof OceanSites) {
			NBTTagList list = tag.getTagList("Sites", 10);
			for (int i = 0; i < list.tagCount(); i++) { NBTTagCompound site = list.getCompoundTagAt(i);
				if (site.getString("Template").startsWith(name.equals("shipwreck") ? "shipwreck/" : "underwater_ruin/")) points.add(new ChunkCoordinates(site.getInteger("X"), site.getInteger("Y"), site.getInteger("Z")));
			}
		} else if (data instanceof OceanTreasure) {
			NBTTagList list = tag.getTagList("Locations", 10);
			for (int i = 0; i < list.tagCount(); i++) { int[] p = list.getCompoundTagAt(i).getIntArray("Position"); if (p.length == 3) points.add(new ChunkCoordinates(p[0], p[1], p[2])); }
		} else {
			NBTTagList list = tag.getTagList("Outposts", 10);
			for (int i = 0; i < list.tagCount(); i++) { long p = list.getCompoundTagAt(i).getLong("Pos"); points.add(new ChunkCoordinates((int) p * 16 + 8, -1, (int) (p >> 32) * 16 + 8)); }
		}
		ChunkCoordinates result = null; double best = Double.MAX_VALUE;
		for (ChunkCoordinates p : points) { double d = distance(p, x, z); if (d < best) { best = d; result = p; } }
		return result;
	}
	private static double distance(ChunkCoordinates p, int x, int z) { double dx = (double) p.posX - x, dz = (double) p.posZ - z; return dx * dx + dz * dz; }
	private static ChunkCoordinates monument(World world, int x, int z) {
		ChunkCoordinates result = null; double best = Double.MAX_VALUE;
		int gx = Math.floorDiv(x >> 4, 32), gz = Math.floorDiv(z >> 4, 32);
		for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
			int rx = gx + dx, rz = gz + dz; Random random = new Random(rx * 341873128712L + rz * 132897987541L + world.getSeed() + 10387313L);
			int cx = rx * 32 + (random.nextInt(27) + random.nextInt(27)) / 2, cz = rz * 32 + (random.nextInt(27) + random.nextInt(27)) / 2;
			if (!OceanMonument.canSpawnAt(world, cx, cz)) continue;
			ChunkCoordinates p = new ChunkCoordinates(cx * 16 + 8, 58, cz * 16 + 8); double d = distance(p, x, z);
			if (d < best) { best = d; result = p; }
		}
		return result;
	}
}
