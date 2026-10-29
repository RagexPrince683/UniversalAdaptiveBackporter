package ganymedes01.etfuturum.blocks;

import java.util.Random;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.tileentities.TileEntityLectern;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.*;

public final class BlockLectern extends BlockStructureDecoration {
    public BlockLectern(){super(Material.wood,"lectern","lectern");setHardness(2.5F);setStepSound(soundTypeWood);}
    @Override public boolean hasTileEntity(int meta){return true;}
    @Override public TileEntity createTileEntity(World world,int meta){return new TileEntityLectern();}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase entity,ItemStack stack){world.setBlockMetadataWithNotify(x,y,z,net.minecraft.util.MathHelper.floor_double(entity.rotationYaw*4/360+.5)&3,3);}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){
        TileEntity raw=world.getTileEntity(x,y,z);if(!(raw instanceof TileEntityLectern))return false;TileEntityLectern tile=(TileEntityLectern)raw;
        if(tile.getStackInSlot(0)!=null){if(!world.isRemote)player.openGui(EtFuturum.instance,13,world,x,y,z);return true;}
        ItemStack held=player.getHeldItem();if(!TileEntityLectern.book(held)||!player.canPlayerEdit(x,y,z,side,held))return false;
        if(!world.isRemote){ItemStack copy=held.copy();copy.stackSize=1;tile.setInventorySlotContents(0,copy);if(!player.capabilities.isCreativeMode&&--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);player.inventory.markDirty();}return true;
    }
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta){TileEntity raw=world.getTileEntity(x,y,z);if(raw instanceof TileEntityLectern){ItemStack book=((TileEntityLectern)raw).getStackInSlot(0);if(book!=null)dropBlockAsItem(world,x,y,z,book);}super.breakBlock(world,x,y,z,block,meta);world.func_147453_f(x,y,z,this);if((meta&4)!=0)world.notifyBlocksOfNeighborChange(x,y-1,z,this);}
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){TileEntity raw=world.getTileEntity(x,y,z);return raw instanceof TileEntityLectern?((TileEntityLectern)raw).signal():0;}
    @Override public boolean canProvidePower(){return true;}
    @Override public int isProvidingWeakPower(IBlockAccess world,int x,int y,int z,int side){return (world.getBlockMetadata(x,y,z)&4)!=0?15:0;}
    @Override public int isProvidingStrongPower(IBlockAccess world,int x,int y,int z,int side){return side==1?isProvidingWeakPower(world,x,y,z,side):0;}
    @Override public void updateTick(World world,int x,int y,int z,Random random){TileEntity raw=world.getTileEntity(x,y,z);if(raw instanceof TileEntityLectern)((TileEntityLectern)raw).endPulse();}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta){return super.getReleasedModel(meta&3);}
}
