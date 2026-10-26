package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public final class SculkSensorTile extends TileEntity implements ITickable {
    private int cooldown;
    @Override public void update(){if(world.isRemote||!Content.available("sculk_sensor"))return;if(cooldown>0){if(--cooldown==10){world.setBlockState(pos,getBlockType().getDefaultState(),3);markDirty();}return;}if((world.getTotalWorldTime()+pos.getX()+pos.getZ())%10!=0)return;for(EntityPlayer player:world.playerEntities){if(player.capabilities.disableDamage||player.isSneaking()||player.getDistanceSqToCenter(pos)>64||player.getDistanceSq(player.prevPosX,player.prevPosY,player.prevPosZ)<.0001)continue;BlockPos foot=new BlockPos(player.posX,player.getEntityBoundingBox().minY-.01,player.posZ);if(!world.isBlockLoaded(foot))continue;Block floor=world.getBlockState(foot).getBlock();if(floor==Blocks.WOOL||floor==Blocks.CARPET)continue;boolean blocked=false;for(int i=0;i<=16;i++){double a=i/16D;BlockPos at=new BlockPos(pos.getX()+.5+(player.posX-pos.getX()-.5)*a,pos.getY()+.5+(player.posY-pos.getY()-.5)*a,pos.getZ()+.5+(player.posZ-pos.getZ()-.5)*a);if(!world.isBlockLoaded(at)||world.getBlockState(at).getBlock()==Blocks.WOOL){blocked=true;break;}}if(blocked)continue;cooldown=40;world.setBlockState(pos,getBlockType().getDefaultState().withProperty(SculkSensor.PHASE,SculkSensor.Phase.ACTIVE),3);markDirty();world.playSound(null,pos,ModernSounds.get("block.sculk_sensor.clicking"),SoundCategory.BLOCKS,1,1);for(SculkShriekerTile shrieker:SculkShriekerTile.nearby(world,pos))((SculkShrieker)Content.block("sculk_shrieker")).shriek(world,shrieker.getPos(),player);break;}}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("Cooldown",cooldown);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);cooldown=MathHelper.clamp(tag.getInteger("Cooldown"),0,40);}
}
