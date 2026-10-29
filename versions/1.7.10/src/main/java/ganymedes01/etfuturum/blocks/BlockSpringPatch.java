package ganymedes01.etfuturum.blocks;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.world.*;
public final class BlockSpringPatch extends BlockStructureDecoration implements IGrowable {
    public final boolean litter;
    public BlockSpringPatch(boolean litter){super(Material.plants,litter?"leaf_litter":"wildflowers",litter?new String[]{"leaf_litter_1","leaf_litter_2","leaf_litter_3","leaf_litter_4"}:new String[]{"wildflowers_1","wildflowers_2","wildflowers_3","wildflowers_4"});this.litter=litter;setHardness(0);setStepSound(soundTypeGrass);setBlockBounds(0,0,0,1,.125F,1);}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public int colorMultiplier(IBlockAccess world,int x,int y,int z){net.minecraft.world.biome.BiomeGenBase biome=world.getBiomeGenForCoords(x,z);return litter?ganymedes01.etfuturum.client.DryFoliageColors.color(biome.temperature,biome.rainfall):biome.getBiomeGrassColor(x,y,z);}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public int getRenderColor(int meta){return litter?ganymedes01.etfuturum.client.DryFoliageColors.color(.5,1):0x79c05a;}
    @Override public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return null;}
    @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){Block base=world.getBlock(x,y-1,z);return base==Blocks.grass||base==Blocks.dirt||base==Blocks.mycelium;}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor){if(!world.isRemote&&!canPlaceBlockAt(world,x,y,z)){dropBlockAsItem(world,x,y,z,world.getBlockMetadata(x,y,z),0);world.setBlockToAir(x,y,z);}}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase entity,ItemStack stack){world.setBlockMetadataWithNotify(x,y,z,(world.getBlockMetadata(x,y,z)&12)|(net.minecraft.util.MathHelper.floor_double(entity.rotationYaw*4/360+.5)&3),3);}
    @Override public ArrayList<ItemStack> getDrops(World world,int x,int y,int z,int meta,int fortune){ArrayList<ItemStack> drops=new ArrayList<>();drops.add(new ItemStack(this,(meta>>2)+1));return drops;}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){ItemStack held=player.getHeldItem();int meta=world.getBlockMetadata(x,y,z);if(held==null||held.getItem()!=Item.getItemFromBlock(this)||(meta>>2)==3||!player.canPlayerEdit(x,y,z,side,held))return false;if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,meta+4,3);if(!player.capabilities.isCreativeMode&&--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}return true;}
    @Override public boolean func_149851_a(World world,int x,int y,int z,boolean client){return !litter;}@Override public boolean func_149852_a(World world,Random random,int x,int y,int z){return !litter;}@Override public void func_149853_b(World world,Random random,int x,int y,int z){int meta=world.getBlockMetadata(x,y,z);if((meta>>2)<3)world.setBlockMetadataWithNotify(x,y,z,meta+4,3);else dropBlockAsItem(world,x,y,z,new ItemStack(this));}
}
