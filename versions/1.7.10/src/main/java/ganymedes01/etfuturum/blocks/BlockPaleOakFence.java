package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.core.utils.Utils;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;

public class BlockPaleOakFence extends BlockFence {
	public BlockPaleOakFence() {
		super("pale_oak_planks", Material.wood); setBlockName(Utils.getUnlocalisedName("pale_oak_fence"));
		setHardness(2); setResistance(5); setStepSound(soundTypeWood); setCreativeTab(EtFuturum.creativeTabBlocks);
	}
}
