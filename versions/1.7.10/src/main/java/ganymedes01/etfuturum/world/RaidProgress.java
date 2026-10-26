package ganymedes01.etfuturum.world;

import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.village.Village;
import net.minecraft.world.*;
import java.util.*;

/** World-owned, persisted raid state. No global entity or tile searches. */
public class RaidProgress extends WorldSavedData {
	private final List<Raid> raids = new ArrayList<>();
	private int nextId = 1;
	private final Set<Long> outposts = new HashSet<>();
	public void addOutpost(int x, int z) { if (outposts.add(ChunkCoordIntPair.chunkXZ2Int(x, z))) markDirty(); }
	public boolean isOutpost(int x, int z) { return outposts.contains(ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4)); }
	private static final class Raid { int id, x, y, z, wave, delay = 300, age; final Set<UUID> alive = new HashSet<>(); }
	public RaidProgress(String name) { super(name); }
	public static RaidProgress get(World world) {
		RaidProgress data = (RaidProgress) world.perWorldStorage.loadData(RaidProgress.class, "etfuturum_raids");
		if (data == null) { data = new RaidProgress("etfuturum_raids"); world.perWorldStorage.setData("etfuturum_raids", data); }
		return data;
	}
	public boolean start(World world, EntityPlayer player, Village village) {
		if (raids.size() >= 16) return false;
		ChunkCoordinates center = village.getCenter();
		for (Raid raid : raids) if ((raid.x - center.posX) * (raid.x - center.posX) + (raid.z - center.posZ) * (raid.z - center.posZ) < 9216) return false;
		Raid raid = new Raid(); raid.id = nextId++; raid.x = center.posX; raid.y = center.posY; raid.z = center.posZ; raids.add(raid); markDirty(); player.addChatMessage(new ChatComponentText("A raid is approaching the village!")); return true;
	}
	public boolean isActive(int id) { for (Raid raid : raids) if (raid.id == id) return true; return false; }
	public void died(EntityIllager mob) { for (Raid raid : raids) if (raid.id == mob.raidId && raid.alive.remove(mob.getUniqueID())) { markDirty(); break; } }
	public void tick(World world) {
		if (world.provider.dimensionId != 0 || !ConfigEntities.enableRaids) return;
		if (world.difficultySetting == EnumDifficulty.PEACEFUL) { if (!raids.isEmpty()) { raids.clear(); markDirty(); } return; }
		Iterator<Raid> iterator = raids.iterator();
		while (iterator.hasNext()) {
			Raid raid = iterator.next();
			if (++raid.age > 3600) { iterator.remove(); markDirty(); continue; }
			if (world.getClosestPlayer(raid.x, raid.y, raid.z, 128) == null || !world.blockExists(raid.x, raid.y, raid.z)) continue;
			Village village = world.villageCollectionObj.findNearestVillage(raid.x, raid.y, raid.z, 48);
			if (village == null || village.getNumVillagers() == 0) { iterator.remove(); markDirty(); continue; }
			if (!raid.alive.isEmpty()) continue;
			int waves = world.difficultySetting == EnumDifficulty.EASY ? 3 : world.difficultySetting == EnumDifficulty.HARD ? 7 : 5;
			if (raid.wave >= waves) {
				for (Object object : world.playerEntities) { EntityPlayer player = (EntityPlayer) object; if (player.getDistanceSq(raid.x, raid.y, raid.z) <= 9216) { player.getEntityData().setInteger("EFRHeroOfVillage", 48000); player.addExperience(100); player.addChatMessage(new ChatComponentText("Raid defeated! The village celebrates your victory.")); } }
				iterator.remove(); markDirty(); continue;
			}
			if (raid.delay-- > 0) continue;
			int spawned = wave(world, raid);
			if (spawned > 0) { raid.wave++; raid.delay = 15; markDirty(); } else raid.delay = 10;
		}
		if (!raids.isEmpty()) markDirty();
	}
	private int wave(World world, Raid raid) {
		int spawned = 0;
		for (int i = 0, count = 4 + raid.wave; i < count; i++) {
			EntityIllager mob = raid.wave >= 2 && i == 0 ? new EntityRavager(world) : raid.wave >= 3 && i == 1 ? new EntityEvoker(world) : i % 3 == 0 ? new EntityVindicator(world) : new EntityPillager(world);
			for (int attempt = 0; attempt < 8; attempt++) {
				double angle = world.rand.nextDouble() * Math.PI * 2;
				int x = raid.x + (int) (Math.cos(angle) * 48), z = raid.z + (int) (Math.sin(angle) * 48);
				if (!world.blockExists(x, 0, z)) continue;
				int y = world.getTopSolidOrLiquidBlock(x, z);
				mob.setLocationAndAngles(x + .5, y, z + .5, 0, 0);
				if (world.isAnyLiquid(mob.boundingBox) || !world.getCollidingBoundingBoxes(mob, mob.boundingBox).isEmpty() || !world.checkNoEntityCollision(mob.boundingBox)) continue;
				mob.raidId = raid.id; mob.setHomeArea(raid.x, raid.y, raid.z, 64); mob.setCaptain(i == 0 && !(mob instanceof EntityRavager));
				if (world.spawnEntityInWorld(mob)) { raid.alive.add(mob.getUniqueID()); spawned++; }
				break;
			}
		}
		return spawned;
	}
	@Override public void writeToNBT(NBTTagCompound tag) {
		NBTTagList towers = new NBTTagList(); for (long position : outposts) { NBTTagCompound tower = new NBTTagCompound(); tower.setLong("Pos", position); towers.appendTag(tower); } tag.setTag("Outposts", towers);
		tag.setInteger("NextId", nextId); NBTTagList list = new NBTTagList();
		for (Raid raid : raids) { NBTTagCompound entry = new NBTTagCompound(); entry.setInteger("Id", raid.id); entry.setInteger("X", raid.x); entry.setInteger("Y", raid.y); entry.setInteger("Z", raid.z); entry.setInteger("Wave", raid.wave); entry.setInteger("Delay", raid.delay); entry.setInteger("Age", raid.age); NBTTagList mobs = new NBTTagList(); for (UUID mob : raid.alive) mobs.appendTag(new NBTTagString(mob.toString())); entry.setTag("Mobs", mobs); list.appendTag(entry); }
		tag.setTag("Raids", list);
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		outposts.clear(); NBTTagList towers = tag.getTagList("Outposts", 10); for (int i = 0; i < towers.tagCount(); i++) outposts.add(towers.getCompoundTagAt(i).getLong("Pos"));
		raids.clear(); nextId = Math.max(1, tag.getInteger("NextId")); NBTTagList list = tag.getTagList("Raids", 10);
		for (int i = 0; i < Math.min(16, list.tagCount()); i++) { NBTTagCompound entry = list.getCompoundTagAt(i); Raid raid = new Raid(); raid.id = Math.max(1, entry.getInteger("Id")); raid.x = entry.getInteger("X"); raid.y = MathHelper.clamp_int(entry.getInteger("Y"), 1, 255); raid.z = entry.getInteger("Z"); raid.wave = MathHelper.clamp_int(entry.getInteger("Wave"), 0, 7); raid.delay = MathHelper.clamp_int(entry.getInteger("Delay"), 0, 300); raid.age = MathHelper.clamp_int(entry.getInteger("Age"), 0, 3601); NBTTagList mobs = entry.getTagList("Mobs", 8); for (int n = 0; n < Math.min(64, mobs.tagCount()); n++) try { raid.alive.add(UUID.fromString(mobs.getStringTagAt(n))); } catch (IllegalArgumentException ignored) { } raids.add(raid); nextId = Math.max(nextId, raid.id + 1); }
	}
}
