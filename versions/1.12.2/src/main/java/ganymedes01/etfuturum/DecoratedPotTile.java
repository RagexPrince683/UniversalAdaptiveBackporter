package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.InvWrapper;

public final class DecoratedPotTile extends TileEntity implements IInventory {
    public final String[] designs={"","","",""};private ItemStack contents=ItemStack.EMPTY;private final List<ItemStack> legacy=new ArrayList<>();private final IItemHandler handler=new InvWrapper(this);
    public void setDesigns(ItemStack stack){readDesigns(stack.hasTagCompound()?stack.getTagCompound().getCompoundTag("BlockEntityTag"):new NBTTagCompound());markDirty();}
    private void readDesigns(NBTTagCompound tag){Arrays.fill(designs,"");NBTTagList list=tag.getTagList("sherds",8);for(int i=0;i<Math.min(4,list.tagCount());i++){String id=list.getStringTagAt(i);if(!id.equals("minecraft:brick"))designs[i]=id;}}
    private void writeDesigns(NBTTagCompound tag){NBTTagList list=new NBTTagList();for(String design:designs)list.appendTag(new NBTTagString(design.isEmpty()?"minecraft:brick":design));tag.setTag("sherds",list);}
    public ItemStack potItem(){ItemStack stack=Content.stack("decorated_pot");NBTTagCompound tag=new NBTTagCompound(),block=new NBTTagCompound();writeDesigns(block);tag.setTag("BlockEntityTag",block);stack.setTagCompound(tag);return stack;}
    public List<ItemStack> allContents(){List<ItemStack> result=new ArrayList<>(legacy);if(!contents.isEmpty())result.add(contents.copy());return result;}
    @Override public int getSizeInventory(){return 1;}
    @Override public boolean isEmpty(){return contents.isEmpty();}
    @Override public ItemStack getStackInSlot(int slot){return slot==0?contents:ItemStack.EMPTY;}
    @Override public ItemStack decrStackSize(int slot,int amount){if(slot!=0)return ItemStack.EMPTY;ItemStack result=contents.splitStack(amount);markDirty();return result;}
    @Override public ItemStack removeStackFromSlot(int slot){if(slot!=0)return ItemStack.EMPTY;ItemStack result=contents;contents=ItemStack.EMPTY;markDirty();return result;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot==0){contents=stack;if(!contents.isEmpty())contents.setCount(Math.min(contents.getCount(),Math.min(64,contents.getMaxStackSize())));markDirty();}}
    @Override public int getInventoryStackLimit(){return 64;}
    @Override public String getName(){return "tile.etfuturum.decorated_pot.name";}
    @Override public boolean hasCustomName(){return false;}
    @Override public ITextComponent getDisplayName(){return new TextComponentTranslation(getName());}
    @Override public boolean isUsableByPlayer(EntityPlayer player){return world.getTileEntity(pos)==this&&player.getDistanceSq(pos)<=64;}
    @Override public void openInventory(EntityPlayer player){ }
    @Override public void closeInventory(EntityPlayer player){ }
    @Override public boolean isItemValidForSlot(int slot,ItemStack stack){return slot==0;}
    @Override public int getField(int id){return 0;}
    @Override public void setField(int id,int value){ }
    @Override public int getFieldCount(){return 0;}
    @Override public void clear(){contents=ItemStack.EMPTY;legacy.clear();markDirty();}
    @Override public void markDirty(){super.markDirty();if(world!=null)world.updateComparatorOutputLevel(pos,getBlockType());}
    @Override public boolean hasCapability(Capability<?> capability,EnumFacing side){return capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY||super.hasCapability(capability,side);}
    @Override public <T>T getCapability(Capability<T> capability,EnumFacing side){return capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY?CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler):super.getCapability(capability,side);}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);writeDesigns(tag);NBTTagList items=new NBTTagList();for(ItemStack item:allContents())items.appendTag(item.writeToNBT(new NBTTagCompound()));tag.setTag("PotItems",items);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);readDesigns(tag);contents=ItemStack.EMPTY;legacy.clear();NBTTagList items=tag.getTagList(tag.hasKey("PotItems")?"PotItems":"Items",10);for(int i=0;i<items.tagCount();i++){ItemStack stack=new ItemStack(items.getCompoundTagAt(i));if(!stack.isEmpty()){if(contents.isEmpty())contents=stack;else legacy.add(stack);}}}
    @Override public NBTTagCompound getUpdateTag(){NBTTagCompound tag=super.writeToNBT(new NBTTagCompound());writeDesigns(tag);return tag;}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,1,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager network,SPacketUpdateTileEntity packet){readDesigns(packet.getNbtCompound());world.markBlockRangeForRenderUpdate(pos,pos);}
}
