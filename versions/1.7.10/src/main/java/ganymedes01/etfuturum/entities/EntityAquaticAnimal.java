package ganymedes01.etfuturum.entities;

import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.*;
import net.minecraft.world.World;

/** A short-range water steering primitive, independent of land pathfinding. */
public abstract class EntityAquaticAnimal extends EntityAnimal {
	protected double swimX, swimY, swimZ;
	private int swimTicks;
	public EntityAquaticAnimal(World world) { super(world); }
	@Override protected boolean isAIEnabled() { return true; }
	protected final boolean waterAt(int x, int y, int z) { return worldObj.blockExists(x, y, z) && worldObj.getBlock(x, y, z).getMaterial() == Material.water; }
	protected boolean swimDestination() { return false; }
	protected double swimAcceleration() { return .02; }
	protected boolean canSwim() { return true; }
	protected boolean wetSpawn(int low, int high) {
		int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
		return worldObj.provider.dimensionId == 0 && y >= low && y <= high && waterAt(x, y, z) && waterAt(x, y + 1, z)
				&& worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
	}
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (!isInWater()) { super.moveEntityWithHeading(strafe, forward); return; }
		if (worldObj.isRemote) return;
		if (canSwim()) {
			boolean directed = swimDestination();
			if (!directed && (--swimTicks <= 0 || isCollidedHorizontally)) {
				swimTicks = 30 + rand.nextInt(40);
				for (int i = 0; i < 5; i++) {
					double x = posX + (rand.nextDouble() - .5) * 12, y = posY + (rand.nextDouble() - .5) * 6, z = posZ + (rand.nextDouble() - .5) * 12;
					if (waterAt(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z))) { swimX = x; swimY = y; swimZ = z; break; }
				}
			}
			double dx = swimX - posX, dy = swimY - posY, dz = swimZ - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
			boolean loaded = worldObj.blockExists(MathHelper.floor_double(swimX), MathHelper.floor_double(swimY), MathHelper.floor_double(swimZ));
			if (loaded && length > .5 && worldObj.func_147447_a(Vec3.createVectorHelper(posX, posY + height / 2, posZ), Vec3.createVectorHelper(swimX, swimY, swimZ), false, true, false) == null) {
				double acceleration = swimAcceleration(); motionX += dx / length * acceleration; motionY += dy / length * acceleration; motionZ += dz / length * acceleration;
				rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; renderYawOffset = rotationYaw;
			} else { swimTicks = 0; if (isCollidedHorizontally) motionY += .04; }
		}
		moveEntity(motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9;
	}
}
