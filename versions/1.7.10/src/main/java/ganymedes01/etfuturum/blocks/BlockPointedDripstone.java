package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.entities.EntityFallingDripstone;
import ganymedes01.etfuturum.client.sound.ModSounds;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.lib.RenderIDs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import java.util.Random;

public class BlockPointedDripstone extends Block {

	private IIcon[] downIcons;
	private IIcon[] upIcons;
	private static final int states = 5;

	public static DamageSource STALACTITE_DAMAGE = new DamageSource("stalactite");
	public static final DamageSource STALAGMITE_DAMAGE = new DamageSource("stalagmite");

	public BlockPointedDripstone() {
		super(Material.rock);
		Utils.setBlockSound(this, ModSounds.soundPointedDripstone);
		this.setHardness(1.5F);
		this.setResistance(3F);
		this.setHarvestLevel("pickaxe", 0);
		this.setBlockName(Utils.getUnlocalisedName("pointed_dripstone"));
		this.setBlockTextureName("pointed_dripstone");
		this.setCreativeTab(EtFuturum.creativeTabBlocks);
		this.setTickRandomly(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		downIcons = new IIcon[5];
		downIcons[0] = reg.registerIcon(getTextureName() + "_down_tip");
		downIcons[1] = reg.registerIcon(getTextureName() + "_down_frustum");
		downIcons[2] = reg.registerIcon(getTextureName() + "_down_middle");
		downIcons[3] = reg.registerIcon(getTextureName() + "_down_base");
		downIcons[4] = reg.registerIcon(getTextureName() + "_down_tip_merge");

		upIcons = new IIcon[5];
		upIcons[0] = reg.registerIcon(getTextureName() + "_up_tip");
		upIcons[1] = reg.registerIcon(getTextureName() + "_up_frustum");
		upIcons[2] = reg.registerIcon(getTextureName() + "_up_middle");
		upIcons[3] = reg.registerIcon(getTextureName() + "_up_base");
		upIcons[4] = reg.registerIcon(getTextureName() + "_up_tip_merge");

		this.blockIcon = downIcons[3];
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float subX, float subY, float subZ, int meta) {
		if (side < 2) {
			return side * states;
		}
		if (canDripstoneStayHere(world, x, y, z, -1)) {
			return states;
		}
		return 0;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		int checkY = side == 0 || world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN) ? 1 : -1;
		return canDripstoneStayHere(world, x, y, z, checkY);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (world.isRemote) return;
		if (!canBlockStay(world, x, y, z)) {
			if (world.getBlockMetadata(x, y, z) < states) world.scheduleBlockUpdate(x, y, z, this, 2);
			else {
				world.setBlockToAir(x, y, z);
				this.dropBlockAsItem(world, x, y, z, 0, 0);
			}
			return;
		}
		setState(world, x, y, z);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		onNeighborBlockChange(world, x, y, z, this);
	}

