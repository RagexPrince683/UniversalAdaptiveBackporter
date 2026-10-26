package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class CoralPlant extends AquaticPlant {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public final String color, shape;
    public final boolean dead;
    CoralPlant(String color, boolean dead, String shape, boolean wet) {
        super(wet); this.color=color; this.dead=dead; this.shape=shape;
        setDefaultState(getDefaultState().withProperty(FACING,EnumFacing.NORTH));
    }
    @Override protected BlockStateContainer createBlockState() { return states(FACING); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(FACING,EnumFacing.byIndex((meta&3)+2)); }
    @Override public int getMetaFromState(IBlockState state) { return shape.equals("_wall_fan") ? state.getValue(FACING).getIndex()-2 : 0; }
    @Override public int quantityDropped(Random random) { return 0; }
    private String canonical() { return (dead?"dead_":"")+color+"_coral"+(shape.equals("_wall_fan")?"_fan":shape); }
    @Override protected ItemStack getSilkTouchDrop(IBlockState state) { return Content.stack(canonical()); }
    @Override public ItemStack getItem(World world, BlockPos pos, IBlockState state) { return Content.stack(canonical()); }
    @Override public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) { return true; }
    @Override protected boolean supported(World world, BlockPos pos, IBlockState state) {
        EnumFacing face = shape.equals("_wall_fan") ? state.getValue(FACING) : EnumFacing.UP;
        BlockPos support=pos.offset(face.getOpposite()); return pos.getY()>0 && world.getBlockState(support).isSideSolid(world,support,face);
    }
    private String name(boolean dry, boolean dies) { return (dies?"dead_":"")+color+"_coral"+shape+(dry?"":"_underwater"); }
    @Override protected Block drained() { return Content.block(name(true,dead)); }
    @Override protected Block filled() { return Content.block(name(false,dead)); }
    @Override public void onBlockAdded(World world, BlockPos pos, IBlockState state) { if (!world.isRemote&&!dead&&!submerged) world.scheduleUpdate(pos,this,60+world.rand.nextInt(40)); }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos from) { super.neighborChanged(state,world,pos,neighbor,from); if(world.getBlockState(pos).getBlock()==this)onBlockAdded(world,pos,state); }
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) { if(!world.isRemote&&!dead&&!submerged&&!CoralBlock.hasWater(world,pos))world.setBlockState(pos,Content.block(name(true,true)).getDefaultState().withProperty(FACING,state.getValue(FACING)),3); }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        if (shape.equals("_fan")) return new AxisAlignedBB(.125,0,.125,.875,.25,.875);
        if (!shape.equals("_wall_fan")) return new AxisAlignedBB(.125,0,.125,.875,.75,.875);
        switch(state.getValue(FACING)) {
            case NORTH:return new AxisAlignedBB(0,.25,.25,1,.75,1);
            case SOUTH:return new AxisAlignedBB(0,.25,0,1,.75,.75);
            case WEST:return new AxisAlignedBB(.25,.25,0,1,.75,1);
            default:return new AxisAlignedBB(0,.25,0,.75,.75,1);
        }
    }
}
