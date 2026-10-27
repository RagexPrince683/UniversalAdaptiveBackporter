package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.properties.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BarrelBlock extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool OPEN = PropertyBool.create("open");
    public final StorageTier tier;
    BarrelBlock(){this(StorageTier.WOOD);}
    BarrelBlock(StorageTier tier) { super(tier==StorageTier.WOOD?Material.WOOD:Material.IRON);this.tier=tier;boolean resistant=tier==StorageTier.OBSIDIAN||tier==StorageTier.DARKSTEEL||tier==StorageTier.NETHERITE;setHardness(resistant?10:2.5F);setResistance(resistant?2000:2.5F);setHarvestLevel(tier==StorageTier.WOOD?"axe":"pickaxe",tier==StorageTier.WOOD?0:1); setSoundType(tier==StorageTier.WOOD?net.minecraft.block.SoundType.WOOD:net.minecraft.block.SoundType.METAL); setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP).withProperty(OPEN, false)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING, OPEN); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7)).withProperty(OPEN, (meta & 8) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getIndex() | (state.getValue(OPEN) ? 8 : 0); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float x, float y, float z, int meta, EntityLivingBase placer, EnumHand hand) { return getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite()); }
    @Override public IBlockState withRotation(IBlockState state, Rotation rotation) { return state.withProperty(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public IBlockState withMirror(IBlockState state, Mirror mirror) { return withRotation(state, mirror.toRotation(state.getValue(FACING))); }
    @Override public void getSubBlocks(net.minecraft.creativetab.CreativeTabs tab,net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> items){if(IronStorage.available(tier))super.getSubBlocks(tab,items);}
    @Override public net.minecraft.util.BlockRenderLayer getRenderLayer(){return tier==StorageTier.CRYSTAL?net.minecraft.util.BlockRenderLayer.TRANSLUCENT:net.minecraft.util.BlockRenderLayer.SOLID;}
    @Override public boolean isOpaqueCube(IBlockState state){return tier!=StorageTier.CRYSTAL;}
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World world, IBlockState state) { return new BarrelTile(tier); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) {
        if (!world.isRemote && world.getTileEntity(pos) instanceof BarrelTile) IronStorage.open(player,(BarrelTile)world.getTileEntity(pos),world,pos);
        return true;
    }
    @Override public void onBlockPlacedBy(World world,BlockPos pos,IBlockState state,EntityLivingBase placer,net.minecraft.item.ItemStack stack){if(stack.hasDisplayName()&&world.getTileEntity(pos) instanceof BarrelTile)((BarrelTile)world.getTileEntity(pos)).setCustomName(stack.getDisplayName());}
    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.getTileEntity(pos) instanceof BarrelTile && !((BarrelTile)world.getTileEntity(pos)).upgrading) InventoryHelper.dropInventoryItems(world, pos, (BarrelTile) world.getTileEntity(pos));
        super.breakBlock(world, pos, state);
    }
    @Override public boolean hasComparatorInputOverride(IBlockState state) { return true; }
    @Override public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return net.minecraft.inventory.Container.calcRedstone(world.getTileEntity(pos) instanceof BarrelTile ? (BarrelTile) world.getTileEntity(pos) : null);
    }
}
