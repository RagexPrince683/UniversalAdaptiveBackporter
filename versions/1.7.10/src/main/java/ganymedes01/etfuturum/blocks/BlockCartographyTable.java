package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.core.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockCartographyTable extends Block {
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, net.minecraft.entity.player.EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) player.openGui(EtFuturum.instance, ganymedes01.etfuturum.lib.GUIIDs.CARTOGRAPHY_TABLE, world, x, y, z);
		return true;
	}

	private IIcon topIcon;
	private IIcon sideIcon;
	private IIcon bottomIcon;

	public BlockCartographyTable() {
		super(Material.wood);
		this.setStepSound(soundTypeWood);
		this.setHardness(2.5F);
		this.setHarvestLevel("axe", 0);
		this.setResistance(2.5F);
		this.setBlockName(Utils.getUnlocalisedName("cartography_table"));
		this.setBlockTextureName("cartography_table");
		this.setCreativeTab(EtFuturum.creativeTabBlocks);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return side == 1 ? this.topIcon : (side == 0 ? Blocks.planks.getIcon(0, 5) : (side == 2 || side == 5 ? this.bottomIcon : side == 3 ? this.blockIcon : this.sideIcon));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.blockIcon = reg.registerIcon(this.getTextureName() + "_side1");
		this.topIcon = reg.registerIcon(this.getTextureName() + "_top");
		this.sideIcon = reg.registerIcon(this.getTextureName() + "_side2");
		this.bottomIcon = reg.registerIcon(this.getTextureName() + "_side3");
	}

	@Override
	public boolean isFlammable(IBlockAccess aWorld, int aX, int aY, int aZ, ForgeDirection aSide) {
		return true;
	}

	@Override
	public int getFlammability(IBlockAccess aWorld, int aX, int aY, int aZ, ForgeDirection aSide) {
		return 5;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess aWorld, int aX, int aY, int aZ, ForgeDirection aSide) {
		return 20;
	}

}
