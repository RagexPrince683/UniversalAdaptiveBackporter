package ganymedes01.etfuturum.world.nether.biome;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.world.nether.biome.decorator.BasaltDeltasDecorator;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;

public class BiomeBasaltDeltas extends NetherBiomeBase {
	public BiomeBasaltDeltas(int id) {
		super(id, new BasaltDeltasDecorator());
		fogSkyColor = 0x534868;
		topBlock = ModBlocks.BLACKSTONE.get();
		fillerBlock = ModBlocks.BASALT.get();

		spawnableMonsterList.clear();
		ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, EntityGhast.class, 40, 1, 1);
		ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, EntityMagmaCube.class, 100, 2, 5);
	}
}
