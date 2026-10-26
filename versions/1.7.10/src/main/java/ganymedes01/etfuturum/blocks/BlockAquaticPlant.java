package ganymedes01.etfuturum.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Local contained-water behavior, independent of any general fluidlogging implementation. */
public abstract class BlockAquaticPlant extends BaseBlock {
	public final boolean submerged;

	protected BlockAquaticPlant(String name, boolean submerged) {
		super(submerged ? Material.water : Material.plants);
		this.submerged = submerged;
		setNames(name); setHardness(0); setLightOpacity(0);
		setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundWetGrass);
	}

	public static boolean sourceWater(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		return (block == Blocks.water || block == Blocks.flowing_water) && world.getBlockMetadata(x, y, z) == 0;
	}
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	@Override public boolean canCollideCheck(int meta, boolean liquid) { return true; }
	@Override public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) { return false; }
	@Override public boolean isReplaceable(IBlockAccess world, int x, int y, int z) { return false; }
	@Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
	@Override public int damageDropped(int meta) { return 0; }
	// Chunk renderers use this to schedule the translucent pass, even when no ordinary water remains.
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public int getRenderBlockPass() { return submerged ? 1 : 0; }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public boolean canRenderInPass(int pass) { return pass == 0 || submerged && pass == 1; }
	@Override public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean harvest) {
		return world.setBlock(x, y, z, submerged ? Blocks.water : Blocks.air, 0, 3);
	}
	@Override public void onBlockDestroyedByExplosion(World world, int x, int y, int z, net.minecraft.world.Explosion explosion) {
		if (submerged && !world.isRemote && world.isAirBlock(x, y, z)) world.setBlock(x, y, z, Blocks.water, 0, 3);
	}
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		if (!world.isRemote && !canBlockStay(world, x, y, z)) {
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, submerged ? Blocks.water : Blocks.air, 0, 3);
		}
	}
	protected Block drainedBlock() { return Blocks.air; }
	protected Block filledBlock() { return this; }
	@Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
		ItemStack held = player.getHeldItem();
		if (held == null || !player.canPlayerEdit(x, y, z, side, held)) return false;
		boolean drain = submerged && held.getItem() == Items.bucket;
		boolean fill = !submerged && held.getItem() == Items.water_bucket && !world.provider.isHellWorld;
		if (!drain && !fill) return false;
		if (!world.isRemote) {
			Block replacement = drain ? drainedBlock() : filledBlock();
			int meta = world.getBlockMetadata(x, y, z);
			if (replacement == Blocks.air) dropBlockAsItem(world, x, y, z, meta, 0);
			world.setBlock(x, y, z, replacement, replacement == Blocks.air ? 0 : meta, 3);
			if (!player.capabilities.isCreativeMode) {
				ItemStack result = new ItemStack(drain ? Items.water_bucket : Items.bucket);
				if (--held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
				else if (!player.inventory.addItemStackToInventory(result)) player.dropPlayerItemWithRandomChoice(result, false);
			}
		}
		return true;
	}
}
