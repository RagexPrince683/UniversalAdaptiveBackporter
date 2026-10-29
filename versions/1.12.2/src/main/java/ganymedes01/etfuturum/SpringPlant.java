package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.IShearable;

public final class SpringPlant extends BlockBush implements IGrowable,IShearable {
    public final String kind;
    public SpringPlant(String kind){this.kind=kind;setSoundType(SoundType.PLANT);setHardness(0);if(kind.equals("firefly_bush"))setLightLevel(2/15F);}
    public boolean dry(){return kind.endsWith("dry_grass");}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return world.getBlockState(pos).getBlock().isReplaceable(world,pos)&&canBlockStay(world,pos,getDefaultState());}
    @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state){Block base=world.getBlockState(pos.down()).getBlock();return kind.equals("cactus_flower")?base==Blocks.CACTUS||base==Blocks.SAND||base==Blocks.RED_SANDSTONE:dry()?base==Blocks.SAND||base==Blocks.HARDENED_CLAY||base==Blocks.STAINED_HARDENED_CLAY||super.canBlockStay(world,pos,state):super.canBlockStay(world,pos,state)||base==Content.BLOCKS.get("mud")||base==Content.BLOCKS.get("moss_block");}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return !kind.equals("cactus_flower")&&!kind.equals("tall_dry_grass");}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(kind.equals("short_dry_grass"))world.setBlockState(pos,Content.block("tall_dry_grass").getDefaultState(),3);else spawnAsEntity(world,pos,new ItemStack(this));}
    @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){if(!dry())drops.add(new ItemStack(this));else if(world instanceof World&&((World)world).rand.nextInt(8)==0)drops.add(new ItemStack(Items.STICK));}
    @Override public boolean isShearable(ItemStack tool,IBlockAccess world,BlockPos pos){return true;}
    @Override public java.util.List<ItemStack> onSheared(ItemStack tool,IBlockAccess world,BlockPos pos,int fortune){return java.util.Collections.singletonList(new ItemStack(this));}
    @Override public net.minecraft.util.math.AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess access,BlockPos pos){return new AxisAlignedBB(.1,0,.1,.9,kind.equals("cactus_flower")?.375:1,.9);}
    @Override @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT) public void randomDisplayTick(IBlockState state,World world,BlockPos pos,Random random){
        if(kind.equals("firefly_bush")&&world.getLight(pos.up())<=13&&random.nextInt(3)==0)world.spawnParticle(EnumParticleTypes.END_ROD,pos.getX()+random.nextDouble()*6-2.5,pos.getY()+random.nextDouble()*2,pos.getZ()+random.nextDouble()*6-2.5,0,.005,0);
        if(dry()&&random.nextInt(200)==0)world.playSound(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,ModernSounds.get("block.dry_grass.ambient"),SoundCategory.AMBIENT,.5F,1,false);
        if(kind.equals("firefly_bush")&&random.nextInt(160)==0&&world.getLight(pos.up())<=13)world.playSound(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,ModernSounds.get("block.firefly_bush.idle"),SoundCategory.AMBIENT,.4F,1,false);
    }
}
