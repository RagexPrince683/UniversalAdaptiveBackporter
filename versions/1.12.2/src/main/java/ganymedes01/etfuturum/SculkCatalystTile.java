package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
/** Bounded, saved XP charge. One successful conversion spends one XP; overflow remains native XP. */
public final class SculkCatalystTile extends TileEntity implements ITickable {
 private static final Map<World,Set<SculkCatalystTile>> LOADED=new IdentityHashMap<>();private int charge,misses;
 @Override public void onLoad(){if(!world.isRemote)LOADED.computeIfAbsent(world,w->new HashSet<>()).add(this);}
 private void remove(){Set<SculkCatalystTile> tiles=LOADED.get(world);if(tiles!=null){tiles.remove(this);if(tiles.isEmpty())LOADED.remove(world);}}
 @Override public void invalidate(){remove();super.invalidate();}@Override public void onChunkUnload(){remove();super.onChunkUnload();}
 public static void clear(World world){LOADED.remove(world);}
 public static SculkCatalystTile nearest(World world,double x,double y,double z){SculkCatalystTile found=null;double distance=64;for(SculkCatalystTile tile:LOADED.getOrDefault(world,Collections.emptySet()))if(!tile.isInvalid()&&world.isBlockLoaded(tile.pos)){double value=tile.pos.distanceSq(x,y,z);if(value<=distance){distance=value;found=tile;}}return found;}
 public int accept(int amount){if(world==null||world.isRemote||!Content.available("sculk_catalyst")||!Content.available("sculk"))return 0;int accepted=Math.min(Math.max(0,amount),4096-charge);if(accepted>0){charge+=accepted;misses=0;world.setBlockState(pos,world.getBlockState(pos).withProperty(SculkCatalyst.BLOOM,true),3);world.scheduleUpdate(pos,getBlockType(),8);world.playSound(null,pos,ModernSounds.get("block.sculk_catalyst.bloom"),SoundCategory.BLOCKS,2,1);markDirty();}return accepted;}
 @Override public void update(){if(world.isRemote||charge==0||!Content.available("sculk"))return;for(int i=0;i<8&&charge>0;i++){BlockPos target=pos.add(world.rand.nextInt(17)-8,world.rand.nextInt(5)-2,world.rand.nextInt(17)-8);if(!world.isBlockLoaded(target)||target.distanceSq(pos)>64)continue;Block block=world.getBlockState(target).getBlock();boolean replace=block==Blocks.STONE||block==Blocks.DIRT||block==Blocks.GRASS||block==Blocks.SAND||block==Blocks.GRAVEL||block==Blocks.NETHERRACK||ganymedes01.etfuturum.api.tags.helpers.BlockTags.hasTag(block,"minecraft:sculk_replaceable");if(replace&&world.getTileEntity(target)==null&&world.setBlockState(target,Content.block("sculk").getDefaultState(),3)){charge--;misses=0;markDirty();}else if(++misses>=512){charge=0;markDirty();}}}
 @Override public boolean shouldRefresh(World world,BlockPos pos,net.minecraft.block.state.IBlockState old,net.minecraft.block.state.IBlockState next){return old.getBlock()!=next.getBlock();}
 @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("Charge",charge);tag.setInteger("Misses",misses);return tag;}
 @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);charge=MathHelper.clamp(tag.getInteger("Charge"),0,4096);misses=MathHelper.clamp(tag.getInteger("Misses"),0,512);}
}
