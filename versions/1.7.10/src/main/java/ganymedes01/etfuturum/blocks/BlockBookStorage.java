package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.tileentities.TileEntityBookStorage;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.*;

public final class BlockBookStorage extends BlockStructureDecoration {
    public final boolean books;public final String wood;
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[][] filled;
    public BlockBookStorage(boolean books,String wood){super(Material.wood,books?"chiseled_bookshelf":wood+"_shelf",books?new String[]{"chiseled_bookshelf_shell"}:new String[]{wood+"_shelf",wood+"_shelf_powered"});this.books=books;this.wood=wood;setHardness(1.5F);setStepSound(soundTypeWood);}
    @Override public boolean isOpaqueCube(){return books;}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess access,int x,int y,int z){if(books){setBlockBounds(0,0,0,1,1,1);return;}switch(access.getBlockMetadata(x,y,z)&3){case 0:setBlockBounds(0,0,.5F,1,1,1);break;case 1:setBlockBounds(0,0,0,.5F,1,1);break;case 2:setBlockBounds(0,0,0,1,1,.5F);break;default:setBlockBounds(.5F,0,0,1,1,1);}}
    @Override public boolean hasTileEntity(int meta){return true;}@Override public TileEntity createTileEntity(World world,int meta){return new TileEntityBookStorage();}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase entity,ItemStack stack){world.setBlockMetadataWithNotify(x,y,z,(world.getBlockMetadata(x,y,z)&4)|(MathHelper.floor_double(entity.rotationYaw*4/360+.5)&3),3);}
    public static int face(int meta){return new int[]{2,5,3,4}[meta&3];}
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityBookStorage?((TileEntityBookStorage)tile).last+1:0;}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){
        if(side!=face(world.getBlockMetadata(x,y,z))||!player.canPlayerEdit(x,y,z,side,player.getHeldItem()))return false;TileEntity tile=world.getTileEntity(x,y,z);if(!(tile instanceof TileEntityBookStorage))return false;TileEntityBookStorage storage=(TileEntityBookStorage)tile;if(world.isRemote)return true;
        if(!books&&world.isBlockIndirectlyGettingPowered(x,y,z)){java.util.List<int[]> group=group(world,x,y,z,world.getBlockMetadata(x,y,z)&3);for(int[] p:group)if(!player.canPlayerEdit(p[0],p[1],p[2],side,player.getHeldItem()))return false;int hotbar=9-group.size()*3;for(int[] p:group){TileEntityBookStorage shelf=(TileEntityBookStorage)world.getTileEntity(p[0],p[1],p[2]);for(int i=0;i<3;i++){ItemStack held=player.inventory.getStackInSlot(hotbar),saved=shelf.getStackInSlot(i);player.inventory.setInventorySlotContents(hotbar++,saved);shelf.setInventorySlotContents(i,held);}}player.inventory.markDirty();player.inventoryContainer.detectAndSendChanges();return true;}
        float horizontal=side==2?1-hx:side==3?hx:side==4?hz:1-hz;int slot=Math.min(2,(int)(horizontal*3))+(books&&hy<.5?3:0);ItemStack saved=storage.getStackInSlot(slot),held=player.getHeldItem();
        if(!books){storage.setInventorySlotContents(slot,held);player.inventory.setInventorySlotContents(player.inventory.currentItem,saved);player.inventory.markDirty();return true;}
        if(saved!=null){ItemStack removed=storage.getStackInSlotOnClosing(slot);if(!player.inventory.addItemStackToInventory(removed))player.dropPlayerItemWithRandomChoice(removed,false);}
        else if(held!=null&&storage.isItemValidForSlot(slot,held)){ItemStack added=held.copy();added.stackSize=books?1:Math.min(64,held.stackSize);storage.setInventorySlotContents(slot,added);if(!player.capabilities.isCreativeMode){held.stackSize-=added.stackSize;if(held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}}
        return true;
    }
    private static boolean member(World world,int x,int y,int z,int facing){if(!world.blockExists(x,y,z))return false;Block block=world.getBlock(x,y,z);return block instanceof BlockBookStorage&&!((BlockBookStorage)block).books&&(world.getBlockMetadata(x,y,z)&3)==facing&&world.isBlockIndirectlyGettingPowered(x,y,z)&&world.getTileEntity(x,y,z) instanceof TileEntityBookStorage;}
    private static java.util.List<int[]> group(World world,int x,int y,int z,int facing){int[] dx={-1,0,1,0},dz={0,-1,0,1};int sx=x,sz=z;for(int i=0;i<2&&member(world,sx+dx[facing],y,sz+dz[facing],facing);i++){sx+=dx[facing];sz+=dz[facing];}if(member(world,sx+dx[facing],y,sz+dz[facing],facing))return java.util.Collections.singletonList(new int[]{x,y,z});java.util.List<int[]> group=new java.util.ArrayList<>();for(int i=0;i<3&&member(world,sx,y,sz,facing);i++,sx-=dx[facing],sz-=dz[facing])group.add(new int[]{sx,y,sz});return member(world,sx,y,sz,facing)?java.util.Collections.singletonList(new int[]{x,y,z}):group;}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){if(!books&&!world.isRemote){int meta=world.getBlockMetadata(x,y,z),next=(meta&3)|(world.isBlockIndirectlyGettingPowered(x,y,z)?4:0);if(meta!=next)world.setBlockMetadataWithNotify(x,y,z,next,3);}}
    @Override public void onBlockAdded(World world,int x,int y,int z){onNeighborBlockChange(world,x,y,z,this);}
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta){TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityBookStorage&&!world.isRemote)for(int i=0;i<((TileEntityBookStorage)tile).getSizeInventory();i++){ItemStack stack=((TileEntityBookStorage)tile).getStackInSlotOnClosing(i);if(stack!=null)world.spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(world,x+.5,y+.5,z+.5,stack));}super.breakBlock(world,x,y,z,block,meta);world.removeTileEntity(x,y,z);}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas){super.registerBlockIcons(atlas);if(books){filled=new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[13][4];String[] slots={"top_left","top_mid","top_right","bottom_left","bottom_mid","bottom_right"};for(int i=0;i<13;i++)for(int rotation=0;rotation<4;rotation++)filled[i][rotation]=new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas,i==12?"chiseled_bookshelf_empty":"chiseled_bookshelf_"+(i<6?"empty":"occupied")+"_slot_"+slots[i%6],rotation);}}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta){return books?filled[12][meta&3]:super.getReleasedModel(meta);}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void render(net.minecraft.world.IBlockAccess world,int x,int y,int z,net.minecraft.util.IIcon override){int meta=world.getBlockMetadata(x,y,z);super.getReleasedModel(meta).render(this,world,x,y,z,override);if(books){TileEntity tile=world.getTileEntity(x,y,z);for(int i=0;i<6;i++)filled[i+(tile instanceof TileEntityBookStorage&&((TileEntityBookStorage)tile).getStackInSlot(i)!=null?6:0)][meta&3].render(this,world,x,y,z,override);}}
}
