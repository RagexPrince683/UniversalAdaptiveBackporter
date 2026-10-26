package ganymedes01.etfuturum.blocks.itemblocks;

import ganymedes01.etfuturum.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class ItemBlockCoral extends ItemBlock {
	public ItemBlockCoral(Block block) { super(block); }
	@Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
		BlockCoral coral = (BlockCoral) field_150939_a;
		if (side == 0 || stack.stackSize == 0) return false;
		ForgeDirection face = ForgeDirection.getOrientation(side);
		if (!world.getBlock(x, y, z).isSideSolid(world, x, y, z, face)) return false;
		x += face.offsetX; y += face.offsetY; z += face.offsetZ;
		if (!player.canPlayerEdit(x, y, z, side, stack) || y < 1 || y > 255 || !(world.isAirBlock(x, y, z) || BlockAquaticPlant.sourceWater(world, x, y, z))) return false;
		if (side != 1 && !coral.shape.equals("_fan")) return false;
		Block placed = CoralContent.get(coral.color, coral.dead, side == 1 ? coral.shape : "_wall_fan", BlockAquaticPlant.sourceWater(world, x, y, z));
		if (!world.isRemote && world.setBlock(x, y, z, placed, side == 1 ? 0 : side - 2, 3)) {
			world.playSoundEffect(x + .5, y + .5, z + .5, placed.stepSound.func_150496_b(), 1, 1);
			if (!player.capabilities.isCreativeMode) stack.stackSize--;
		}
		return true;
	}
}
