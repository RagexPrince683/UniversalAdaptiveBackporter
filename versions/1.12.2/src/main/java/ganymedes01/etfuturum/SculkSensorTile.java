package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public final class SculkSensorTile extends TileEntity implements ITickable {
 private static final Map<World,Map<Long,Set<SculkSensorTile>>> LOADED=new IdentityHashMap<>();private int cooldown;public int power,frequency;
 @Override public void onLoad(){if(!world.isRemote)LOADED.computeIfAbsent(world,w->new HashMap<>()).computeIfAbsent(ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4),c->new HashSet<>()).add(this);}
 private void remove(){Map<Long,Set<SculkSensorTile>> chunks=LOADED.get(world);if(chunks==null)return;long key=ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4);Set<SculkSensorTile> tiles=chunks.get(key);if(tiles!=null){tiles.remove(this);if(tiles.isEmpty())chunks.remove(key);}if(chunks.isEmpty())LOADED.remove(world);}
 @Override public void invalidate(){remove();super.invalidate();}@Override public void onChunkUnload(){remove();super.onChunkUnload();}public static void clear(World world){LOADED.remove(world);}
 public static void emit(World world,double x,double y,double z,int frequency,EntityPlayer player){Map<Long,Set<SculkSensorTile>> chunks=LOADED.get(world);if(chunks==null)return;int cx=MathHelper.floor(x)>>4,cz=MathHelper.floor(z)>>4;for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(SculkSensorTile tile:new ArrayList<>(chunks.getOrDefault(ChunkPos.asLong(cx+dx,cz+dz),Collections.emptySet())))tile.receive(x,y,z,frequency,player);}
 public boolean receive(double x,double y,double z,int vibration,EntityPlayer player){if(world==null||world.isRemote||cooldown>0||isInvalid()||!Content.available(getBlockType().getRegistryName().getPath())||vibration<1||vibration>15)return false;boolean calibrated=getBlockType() instanceof CalibratedSculkSensor;int range=calibrated?16:8;double distance=(x-pos.getX()-.5)*(x-pos.getX()-.5)+(y-pos.getY()-.5)*(y-pos.getY()-.5)+(z-pos.getZ()-.5)*(z-pos.getZ()-.5);if(distance>range*range)return false;IBlockState state=world.getBlockState(pos);if(calibrated){EnumFacing side=state.getValue(CalibratedSculkSensor.FACING).getOpposite();BlockPos input=pos.offset(side);int signal=world.getRedstonePower(input,side);if(signal!=0&&signal!=vibration)return false;}
 for(int i=1;i<32;i++){double a=i/32D;BlockPos at=new BlockPos(pos.getX()+.5+(x-pos.getX()-.5)*a,pos.getY()+.5+(y-pos.getY()-.5)*a,pos.getZ()+.5+(z-pos.getZ()-.5)*a);if(!world.isBlockLoaded(at)||world.getBlockState(at).getBlock()==Blocks.WOOL)return false;}
 frequency=vibration;power=Math.max(1,15-(int)Math.floor(15*Math.sqrt(distance)/range));cooldown=calibrated?20:40;world.setBlockState(pos,state.withProperty(SculkSensor.PHASE,SculkSensor.Phase.ACTIVE),3);world.updateComparatorOutputLevel(pos,getBlockType());markDirty();world.playSound(null,pos,ModernSounds.get("block.sculk_sensor.clicking"),SoundCategory.BLOCKS,1,1);if(player!=null)for(SculkShriekerTile shrieker:SculkShriekerTile.nearby(world,pos))((SculkShrieker)Content.block("sculk_shrieker")).shriek(world,shrieker.getPos(),player);return true;}
 @Override public void update(){if(world.isRemote||cooldown==0)return;if(--cooldown==10||cooldown==0){power=0;world.setBlockState(pos,world.getBlockState(pos).withProperty(SculkSensor.PHASE,cooldown==0?SculkSensor.Phase.INACTIVE:SculkSensor.Phase.COOLDOWN),3);world.updateComparatorOutputLevel(pos,getBlockType());markDirty();}}
 @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
 @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}
 @Override public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket(){return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos,1,getUpdateTag());}
 @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());}
 @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("Cooldown",cooldown);tag.setInteger("Power",power);tag.setInteger("Frequency",frequency);return tag;}
 @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);cooldown=MathHelper.clamp(tag.getInteger("Cooldown"),0,40);power=tag.hasKey("Power")?MathHelper.clamp(tag.getInteger("Power"),0,15):cooldown>10?15:0;frequency=MathHelper.clamp(tag.getInteger("Frequency"),0,15);}
}
