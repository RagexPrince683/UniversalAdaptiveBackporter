package ganymedes01.etfuturum.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPaleOakPlanks extends BaseBlock {
	public BlockPaleOakPlanks() { super(Material.wood); setNames("pale_oak_planks"); setHardness(2); setResistance(5); setStepSound(soundTypeWood); }
	@Override public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) { return 20; }
	@Override public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) { return 5; }
}
