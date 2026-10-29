package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Direct, server-owned slots. Chiseled bookshelves and Copper Age shelves retain their own inventories. */
public class BookStorage extends Block {
    public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public static final PropertyBool POWERED=PropertyBool.create("powered");
    public static final PropertyBool[] OCCUPIED=new PropertyBool[6];
    static{for(int i=0;i<6;i++)OCCUPIED[i]=PropertyBool.create("slot_"+i+"_occupied");}
    public final boolean books;public final String wood;
    public BookStorage(boolean books,String wood){super(Material.WOOD);this.books=books;this.wood=wood;setHardness(1.5F);setSoundType(SoundType.WOOD);IBlockState state=blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH);if(books){for(PropertyBool property:OCCUPIED)state=state.withProperty(property,false);}else{state=state.withProperty(POWERED,false);setLightOpacity(0);}setDefaultState(state);if(!wood.equals("crimson")&&!wood.equals("warped"))net.minecraft.init.Blocks.FIRE.setFireInfo(this,books?30:5,20);}
    @Override protected BlockStateContainer createBlockState(){IProperty<?>[] properties=new IProperty[7];properties[0]=FACING;System.arraycopy(OCCUPIED,0,properties,1,6);return new BlockStateContainer(this,properties);}
    @Override public IBlockState getStateFromMeta(int meta){IBlockState state=getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3));return books?state:state.withProperty(POWERED,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|(!books&&state.getValue(POWERED)?4:0);}
    @Override public IBlockState getActualState(IBlockState state,IBlockAccess access,BlockPos pos){TileEntity tile=access.getTileEntity(pos);if(books&&tile instanceof Storage)for(int i=0;i<6;i++)state=state.withProperty(OCCUPIED[i],!((Storage)tile).getStackInSlot(i).isEmpty());return state;}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase entity,EnumHand hand){return getDefaultState().withProperty(FACING,entity.getHorizontalFacing().getOpposite());}
    @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
    public static final class Shelf extends BookStorage {public Shelf(String wood){super(false,wood);}@Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,POWERED);}}
    @Override public boolean isOpaqueCube(IBlockState state){return books;}@Override public boolean isFullCube(IBlockState state){return books;}@Override public BlockRenderLayer getRenderLayer(){return books?BlockRenderLayer.SOLID:BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess access,BlockPos pos){if(books)return FULL_BLOCK_AABB;switch(state.getValue(FACING)){case NORTH:return new AxisAlignedBB(0,0,.5,1,1,1);case SOUTH:return new AxisAlignedBB(0,0,0,1,1,.5);case WEST:return new AxisAlignedBB(.5,0,0,1,1,1);default:return new AxisAlignedBB(0,0,0,.5,1,1);}}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new Storage();}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);return tile instanceof Storage?((Storage)tile).last+1:0;}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){
        if(side!=state.getValue(FACING)||!player.canPlayerEdit(pos,side,player.getHeldItem(hand)))return false;TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof Storage))return false;Storage storage=(Storage)tile;
        if(world.isRemote)return true;
        if(!books&&world.isBlockPowered(pos)){java.util.List<BlockPos> group=group(world,pos,state.getValue(FACING));for(BlockPos member:group)if(!player.canPlayerEdit(member,side,player.getHeldItem(hand)))return false;int hotbar=9-group.size()*3;for(BlockPos member:group){Storage shelf=(Storage)world.getTileEntity(member);for(int i=0;i<3;i++){ItemStack held=player.inventory.getStackInSlot(hotbar),saved=shelf.getStackInSlot(i);player.inventory.setInventorySlotContents(hotbar++,saved);shelf.setInventorySlotContents(i,held);}}player.inventory.markDirty();player.inventoryContainer.detectAndSendChanges();return true;}
        float horizontal=side==EnumFacing.NORTH?1-x:side==EnumFacing.SOUTH?x:side==EnumFacing.WEST?z:1-z;int slot=Math.min(2,(int)(horizontal*3))+(books&&y<.5F?3:0);ItemStack saved=storage.getStackInSlot(slot),held=player.getHeldItem(hand);
        if(!books){storage.setInventorySlotContents(slot,held);player.setHeldItem(hand,saved);player.inventory.markDirty();return true;}
        if(!saved.isEmpty()){ItemStack removed=storage.removeStackFromSlot(slot);if(!player.inventory.addItemStackToInventory(removed))player.dropItem(removed,false);}
        else if(!held.isEmpty()&&storage.isItemValidForSlot(slot,held)){ItemStack added=held.copy();added.setCount(books?1:Math.min(64,held.getCount()));storage.setInventorySlotContents(slot,added);if(!player.capabilities.isCreativeMode)held.shrink(added.getCount());}
        return true;
    }
    private static boolean member(World world,BlockPos pos,EnumFacing facing){if(!world.isBlockLoaded(pos))return false;IBlockState state=world.getBlockState(pos);return state.getBlock() instanceof BookStorage&&!((BookStorage)state.getBlock()).books&&state.getValue(FACING)==facing&&world.isBlockPowered(pos)&&world.getTileEntity(pos) instanceof Storage;}
    private static java.util.List<BlockPos> group(World world,BlockPos origin,EnumFacing facing){EnumFacing left=facing.rotateYCCW(),right=left.getOpposite();BlockPos start=origin;for(int i=0;i<2&&member(world,start.offset(left),facing);i++)start=start.offset(left);if(member(world,start.offset(left),facing))return java.util.Collections.singletonList(origin);java.util.List<BlockPos> result=new java.util.ArrayList<>();for(int i=0;i<3&&member(world,start,facing);i++,start=start.offset(right))result.add(start);return member(world,start,facing)?java.util.Collections.singletonList(origin):result;}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(!world.isRemote&&!books){boolean powered=world.isBlockPowered(pos);if(powered!=state.getValue(POWERED))world.setBlockState(pos,state.withProperty(POWERED,powered),3);}}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){neighborChanged(state,world,pos,this,pos);}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){TileEntity tile=world.getTileEntity(pos);if(tile instanceof Storage)InventoryHelper.dropInventoryItems(world,pos,(Storage)tile);super.breakBlock(world,pos,state);world.updateComparatorOutputLevel(pos,this);}
    public static final class Storage extends TileEntity implements IInventory {
        private final ItemStack[] items=new ItemStack[6];public int last=-1;
        public Storage(){Arrays.fill(items,ItemStack.EMPTY);}
        @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState previous,IBlockState next){return previous.getBlock()!=next.getBlock();}
        private boolean books(){return world==null||!(getBlockType() instanceof BookStorage)||((BookStorage)getBlockType()).books;}
        @Override public int getSizeInventory(){return books()?6:3;}
        @Override public boolean isEmpty(){for(ItemStack stack:items)if(!stack.isEmpty())return false;return true;}
        @Override public ItemStack getStackInSlot(int slot){return slot>=0&&slot<getSizeInventory()?items[slot]:ItemStack.EMPTY;}
        @Override public ItemStack decrStackSize(int slot,int count){ItemStack stack=getStackInSlot(slot);if(stack.isEmpty())return ItemStack.EMPTY;ItemStack result=stack.splitStack(count);if(stack.isEmpty())items[slot]=ItemStack.EMPTY;changed(slot);return result;}
        @Override public ItemStack removeStackFromSlot(int slot){ItemStack result=getStackInSlot(slot);if(!result.isEmpty()){items[slot]=ItemStack.EMPTY;changed(slot);}return result;}
        @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot<0||slot>=getSizeInventory()||!stack.isEmpty()&&!isItemValidForSlot(slot,stack))return;items[slot]=stack;if(!stack.isEmpty())stack.setCount(Math.min(getInventoryStackLimit(),stack.getCount()));changed(slot);}
        private void changed(int slot){last=slot;markDirty();if(world!=null&&!world.isRemote){IBlockState state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,3);world.updateComparatorOutputLevel(pos,getBlockType());}}
        @Override public int getInventoryStackLimit(){return books()?1:64;}
        @Override public boolean isItemValidForSlot(int slot,ItemStack stack){Item item=stack.getItem();return !books()||item==Items.BOOK||item==Items.ENCHANTED_BOOK||item==Items.WRITABLE_BOOK||item==Items.WRITTEN_BOOK;}
        @Override public boolean isUsableByPlayer(EntityPlayer player){return world!=null&&world.getTileEntity(pos)==this&&player.getDistanceSq(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64;}
        @Override public String getName(){return books()?"container.chiseled_bookshelf":"container.shelf";}@Override public boolean hasCustomName(){return false;}@Override public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentTranslation(getName());}
        @Override public void openInventory(EntityPlayer player){}@Override public void closeInventory(EntityPlayer player){}@Override public int getField(int id){return 0;}@Override public void setField(int id,int value){}@Override public int getFieldCount(){return 0;}@Override public void clear(){Arrays.fill(items,ItemStack.EMPTY);changed(0);}
        @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);NBTTagList list=new NBTTagList();for(int i=0;i<6;i++)if(!items[i].isEmpty()){NBTTagCompound entry=items[i].writeToNBT(new NBTTagCompound());entry.setByte("Slot",(byte)i);list.appendTag(entry);}tag.setTag("Items",list);tag.setInteger("Last",last);return tag;}
        @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);Arrays.fill(items,ItemStack.EMPTY);NBTTagList list=tag.getTagList("Items",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound entry=list.getCompoundTagAt(i);int slot=entry.getByte("Slot");if(slot>=0&&slot<6)items[slot]=new ItemStack(entry);}last=Math.max(-1,Math.min(5,tag.getInteger("Last")));}
        @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}@Override public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket(){return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos,1,getUpdateTag());}@Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());world.markBlockRangeForRenderUpdate(pos,pos);}
    }
}
