package ganymedes01.etfuturum;

import com.google.gson.*;
import net.minecraft.world.WorldServer;
import net.minecraft.util.math.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Native finite encounters; released chamber configuration is separate from the legacy dungeon variant. */
public class TrialSpawnerTile extends TileEntityMobSpawner {
	private int cooldown, spawned, activeTicks, spawnDelay, warmup;
	private String configuration = "";
	private boolean ominous, rewarding;
	private final ArrayDeque<ItemStack> rewards = new ArrayDeque<>();
	private final Set<UUID> active = new HashSet<>(), players = new HashSet<>();
	private final Map<UUID, int[]> lastPositions = new HashMap<>();
	private final Map<UUID, Integer> entityIds = new HashMap<>();
	private static final Map<String, JsonObject> CONFIGS = new HashMap<>();
	public void configure(String id) { if (!id.startsWith("minecraft:trial_chamber/") || !id.endsWith("/normal")) throw new IllegalArgumentException("Invalid trial configuration " + id); configuration = id;JsonArray entries=config().getAsJsonArray("spawn_potentials");String mob=entries.get(0).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("entity").get("id").getAsString();if(mob.equals("minecraft:breeze")||mob.equals("minecraft:bogged"))mob=UAB.ID+":"+mob.substring(10);getSpawnerBaseLogic().setEntityId(new ResourceLocation(mob));markDirty(); }
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
		RayTraceResult hit = world.rayTraceBlocks(new Vec3d(player.posX,player.posY+player.getEyeHeight(),player.posZ),new Vec3d(pos.getX()+.5,pos.getY()+.8,pos.getZ()+.5),false,true,false);
		return hit == null || pos.equals(hit.getBlockPos());
	}
	@Override public void update() {
		if(world.isRemote){super.update();return;}if(world.getTotalWorldTime()%20!=0)return;
		warmup++; markDirty();
		if (cooldown > 0) { state(0); cooldown--; if (cooldown == 0) { ominous = false; players.clear(); } return; }
		if (rewarding) {
			state(2);
			if (!rewards.isEmpty()) { ItemStack reward = rewards.peek(); if (world.spawnEntity(new net.minecraft.entity.item.EntityItem(world, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, reward.copy()))) { rewards.remove(); sound("eject_item"); } }
			else { rewarding = false; spawned = 0; cooldown = 1800; activeTicks = 0; players.clear(); }
			return;
		}
		if (world.getDifficulty() == net.minecraft.world.EnumDifficulty.PEACEFUL || !world.getGameRules().getBoolean("doMobSpawning")) { state(0); return; }
		for (Object object : world.playerEntities) {
			EntityPlayer player = (EntityPlayer) object;
			if (player.capabilities.disableDamage || player.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 196 || !visible(player)) continue;
			if (players.size() < 128) players.add(player.getUniqueID());
			NBTTagCompound data = player.getEntityData();
			if (!configuration.isEmpty() && data.getInteger("EFRBadOmen") > 0) { data.setInteger("EFRBadOmen", 0); data.setInteger("EFRTrialOmen", 18000 * (1 + MathHelper.clamp(data.getInteger("EFRBadOmenAmplifier"), 0, 4))); }
			if (!configuration.isEmpty() && data.getInteger("EFRTrialOmen") > 0 && !ominous) { ominous = true; sound("ominous_activate"); }
		}
		if (players.isEmpty()) { state(0); return; }
		state(1);
		Set<UUID> present = new HashSet<>();
		boolean recoverIds = false;
		for (UUID id : active) {
			Integer entityId = entityIds.get(id); Entity entity = entityId == null ? null : world.getEntityByID(entityId);
			if (entity instanceof EntityLiving && id.equals(entity.getUniqueID()) && entity.isEntityAlive()) { present.add(id); lastPositions.put(id, new int[]{MathHelper.floor(entity.posX), MathHelper.floor(entity.posY), MathHelper.floor(entity.posZ)}); }
			else recoverIds = true;
		}
		// Native runtime IDs are transient; recover saved UUIDs across the whole loaded world after a reload.
		if (recoverIds) for (Object object : world.loadedEntityList) {
			if (object instanceof EntityLiving && ((EntityLiving)object).isEntityAlive() && active.contains(((EntityLiving)object).getUniqueID())) { EntityLiving mob=(EntityLiving)object; UUID id = mob.getUniqueID(); entityIds.put(id, mob.getEntityId()); present.add(id); lastPositions.put(id, new int[]{MathHelper.floor(mob.posX), MathHelper.floor(mob.posY), MathHelper.floor(mob.posZ)}); }
		}
		// Missing entities in unloaded chunks remain outstanding; absence is not proof of a kill.
		if (warmup > 10) for (Iterator<UUID> it = active.iterator(); it.hasNext();) { UUID id = it.next(); int[] pos = lastPositions.get(id); if (!present.contains(id) && pos != null && world.isBlockLoaded(new BlockPos(pos[0],pos[1],pos[2]))) { it.remove(); lastPositions.remove(id); entityIds.remove(id); } }
		if (++activeTicks > 1800) { active.clear(); lastPositions.clear(); entityIds.clear(); spawned = 0; cooldown = 1800; activeTicks = 0; state(0); return; }
		JsonObject config = configuration.isEmpty() ? null : config(); int additional = Math.max(0, players.size() - 1);
		int total = config == null ? 6 : count(config, "total_mobs", 6) + additional * count(config, "total_mobs_added_per_player", 2);
		int simultaneous = config == null ? 2 : count(config, "simultaneous_mobs", 2) + (int) Math.ceil(additional * (config.has("simultaneous_mobs_added_per_player") ? config.get("simultaneous_mobs_added_per_player").getAsFloat() : 1));
		if (spawned >= total && active.isEmpty()) {
			for (UUID ignored : players) {
				if (config == null) rewards.add(world.rand.nextBoolean() ? new ItemStack(net.minecraft.init.Items.EMERALD, 2 + world.rand.nextInt(4)) : Content.stack("ominous_bottle"));
				else {
					String table = "minecraft:spawners/trial_chamber/" + (world.rand.nextBoolean() ? "key" : "consumables");
					if (config.has("loot_tables_to_eject")) {
						JsonArray tables = config.getAsJsonArray("loot_tables_to_eject"); int weight = 0; for (JsonElement e : tables) weight += e.getAsJsonObject().get("weight").getAsInt(); int choice = world.rand.nextInt(weight);
						for (JsonElement e : tables) { JsonObject entry = e.getAsJsonObject(); choice -= entry.get("weight").getAsInt(); if (choice < 0) { table = entry.get("data").getAsString(); break; } }
					}
					rewards.addAll(ModernLoot.roll(table,world.rand,(WorldServer)world));
				}
			}
			rewarding = true; state(2); return;
		}

		if (active.size() >= simultaneous || spawned >= total || --spawnDelay > 0) return;
		spawnDelay = config == null ? 3 : Math.max(1, count(config, "ticks_between_spawn", 40) / 20);
		for (int attempt = 0; attempt < 8; attempt++) {
			int px = pos.getX() + world.rand.nextInt(9) - 4, pz = pos.getZ() + world.rand.nextInt(9) - 4;
			if (!world.isBlockLoaded(new BlockPos(px-1,pos.getY()+3,pz-1)) || !world.isBlockLoaded(new BlockPos(px+1,pos.getY(),pz+1))) continue;
			EntityLiving mob = createMob(config, px, pos.getY() + 1, pz);
            if(!MobRegistry.enabled(mob.getClass())&&(mob instanceof EntityBreeze||mob instanceof EntityBogged))return;
            int spawnY=0;
            // Released chamber floors and dungeon floors sit below a raised spawner block.
            for(int offset:new int[]{0,-1,1,-2}){int y=pos.getY()+1+offset;if(y<2||!world.isBlockLoaded(new BlockPos(px-1,y-1,pz-1))||!world.isBlockLoaded(new BlockPos(px+1,y+3,pz+1)))continue;mob.setPosition(px+.5,y,pz+.5);if(world.getBlockState(new BlockPos(px,y-1,pz)).isOpaqueCube()&&world.getCollisionBoxes(mob,mob.getEntityBoundingBox()).isEmpty()&&world.checkNoEntityCollision(mob.getEntityBoundingBox())&&!world.containsAnyLiquid(mob.getEntityBoundingBox())){spawnY=y;break;}}
            if(spawnY==0)continue;
            if(mob instanceof EntityCreature)((EntityCreature)mob).setHomePosAndDistance(pos,16);
            if(world.spawnEntity(mob)){sound("spawn_mob");active.add(mob.getUniqueID());entityIds.put(mob.getUniqueID(),mob.getEntityId());lastPositions.put(mob.getUniqueID(),new int[]{px,spawnY,pz});spawned++;}break;

		}
	}
	private EntityLiving createMob(JsonObject config, int x, int y, int z) {
		if (config == null) { EntityLiving mob = (getBlockMetadata() & 1) == 0 ? new EntityBreeze(world) : new EntityBogged(world); mob.setLocationAndAngles(x + .5, y, z + .5, 0, 0); mob.onInitialSpawn(world.getDifficultyForLocation(mob.getPosition()),null); return mob; }
		JsonArray potentials = config.getAsJsonArray("spawn_potentials"); int totalWeight = 0;
		for (JsonElement entry : potentials) totalWeight += entry.getAsJsonObject().get("weight").getAsInt();
		int pick = world.rand.nextInt(totalWeight); JsonObject entity = null;
		for (JsonElement entry : potentials) { pick -= entry.getAsJsonObject().get("weight").getAsInt(); if (pick < 0) { entity = entry.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("entity"); break; } }
		String id = entity.get("id").getAsString().replace("minecraft:", "");
		EntityLiving mob;
		switch (id) {
			case "breeze": mob = new EntityBreeze(world); break;
			case "bogged": mob = new EntityBogged(world); break;
			case "stray": mob = new EntityStray(world); break;
			case "husk": mob = new EntityHusk(world); break;
			case "zombie": mob = new EntityZombie(world); break;
			case "skeleton": mob = new EntitySkeleton(world); break;
			case "spider": mob = new EntitySpider(world); break;
			case "cave_spider": mob = new EntityCaveSpider(world); break;
			case "silverfish": mob = new EntitySilverfish(world); break;
			case "slime": mob = new EntitySlime(world); break;
			default: throw new IllegalArgumentException("Unmapped trial mob " + id);
		}
		mob.setLocationAndAngles(x + .5, y, z + .5, 0, 0); mob.onInitialSpawn(world.getDifficultyForLocation(mob.getPosition()),null);
		if (mob instanceof EntityZombie && entity.has("IsBaby")) ((EntityZombie)mob).setChild(entity.get("IsBaby").getAsInt() != 0);
		if (mob instanceof EntitySlime && entity.has("Size")) { EntitySlime slime=(EntitySlime)mob; NBTTagCompound tag = new NBTTagCompound(); slime.writeToNBT(tag); tag.setInteger("Size", entity.get("Size").getAsInt()); slime.readFromNBT(tag); }
		return mob;
	}

	private void sound(String event){world.playSound(null,pos,ModernSounds.get("block.trial_spawner."+event),SoundCategory.BLOCKS,1,1);}
    private void state(int phase){int meta=getBlockMetadata(),previous=meta>>1&3,next=(meta&1)|(phase<<1)|(ominous?8:0);if(meta==next)return;world.setBlockState(pos,world.getBlockState(pos).getBlock().getStateFromMeta(next),2);if(phase==1&&previous==0)sound("open_shutter");else if(phase==0&&previous==1)sound("close_shutter");}
    @Override public boolean shouldRefresh(net.minecraft.world.World world,BlockPos pos,net.minecraft.block.state.IBlockState before,net.minecraft.block.state.IBlockState after){return before.getBlock()!=after.getBlock();}
    @Override public NBTTagCompound getUpdateTag(){NBTTagCompound tag=super.writeToNBT(new NBTTagCompound());tag.setString("Configuration",configuration);return tag;}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,1,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager manager,SPacketUpdateTileEntity packet){getSpawnerBaseLogic().readFromNBT(packet.getNbtCompound());configuration=packet.getNbtCompound().getString("Configuration");}
	@Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag); tag.setInteger("Cooldown", cooldown); tag.setInteger("Spawned", spawned); tag.setInteger("ActiveTicks", activeTicks); tag.setString("Configuration", configuration); tag.setBoolean("Ominous", ominous); tag.setInteger("SpawnDelay", spawnDelay); tag.setBoolean("Rewarding", rewarding); NBTTagList rewardList = new NBTTagList(); for (ItemStack stack : rewards) rewardList.appendTag(stack.writeToNBT(new NBTTagCompound())); tag.setTag("Rewards", rewardList);
		NBTTagList list = new NBTTagList(), positions = new NBTTagList(), participants = new NBTTagList();
		for (UUID id : active) { list.appendTag(new NBTTagString(id.toString())); int[] pos = lastPositions.get(id); if (pos != null) { NBTTagCompound entry = new NBTTagCompound(); entry.setString("Id", id.toString()); entry.setIntArray("Pos", pos); positions.appendTag(entry); } }
		for (UUID id : players) participants.appendTag(new NBTTagString(id.toString())); tag.setTag("Mobs", list); tag.setTag("Positions", positions); tag.setTag("Players", participants);
        return tag;
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag); cooldown = MathHelper.clamp(tag.getInteger("Cooldown"), 0, 1800); spawned = MathHelper.clamp(tag.getInteger("Spawned"), 0, 1024); activeTicks = MathHelper.clamp(tag.getInteger("ActiveTicks"), 0, 1800); configuration = tag.getString("Configuration"); ominous = tag.getBoolean("Ominous"); spawnDelay = MathHelper.clamp(tag.getInteger("SpawnDelay"), 0, 60); warmup = 0; rewarding = tag.getBoolean("Rewarding"); rewards.clear(); NBTTagList rewardList = tag.getTagList("Rewards", 10); for (int i = 0; i < Math.min(1024, rewardList.tagCount()); i++) { ItemStack stack = new ItemStack(rewardList.getCompoundTagAt(i)); if (!stack.isEmpty()) rewards.add(stack); } active.clear(); players.clear(); lastPositions.clear(); entityIds.clear();
		NBTTagList list = tag.getTagList("Mobs", 8), positions = tag.getTagList("Positions", 10), participants = tag.getTagList("Players", 8);
		for (int i = 0; i < Math.min(1024, list.tagCount()); i++) try { active.add(UUID.fromString(list.getStringTagAt(i))); } catch (IllegalArgumentException ignored) { }
		for (int i = 0; i < Math.min(1024, positions.tagCount()); i++) try { NBTTagCompound entry = positions.getCompoundTagAt(i); int[] pos = entry.getIntArray("Pos"); if (pos.length == 3) lastPositions.put(UUID.fromString(entry.getString("Id")), pos); } catch (IllegalArgumentException ignored) { }
		for (int i = 0; i < Math.min(128, participants.tagCount()); i++) try { players.add(UUID.fromString(participants.getStringTagAt(i))); } catch (IllegalArgumentException ignored) { }
	}
}
