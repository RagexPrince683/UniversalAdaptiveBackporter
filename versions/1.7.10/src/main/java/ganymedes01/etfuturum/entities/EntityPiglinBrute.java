package ganymedes01.etfuturum.entities;

import net.minecraft.world.World;

public class EntityPiglinBrute extends EntityPiglin {
	public EntityPiglinBrute(World world) {
		super(world);
		func_110163_bv();
	}

	@Override public boolean isBrute() { return true; }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.piglin_brute.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.piglin_brute.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.piglin_brute.death"; }
}
