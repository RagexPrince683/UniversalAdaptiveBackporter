package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntityTadpole;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
final class Frogspawn extends Block {
    private static final PropertyInteger PENDING=PropertyInteger.create("hatch_count",0,5);
    Frogspawn(){super(Material.PLANTS);setHardness(0);setDefaultState(blockState.getBaseState().withProperty(PENDING,0));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,PENDING);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(PENDING,Math.min(5,meta&7));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(PENDING);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,0,0,1,.0625,1);}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
    @Override public int quantityDropped(java.util.Random random){return 0;}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return super.canPlaceBlockAt(world,pos)&&world.getBlockState(pos.down()).getMaterial()==Material.WATER;}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){world.scheduleUpdate(pos,this,3600+world.rand.nextInt(2400));}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(world.getBlockState(pos.down()).getMaterial()!=Material.WATER)world.setBlockToAir(pos);}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,java.util.Random random){if(world.isRemote)return;if(world.getBlockState(pos.down()).getMaterial()!=Material.WATER){world.setBlockToAir(pos);return;}if(!MobRegistry.enabled(EntityTadpole.class)){world.scheduleUpdate(pos,this,200);return;}int remaining=state.getValue(PENDING)==0?2+random.nextInt(4):state.getValue(PENDING),spawned=0;for(int i=0,count=remaining;i<count;i++){EntityTadpole tadpole=new EntityTadpole(world);tadpole.setLocationAndAngles(pos.getX()+.2+random.nextDouble()*.6,pos.getY()-.5,pos.getZ()+.2+random.nextDouble()*.6,random.nextFloat()*360,0);if(world.spawnEntity(tadpole)){remaining--;spawned++;}}if(remaining==0)world.setBlockToAir(pos);else{world.setBlockState(pos,state.withProperty(PENDING,remaining),2);world.scheduleUpdate(pos,this,200);}if(spawned>0)world.playSound(null,pos,ModernSounds.get("block.frogspawn.hatch"),SoundCategory.BLOCKS,1,1);}
}
