package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.client.sound.ModSounds;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

public class BlockShroomlight extends BaseBlock {


	public BlockShroomlight() {
		super(Material.gourd);
		setNames("shroomlight");
		setBlockSound(ModSounds.soundShroomlight);
		setResistance(1);
		setHardness(1);
		setLightLevel(1);
	}

	@Override
	public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
		return true;
	}
}
