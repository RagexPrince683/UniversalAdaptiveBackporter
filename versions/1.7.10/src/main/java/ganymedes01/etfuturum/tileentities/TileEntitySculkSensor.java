package ganymedes01.etfuturum.tileentities;
import java.util.*;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.*;
public class TileEntitySculkSensor extends TileEntity {
 private static final Map<World,Map<Long,Set<TileEntitySculkSensor>>> LOADED=new IdentityHashMap<>();private int cooldown;public int power,frequency;
 @Override public void validate(){super.validate();if(worldObj!=null&&!worldObj.isRemote)LOADED.computeIfAbsent(worldObj,w->new HashMap<>()).computeIfAbsent(ChunkCoordIntPair.chunkXZ2Int(xCoord>>4,zCoord>>4),c->new HashSet<>()).add(this);}
 private void remove(){Map<Long,Set<TileEntitySculkSensor>> chunks=LOADED.get(worldObj);if(chunks==null)return;long key=ChunkCoordIntPair.chunkXZ2Int(xCoord>>4,zCoord>>4);Set<TileEntitySculkSensor> tiles=chunks.get(key);if(tiles!=null){tiles.remove(this);if(tiles.isEmpty())chunks.remove(key);}if(chunks.isEmpty())LOADED.remove(worldObj);}
 @Override public void invalidate(){remove();super.invalidate();}@Override public void onChunkUnload(){remove();super.onChunkUnload();}public static void clear(World world){LOADED.remove(world);}
 public static void emit(World world,double x,double y,double z,int frequency,EntityPlayer player){Map<Long,Set<TileEntitySculkSensor>> chunks=LOADED.get(world);if(chunks==null)return;int cx=MathHelper.floor_double(x)>>4,cz=MathHelper.floor_double(z)>>4;for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(TileEntitySculkSensor tile:new ArrayList<>(chunks.getOrDefault(ChunkCoordIntPair.chunkXZ2Int(cx+dx,cz+dz),Collections.emptySet())))tile.receive(x,y,z,frequency,player);}
 public boolean receive(double x,double y,double z,int vibration,EntityPlayer player){if(worldObj==null||worldObj.isRemote||cooldown>0||isInvalid()||!ganymedes01.etfuturum.configuration.configs.ConfigExperiments.enableSculk||vibration<1||vibration>15)return false;boolean calibrated=getBlockType() instanceof BlockCalibratedSculkSensor;int range=calibrated?16:8;double distance=getDistanceFrom(x,y,z);if(distance>range*range)return false;int meta=getBlockMetadata();if(calibrated){int facing=BlockBookStorage.face(meta)^1;net.minecraftforge.common.util.ForgeDirection side=net.minecraftforge.common.util.ForgeDirection.getOrientation(facing);int signal=worldObj.getIndirectPowerLevelTo(xCoord+side.offsetX,yCoord,zCoord+side.offsetZ,facing);if(signal!=0&&signal!=vibration)return false;}
 for(int i=1;i<32;i++){double a=i/32D;int bx=MathHelper.floor_double(xCoord+.5+(x-xCoord-.5)*a),by=MathHelper.floor_double(yCoord+.5+(y-yCoord-.5)*a),bz=MathHelper.floor_double(zCoord+.5+(z-zCoord-.5)*a);if(!worldObj.blockExists(bx,by,bz)||worldObj.getBlock(bx,by,bz)==Blocks.wool)return false;}
 frequency=vibration;power=Math.max(1,15-(int)Math.floor(15*Math.sqrt(distance)/range));cooldown=calibrated?20:40;worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,(meta&3)|4,3);worldObj.func_147453_f(xCoord,yCoord,zCoord,getBlockType());markDirty();worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,ganymedes01.etfuturum.Tags.MC_ASSET_VER+":block.sculk_sensor.clicking",1,1);if(player!=null)for(TileEntitySculkShrieker shrieker:TileEntitySculkShrieker.nearby(worldObj,xCoord,yCoord,zCoord))((BlockSculkShrieker)ModBlocks.SCULK_SHRIEKER.get()).shriek(worldObj,shrieker.xCoord,shrieker.yCoord,shrieker.zCoord,player);return true;}
 @Override public void updateEntity(){if(worldObj.isRemote||cooldown==0)return;if(--cooldown==10||cooldown==0){power=0;worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,(getBlockMetadata()&3)|(cooldown==0?0:8),3);worldObj.func_147453_f(xCoord,yCoord,zCoord,getBlockType());markDirty();}}
 @Override public net.minecraft.network.Packet getDescriptionPacket(){NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag);}
 @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}
 @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("Cooldown",cooldown);tag.setInteger("Power",power);tag.setInteger("Frequency",frequency);}
 @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);cooldown=MathHelper.clamp_int(tag.getInteger("Cooldown"),0,40);power=tag.hasKey("Power")?MathHelper.clamp_int(tag.getInteger("Power"),0,15):cooldown>10?15:0;frequency=MathHelper.clamp_int(tag.getInteger("Frequency"),0,15);}
}
