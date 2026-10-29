package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.tileentities.TileEntityCrafter;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.*;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockCrafter extends BlockContainer {
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private IIcon[][] icons;
    public BlockCrafter(){super(Material.rock);setBlockName("etfuturum.crafter");setBlockTextureName("crafter");setHardness(1.5F);setResistance(3);}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new TileEntityCrafter();}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    @Override public void registerBlockIcons(IIconRegister register){icons=new IIcon[4][6];String[] faces={"bottom","top","north","south","west","east"};for(int mode=0;mode<4;mode++)for(int face=0;face<6;face++){String name="crafter_"+faces[face];if(mode>=2&&(face==1||face==2||face==4||face==5))name+="_crafting";else if((mode!=0)&&(face==1||face==3||face==4||face==5))name+="_triggered";icons[mode][face]=register.registerIcon(name);}}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    private IIcon icon(int side,int meta,boolean crafting){int facing=meta&7,mode=((meta&8)!=0?1:0)|(crafting?2:0);if(side==facing)return icons[mode][2];if(side==(facing^1))return icons[mode][3];if(side==0||side==1)return icons[mode][side];ForgeDirection forward=ForgeDirection.getOrientation(facing),face=ForgeDirection.getOrientation(side);return icons[mode][face.offsetX==forward.offsetZ&&face.offsetZ==-forward.offsetX?4:5];}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    @Override public IIcon getIcon(int side,int meta){return icon(side,meta,false);}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    @Override public IIcon getIcon(IBlockAccess world,int x,int y,int z,int side){TileEntity tile=world.getTileEntity(x,y,z);return icon(side,world.getBlockMetadata(x,y,z),tile instanceof TileEntityCrafter&&((TileEntityCrafter)tile).craftingTicks>0);}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase player,ItemStack stack){world.setBlockMetadataWithNotify(x,y,z,BlockPistonBase.determineOrientation(world,x,y,z,player),3);power(world,x,y,z);}
    @Override public void onBlockAdded(World world,int x,int y,int z){power(world,x,y,z);}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor){power(world,x,y,z);}
    public void power(World world,int x,int y,int z){if(world.isRemote||world.getBlock(x,y,z)!=this)return;int meta=world.getBlockMetadata(x,y,z);boolean powered=world.isBlockIndirectlyGettingPowered(x,y,z)||world.isBlockIndirectlyGettingPowered(x,y+1,z);if(powered==((meta&8)!=0))return;world.setBlockMetadataWithNotify(x,y,z,(meta&7)|(powered?8:0),3);TileEntity tile=world.getTileEntity(x,y,z);if(powered&&tile instanceof TileEntityCrafter)((TileEntityCrafter)tile).pulse();}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hitX,float hitY,float hitZ){if(!world.isRemote&&world.getTileEntity(x,y,z) instanceof TileEntityCrafter)player.openGui(EtFuturum.instance,14,world,x,y,z);return true;}
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityCrafter?((TileEntityCrafter)tile).signal():0;}
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta){TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityCrafter){TileEntityCrafter crafter=(TileEntityCrafter)tile;for(int i=0;i<9;i++){ItemStack stack=crafter.getStackInSlotOnClosing(i);if(stack!=null)world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack));}crafter.dropOutputs();world.func_147453_f(x,y,z,this);}super.breakBlock(world,x,y,z,block,meta);}
    @Override public boolean rotateBlock(World world,int x,int y,int z,ForgeDirection face){if(face.ordinal()>=6||(world.getBlockMetadata(x,y,z)&7)==face.ordinal())return false;return world.setBlockMetadataWithNotify(x,y,z,(world.getBlockMetadata(x,y,z)&8)|face.ordinal(),3);}
}
