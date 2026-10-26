package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Local source water, bucket transitions and native fluid queries for aquatic plants. */
public abstract class AquaticPlant extends Block {
    public final boolean submerged;
    protected AquaticPlant(boolean wet) {
        super(wet ? Material.WATER : Material.PLANTS); submerged = wet;
        setHardness(0); setLightOpacity(0); setSoundType(SoundType.PLANT);
        if (wet) setDefaultState(blockState.getBaseState().withProperty(BlockLiquid.LEVEL, 0));
    }
    protected BlockStateContainer states(IProperty<?>... properties) {
        if (material != Material.WATER) return new BlockStateContainer(this, properties);
        IProperty<?>[] wet = java.util.Arrays.copyOf(properties, properties.length+1); wet[properties.length] = BlockLiquid.LEVEL;
        return new BlockStateContainer(this, wet);
    }
    @Override protected BlockStateContainer createBlockState() { return states(); }
    public static boolean sourceWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) && state.getValue(BlockLiquid.LEVEL) == 0;
    }
    protected abstract boolean supported(World world, BlockPos pos, IBlockState state);
    protected Block drained() { return Blocks.AIR; }
    protected Block filled() { return this; }
    @Override public boolean canPlaceBlockAt(World world, BlockPos pos) { return pos.getY()>0 && pos.getY()<world.getHeight() && (world.isAirBlock(pos) || sourceWater(world,pos)) && supported(world,pos,getDefaultState()); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return NULL_AABB; }
    @Override public int damageDropped(IBlockState state) { return 0; }
    @Override public BlockRenderLayer getRenderLayer() { return submerged ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.CUTOUT; }
    @Override public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) { return layer == BlockRenderLayer.CUTOUT || submerged && layer == BlockRenderLayer.TRANSLUCENT; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return !submerged || world.getBlockState(pos.offset(side)).getMaterial()!=Material.WATER; }
    @Override public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity, double y, Material material, boolean head) { return submerged && material == Material.WATER ? y < pos.getY()+1 : null; }
    @Override public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB box, Material material) { return submerged && material == Material.WATER ? box.intersects(new AxisAlignedBB(pos)) : null; }
    @Override public Boolean isAABBInsideLiquid(World world, BlockPos pos, AxisAlignedBB box) { return submerged ? box.intersects(new AxisAlignedBB(pos)) : null; }
    @Override public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean harvest) { return world.setBlockState(pos, (submerged ? Blocks.WATER : Blocks.AIR).getDefaultState(), 3); }
    @Override public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) { if (submerged && !world.isRemote && world.isAirBlock(pos)) world.setBlockState(pos, Blocks.WATER.getDefaultState(), 3); }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos from) {
        if (!world.isRemote && !supported(world,pos,state)) { dropBlockAsItem(world,pos,state,0); world.setBlockState(pos,(submerged?Blocks.WATER:Blocks.AIR).getDefaultState(),3); }
    }
    @SuppressWarnings({"rawtypes", "unchecked"}) private static IBlockState transfer(IBlockState from, Block target) {
        IBlockState result = target.getDefaultState();
        for (java.util.Map.Entry<IProperty<?>, Comparable<?>> property : from.getProperties().entrySet()) if (result.getProperties().containsKey(property.getKey())) result = result.withProperty((IProperty)property.getKey(), (Comparable)property.getValue());
        return result;
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty() || !player.canPlayerEdit(pos,side,held)) return false;
        boolean drain = submerged && held.getItem() == Items.BUCKET, fill = !submerged && held.getItem() == Items.WATER_BUCKET && !world.provider.doesWaterVaporize();
        if (!drain && !fill) return false;
        if (!world.isRemote) {
            Block replacement = drain ? drained() : filled();
            if (replacement == Blocks.AIR) dropBlockAsItem(world,pos,state,0);
            world.setBlockState(pos, transfer(state,replacement),3);
            if (!player.capabilities.isCreativeMode) {
                ItemStack result = new ItemStack(drain ? Items.WATER_BUCKET : Items.BUCKET);
                held.shrink(1); if (held.isEmpty()) player.setHeldItem(hand,result); else if (!player.inventory.addItemStackToInventory(result)) player.dropItem(result,false);
            }
        }
        return true;
    }
}
