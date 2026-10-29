package ganymedes01.etfuturum.blocks;

import java.util.*;
import ganymedes01.etfuturum.ModernAdditions;
import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.world.*;
import net.minecraftforge.common.IShearable;

public final class BlockSpringPlant extends BlockBush implements IGrowable,IShearable {
    public final String kind;
    public BlockSpringPlant(String kind){this.kind=kind;setBlockName("etfuturum."+kind);setBlockTextureName(kind);setStepSound(soundTypeGrass);if(kind.equals("firefly_bush"))setLightLevel(2/15F);if(kind.equals("cactus_flower"))setBlockBounds(.1F,0,.1F,.9F,.375F,.9F);}
    public boolean dry(){return kind.endsWith("dry_grass");}
    @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){return world.getBlock(x,y,z).isReplaceable(world,x,y,z)&&canBlockStay(world,x,y,z);}
    @Override public boolean canBlockStay(World world,int x,int y,int z){Block base=world.getBlock(x,y-1,z);return kind.equals("cactus_flower")?base==Blocks.cactus||base==Blocks.sand:dry()?base==Blocks.sand||base==Blocks.hardened_clay||base==Blocks.stained_hardened_clay||super.canBlockStay(world,x,y,z):super.canBlockStay(world,x,y,z);}
    @Override public boolean func_149851_a(World world,int x,int y,int z,boolean client){return !kind.equals("cactus_flower")&&!kind.equals("tall_dry_grass");}
    @Override public boolean func_149852_a(World world,Random random,int x,int y,int z){return true;}
    @Override public void func_149853_b(World world,Random random,int x,int y,int z){if(kind.equals("short_dry_grass"))world.setBlock(x,y,z,ModernAdditions.block("tall_dry_grass"),0,3);else dropBlockAsItem(world,x,y,z,new ItemStack(this));}
    @Override public ArrayList<ItemStack> getDrops(World world,int x,int y,int z,int meta,int fortune){ArrayList<ItemStack> drops=new ArrayList<>();if(!dry())drops.add(new ItemStack(this));else if(world.rand.nextInt(8)==0)drops.add(new ItemStack(Items.stick));return drops;}
    @Override public boolean isShearable(ItemStack tool,IBlockAccess world,int x,int y,int z){return true;}@Override public ArrayList<ItemStack> onSheared(ItemStack tool,IBlockAccess world,int x,int y,int z,int fortune){ArrayList<ItemStack> result=new ArrayList<>();result.add(new ItemStack(this));return result;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public int colorMultiplier(IBlockAccess world,int x,int y,int z){return kind.equals("bush")||kind.equals("firefly_bush")?world.getBiomeGenForCoords(x,z).getBiomeGrassColor(x,y,z):0xffffff;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void randomDisplayTick(World world,int x,int y,int z,Random random){if(kind.equals("firefly_bush")&&world.getBlockLightValue(x,y+1,z)<=13&&random.nextInt(3)==0)world.spawnParticle("reddust",x+random.nextDouble()*6-2.5,y+random.nextDouble()*2,z+random.nextDouble()*6-2.5,1,1,.1);if(dry()&&random.nextInt(200)==0)world.playSound(x+.5,y+.5,z+.5,"minecraft_1.21.11:block.dry_grass.ambient",.5F,1,false);if(kind.equals("firefly_bush")&&random.nextInt(160)==0&&world.getBlockLightValue(x,y+1,z)<=13)world.playSound(x+.5,y+.5,z+.5,"minecraft_1.21.11:block.firefly_bush.idle",.4F,1,false);}
}
