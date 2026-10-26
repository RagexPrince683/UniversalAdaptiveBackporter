package ganymedes01.etfuturum.blocks;

import net.minecraft.block.material.Material;

public class BlockPaleOakSlab extends BaseSlab {
	public BlockPaleOakSlab(boolean doubleSlab) {
		super(doubleSlab, Material.wood, "pale_oak_planks");
		setNames(doubleSlab ? "double_pale_oak_slab" : "pale_oak_slab"); setHardness(2); setResistance(5); setStepSound(soundTypeWood);
	}
	@Override public String func_150002_b(int meta) { return "pale_oak_slab"; }
}
