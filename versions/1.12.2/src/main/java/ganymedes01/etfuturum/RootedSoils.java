package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.eventhandler.*;

/** Surface/bonemeal acquisition, without taking ownership of cave biomes. */
public final class RootedSoils {
    private RootedSoils(){}
    public static boolean enabled(){return Features.enabled("content.rooted_soils");}
    static void define(){Content.add("rooted_dirt","content.rooted_soils",new Soil());Content.add("hanging_roots","content.rooted_soils",new Roots());MinecraftForge.EVENT_BUS.register(new RootedSoils());}
    static void recipes(){if(enabled()&&Features.enabled("functions.enableAutoAddComposter")){Composting.register(Content.stack("hanging_roots"),30);}}
    public static void tree(World world,BlockPos pos){if(enabled()&&!world.isRemote&&world.getTileEntity(pos)==null){Block block=world.getBlockState(pos).getBlock();if(block==Blocks.DIRT||block==Blocks.GRASS||block==Blocks.CLAY||block instanceof MossBlock)world.setBlockState(pos,Content.block("rooted_dirt").getDefaultState(),3);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void hoe(UseHoeEvent event){if(event.getResult()==Event.Result.DENY||!enabled()||event.getWorld().getBlockState(event.getPos()).getBlock()!=Content.block("rooted_dirt")||!event.getEntityPlayer().canPlayerEdit(event.getPos(),EnumFacing.UP,event.getCurrent()))return;if(!event.getWorld().isRemote){event.getWorld().setBlockState(event.getPos(),Blocks.DIRT.getDefaultState(),3);Block.spawnAsEntity(event.getWorld(),event.getPos(),Content.stack("hanging_roots"));}event.setResult(Event.Result.ALLOW);}
    public static final class Soil extends Block implements IGrowable {
        Soil(){super(Material.GROUND);setHardness(.5F);setSoundType(SoundType.GROUND);setHarvestLevel("shovel",0);}
        @Override public boolean canSustainPlant(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side,IPlantable plant){return side==EnumFacing.UP&&plant.getPlantType(world,pos.up())==EnumPlantType.Plains;}
        @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return enabled()&&world.isAirBlock(pos.down());}
        @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
        @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(!world.isRemote&&canGrow(world,pos,state,false))world.setBlockState(pos.down(),Content.block("hanging_roots").getDefaultState(),3);}
    }
    public static final class Roots extends BlockBush implements IShearable {
        Roots(){super(Material.PLANTS);setHardness(0);setSoundType(SoundType.PLANT);}
        @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return world.getBlockState(pos).getBlock().isReplaceable(world,pos)&&canBlockStay(world,pos,getDefaultState());}
        @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state){return pos.getY()<world.getHeight()-1&&world.isSideSolid(pos.up(),EnumFacing.DOWN);}
        @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.125,.625,.125,.875,1,.875);}
        @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){}
        @Override public boolean canSilkHarvest(World world,BlockPos pos,IBlockState state,net.minecraft.entity.player.EntityPlayer player){return false;}
        @Override public boolean isShearable(ItemStack tool,IBlockAccess world,BlockPos pos){return true;}
        @Override public List<ItemStack> onSheared(ItemStack tool,IBlockAccess world,BlockPos pos,int fortune){return Collections.singletonList(new ItemStack(this));}
    }
}
