package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.blocks.BlockLectern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityLectern extends TileEntity implements ISidedInventory {
    private ItemStack book;public int page;private long pulseUntil;private boolean loaded;
    @Override public boolean shouldRefresh(net.minecraft.block.Block previous,net.minecraft.block.Block next,int previousMeta,int nextMeta,net.minecraft.world.World world,int x,int y,int z){return previous!=next;}
    public static boolean book(ItemStack stack){return stack!=null&&(stack.getItem()==Items.writable_book||stack.getItem()==Items.written_book);}
    public int pages(){return book!=null&&book.hasTagCompound()?Math.max(1,Math.min(100,book.getTagCompound().getTagList("pages",8).tagCount())):1;}
    public int signal(){return book==null?0:1+(pages()==1?0:(int)(14L*page/(pages()-1)));}
    public boolean turn(int target){if(book==null)return false;int next=Math.max(0,Math.min(pages()-1,target));if(next==page)return false;page=next;pulseUntil=worldObj.getTotalWorldTime()+2;changed();worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,getBlockType(),2);return true;}
    public void endPulse(){if(pulseUntil>worldObj.getTotalWorldTime()){worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,getBlockType(),(int)Math.min(2,pulseUntil-worldObj.getTotalWorldTime()));return;}pulseUntil=0;changed();}
    @Override public void updateEntity(){if(loaded||worldObj.isRemote)return;loaded=true;changed();if(pulseUntil>0)worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,getBlockType(),(int)Math.max(1,Math.min(2,pulseUntil-worldObj.getTotalWorldTime())));}
    private void changed(){markDirty();if(worldObj!=null&&!worldObj.isRemote&&getBlockType() instanceof BlockLectern){int meta=worldObj.getBlockMetadata(xCoord,yCoord,zCoord);worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,(meta&3)|(pulseUntil>worldObj.getTotalWorldTime()&&book!=null?4:0),3);worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);worldObj.notifyBlocksOfNeighborChange(xCoord,yCoord-1,zCoord,getBlockType());worldObj.func_147453_f(xCoord,yCoord,zCoord,getBlockType());}}
    /** Translate released template book components without changing player-book NBT. */
    public void template(NBTTagCompound data){
        if(!data.hasKey("Book",10))return;NBTTagCompound raw=data.getCompoundTag("Book");String id=raw.getString("id");
        if(!id.equals("minecraft:written_book")&&!id.equals("minecraft:writable_book"))return;
        ItemStack stack=new ItemStack(id.equals("minecraft:written_book")?Items.written_book:Items.writable_book);
        NBTTagCompound tag=(NBTTagCompound)raw.getCompoundTag("tag").copy(),components=raw.getCompoundTag("components");
        NBTTagCompound content=components.getCompoundTag(id.equals("minecraft:written_book")?"minecraft:written_book_content":"minecraft:writable_book_content");
        if(content.hasKey("pages",9)){net.minecraft.nbt.NBTTagList source=content.getTagList("pages",10),pages=new net.minecraft.nbt.NBTTagList();
            for(int i=0;i<Math.min(100,source.tagCount());i++)pages.appendTag(new net.minecraft.nbt.NBTTagString(source.getCompoundTagAt(i).getString("raw")));
            if(source.tagCount()==0)pages=(net.minecraft.nbt.NBTTagList)content.getTagList("pages",8).copy();tag.setTag("pages",pages);
            tag.setString("author",content.getString("author"));tag.setString("title",content.hasKey("title",10)?content.getCompoundTag("title").getString("raw"):content.getString("title"));}
        stack.setTagCompound(tag);setInventorySlotContents(0,stack);page=Math.max(0,Math.min(pages()-1,data.getInteger("Page")));changed();
    }
    @Override public int getSizeInventory(){return 1;}
    @Override public ItemStack getStackInSlot(int slot){return slot==0?book:null;}
    @Override public ItemStack decrStackSize(int slot,int count){return slot!=0||count<=0?null:getStackInSlotOnClosing(0);}
    @Override public ItemStack getStackInSlotOnClosing(int slot){if(slot!=0)return null;ItemStack result=book;book=null;page=0;pulseUntil=0;changed();return result;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot!=0||stack!=null&&!book(stack))return;book=stack;if(book!=null)book.stackSize=1;page=0;pulseUntil=0;changed();}
    @Override public int getInventoryStackLimit(){return 1;}@Override public boolean isItemValidForSlot(int slot,ItemStack stack){return false;}
    @Override public int[] getAccessibleSlotsFromSide(int side){return side==0?new int[]{0}:new int[0];}
    @Override public boolean canInsertItem(int slot,ItemStack stack,int side){return false;}
    @Override public boolean canExtractItem(int slot,ItemStack stack,int side){return slot==0&&side==0;}
    @Override public boolean isUseableByPlayer(EntityPlayer player){return worldObj!=null&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&player.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64;}
    @Override public String getInventoryName(){return "container.lectern";}@Override public boolean hasCustomInventoryName(){return false;}@Override public void openInventory(){}@Override public void closeInventory(){}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);if(book!=null)tag.setTag("Book",book.writeToNBT(new NBTTagCompound()));tag.setInteger("Page",page);tag.setLong("PulseUntil",pulseUntil);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);ItemStack saved=ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Book"));book=book(saved)?saved:null;if(book!=null)book.stackSize=1;page=Math.max(0,Math.min(pages()-1,tag.getInteger("Page")));pulseUntil=Math.max(0,tag.getLong("PulseUntil"));loaded=false;}
    @Override public net.minecraft.network.Packet getDescriptionPacket(){NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag);}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());worldObj.markBlockRangeForRenderUpdate(xCoord,yCoord,zCoord,xCoord,yCoord,zCoord);}
}
