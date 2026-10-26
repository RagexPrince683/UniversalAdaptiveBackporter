package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public abstract class BaseLeaves extends BlockLeaves implements ISubBlocksBlock {

	private int[] field_150128_a;
	private final String[] types;

	public BaseLeaves(String... types) {
		this.types = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			this.types[i] = types[i] + "_leaves";
		}
	}

	public int getRange(int meta) {
		return 4;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < getTypes().length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return field_150129_M[isOpaqueCube() /*OptiFine compat*/ ? 1 : 0][(meta % 4) % types.length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.field_150129_M[0] = new IIcon[types.length];
		this.field_150129_M[1] = new IIcon[types.length];
		for (int i = 0; i < types.length; ++i) {
			this.field_150129_M[0][i] = reg.registerIcon(types[i]);
			this.field_150129_M[1][i] = reg.registerIcon(types[i] + "_opaque");
		}
	}

	@Override
	public String[] func_150125_e() {
		return getTypes();
	}

	@Override
	public abstract Item getItemDropped(int meta, Random random, int fortune);

	@Override
	public boolean isOpaqueCube() { //OptiFine compat
		return Blocks.leaves.isOpaqueCube();
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) { //OptiFine compat
		return Blocks.leaves.shouldSideBeRendered(worldIn, x, y, z, side);
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return 30;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return 60;
	}

	@Override
	public IIcon[] getIcons() {
		return field_150129_M[0];
	}

	@Override
	public String[] getTypes() {
		return types;
	}

	@Override
	public String getNameFor(ItemStack stack) {
		return types[stack.getItemDamage() % types.length];
	}

	@Override
	public void updateTick(World worldIn, int x, int y, int z, Random random) {
		if (!worldIn.isRemote) {
			int l = worldIn.getBlockMetadata(x, y, z);
			int decayRange = getRange(l % 4);

			if ((l & 8) != 0 && (l & 4) == 0) {
				int i1 = decayRange + 1;
				byte b1 = 32;
				int j1 = b1 * b1;
				int k1 = b1 / 2;

				if (this.field_150128_a == null) {
					this.field_150128_a = new int[b1 * b1 * b1];
				}

				int l1;

				if (worldIn.checkChunksExist(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
					int i2;
					int j2;

					for (l1 = -decayRange; l1 <= decayRange; ++l1) {
						for (i2 = -decayRange; i2 <= decayRange; ++i2) {
							for (j2 = -decayRange; j2 <= decayRange; ++j2) {
								Block block = worldIn.getBlock(x + l1, y + i2, z + j2);

								int i = (l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1;
								if (!block.canSustainLeaves(worldIn, x + l1, y + i2, z + j2)) {
									if (block.isLeaves(worldIn, x + l1, y + i2, z + j2)) {
										this.field_150128_a[i] = -2;
									} else {
										this.field_150128_a[i] = -1;
									}
								} else {
									this.field_150128_a[i] = 0;
								}
							}
						}
					}

					for (l1 = 1; l1 <= decayRange; ++l1) {
						for (i2 = -decayRange; i2 <= decayRange; ++i2) {
							for (j2 = -decayRange; j2 <= decayRange; ++j2) {
								for (int k2 = -decayRange; k2 <= decayRange; ++k2) {
									if (this.field_150128_a[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1) {
										int i = (i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1;
										if (this.field_150128_a[i] == -2) {
											this.field_150128_a[i] = l1;
										}

										int i3 = (i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1;
										if (this.field_150128_a[i3] == -2) {
											this.field_150128_a[i3] = l1;
										}

										int i4 = (i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1;
										if (this.field_150128_a[i4] == -2) {
											this.field_150128_a[i4] = l1;
										}

										int i5 = (i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1;
										if (this.field_150128_a[i5] == -2) {
											this.field_150128_a[i5] = l1;
										}

										int i6 = (i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1);
										if (this.field_150128_a[i6] == -2) {
											this.field_150128_a[i6] = l1;
										}

										int i7 = (i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1;
										if (this.field_150128_a[i7] == -2) {
											this.field_150128_a[i7] = l1;
										}
									}
								}
							}
						}
					}
				}

				l1 = this.field_150128_a[k1 * j1 + k1 * b1 + k1];

				if (l1 >= 0) {
					worldIn.setBlockMetadataWithNotify(x, y, z, l & -9, 4);
				} else {
					this.removeLeaves(worldIn, x, y, z);
				}
			}
		}
	}
}
