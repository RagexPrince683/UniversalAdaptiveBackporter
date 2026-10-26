package ganymedes01.etfuturum.world.nether.biome;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.world.nether.biome.decorator.NetherForestDecorator;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Blocks;

public class BiomeCrimsonForest extends NetherBiomeBase {

	public BiomeCrimsonForest(int id) {
		super(id, NetherForestDecorator.newCrimsonForestDecorator());
		this.setColor(0xFA9418);
		fogSkyColor = 0xAD0808;
		this.topBlock = ModBlocks.NYLIUM.get();
		this.fillerBlock = Blocks.netherrack;

		spawnableMonsterList.clear();
		ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, EntityPigZombie.class, 1, 2, 4);
		if (ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableNetherMobs) {
			ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, ganymedes01.etfuturum.entities.EntityPiglin.class, 5, 3, 4);
			ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, ganymedes01.etfuturum.entities.EntityHoglin.class, 9, 3, 4);
		}
	}
}
