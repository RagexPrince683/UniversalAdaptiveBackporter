package ganymedes01.etfuturum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class PotionCauldronTile extends TileEntity {
    public ItemStack potion=ItemStack.EMPTY;
    void setPotion(ItemStack value){potion=value.copy();potion.setCount(1);markDirty();IBlockState state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,3);}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);if(!potion.isEmpty())tag.setTag("Potion",potion.writeToNBT(new NBTTagCompound()));return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);potion=new ItemStack(tag.getCompoundTag("Potion"));}
    @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,0,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager network,SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());world.markBlockRangeForRenderUpdate(pos,pos);}
}
