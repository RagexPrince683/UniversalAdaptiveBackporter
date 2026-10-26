package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.sound.ModSounds;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.lib.RenderIDs;
import net.minecraft.block.BlockBush;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import java.util.List;
import java.util.Random;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraftforge.event.terraingen.TerrainGen;

public class BlockAzalea extends BlockBush implements ISubBlocksBlock, IGrowable {

	public IIcon[] sideIcons;
	public IIcon[] topIcons;
	public int meta;

	private final String[] types = new String[]{"azalea", "flowering_azalea"};

	public BlockAzalea() {
		super(Material.wood);
		setHardness(0.0F);
		setResistance(0.0F);
		Utils.setBlockSound(this, ModSounds.soundAzalea);
		setBlockName(Utils.getUnlocalisedName("azalea"));
		setBlockTextureName("azalea");
		setCreativeTab(EtFuturum.creativeTabBlocks);
		setBlockBounds(0, 0, 0, 1, 1, 1);
	}

	@Override
	public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
		return ModBlocks.AZALEA_LEAVES.isEnabled();
	}

	@Override
	public boolean func_149852_a(World world, Random random, int x, int y, int z) {
		return random.nextFloat() < 0.45F;
	}

	@Override
	public void func_149853_b(World world, Random random, int x, int y, int z) {
		if (world.isRemote || !ModBlocks.AZALEA_LEAVES.isEnabled()
				|| !world.checkChunksExist(x - 2, y, z - 2, x + 2, y + 8, z + 2)
				|| !TerrainGen.saplingGrowTree(world, random, x, y, z)) return;
		int plantMeta = world.getBlockMetadata(x, y, z);
		Block soil = world.getBlock(x, y - 1, z);
		int soilMeta = world.getBlockMetadata(x, y - 1, z);
		world.setBlock(x, y, z, Blocks.air, 0, 4);
		// The native oak generator requires sapling soil; azalea can also be planted on clay.
		if (soil.getMaterial() == Material.clay) world.setBlock(x, y - 1, z, Blocks.dirt, 0, 4);
		WorldGenTrees tree = new WorldGenTrees(true, 4, 0, 0, false) {
			@Override
			protected void setBlockAndNotifyAdequately(World target, int tx, int ty, int tz, Block block, int meta) {
				if (block == Blocks.leaves) {
					block = ModBlocks.AZALEA_LEAVES.get();
					meta = random.nextInt(4) == 0 ? 1 : 0;
				}
				super.setBlockAndNotifyAdequately(target, tx, ty, tz, block, meta);
			}
		};
		if (!tree.generate(world, random, x, y, z)) {
			world.setBlock(x, y - 1, z, soil, soilMeta, 4);
			world.setBlock(x, y, z, this, plantMeta, 3);
		}
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Plains;
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return world.getBlock(x, y - 1, z).getMaterial() == Material.clay || super.canBlockStay(world, x, y, z);
	}

	@Override
	public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collider) {
		setBlockBounds(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
		super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
		setBlockBounds(0.4375F, 0.5F, 0.4375F, 0.5625F, 1.0F, 0.5625F);
		super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);

		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x + 0.0F, y + 0.5F, z + 0.0F, x + 1.0F, y + 1.0F, z + 1.0F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x + 0.0F, y + 0.5F, z + 0.0F, x + 1.0F, y + 1.0F, z + 1.0F);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < getTypes().length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public int getRenderType() {
		return RenderIDs.AZALEA;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
		return side != 0 && super.shouldSideBeRendered(worldIn, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.blockIcon = reg.registerIcon(this.getTextureName() + "_plant");

		sideIcons = new IIcon[2];
		topIcons = new IIcon[2];
		sideIcons[0] = reg.registerIcon(this.getTextureName() + "_side");
		sideIcons[1] = reg.registerIcon("flowering_" + this.getTextureName() + "_side");
		topIcons[0] = reg.registerIcon(this.getTextureName() + "_top");
		topIcons[1] = reg.registerIcon("flowering_" + this.getTextureName() + "_top");
	}

	@Override
	public int damageDropped(int meta) {
		return meta % getTypes().length;
	}

	@Override
	public IIcon[] getIcons() {
		return sideIcons;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		if (side == 0) {
			return this.blockIcon;
		}
		if (side == 1) {
			return topIcons[meta % topIcons.length];
		}
		return sideIcons[meta % topIcons.length];
	}

	@Override
	public String[] getTypes() {
		return types;
	}

	@Override
	public String getNameFor(ItemStack stack) {
		return getTypes()[stack.getItemDamage() % types.length];
	}

	@Override
	public MapColor getMapColor(int meta) {
		return MapColor.grassColor;
	}
}
