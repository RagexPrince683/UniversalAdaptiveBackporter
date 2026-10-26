package ganymedes01.etfuturum.world.nether.biome;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.world.nether.biome.decorator.NetherForestDecorator;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;

public class BiomeWarpedForest extends NetherBiomeBase {

	public BiomeWarpedForest(int id) {
		super(id, NetherForestDecorator.newWarpedForestDecorator());
		this.setColor(0x49907B);
		fogSkyColor = 0x5700AF;
		this.topBlock = ModBlocks.NYLIUM.get();
		this.field_150604_aj = 1; // topBlockMetadata
		this.fillerBlock = Blocks.netherrack;

		spawnableMonsterList.clear();
		ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableMonsterList, EntityEnderman.class, 1, 1, 4);
	}

	@Override
	public float getSpawningChance() {
		return 0.035F;
	}
}