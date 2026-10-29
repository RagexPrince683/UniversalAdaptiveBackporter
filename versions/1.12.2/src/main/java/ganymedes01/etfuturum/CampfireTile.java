package ganymedes01.etfuturum;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.block.state.IBlockState;

public final class CampfireTile extends TileEntity implements ITickable {
    private final ItemStack[] items={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};private final int[] elapsed=new int[4];
    public ItemStack displayed(int slot){return items[slot];}
    private void sync(){markDirty();IBlockState state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,3);}
    @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}
    @Override public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket(){return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos,1,getUpdateTag());}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());}
    private static ItemStack result(ItemStack input){ItemStack output=FurnaceRecipes.instance().getSmeltingResult(input);return !output.isEmpty()&&output.getItem() instanceof ItemFood&&(!UAB.ID.equals(output.getItem().getRegistryName().getNamespace())||Content.available(output.getItem().getRegistryName().getPath()))?output:ItemStack.EMPTY;}
    public boolean insert(EntityPlayer player,EnumHand hand){ItemStack stack=player.getHeldItem(hand);if(result(stack).isEmpty()||!Content.available(getBlockType().getRegistryName().getPath()))return false;for(int i=0;i<4;i++)if(items[i].isEmpty()){if(!world.isRemote){items[i]=stack.copy();items[i].setCount(1);elapsed[i]=0;if(!player.capabilities.isCreativeMode)stack.shrink(1);sync();}return true;}return false;}
    @Override public void update(){if(world.isRemote)return;boolean lit=world.getBlockState(pos).getValue(CampfireBlock.LIT);for(int i=0;i<4;i++)if(!items[i].isEmpty()){if(lit){ItemStack output=result(items[i]);if(!output.isEmpty()&&++elapsed[i]>=600&&ItemDelivery.drop(world,pos.getX()+.5,pos.getY()+.6,pos.getZ()+.5,output.copy())){items[i]=ItemStack.EMPTY;elapsed[i]=0;sync();}}else elapsed[i]=Math.max(0,elapsed[i]-2);markDirty();}}
    public void dropContents(){if(world.isRemote)return;for(int i=0;i<4;i++)if(!items[i].isEmpty()){ItemDelivery.drop(world,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,items[i]);items[i]=ItemStack.EMPTY;}}
    @Override public boolean shouldRefresh(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,IBlockState old,IBlockState next){return old.getBlock()!=next.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);NBTTagList list=new NBTTagList();for(int i=0;i<4;i++)if(!items[i].isEmpty()){NBTTagCompound item=items[i].writeToNBT(new NBTTagCompound());item.setInteger("Slot",i);item.setInteger("CookingTime",elapsed[i]);list.appendTag(item);}tag.setTag("Items",list);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);java.util.Arrays.fill(items,ItemStack.EMPTY);java.util.Arrays.fill(elapsed,0);NBTTagList list=tag.getTagList("Items",10);for(int j=0;j<list.tagCount();j++){NBTTagCompound item=list.getCompoundTagAt(j);int slot=item.getInteger("Slot");if(slot>=0&&slot<4){items[slot]=new ItemStack(item);if(!items[slot].isEmpty())items[slot].setCount(1);elapsed[slot]=Math.max(0,Math.min(600,item.getInteger("CookingTime")));}}}
}
