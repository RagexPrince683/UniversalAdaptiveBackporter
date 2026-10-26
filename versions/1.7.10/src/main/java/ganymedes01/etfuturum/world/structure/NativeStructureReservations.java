package ganymedes01.etfuturum.world.structure;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;
import java.util.Random;

/** Replay native candidate/start RNG without terrain access, chunk requests or placement. */
public final class NativeStructureReservations {
	private NativeStructureReservations() { }
	public static boolean surface(World world, StructureBoundingBox box) {
		Village village = new Village(); Scattered scattered = new Scattered();
		Random base = new Random(world.getSeed()); long a = base.nextLong(), b = base.nextLong();
		for (int x = (box.minX - 112) >> 4; x <= (box.maxX + 112) >> 4; x++) for (int z = (box.minZ - 112) >> 4; z <= (box.maxZ + 112) >> 4; z++) {
			long seed = (long) x * a ^ (long) z * b ^ world.getSeed();
			if (village.intersects(world, x, z, seed, box) || scattered.intersects(world, x, z, seed, box)) return true;
		} return false;
	}
	private static class Village extends MapGenVillage {
		boolean intersects(World world, int x, int z, long seed, StructureBoundingBox box) { worldObj = world; rand.setSeed(seed); rand.nextInt(); if (!canSpawnStructureAtCoords(x, z)) return false; StructureStart start = getStructureStart(x, z); return start.isSizeableStructure() && start.getBoundingBox().intersectsWith(box.minX, box.minZ, box.maxX, box.maxZ); }
	}
	private static class Scattered extends MapGenScatteredFeature {
		boolean intersects(World world, int x, int z, long seed, StructureBoundingBox box) { worldObj = world; rand.setSeed(seed); rand.nextInt(); return canSpawnStructureAtCoords(x, z) && getStructureStart(x, z).getBoundingBox().intersectsWith(box.minX, box.minZ, box.maxX, box.maxZ); }
	}
	public static boolean fortress(World world, StructureBoundingBox box) {
		Fortress probe = new Fortress(); probe.world(world);
		for (int gx = (box.minX - 160) >> 8; gx <= (box.maxX + 160) >> 8; gx++) for (int gz = (box.minZ - 160) >> 8; gz <= (box.maxZ + 160) >> 8; gz++) {
			Random rng = new Random((long) (gx ^ gz << 4) ^ world.getSeed()); rng.nextInt();
			if (rng.nextInt(3) != 0) continue;
			int x = (gx << 4) + 4 + rng.nextInt(8), z = (gz << 4) + 4 + rng.nextInt(8);
			if (probe.intersects(x, z, box)) return true;
		}
		return false;
	}
	private static class Fortress extends MapGenNetherBridge {
		void world(World world) { worldObj = world; }
		boolean intersects(int x, int z, StructureBoundingBox box) { return canSpawnStructureAtCoords(x, z) && getStructureStart(x, z).getBoundingBox().intersectsWith(box); }
	}
	public static boolean underground(World world, StructureBoundingBox box) {
		Mineshaft mines = new Mineshaft(); Stronghold stronghold = new Stronghold();
		Random base = new Random(world.getSeed()); long a = base.nextLong(), b = base.nextLong();
		for (int x = (box.minX - 160) >> 4; x <= (box.maxX + 160) >> 4; x++) for (int z = (box.minZ - 160) >> 4; z <= (box.maxZ + 160) >> 4; z++) {
			long seed = (long) x * a ^ (long) z * b ^ world.getSeed();
			if (mines.intersects(world, x, z, seed, box) || stronghold.intersects(world, x, z, seed, box)) return true;
		}
		return false;
	}
	private static class Mineshaft extends MapGenMineshaft {
		boolean intersects(World world, int x, int z, long seed, StructureBoundingBox box) { worldObj = world; rand.setSeed(seed); rand.nextInt(); return canSpawnStructureAtCoords(x, z) && getStructureStart(x, z).getBoundingBox().intersectsWith(box); }
	}
	private static class Stronghold extends MapGenStronghold {
		boolean intersects(World world, int x, int z, long seed, StructureBoundingBox box) { worldObj = world; rand.setSeed(seed); rand.nextInt(); return canSpawnStructureAtCoords(x, z) && getStructureStart(x, z).getBoundingBox().intersectsWith(box); }
	}
}
