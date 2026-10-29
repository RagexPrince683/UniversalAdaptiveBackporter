package ganymedes01.etfuturum.tileentities;

import java.util.*;
import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.blocks.BlockCrafter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Native recipe execution and saved output ownership, entirely on the server thread. */
public final class TileEntityCrafter extends TileEntity implements ISidedInventory {
    private static final int[] SLOTS={0,1,2,3,4,5,6,7,8};
    private final ItemStack[] items=new ItemStack[9];private final Deque<ItemStack> outputs=new ArrayDeque<>();
    public int disabled,craftingTicks;private int delay=-1,retry;private boolean first=true,busy;
    @Override public boolean shouldRefresh(net.minecraft.block.Block old,net.minecraft.block.Block next,int oldMeta,int nextMeta,World world,int x,int y,int z){return old!=next;}
    private static boolean empty(ItemStack stack){return stack==null||stack.stackSize<=0;}
    public boolean disabled(int slot){return slot>=0&&slot<9&&(disabled&(1<<slot))!=0;}
    public boolean toggle(int slot){if(slot<0||slot>=9||!empty(items[slot]))return false;disabled^=1<<slot;markDirty();sync();return true;}
    public int signal(){int count=0;for(int i=0;i<9;i++)if(disabled(i)||!empty(items[i]))count++;return count;}
    public void pulse(){if(delay<0){delay=4;markDirty();}}
    @Override public void updateEntity(){
        if(worldObj.isRemote){if(craftingTicks>0&&--craftingTicks==0)worldObj.markBlockRangeForRenderUpdate(xCoord,yCoord,zCoord,xCoord,yCoord,zCoord);return;}
        if(busy)return;busy=true;try{
            if(first){first=false;if(getBlockType() instanceof BlockCrafter)((BlockCrafter)getBlockType()).power(worldObj,xCoord,yCoord,zCoord);}
            if(craftingTicks>0&&--craftingTicks==0){markDirty();sync();}
            if(retry>0)retry--;if(!outputs.isEmpty()&&retry==0)flush();
            if(delay>=0&&--delay<=0){if(!outputs.isEmpty())delay=1;else{delay=-1;craft();}}
            if(!outputs.isEmpty()&&retry==0)flush();
        }finally{busy=false;}
    }
    private void craft(){
        InventoryCrafting grid=new InventoryCrafting(new Container(){@Override public boolean canInteractWith(EntityPlayer player){return false;}},3,3);
        ItemStack[] before=new ItemStack[9];int mask=disabled;
        for(int i=0;i<9;i++){before[i]=empty(items[i])?null:items[i].copy();grid.setInventorySlotContents(i,disabled(i)||before[i]==null?null:before[i].copy());}
        ItemStack result=CraftingManager.getInstance().findMatchingRecipe(grid,worldObj);if(empty(result)){fail();return;}
        List<ItemStack> delivery=new ArrayList<>();if(!split(result,delivery)){fail();return;}
        // Legacy recipes have no remaining-items API; use Forge's native per-stack container callback.
        for(int i=0;i<9;i++){ItemStack ingredient=grid.getStackInSlot(i);if(ingredient==null||!ingredient.getItem().hasContainerItem(ingredient))continue;ItemStack remainder=ingredient.getItem().getContainerItem(ingredient);if(remainder!=null&&remainder.isItemStackDamageable()&&remainder.getItemDamage()>remainder.getMaxDamage())continue;if(!split(remainder,delivery)){fail();return;}}
        if(worldObj.getTileEntity(xCoord,yCoord,zCoord)!=this||!(getBlockType() instanceof BlockCrafter)||mask!=disabled)return;for(int i=0;i<9;i++)if(!ItemStack.areItemStacksEqual(before[i],getStackInSlot(i)))return;
        for(int i=0;i<9;i++)if(!disabled(i)&&!empty(items[i])&&--items[i].stackSize<=0)items[i]=null;
        outputs.addAll(delivery);craftingTicks=6;markDirty();sync();
        worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,Tags.MC_ASSET_VER+":block.crafter.craft",1,1);
        ganymedes01.etfuturum.core.handlers.SculkEventHandler.emit(worldObj,xCoord+.5,yCoord+.5,zCoord+.5,10,null);
        ForgeDirection face=facing();worldObj.playAuxSFX(2000,xCoord,yCoord,zCoord,(face.offsetX+1)+(face.offsetZ+1)*3);
    }
    private static boolean split(ItemStack stack,List<ItemStack> list){if(empty(stack))return true;int count=stack.stackSize;if(count>4096)return false;int limit=Math.max(1,Math.min(64,stack.getMaxStackSize()));while(count>0){if(list.size()>=128)return false;ItemStack part=stack.copy();part.stackSize=Math.min(limit,count);list.add(part);count-=part.stackSize;}return true;}
    private void fail(){markDirty();worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,Tags.MC_ASSET_VER+":block.crafter.fail",1,1);}
    private ForgeDirection facing(){return ForgeDirection.getOrientation(Math.min(5,worldObj.getBlockMetadata(xCoord,yCoord,zCoord)&7));}
    private void flush(){
        ForgeDirection face=facing();int x=xCoord+face.offsetX,y=yCoord+face.offsetY,z=zCoord+face.offsetZ;if(!worldObj.blockExists(x,Math.max(0,Math.min(worldObj.getHeight()-1,y)),z)){retry=20;return;}
        int count=outputs.size();while(count-->0&&!outputs.isEmpty()){
            // Remove before external callbacks, so breaking this block cannot duplicate the offered stack.
            ItemStack remaining=outputs.removeFirst();markDirty();IInventory inventory=TileEntityHopper.func_145893_b(worldObj,x+.5,y+.5,z+.5);
            if(inventory!=null)remaining=TileEntityHopper.func_145889_a(inventory,remaining,face.getOpposite().ordinal());
            if(!empty(remaining)&&!eject(remaining,face)){outputs.addFirst(remaining);retry=20;markDirty();return;}
            if(worldObj.getTileEntity(xCoord,yCoord,zCoord)!=this)return;markDirty();
        }
    }
    private boolean eject(ItemStack stack,ForgeDirection face){EntityItem entity=new EntityItem(worldObj,xCoord+.5+face.offsetX*.7,yCoord+.5+face.offsetY*.7,zCoord+.5+face.offsetZ*.7,stack.copy());entity.motionX=face.offsetX*.1;entity.motionY=.1+face.offsetY*.1;entity.motionZ=face.offsetZ*.1;entity.delayBeforeCanPickup=10;return worldObj.spawnEntityInWorld(entity);}
    public void dropOutputs(){while(!outputs.isEmpty()){ItemStack stack=outputs.removeFirst();worldObj.spawnEntityInWorld(new EntityItem(worldObj,xCoord+.5,yCoord+.5,zCoord+.5,stack));}markDirty();}
    private void sync(){if(worldObj!=null&&!worldObj.isRemote)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
    @Override public void markDirty(){super.markDirty();if(worldObj!=null&&!worldObj.isRemote)worldObj.func_147453_f(xCoord,yCoord,zCoord,getBlockType());}
    @Override public int getSizeInventory(){return 9;}
    @Override public ItemStack getStackInSlot(int slot){return slot>=0&&slot<9&&!empty(items[slot])?items[slot]:null;}
    @Override public ItemStack decrStackSize(int slot,int count){if(slot<0||slot>=9||count<=0||empty(items[slot]))return null;ItemStack result=items[slot].splitStack(count);if(items[slot].stackSize<=0)items[slot]=null;markDirty();return result;}
    @Override public ItemStack getStackInSlotOnClosing(int slot){if(slot<0||slot>=9)return null;ItemStack result=items[slot];items[slot]=null;markDirty();return result;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot<0||slot>=9||!empty(stack)&&disabled(slot))return;items[slot]=empty(stack)?null:stack;if(items[slot]!=null)items[slot].stackSize=Math.min(64,items[slot].stackSize);markDirty();}
    @Override public int getInventoryStackLimit(){return 64;}@Override public boolean isItemValidForSlot(int slot,ItemStack stack){return slot>=0&&slot<9&&!disabled(slot);}
    @Override public int[] getAccessibleSlotsFromSide(int face){return SLOTS;}
    @Override public boolean canInsertItem(int slot,ItemStack stack,int face){if(!isItemValidForSlot(slot,stack))return false;ItemStack current=items[slot];if(empty(current))return true;for(int i=0;i<9;i++)if(i!=slot&&!disabled(i)){ItemStack other=items[i];if(empty(other)||stack.isItemEqual(other)&&ItemStack.areItemStackTagsEqual(stack,other)&&other.stackSize<current.stackSize)return false;}return true;}
    @Override public boolean canExtractItem(int slot,ItemStack stack,int face){return slot>=0&&slot<9;}
    @Override public boolean isUseableByPlayer(EntityPlayer player){return worldObj!=null&&!player.isDead&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&player.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64;}
    @Override public String getInventoryName(){return "container.crafter";}@Override public boolean hasCustomInventoryName(){return false;}@Override public void openInventory(){}@Override public void closeInventory(){}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);NBTTagList contents=new NBTTagList();for(int i=0;i<9;i++)if(!empty(items[i])){NBTTagCompound entry=items[i].writeToNBT(new NBTTagCompound());entry.setByte("Slot",(byte)i);contents.appendTag(entry);}tag.setTag("Items",contents);tag.setInteger("Disabled",disabled);tag.setInteger("CraftDelay",delay);tag.setInteger("CraftingTicks",craftingTicks);NBTTagList list=new NBTTagList();for(ItemStack stack:outputs)list.appendTag(stack.writeToNBT(new NBTTagCompound()));tag.setTag("Outputs",list);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);Arrays.fill(items,null);NBTTagList contents=tag.getTagList("Items",10);for(int i=0;i<contents.tagCount();i++){NBTTagCompound entry=contents.getCompoundTagAt(i);int slot=entry.getByte("Slot");if(slot>=0&&slot<9)items[slot]=ItemStack.loadItemStackFromNBT(entry);}disabled=tag.getInteger("Disabled")&511;for(int i=0;i<9;i++)if(!empty(items[i]))disabled&=~(1<<i);delay=tag.hasKey("CraftDelay")?Math.max(-1,Math.min(4,tag.getInteger("CraftDelay"))):-1;craftingTicks=Math.max(0,Math.min(6,tag.getInteger("CraftingTicks")));outputs.clear();NBTTagList list=tag.getTagList("Outputs",10);for(int i=0;i<Math.min(128,list.tagCount());i++){ItemStack stack=ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));if(!empty(stack)){stack.stackSize=Math.min(64,stack.stackSize);outputs.add(stack);}}first=true;}
    private NBTTagCompound flags(){NBTTagCompound tag=new NBTTagCompound();tag.setInteger("Disabled",disabled);tag.setInteger("CraftingTicks",craftingTicks);return tag;}
    @Override public net.minecraft.network.Packet getDescriptionPacket(){return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,flags());}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.S35PacketUpdateTileEntity packet){NBTTagCompound tag=packet.func_148857_g();disabled=tag.getInteger("Disabled")&511;craftingTicks=Math.max(0,Math.min(6,tag.getInteger("CraftingTicks")));worldObj.markBlockRangeForRenderUpdate(xCoord,yCoord,zCoord,xCoord,yCoord,zCoord);}
}