	private void collapse(World world, int x, int y, int z) {
		int count = 0;
		while (y - count >= 0 && world.getBlock(x, y - count, z) == this
				&& world.getBlockMetadata(x, y - count, z) < states) count++;
		if (count == 0) return;
		EntityFallingDripstone falling = new EntityFallingDripstone(world, x + 0.5, y - count + 1.5, z + 0.5,
				world.getBlockMetadata(x, y, z), count);
		if (!world.spawnEntityInWorld(falling)) return;
		// Clear the column before notifying neighbors, so notifications cannot spawn duplicate columns.
		for (int i = 0; i < count; i++) world.setBlock(x, y - i, z, Blocks.air, 0, 2);
		for (int i = 0; i < count; i++) world.notifyBlocksOfNeighborChange(x, y - i, z, this);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if (world.isRemote || world.getBlock(x, y, z) != this) return;
		int meta = world.getBlockMetadata(x, y, z);
		if (!canBlockStay(world, x, y, z)) {
			if (meta < states) collapse(world, x, y, z);
			else onNeighborBlockChange(world, x, y, z, this);
			return;
		}
		if (meta != 0) return; // Only an unmerged, downward tip owns growth and fluid transfer.
		int baseY = y;
		while (baseY - y < 10 && world.getBlock(x, baseY + 1, z) == this
				&& world.getBlockMetadata(x, baseY + 1, z) < states) baseY++;
		if (baseY - y >= 10) return;
		if (!world.provider.isHellWorld && ModBlocks.MUD.isEnabled()
				&& world.getBlock(x, baseY + 2, z) == ModBlocks.MUD.get() && random.nextInt(256) < 45)
			world.setBlock(x, baseY + 2, z, Blocks.clay, 0, 3);
		Block source = world.getBlock(x, baseY + 2, z);
		// Forge's reverse registry omits vanilla flowing blocks, including metadata-zero sources.
		Fluid fluid = world.getBlockMetadata(x, baseY + 2, z) != 0 ? null
				: source.getMaterial() == Material.water ? FluidRegistry.WATER
				: source.getMaterial() == Material.lava ? FluidRegistry.LAVA : FluidRegistry.lookupFluidForBlock(source);
		if (fluid == null) return;
		for (int targetY = y - 1; targetY >= Math.max(0, y - 11); targetY--) {
			Block target = world.getBlock(x, targetY, z);
			if (target == Blocks.cauldron) {
				int level = world.getBlockMetadata(x, targetY, z);
				if (fluid == FluidRegistry.WATER && level < 3 && random.nextInt(256) < 45)
					world.setBlockMetadataWithNotify(x, targetY, z, level + 1, 3);
				else if (fluid == FluidRegistry.LAVA && level == 0 && ModBlocks.LAVA_CAULDRON.isEnabled() && random.nextInt(256) < 15)
					world.setBlock(x, targetY, z, ModBlocks.LAVA_CAULDRON.get(), 3, 3);
				break;
			}
			if (!world.isAirBlock(x, targetY, z)) break;
		}
		if (fluid != FluidRegistry.WATER || baseY - y >= 7
				|| world.getBlock(x, baseY + 1, z) != ModBlocks.DRIPSTONE_BLOCK.get() || random.nextInt(5625) >= 64) return;
		if (random.nextBoolean()) {
			if (y > 1 && world.isAirBlock(x, y - 1, z)) world.setBlock(x, y - 1, z, this, 0, 3);
		} else {
			for (int targetY = y - 1; targetY > Math.max(0, y - 11); targetY--) {
				if (world.getBlock(x, targetY, z) == this && world.getBlockMetadata(x, targetY, z) == states) {
					if (world.isAirBlock(x, targetY + 1, z)) world.setBlock(x, targetY + 1, z, this, states, 3);
					break;
				}
				if (!world.isAirBlock(x, targetY, z)) break;
				if (world.isSideSolid(x, targetY - 1, z, ForgeDirection.UP)) {
					world.setBlock(x, targetY, z, this, states, 3);
					break;
				}
			}
		}
	}

	@Override
	public int damageDropped(int meta) { return 0; }

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta % 5 == 4) meta = 0;
		float offset = 0.0625F + ((float) (2 - meta % states) * 0.125F);

		return AxisAlignedBB.getBoundingBox(x + offset, y, z + offset, x + (1 - offset), y + 1.0F, z + (1 - offset));
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
		int meta = access.getBlockMetadata(x, y, z);
		if (meta % 5 == 4) meta = 0;
		float offset = 0.0625F + ((float) (2 - meta % states) * 0.0625F);

		this.setBlockBounds(offset, 0, offset, 1 - offset, 1.0F, 1 - offset);
	}

	private void setState(World world, int x, int y, int z) {
		int meta = 0;
		boolean up = world.getBlockMetadata(x, y, z) > 4;
		for (meta = 0; meta < 2; meta++) {
			if (world.getBlockMetadata(x, y + (up ? 1 : -1) + (up ? meta : -meta), z) > 4 != up || world.getBlock(x, y + (up ? 1 : -1) + (up ? meta : -meta), z) != this) {
				break;
			}
		}
		if (meta % 5 == 0 && world.getBlock(x, y + (up ? 1 : -1), z) == this && world.getBlockMetadata(x, y + (up ? 1 : -1), z) > 4 != up) {
			meta = 4;
		}
		if (meta % 5 == 2 && world.getBlock(x, y + (up ? 1 : -1), z) == this && world.getBlockMetadata(x, y + (up ? 1 : -1), z) % 5 == 2
				&& world.getBlock(x, y + (up ? -1 : 1), z).isSideSolid(world, x, y + (up ? -1 : 1), z, up ? ForgeDirection.UP : ForgeDirection.DOWN)) {
			meta = 3;
		}
		if (up) meta += states;
		world.setBlockMetadataWithNotify(x, y, z, meta, 3);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		int checkY = world.getBlockMetadata(x, y, z) < states ? 1 : -1;
		return canDripstoneStayHere(world, x, y, z, checkY);
	}

	private boolean canDripstoneStayHere(World world, int x, int y, int z, int offset) {
		return world.isSideSolid(x, y + offset, z, offset > 0 ? ForgeDirection.DOWN : ForgeDirection.UP)
				|| (offset == (world.getBlockMetadata(x, y + offset, z) < states ? 1 : -1) && world.getBlock(x, y + offset, z) == this);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return meta < states ? downIcons[meta % states] : upIcons[meta % states];
	}

	@Override
	public int getRenderType() {
		return RenderIDs.POINTED_DRIPSTONE;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public String getItemIconName() {
		return "pointed_dripstone";
	}

}
