package ganymedes01.etfuturum.entities;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.world.World;

public class EntityTropicalFish extends EntityFish {
	private static final int[] COMMON = {65536, 459008, 917504, 918273, 918529, 16778497, 67110144, 67371009, 67764993, 101253888, 117441025, 117441793, 117506305, 117899265, 118161664, 185008129, 234882305, 235340288, 235601920, 251658496, 264438017, 50660352};
	public EntityTropicalFish(World world) { super(world); setSize(.5F, .4F); }
	@Override public int fishType() { return 2; }
	@Override public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {
		super.onInitialSpawn(difficulty, data);
		if (data instanceof School) setVariant(((School) data).variant);
		else { setVariant(rand.nextFloat() < .9F ? COMMON[rand.nextInt(COMMON.length)] : rand.nextInt(2) | rand.nextInt(6) << 8 | rand.nextInt(16) << 16 | rand.nextInt(16) << 24); data = new School(getVariant()); }
		return data;
	}
	@Override public void setVariant(int variant) { super.setVariant((variant & 1) | Math.min(5, (variant >>> 8) & 255) << 8 | (variant & 0x0F0F0000)); }
	private static class School implements IEntityLivingData { final int variant; School(int variant) { this.variant = variant; } }
}
