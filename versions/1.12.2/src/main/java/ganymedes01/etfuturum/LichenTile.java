package ganymedes01.etfuturum;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Six attachments require more than four metadata bits; native tile packets synchronize the mask. */
public final class LichenTile extends TileEntity {
    int sides;
    public void setSides(int mask){sides=mask&63;markDirty();if(world!=null){IBlockState state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,3);}}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState before,IBlockState after){return before.getBlock()!=after.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("SideMap",sides);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);sides=tag.getInteger("SideMap")&63;}
    @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,0,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager manager,SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());if(world!=null)world.markBlockRangeForRenderUpdate(pos,pos);}
}
