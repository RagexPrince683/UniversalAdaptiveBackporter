package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class SculkShriekerTile extends TileEntity {
    private static final Map<World,Set<SculkShriekerTile>> LOADED=new IdentityHashMap<>();
    @Override public void onLoad(){if(!world.isRemote)LOADED.computeIfAbsent(world,key->Collections.newSetFromMap(new IdentityHashMap<>())).add(this);}
    private void remove(){Set<SculkShriekerTile> set=LOADED.get(world);if(set!=null){set.remove(this);if(set.isEmpty())LOADED.remove(world);}}
    @Override public void invalidate(){remove();super.invalidate();}
    @Override public void onChunkUnload(){remove();super.onChunkUnload();}
    @Override public boolean shouldRefresh(World world,BlockPos pos,net.minecraft.block.state.IBlockState old,net.minecraft.block.state.IBlockState state){return old.getBlock()!=state.getBlock();}
    static List<SculkShriekerTile> nearby(World world,BlockPos pos){List<SculkShriekerTile> list=new ArrayList<>();for(SculkShriekerTile tile:LOADED.getOrDefault(world,Collections.emptySet()))if(!tile.isInvalid()&&tile.pos.distanceSq(pos)<=64)list.add(tile);return list;}
    @SubscribeEvent public static void unload(WorldEvent.Unload event){LOADED.remove(event.getWorld());}
}
