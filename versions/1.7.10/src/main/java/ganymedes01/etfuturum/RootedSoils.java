package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.blocks.BaseBlock;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import cpw.mods.fml.common.eventhandler.*;

/** Surface/bonemeal acquisition, without taking ownership of cave biomes. */
public final class RootedSoils {
    private RootedSoils(){}
    public static boolean enabled(){return ContentAvailability.enabled("rooted_soils");}
    static void initialize(){ModernAdditions.add("rooted_dirt","rooted_soils",new Soil());ModernAdditions.add("hanging_roots","rooted_soils",new Roots());MinecraftForge.EVENT_BUS.register(new RootedSoils());}
    static void recipes(){if(enabled()&&ganymedes01.etfuturum.configuration.configs.ConfigFunctions.enableAutoAddComposter){ganymedes01.etfuturum.api.CompostingRegistry.registerCompostable(ModernAdditions.stack("hanging_roots"),30);}}
    public static void tree(World world,int x,int y,int z){if(enabled()&&!world.isRemote&&world.getTileEntity(x,y,z)==null){Block block=world.getBlock(x,y,z);if(block==Blocks.dirt||block==Blocks.grass||block==Blocks.clay||block==ModBlocks.MOSS_BLOCK.get())world.setBlock(x,y,z,ModernAdditions.block("rooted_dirt"),0,3);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void hoe(UseHoeEvent event){if(event.getResult()==Event.Result.DENY||!enabled()||event.world.getBlock(event.x,event.y,event.z)!=ModernAdditions.block("rooted_dirt")||!event.entityPlayer.canPlayerEdit(event.x,event.y,event.z,1,event.current))return;if(!event.world.isRemote){event.world.setBlock(event.x,event.y,event.z,Blocks.dirt,0,3);((Soil)ModernAdditions.block("rooted_dirt")).rootsDrop(event.world,event.x,event.y,event.z);}event.setResult(Event.Result.ALLOW);}
    public static final class Soil extends BaseBlock implements IGrowable {
        Soil(){super(Material.ground);setNames("rooted_dirt");setHardness(.5F);setStepSound(soundTypeGravel);setHarvestLevel("shovel",0);}
        @Override public boolean canSustainPlant(IBlockAccess world,int x,int y,int z,ForgeDirection side,IPlantable plant){return side==ForgeDirection.UP&&plant.getPlantType(world,x,y+1,z)==EnumPlantType.Plains;}
        @Override public boolean func_149851_a(World world,int x,int y,int z,boolean client){return enabled()&&world.isAirBlock(x,y-1,z);}
        @Override public boolean func_149852_a(World world,Random random,int x,int y,int z){return true;}
        @Override public void func_149853_b(World world,Random random,int x,int y,int z){if(!world.isRemote&&func_149851_a(world,x,y,z,false))world.setBlock(x,y-1,z,ModernAdditions.block("hanging_roots"),0,3);}
        public void rootsDrop(World world,int x,int y,int z){dropBlockAsItem(world,x,y,z,ModernAdditions.stack("hanging_roots"));}
    }
    public static final class Roots extends BlockBush implements IShearable {
        Roots(){super(Material.plants);setBlockName("etfuturum.hanging_roots");setBlockTextureName("hanging_roots");setHardness(0);setStepSound(soundTypeGrass);setBlockBounds(.125F,.625F,.125F,.875F,1,.875F);}
        @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){return world.getBlock(x,y,z).isReplaceable(world,x,y,z)&&canBlockStay(world,x,y,z);}
        @Override public boolean canBlockStay(World world,int x,int y,int z){return y<world.getHeight()-1&&world.isSideSolid(x,y+1,z,ForgeDirection.DOWN);}
        @Override public java.util.ArrayList<ItemStack> getDrops(World world,int x,int y,int z,int meta,int fortune){return new java.util.ArrayList<>();}
        @Override public boolean canSilkHarvest(World world,net.minecraft.entity.player.EntityPlayer player,int x,int y,int z,int meta){return false;}
        @Override public boolean isShearable(ItemStack tool,IBlockAccess world,int x,int y,int z){return true;}
        @Override public java.util.ArrayList<ItemStack> onSheared(ItemStack tool,IBlockAccess world,int x,int y,int z,int fortune){return new java.util.ArrayList<>(Collections.singletonList(new ItemStack(this)));}
    }
}
