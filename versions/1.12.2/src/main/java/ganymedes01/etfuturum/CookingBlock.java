package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

public final class CookingBlock extends Block {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool LIT = PropertyBool.create("lit");
    private final boolean blast;
    CookingBlock(boolean blast) {
        super(Material.ROCK); this.blast = blast; CookingRecipes.initialize();
        setHardness(3.5F); setResistance(3.5F); setHarvestLevel("pickaxe", 0);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(LIT, false));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING, LIT); }
    @Override public IBlockState getStateFromMeta(int meta) { EnumFacing facing = EnumFacing.byIndex(meta & 7); return getDefaultState().withProperty(FACING, facing.getAxis().isVertical() ? EnumFacing.NORTH : facing).withProperty(LIT, (meta & 8) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getIndex() | (state.getValue(LIT) ? 8 : 0); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float x, float y, float z, int meta, EntityLivingBase placer, EnumHand hand) { return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()); }
    @Override public IBlockState withRotation(IBlockState state, Rotation rotation) { return state.withProperty(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public IBlockState withMirror(IBlockState state, Mirror mirror) { return withRotation(state, mirror.toRotation(state.getValue(FACING))); }
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World world, IBlockState state) { return blast ? new CookingTile.BlastFurnace() : new CookingTile.Smoker(); }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (stack.hasDisplayName() && world.getTileEntity(pos) instanceof CookingTile) ((CookingTile) world.getTileEntity(pos)).setCustomName(stack.getDisplayName());
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) {
        if (!world.isRemote && world.getTileEntity(pos) instanceof CookingTile) {
            CookingTile tile = (CookingTile) world.getTileEntity(pos);
            if (tile.isLocked() && !player.canOpen(tile.getLockCode()) && !player.isSpectator()) {
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation("container.isLocked", tile.getDisplayName()), true);
            } else player.openGui(UAB.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.getTileEntity(pos) instanceof CookingTile) InventoryHelper.dropInventoryItems(world, pos, (CookingTile) world.getTileEntity(pos));
        world.updateComparatorOutputLevel(pos, this); super.breakBlock(world, pos, state);
    }
    @Override public boolean hasComparatorInputOverride(IBlockState state) { return true; }
    @Override public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) { return Container.calcRedstone(world.getTileEntity(pos) instanceof CookingTile ? (CookingTile) world.getTileEntity(pos) : null); }
    @Override public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) { return state.getValue(LIT) ? 13 : 0; }
    @Override public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) return;
        if (Features.enabled("sounds.furnaceCrackling") && random.nextDouble() < .1) world.playSound(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1, .9F + random.nextFloat() * .1F, false);
        if (!blast) world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX()+.5, pos.getY()+1.05, pos.getZ()+.5, 0, 0, 0);
        else {
            EnumFacing face = state.getValue(FACING);
            double spread = random.nextDouble() * .6 - .3;
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX()+.5+face.getXOffset()*.56+(face.getAxis()==EnumFacing.Axis.Z?spread:0), pos.getY()+.2+random.nextDouble()*.3, pos.getZ()+.5+face.getZOffset()*.56+(face.getAxis()==EnumFacing.Axis.X?spread:0), 0, 0, 0);
        }
    }
}
