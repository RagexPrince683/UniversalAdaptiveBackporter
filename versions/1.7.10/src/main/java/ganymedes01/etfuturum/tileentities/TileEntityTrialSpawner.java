package ganymedes01.etfuturum.tileentities;

import com.google.gson.*;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Native finite encounters; released chamber configuration is separate from the legacy dungeon variant. */
public class TileEntityTrialSpawner extends TileEntity {
	private int cooldown, spawned, activeTicks, spawnDelay, warmup;
	private String configuration = "";
	private boolean ominous, rewarding;
	private final ArrayDeque<ItemStack> rewards = new ArrayDeque<>();
	private final Set<UUID> active = new HashSet<>(), players = new HashSet<>();
	private final Map<UUID, int[]> lastPositions = new HashMap<>();
	private final Map<UUID, Integer> entityIds = new HashMap<>();
	private static final Map<String, JsonObject> CONFIGS = new HashMap<>();
	public void configure(String id) { if (!id.startsWith("minecraft:trial_chamber/") || !id.endsWith("/normal")) throw new IllegalArgumentException("Invalid trial configuration " + id); configuration = id; markDirty(); }
	private JsonObject config() {
		String key = configuration.replace("/normal", ominous ? "/ominous" : "/normal");
		return CONFIGS.computeIfAbsent(key, id -> {
			try (InputStream in = getClass().getResourceAsStream("/data/trial_spawner/" + id.replace("minecraft:", "") + ".json")) {
				if (in == null) throw new IllegalArgumentException("Missing trial configuration " + id);
				return new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			} catch (IOException e) { throw new IllegalStateException(key, e); }
		});
	}
	private int count(JsonObject config, String key, int fallback) { return config.has(key) ? (int) Math.ceil(config.get(key).getAsFloat()) : fallback; }
	private boolean visible(EntityPlayer player) {
		MovingObjectPosition hit = worldObj.func_147447_a(Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ), Vec3.createVectorHelper(xCoord + .5, yCoord + .8, zCoord + .5), false, true, false);
		return hit == null || hit.blockX == xCoord && hit.blockY == yCoord && hit.blockZ == zCoord;
	}
	@Override public void updateEntity() {
		if (worldObj.isRemote || worldObj.getTotalWorldTime() % 20 != 0) return;
		warmup++; markDirty();
		if (cooldown > 0) { state(0); cooldown--; if (cooldown == 0) { ominous = false; players.clear(); } return; }
		if (rewarding) {
			state(2);
			if (!rewards.isEmpty()) { ItemStack reward = rewards.peek(); if (worldObj.spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(worldObj, xCoord + .5, yCoord + 1, zCoord + .5, reward.copy()))) { rewards.remove(); sound("eject_item"); } }
			else { rewarding = false; spawned = 0; cooldown = 1800; activeTicks = 0; players.clear(); }
			return;
		}
		if (worldObj.difficultySetting == net.minecraft.world.EnumDifficulty.PEACEFUL || !worldObj.getGameRules().getGameRuleBooleanValue("doMobSpawning")) { state(0); return; }
		for (Object object : worldObj.playerEntities) {
			EntityPlayer player = (EntityPlayer) object;
			if (player.capabilities.disableDamage || player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) > 196 || !visible(player)) continue;
			if (players.size() < 128) players.add(player.getUniqueID());
			NBTTagCompound data = player.getEntityData();
			if (!configuration.isEmpty() && data.getInteger("EFRBadOmen") > 0) { data.setInteger("EFRBadOmen", 0); data.setInteger("EFRTrialOmen", 18000 * (1 + MathHelper.clamp_int(data.getInteger("EFRBadOmenAmplifier"), 0, 4))); }
			if (!configuration.isEmpty() && data.getInteger("EFRTrialOmen") > 0 && !ominous) { ominous = true; sound("ominous_activate"); }
		}
		if (players.isEmpty()) { state(0); return; }
		state(1);
		Set<UUID> present = new HashSet<>();
		boolean recoverIds = false;
		for (UUID id : active) {
			Integer entityId = entityIds.get(id); Entity entity = entityId == null ? null : worldObj.getEntityByID(entityId);
			if (entity instanceof EntityLiving mob && id.equals(mob.getUniqueID()) && mob.isEntityAlive()) { present.add(id); lastPositions.put(id, new int[]{MathHelper.floor_double(mob.posX), MathHelper.floor_double(mob.posY), MathHelper.floor_double(mob.posZ)}); }
			else recoverIds = true;
		}
		// Native runtime IDs are transient; recover saved UUIDs across the whole loaded world after a reload.
		if (recoverIds) for (Object object : worldObj.loadedEntityList) {
			if (object instanceof EntityLiving mob && mob.isEntityAlive() && active.contains(mob.getUniqueID())) { UUID id = mob.getUniqueID(); entityIds.put(id, mob.getEntityId()); present.add(id); lastPositions.put(id, new int[]{MathHelper.floor_double(mob.posX), MathHelper.floor_double(mob.posY), MathHelper.floor_double(mob.posZ)}); }
		}
		// Missing entities in unloaded chunks remain outstanding; absence is not proof of a kill.
		if (warmup > 10) for (Iterator<UUID> it = active.iterator(); it.hasNext();) { UUID id = it.next(); int[] pos = lastPositions.get(id); if (!present.contains(id) && pos != null && worldObj.blockExists(pos[0], pos[1], pos[2])) { it.remove(); lastPositions.remove(id); entityIds.remove(id); } }
		if (++activeTicks > 1800) { active.clear(); lastPositions.clear(); entityIds.clear(); spawned = 0; cooldown = 1800; activeTicks = 0; state(0); return; }
		JsonObject config = configuration.isEmpty() ? null : config(); int additional = Math.max(0, players.size() - 1);
		int total = config == null ? 6 : count(config, "total_mobs", 6) + additional * count(config, "total_mobs_added_per_player", 2);
		int simultaneous = config == null ? 2 : count(config, "simultaneous_mobs", 2) + (int) Math.ceil(additional * (config.has("simultaneous_mobs_added_per_player") ? config.get("simultaneous_mobs_added_per_player").getAsFloat() : 1));
		if (spawned >= total && active.isEmpty()) {
			for (UUID ignored : players) {
				if (config == null) rewards.add(worldObj.rand.nextBoolean() ? new ItemStack(net.minecraft.init.Items.emerald, 2 + worldObj.rand.nextInt(4)) : ModItems.OMINOUS_BOTTLE.newItemStack());
				else {
					String table = "minecraft:spawners/trial_chamber/" + (worldObj.rand.nextBoolean() ? "key" : "consumables");
					if (config.has("loot_tables_to_eject")) {
						JsonArray tables = config.getAsJsonArray("loot_tables_to_eject"); int weight = 0; for (JsonElement e : tables) weight += e.getAsJsonObject().get("weight").getAsInt(); int choice = worldObj.rand.nextInt(weight);
						for (JsonElement e : tables) { JsonObject entry = e.getAsJsonObject(); choice -= entry.get("weight").getAsInt(); if (choice < 0) { table = entry.get("data").getAsString(); break; } }
					}
					rewards.addAll(ganymedes01.etfuturum.world.structure.underground.UndergroundLoot.roll(table, worldObj.rand));
				}
			}
			rewarding = true; state(2); return;
		}

		if (active.size() >= simultaneous || spawned >= total || --spawnDelay > 0) return;
		spawnDelay = config == null ? 3 : Math.max(1, count(config, "ticks_between_spawn", 40) / 20);
		for (int attempt = 0; attempt < 8; attempt++) {
			int px = xCoord + worldObj.rand.nextInt(9) - 4, pz = zCoord + worldObj.rand.nextInt(9) - 4;
			if (!worldObj.blockExists(px - 1, yCoord + 3, pz - 1) || !worldObj.blockExists(px + 1, yCoord, pz + 1)) continue;
			EntityLiving mob = createMob(config, px, yCoord + 1, pz);
			if (!worldObj.getCollidingBoundingBoxes(mob, mob.boundingBox).isEmpty() || !worldObj.checkNoEntityCollision(mob.boundingBox) || worldObj.isAnyLiquid(mob.boundingBox) || !worldObj.getBlock(px, yCoord, pz).isOpaqueCube()) continue;
			if (mob instanceof EntityCreature creature) creature.setHomeArea(xCoord, yCoord, zCoord, 16);
			if (worldObj.spawnEntityInWorld(mob)) { sound("spawn_mob"); active.add(mob.getUniqueID()); entityIds.put(mob.getUniqueID(), mob.getEntityId()); lastPositions.put(mob.getUniqueID(), new int[]{px, yCoord + 1, pz}); spawned++; } break;
		}
	}
	private EntityLiving createMob(JsonObject config, int x, int y, int z) {
		if (config == null) { EntityLiving mob = (getBlockMetadata() & 1) == 0 ? new EntityBreeze(worldObj) : new EntityBogged(worldObj); mob.setLocationAndAngles(x + .5, y, z + .5, 0, 0); mob.onSpawnWithEgg(null); return mob; }
		JsonArray potentials = config.getAsJsonArray("spawn_potentials"); int totalWeight = 0;
		for (JsonElement entry : potentials) totalWeight += entry.getAsJsonObject().get("weight").getAsInt();
		int pick = worldObj.rand.nextInt(totalWeight); JsonObject entity = null;
		for (JsonElement entry : potentials) { pick -= entry.getAsJsonObject().get("weight").getAsInt(); if (pick < 0) { entity = entry.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("entity"); break; } }
		String id = entity.get("id").getAsString().replace("minecraft:", "");
		EntityLiving mob;
		switch (id) {
			case "breeze": mob = new EntityBreeze(worldObj); break;
			case "bogged": mob = new EntityBogged(worldObj); break;
			case "stray": mob = new EntityStray(worldObj); break;
			case "husk": mob = new EntityHusk(worldObj); break;
			case "zombie": mob = new EntityZombie(worldObj); break;
			case "skeleton": mob = new EntitySkeleton(worldObj); break;
			case "spider": mob = new EntitySpider(worldObj); break;
			case "cave_spider": mob = new EntityCaveSpider(worldObj); break;
			case "silverfish": mob = new EntitySilverfish(worldObj); break;
			case "slime": mob = new EntitySlime(worldObj); break;
			default: throw new IllegalArgumentException("Unmapped trial mob " + id);
		}
		mob.setLocationAndAngles(x + .5, y, z + .5, 0, 0); mob.onSpawnWithEgg(null);
		if (mob instanceof EntityZombie zombie && entity.has("IsBaby")) zombie.setChild(entity.get("IsBaby").getAsInt() != 0);
		if (mob instanceof EntitySlime slime && entity.has("Size")) { NBTTagCompound tag = new NBTTagCompound(); slime.writeToNBT(tag); tag.setInteger("Size", entity.get("Size").getAsInt()); slime.readFromNBT(tag); }
		return mob;
	}

	private void sound(String event) { worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.trial_spawner." + event, 1, 1); }
	private void state(int state) { int previous = (getBlockMetadata() >> 1) & 3; int meta = (getBlockMetadata() & 1) | state << 1 | (ominous ? 8 : 0); if (getBlockMetadata() == meta) return; worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 2); if (state == 1 && previous == 0) sound("open_shutter"); else if (state == 0 && previous == 1) sound("close_shutter"); }
	@Override public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag); tag.setInteger("Cooldown", cooldown); tag.setInteger("Spawned", spawned); tag.setInteger("ActiveTicks", activeTicks); tag.setString("Configuration", configuration); tag.setBoolean("Ominous", ominous); tag.setInteger("SpawnDelay", spawnDelay); tag.setBoolean("Rewarding", rewarding); NBTTagList rewardList = new NBTTagList(); for (ItemStack stack : rewards) rewardList.appendTag(stack.writeToNBT(new NBTTagCompound())); tag.setTag("Rewards", rewardList);
		NBTTagList list = new NBTTagList(), positions = new NBTTagList(), participants = new NBTTagList();
		for (UUID id : active) { list.appendTag(new NBTTagString(id.toString())); int[] pos = lastPositions.get(id); if (pos != null) { NBTTagCompound entry = new NBTTagCompound(); entry.setString("Id", id.toString()); entry.setIntArray("Pos", pos); positions.appendTag(entry); } }
		for (UUID id : players) participants.appendTag(new NBTTagString(id.toString())); tag.setTag("Mobs", list); tag.setTag("Positions", positions); tag.setTag("Players", participants);
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag); cooldown = MathHelper.clamp_int(tag.getInteger("Cooldown"), 0, 1800); spawned = MathHelper.clamp_int(tag.getInteger("Spawned"), 0, 1024); activeTicks = MathHelper.clamp_int(tag.getInteger("ActiveTicks"), 0, 1800); configuration = tag.getString("Configuration"); ominous = tag.getBoolean("Ominous"); spawnDelay = MathHelper.clamp_int(tag.getInteger("SpawnDelay"), 0, 60); warmup = 0; rewarding = tag.getBoolean("Rewarding"); rewards.clear(); NBTTagList rewardList = tag.getTagList("Rewards", 10); for (int i = 0; i < Math.min(1024, rewardList.tagCount()); i++) { ItemStack stack = ItemStack.loadItemStackFromNBT(rewardList.getCompoundTagAt(i)); if (stack != null) rewards.add(stack); } active.clear(); players.clear(); lastPositions.clear(); entityIds.clear();
		NBTTagList list = tag.getTagList("Mobs", 8), positions = tag.getTagList("Positions", 10), participants = tag.getTagList("Players", 8);
		for (int i = 0; i < Math.min(1024, list.tagCount()); i++) try { active.add(UUID.fromString(list.getStringTagAt(i))); } catch (IllegalArgumentException ignored) { }
		for (int i = 0; i < Math.min(1024, positions.tagCount()); i++) try { NBTTagCompound entry = positions.getCompoundTagAt(i); int[] pos = entry.getIntArray("Pos"); if (pos.length == 3) lastPositions.put(UUID.fromString(entry.getString("Id")), pos); } catch (IllegalArgumentException ignored) { }
		for (int i = 0; i < Math.min(128, participants.tagCount()); i++) try { players.add(UUID.fromString(participants.getStringTagAt(i))); } catch (IllegalArgumentException ignored) { }
	}
}
