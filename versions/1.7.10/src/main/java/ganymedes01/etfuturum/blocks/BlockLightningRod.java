package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.client.sound.ModSounds;
import ganymedes01.etfuturum.lib.RenderIDs;
import ganymedes01.etfuturum.tileentities.TileEntityLightningRod;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.Random;

public class BlockLightningRod extends BaseBlock implements ICopperFixture {
    private static final ICopperFixture.Info COPPER=new ICopperFixture.Info("lightning_rod",0,false);
    @Override public ICopperFixture.Info copper(){return COPPER;}
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) {
		models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[16];
		for (int power = 0; power < 2; power++) for (int direction = 0; direction < 6; direction++)
			models[power * 8 + direction] = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, power==0?ganymedes01.etfuturum.ModernAdditions.copperName("lightning_rod",copper().stage,copper().wax):"lightning_rod_on", 0, direction);
		blockIcon = models[1].particle;
	}
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[(meta & 8) | ((meta & 7) % 6)]; }
	public BlockLightningRod() {
		super(Material.iron);
		setHardness(3);
		setResistance(6);
		setLightOpacity(0);
		setHarvestLevel("pickaxe", 1);
		setNames("lightning_rod");
		setBlockSound(ModSounds.soundCopper);
		setTickRandomly(true);
		setBlockBounds(0.375F, 0, 0.375F, 0.625F, 1, 0.625F);
	}
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new TileEntityLightningRod(); }
	public TileEntity createNewTileEntity(World world, int meta) { return createTileEntity(world, meta); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int getRenderType() { return RenderIDs.RELEASED_BLOCK; }
	@Override public int damageDropped(int meta) { return 0; }
	@Override public boolean canProvidePower() { return true; }
	@Override public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return (world.getBlockMetadata(x, y, z) & 8) != 0 ? 15 : 0;
	}
	@Override public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
		int direction = world.getBlockMetadata(x, y, z) & 7;
		return side == direction ? isProvidingWeakPower(world, x, y, z, side) : 0;
	}
	@Override public int onBlockPlaced(World world, int x, int y, int z, int side, float hx, float hy, float hz, int meta) {
		return side >= 0 && side < 6 ? side : 1;
	}
	@Override public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		int axis = (world.getBlockMetadata(x, y, z) & 7) / 2;
		if (axis == 1) setBlockBounds(0.375F, 0.375F, 0, 0.625F, 0.625F, 1);
		else if (axis == 2) setBlockBounds(0, 0.375F, 0.375F, 1, 0.625F, 0.625F);
		else setBlockBounds(0.375F, 0, 0.375F, 0.625F, 1, 0.625F);
	}
	public void strike(World world, int x, int y, int z) {
		if (world.isRemote) return;
		if (world.getTileEntity(x, y, z) instanceof TileEntityLightningRod rod) rod.powerForEightTicks();
		world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) | 8, 3);
		notifyPower(world, x, y, z);
		world.scheduleBlockUpdate(x, y, z, this, 8);
	}
	private void notifyPower(World world, int x, int y, int z) {
		world.notifyBlocksOfNeighborChange(x, y, z, this);
		ForgeDirection facing = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z) & 7);
		world.notifyBlocksOfNeighborChange(x - facing.offsetX, y - facing.offsetY, z - facing.offsetZ, this);
	}
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		// Native lookup lazily creates a tile for rods saved before this block had one.
		if (world.isRemote) return;
        if(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("copper_age"))tickDegradation(world,x,y,z,random);
        if(world.getBlock(x,y,z)!=this)return;
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEntityLightningRod rod && !rod.powerExpired()) {
			world.scheduleBlockUpdate(x, y, z, this, rod.powerTicksRemaining());
			return;
		}
		if (!world.isRemote && (world.getBlockMetadata(x, y, z) & 8) != 0) {
			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) & 7, 3);
			notifyPower(world, x, y, z);
		}
	}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,net.minecraft.entity.player.EntityPlayer player,int side,float hx,float hy,float hz){return ganymedes01.etfuturum.configuration.ContentAvailability.enabled("copper_age")&&tryWaxOnWaxOff(world,x,y,z,player);}
	@Override public void onBlockAdded(World world, int x, int y, int z) {
		// Also clear a saved powered state if the old scheduled update is unavailable.
		if (!world.isRemote && (world.getBlockMetadata(x, y, z) & 8) != 0) world.scheduleBlockUpdate(x, y, z, this, 8);
	}
}
