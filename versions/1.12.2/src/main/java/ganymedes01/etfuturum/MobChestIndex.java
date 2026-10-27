package ganymedes01.etfuturum;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.world.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;
/** Loaded positions only: lifecycle changes maintain the index, and queries never request terrain. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class MobChestIndex {
    private static final Map<World,Map<Long,Set<BlockPos>>> WORLDS=new IdentityHashMap<>();
    private MobChestIndex(){}
    public static boolean chest(TileEntity tile){return tile instanceof TileEntityChest||tile instanceof CopperChestTile;}
    public static void add(TileEntity tile){if(tile.getWorld()==null||tile.getWorld().isRemote||!chest(tile))return;BlockPos pos=tile.getPos();WORLDS.computeIfAbsent(tile.getWorld(),key->new HashMap<>()).computeIfAbsent(ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4),key->new HashSet<>()).add(pos.toImmutable());}
    public static void remove(TileEntity tile){Map<Long,Set<BlockPos>> chunks=WORLDS.get(tile.getWorld());if(chunks!=null){BlockPos pos=tile.getPos();Set<BlockPos> set=chunks.get(ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4));if(set!=null)set.remove(pos);}}
    public static List<BlockPos> nearby(World world,double x,double y,double z){List<BlockPos> result=new ArrayList<>();Map<Long,Set<BlockPos>> chunks=WORLDS.get(world);if(chunks==null)return result;int cx=MathHelper.floor(x)>>4,cz=MathHelper.floor(z)>>4;for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){Set<BlockPos> positions=chunks.get(ChunkPos.asLong(cx+dx,cz+dz));if(positions!=null)for(BlockPos pos:positions)if(Math.abs(pos.getX()-x)<=16&&Math.abs(pos.getY()-y)<=8&&Math.abs(pos.getZ()-z)<=16)result.add(pos);}return result;}
    @SubscribeEvent public static void load(ChunkEvent.Load event){if(!event.getWorld().isRemote)for(TileEntity tile:event.getChunk().getTileEntityMap().values())add(tile);}
    @SubscribeEvent public static void unload(ChunkEvent.Unload event){Map<Long,Set<BlockPos>> chunks=WORLDS.get(event.getWorld());if(chunks!=null)chunks.remove(ChunkPos.asLong(event.getChunk().x,event.getChunk().z));}
    @SubscribeEvent public static void unload(WorldEvent.Unload event){WORLDS.remove(event.getWorld());}
}
