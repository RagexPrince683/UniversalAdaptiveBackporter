package ganymedes01.etfuturum;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Only the growing tip owns length and shearing; berry state remains native block metadata. */
public final class CaveVineTile extends TileEntity {
    int maxLength=2+new java.util.Random().nextInt(26);
    boolean sheared;
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState before,IBlockState after){return before.getBlock()!=after.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("MaxLength",maxLength);tag.setBoolean("TipSheared",sheared);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);maxLength=tag.hasKey("MaxLength")?Math.max(2,Math.min(27,tag.getInteger("MaxLength"))):27;sheared=tag.getBoolean("TipSheared");}
}
