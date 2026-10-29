package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

/** Native recipe execution and saved output ownership, entirely on the server thread. */
public final class CrafterTile extends TileEntity implements ISidedInventory,ITickable {
    private static final int[] SLOTS={0,1,2,3,4,5,6,7,8};
    private final NonNullList<ItemStack> items=NonNullList.withSize(9,ItemStack.EMPTY);
    private final Deque<ItemStack> outputs=new ArrayDeque<>();
    private final IItemHandler[] handlers=new IItemHandler[6];
    public int disabled,craftingTicks;private int delay=-1,retry;private boolean first=true,busy;
    public CrafterTile(){for(EnumFacing face:EnumFacing.values())handlers[face.getIndex()]=new SidedInvWrapper(this,face);}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState next){return old.getBlock()!=next.getBlock();}
    public boolean disabled(int slot){return slot>=0&&slot<9&&(disabled&(1<<slot))!=0;}
    public boolean toggle(int slot){if(slot<0||slot>=9||!items.get(slot).isEmpty())return false;disabled^=1<<slot;markDirty();sync();return true;}
    public int signal(){int count=0;for(int i=0;i<9;i++)if(disabled(i)||!items.get(i).isEmpty())count++;return count;}
    public void pulse(){if(delay<0){delay=4;markDirty();}}
    @Override public void update(){
        if(world.isRemote){if(craftingTicks>0&&--craftingTicks==0)world.markBlockRangeForRenderUpdate(pos,pos);return;}
        if(busy)return;busy=true;try{
            if(first){first=false;if(getBlockType() instanceof CrafterBlock)((CrafterBlock)getBlockType()).power(world,pos);}
            if(craftingTicks>0&&--craftingTicks==0){markDirty();sync();}
            if(retry>0)retry--;if(!outputs.isEmpty()&&retry==0)flush();
            if(delay>=0&&--delay<=0){if(!outputs.isEmpty())delay=1;else{delay=-1;craft();}}
            if(!outputs.isEmpty()&&retry==0)flush();
        }finally{busy=false;}
    }
    private void craft(){
        InventoryCrafting grid=new InventoryCrafting(new Container(){@Override public boolean canInteractWith(EntityPlayer player){return false;}},3,3);
        ItemStack[] before=new ItemStack[9];int mask=disabled;
        for(int i=0;i<9;i++){before[i]=items.get(i).copy();grid.setInventorySlotContents(i,disabled(i)?ItemStack.EMPTY:before[i].copy());}
        IRecipe recipe=CraftingManager.findMatchingRecipe(grid,world);if(recipe==null){fail();return;}
        ItemStack result=recipe.getCraftingResult(grid);if(result==null||result.isEmpty()){fail();return;}
        NonNullList<ItemStack> remains=recipe.getRemainingItems(grid);if(remains==null||remains.size()!=9){fail();return;}
        List<ItemStack> delivery=new ArrayList<>();if(!split(result,delivery)){fail();return;}
        for(ItemStack remainder:remains)if(!split(remainder,delivery)){fail();return;}
        if(world.getTileEntity(pos)!=this||!(getBlockType() instanceof CrafterBlock)||mask!=disabled)return;for(int i=0;i<9;i++)if(!ItemStack.areItemStacksEqual(before[i],items.get(i)))return;
        for(int i=0;i<9;i++)if(!disabled(i)&&!items.get(i).isEmpty())items.get(i).shrink(1);
        outputs.addAll(delivery);craftingTicks=6;markDirty();sync();
        world.playSound(null,pos,ModernSounds.get("block.crafter.craft"),SoundCategory.BLOCKS,1,1);
        SculkMechanics.emit(world,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,10,null);
        EnumFacing facing=world.getBlockState(pos).getValue(CrafterBlock.FACING);world.playEvent(2000,pos,(facing.getXOffset()+1)+(facing.getZOffset()+1)*3);
    }
    private static boolean split(ItemStack stack,List<ItemStack> list){if(stack==null)return false;if(stack.isEmpty())return true;int count=stack.getCount();if(count>4096)return false;int limit=Math.max(1,Math.min(64,stack.getMaxStackSize()));while(count>0){if(list.size()>=128)return false;ItemStack part=stack.copy();part.setCount(Math.min(limit,count));list.add(part);count-=part.getCount();}return true;}
    private void fail(){markDirty();world.playSound(null,pos,ModernSounds.get("block.crafter.fail"),SoundCategory.BLOCKS,1,1);}
    private void flush(){
        EnumFacing facing=world.getBlockState(pos).getValue(CrafterBlock.FACING);BlockPos front=pos.offset(facing);if(!world.isBlockLoaded(new BlockPos(front.getX(),Math.max(0,Math.min(world.getHeight()-1,front.getY())),front.getZ()))){retry=20;return;}
        int count=outputs.size();while(count-->0&&!outputs.isEmpty()){
            // Remove before external callbacks, so a receiver breaking this block cannot duplicate the offered stack.
            ItemStack remaining=outputs.removeFirst();markDirty();TileEntity target=world.getTileEntity(front);
            if(target!=null&&target.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,facing.getOpposite())){
                IItemHandler handler=target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,facing.getOpposite());if(handler!=null)remaining=ItemHandlerHelper.insertItemStacked(handler,remaining,false);
            }else{IInventory inventory=TileEntityHopper.getInventoryAtPosition(world,front.getX()+.5,front.getY()+.5,front.getZ()+.5);if(inventory!=null)remaining=TileEntityHopper.putStackInInventoryAllSlots(this,inventory,remaining,facing.getOpposite());}
            if(!remaining.isEmpty()&&!eject(remaining,facing)){outputs.addFirst(remaining);retry=20;markDirty();return;}
            if(world.getTileEntity(pos)!=this)return;markDirty();
        }
    }
    private boolean eject(ItemStack stack,EnumFacing facing){EntityItem entity=new EntityItem(world,pos.getX()+.5+facing.getXOffset()*.7,pos.getY()+.5+facing.getYOffset()*.7,pos.getZ()+.5+facing.getZOffset()*.7,stack.copy());entity.motionX=facing.getXOffset()*.1;entity.motionY=.1+facing.getYOffset()*.1;entity.motionZ=facing.getZOffset()*.1;entity.setDefaultPickupDelay();return world.spawnEntity(entity);}
    public void dropOutputs(){while(!outputs.isEmpty()){ItemStack stack=outputs.removeFirst();InventoryHelper.spawnItemStack(world,pos.getX(),pos.getY(),pos.getZ(),stack);}markDirty();}
    private void sync(){if(world!=null&&!world.isRemote){IBlockState state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,3);}}
    @Override public void markDirty(){super.markDirty();if(world!=null&&!world.isRemote)world.updateComparatorOutputLevel(pos,getBlockType());}
    @Override public int getSizeInventory(){return 9;}@Override public boolean isEmpty(){for(ItemStack stack:items)if(!stack.isEmpty())return false;return true;}
    @Override public ItemStack getStackInSlot(int slot){return slot>=0&&slot<9?items.get(slot):ItemStack.EMPTY;}
    @Override public ItemStack decrStackSize(int slot,int count){if(slot<0||slot>=9||count<=0)return ItemStack.EMPTY;ItemStack result=ItemStackHelper.getAndSplit(items,slot,count);if(!result.isEmpty())markDirty();return result;}
    @Override public ItemStack removeStackFromSlot(int slot){if(slot<0||slot>=9)return ItemStack.EMPTY;ItemStack result=ItemStackHelper.getAndRemove(items,slot);markDirty();return result;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot<0||slot>=9||!stack.isEmpty()&&disabled(slot))return;items.set(slot,stack);if(stack.getCount()>64)stack.setCount(64);markDirty();}
    @Override public int getInventoryStackLimit(){return 64;}@Override public boolean isItemValidForSlot(int slot,ItemStack stack){return slot>=0&&slot<9&&!disabled(slot);}
    @Override public int[] getSlotsForFace(EnumFacing face){return SLOTS;}
    @Override public boolean canInsertItem(int slot,ItemStack stack,EnumFacing face){if(!isItemValidForSlot(slot,stack))return false;ItemStack current=items.get(slot);if(current.isEmpty())return true;for(int i=0;i<9;i++)if(i!=slot&&!disabled(i)){ItemStack other=items.get(i);if(other.isEmpty()||ItemHandlerHelper.canItemStacksStack(stack,other)&&other.getCount()<current.getCount())return false;}return true;}
    @Override public boolean canExtractItem(int slot,ItemStack stack,EnumFacing face){return slot>=0&&slot<9;}
    @Override public boolean isUsableByPlayer(EntityPlayer player){return world!=null&&!player.isDead&&world.getTileEntity(pos)==this&&player.getDistanceSq(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64;}
    @Override public String getName(){return "container.crafter";}@Override public boolean hasCustomName(){return false;}@Override public ITextComponent getDisplayName(){return new TextComponentTranslation(getName());}
    @Override public void openInventory(EntityPlayer player){}@Override public void closeInventory(EntityPlayer player){}@Override public int getField(int id){return id==0?disabled:0;}@Override public void setField(int id,int value){if(id==0)disabled=value&511;}@Override public int getFieldCount(){return 1;}@Override public void clear(){for(int i=0;i<9;i++)items.set(i,ItemStack.EMPTY);markDirty();}
    @Override public boolean hasCapability(Capability<?> capability,EnumFacing face){return capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY||super.hasCapability(capability,face);}
    @Override public <T>T getCapability(Capability<T> capability,EnumFacing face){return capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY?CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers[face==null?1:face.getIndex()]):super.getCapability(capability,face);}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);ItemStackHelper.saveAllItems(tag,items);tag.setInteger("Disabled",disabled);tag.setInteger("CraftDelay",delay);tag.setInteger("CraftingTicks",craftingTicks);NBTTagList list=new NBTTagList();for(ItemStack stack:outputs)list.appendTag(stack.writeToNBT(new NBTTagCompound()));tag.setTag("Outputs",list);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);for(int i=0;i<9;i++)items.set(i,ItemStack.EMPTY);ItemStackHelper.loadAllItems(tag,items);disabled=tag.getInteger("Disabled")&511;for(int i=0;i<9;i++)if(!items.get(i).isEmpty())disabled&=~(1<<i);delay=tag.hasKey("CraftDelay")?Math.max(-1,Math.min(4,tag.getInteger("CraftDelay"))):-1;craftingTicks=Math.max(0,Math.min(6,tag.getInteger("CraftingTicks")));outputs.clear();NBTTagList list=tag.getTagList("Outputs",10);for(int i=0;i<Math.min(128,list.tagCount());i++){ItemStack stack=new ItemStack(list.getCompoundTagAt(i));if(!stack.isEmpty()){stack.setCount(Math.min(64,stack.getCount()));outputs.add(stack);}}first=true;}
    private NBTTagCompound flags(){NBTTagCompound tag=super.writeToNBT(new NBTTagCompound());tag.setInteger("Disabled",disabled);tag.setInteger("CraftingTicks",craftingTicks);return tag;}
    private void flags(NBTTagCompound tag){disabled=tag.getInteger("Disabled")&511;craftingTicks=Math.max(0,Math.min(6,tag.getInteger("CraftingTicks")));world.markBlockRangeForRenderUpdate(pos,pos);}
    @Override public NBTTagCompound getUpdateTag(){return flags();}@Override public void handleUpdateTag(NBTTagCompound tag){flags(tag);}
    @Override public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket(){return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos,1,flags());}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.SPacketUpdateTileEntity packet){flags(packet.getNbtCompound());}
}
