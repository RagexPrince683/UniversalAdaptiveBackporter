package ganymedes01.etfuturum;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.text.*;

public final class LecternTile extends TileEntity implements ISidedInventory {
    private ItemStack book=ItemStack.EMPTY;public int page;private long pulseUntil;
    @Override public boolean shouldRefresh(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,IBlockState previous,IBlockState next){return previous.getBlock()!=next.getBlock();}
    public static boolean book(ItemStack stack){return !stack.isEmpty()&&(stack.getItem()==Items.WRITABLE_BOOK||stack.getItem()==Items.WRITTEN_BOOK);}
    public int pages(){return book.hasTagCompound()?Math.max(1,Math.min(100,book.getTagCompound().getTagList("pages",8).tagCount())):1;}
    public int signal(){return isEmpty()?0:1+(pages()==1?0:(int)(14L*page/(pages()-1)));}
    public boolean turn(int target){if(isEmpty())return false;int next=Math.max(0,Math.min(pages()-1,target));if(next==page)return false;page=next;pulseUntil=world.getTotalWorldTime()+2;changed();world.scheduleUpdate(pos,getBlockType(),2);return true;}
    public void endPulse(){if(pulseUntil>world.getTotalWorldTime()){world.scheduleUpdate(pos,getBlockType(),(int)Math.min(2,pulseUntil-world.getTotalWorldTime()));return;}pulseUntil=0;changed();}
    @Override public void onLoad(){if(!world.isRemote){changed();if(pulseUntil>0)world.scheduleUpdate(pos,getBlockType(),(int)Math.max(1,Math.min(2,pulseUntil-world.getTotalWorldTime())));}}
    private void changed(){markDirty();if(world!=null&&!world.isRemote){IBlockState old=world.getBlockState(pos);if(!(old.getBlock() instanceof LecternBlock))return;IBlockState next=old.withProperty(LecternBlock.HAS_BOOK,!isEmpty()).withProperty(LecternBlock.POWERED,pulseUntil>world.getTotalWorldTime()&&!isEmpty());world.setBlockState(pos,next,3);world.notifyBlockUpdate(pos,old,next,3);world.notifyNeighborsOfStateChange(pos.down(),getBlockType(),false);world.updateComparatorOutputLevel(pos,getBlockType());}}
    /** Translate released template book components without changing player-book NBT. */
    public void template(NBTTagCompound data){
        if(!data.hasKey("Book",10))return;NBTTagCompound raw=data.getCompoundTag("Book");String id=raw.getString("id");
        if(!id.equals("minecraft:written_book")&&!id.equals("minecraft:writable_book"))return;
        ItemStack stack=new ItemStack(id.equals("minecraft:written_book")?Items.WRITTEN_BOOK:Items.WRITABLE_BOOK);
        NBTTagCompound tag=raw.getCompoundTag("tag").copy(),components=raw.getCompoundTag("components");
        NBTTagCompound content=components.getCompoundTag(id.equals("minecraft:written_book")?"minecraft:written_book_content":"minecraft:writable_book_content");
        if(content.hasKey("pages",9)){net.minecraft.nbt.NBTTagList source=content.getTagList("pages",10),pages=new net.minecraft.nbt.NBTTagList();
            for(int i=0;i<Math.min(100,source.tagCount());i++)pages.appendTag(new net.minecraft.nbt.NBTTagString(source.getCompoundTagAt(i).getString("raw")));
            if(source.tagCount()==0)pages=content.getTagList("pages",8).copy();tag.setTag("pages",pages);
            tag.setString("author",content.getString("author"));tag.setString("title",content.hasKey("title",10)?content.getCompoundTag("title").getString("raw"):content.getString("title"));}
        stack.setTagCompound(tag);setInventorySlotContents(0,stack);page=Math.max(0,Math.min(pages()-1,data.getInteger("Page")));changed();
    }
    @Override public int getSizeInventory(){return 1;}@Override public boolean isEmpty(){return book.isEmpty();}
    @Override public ItemStack getStackInSlot(int slot){return slot==0?book:ItemStack.EMPTY;}
    @Override public ItemStack decrStackSize(int slot,int count){if(slot!=0||count<=0||isEmpty())return ItemStack.EMPTY;return removeStackFromSlot(0);}
    @Override public ItemStack removeStackFromSlot(int slot){if(slot!=0)return ItemStack.EMPTY;ItemStack result=book;book=ItemStack.EMPTY;page=0;pulseUntil=0;changed();return result;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot!=0||!stack.isEmpty()&&!book(stack))return;book=stack;if(!book.isEmpty())book.setCount(1);page=0;pulseUntil=0;changed();}
    @Override public int getInventoryStackLimit(){return 1;}@Override public boolean isItemValidForSlot(int slot,ItemStack stack){return false;}
    @Override public int[] getSlotsForFace(EnumFacing side){return side==EnumFacing.DOWN?new int[]{0}:new int[0];}
    @Override public boolean canInsertItem(int slot,ItemStack stack,EnumFacing side){return false;}
    @Override public boolean canExtractItem(int slot,ItemStack stack,EnumFacing side){return slot==0&&side==EnumFacing.DOWN;}
    @Override public boolean isUsableByPlayer(EntityPlayer player){return world!=null&&world.getTileEntity(pos)==this&&player.getDistanceSq(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64;}
    @Override public String getName(){return "container.lectern";}@Override public boolean hasCustomName(){return false;}@Override public ITextComponent getDisplayName(){return new TextComponentTranslation(getName());}
    @Override public void openInventory(EntityPlayer player){}@Override public void closeInventory(EntityPlayer player){}@Override public int getField(int id){return id==0?page:0;}@Override public void setField(int id,int value){if(id==0)page=Math.max(0,Math.min(pages()-1,value));}@Override public int getFieldCount(){return 1;}@Override public void clear(){removeStackFromSlot(0);}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);if(!isEmpty())tag.setTag("Book",book.writeToNBT(new NBTTagCompound()));tag.setInteger("Page",page);tag.setLong("PulseUntil",pulseUntil);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);ItemStack saved=new ItemStack(tag.getCompoundTag("Book"));book=book(saved)?saved:ItemStack.EMPTY;if(!isEmpty())book.setCount(1);page=Math.max(0,Math.min(pages()-1,tag.getInteger("Page")));pulseUntil=Math.max(0,tag.getLong("PulseUntil"));}
    @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}@Override public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket(){return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos,1,getUpdateTag());}@Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());world.markBlockRangeForRenderUpdate(pos,pos);}
}
