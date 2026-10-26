package ganymedes01.etfuturum.world.nether.biome;

import ganymedes01.etfuturum.world.nether.biome.decorator.SoulSandValleyDecorator;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;

public class BiomeSoulSandValley extends NetherBiomeBase {
	public BiomeSoulSandValley(int id) {
		super(id, new SoulSandValleyDecorator());
		fogSkyColor = 0x00BACD;
		this.topBlock = Blocks.soul_sand;
		this.fillerBlock = Blocks.soul_sand;
		removeMonster(EntityPigZombie.class);
		removeMonster(EntityGhast.class);
		ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, EntitySkeleton.class, 20, 1, 5);
		ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, EntityGhast.class, 50, 1, 4);
		ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, EntityEnderman.class, 1, 1, 4);
	}

	@Override
	public float getSpawningChance() {
		return 0.075F;
	}
}
