package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid=UAB.ID)
public final class LightningRodTile extends TileEntity {
    private static final Map<World,Set<LightningRodTile>> LOADED=new IdentityHashMap<>();private long poweredUntil;
    @Override public void onLoad(){if(!world.isRemote){LOADED.computeIfAbsent(world,key->new HashSet<>()).add(this);IBlockState state=world.getBlockState(pos);if(state.getBlock() instanceof LightningRod&&state.getValue(LightningRod.POWERED))world.scheduleUpdate(pos,state.getBlock(),Math.max(1,remaining()));}}
    void power(){poweredUntil=world.getTotalWorldTime()+8;markDirty();}
    int remaining(){return (int)Math.min(8,Math.max(0,poweredUntil-world.getTotalWorldTime()));}
    private void remove(){Set<LightningRodTile> rods=LOADED.get(world);if(rods!=null){rods.remove(this);if(rods.isEmpty())LOADED.remove(world);}}
    @Override public void invalidate(){remove();super.invalidate();}
    @Override public void onChunkUnload(){remove();super.onChunkUnload();}
    @SubscribeEvent public static void unload(WorldEvent.Unload event){LOADED.remove(event.getWorld());}
    public static BlockPos attract(World world,BlockPos strike){if(!Content.available("lightning_rod"))return strike;LightningRodTile nearest=null;double distance=128*128;for(LightningRodTile tile:LOADED.getOrDefault(world,Collections.emptySet())){if(tile.isInvalid()||!world.isBlockLoaded(tile.pos)||!(world.getBlockState(tile.pos).getBlock() instanceof LightningRod)||!world.canSeeSky(tile.pos.up()))continue;double value=tile.pos.distanceSq(strike);if(value<=distance){distance=value;nearest=tile;}}return nearest==null?strike:nearest.pos.up();}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setLong("PoweredUntil",poweredUntil);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);poweredUntil=tag.getLong("PoweredUntil");}
}
