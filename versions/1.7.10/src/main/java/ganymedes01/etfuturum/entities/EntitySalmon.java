package ganymedes01.etfuturum.entities;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.world.World;

public class EntitySalmon extends EntityFish {
	public EntitySalmon(World world) { super(world); setSize(.7F, .4F); }
	@Override public int fishType() { return 1; }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { super.onSpawnWithEgg(data); setVariant(rand.nextInt(3)); return data; }
	@Override public void setVariant(int variant) { super.setVariant(Math.max(0, Math.min(2, variant))); }
}
