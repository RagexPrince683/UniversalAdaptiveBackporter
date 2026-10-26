package ganymedes01.etfuturum.world.end;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.world.end.dimension.WorldProviderEFREnd;
import ganymedes01.etfuturum.world.end.gen.WorldGenEndGateway;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import java.util.*;

/** Keeps the legacy dragon fight and adds its missing, bounded outer-island gateway progression. */
public class EndGatewayProgress extends WorldSavedData {
	public static final Events EVENTS = new Events();
	private static final String NAME = "etfuturum_end_gateways";
	private int defeated, placed;
	private final Set<String> dragons = new HashSet<>();
	public EndGatewayProgress(String name) { super(name); }
	private static EndGatewayProgress get(World world) {
		EndGatewayProgress data = (EndGatewayProgress) world.perWorldStorage.loadData(EndGatewayProgress.class, NAME);
		if (data == null) { data = new EndGatewayProgress(NAME); world.perWorldStorage.setData(NAME, data); }
		return data;
	}
	public static void defeated(EntityDragon dragon) {
		World world = dragon.worldObj;
		if (world.isRemote || !(world.provider instanceof WorldProviderEFREnd) || !ModBlocks.END_GATEWAY.isEnabled()) return;
		EndGatewayProgress data = get(world);
		if (data.defeated >= 20 || !data.dragons.add(dragon.getUniqueID().toString())) return;
		data.defeated++; data.markDirty(); data.placeLoaded(world);
	}
	private void placeLoaded(World world) {
		List<Integer> slots = new ArrayList<>(); for (int i = 0; i < 20; i++) slots.add(i);
		Collections.shuffle(slots, new Random(world.getSeed()));
		for (int i = 0; i < defeated; i++) {
			if ((placed & 1 << i) != 0) continue;
			double angle = Math.PI * 2 * slots.get(i) / 20;
			int x = (int) Math.floor(96 * Math.cos(angle)), z = (int) Math.floor(96 * Math.sin(angle));
			if (!world.checkChunksExist(x - 1, 73, z - 1, x + 1, 77, z + 1)) continue;
			new WorldGenEndGateway().generate(world, world.rand, x, 75, z);
			placed |= 1 << i; markDirty();
		}
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		defeated = Math.max(0, Math.min(20, tag.getInteger("Defeated"))); placed = tag.getInteger("Placed") & 0xFFFFF;
		dragons.clear(); NBTTagList list = tag.getTagList("Dragons", 10);
		for (int i = 0; i < Math.min(20, list.tagCount()); i++) dragons.add(list.getCompoundTagAt(i).getString("UUID"));
	}
	@Override public void writeToNBT(NBTTagCompound tag) {
		tag.setInteger("Defeated", defeated); tag.setInteger("Placed", placed);
		NBTTagList list = new NBTTagList();
		for (String id : dragons) { NBTTagCompound dragon = new NBTTagCompound(); dragon.setString("UUID", id); list.appendTag(dragon); }
		tag.setTag("Dragons", list);
	}
	public static class Events {
		@SubscribeEvent public void tick(TickEvent.WorldTickEvent event) {
			World world = event.world;
			if (event.phase == TickEvent.Phase.END && !world.isRemote && world.provider instanceof WorldProviderEFREnd
					&& ModBlocks.END_GATEWAY.isEnabled() && world.getTotalWorldTime() % 20 == 0) {
				EndGatewayProgress data = get(world);
				if (Integer.bitCount(data.placed) < data.defeated) data.placeLoaded(world);
			}
		}
	}
}
