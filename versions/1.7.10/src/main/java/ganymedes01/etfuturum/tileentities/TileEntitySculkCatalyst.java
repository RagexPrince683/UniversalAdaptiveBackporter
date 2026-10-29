package ganymedes01.etfuturum.tileentities;
import java.util.*;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigExperiments;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
/** Native XP becomes bounded, saved spreading charge instead of duplicated orbs. */
public class TileEntitySculkCatalyst extends TileEntity {
 private static final Map<World,Set<TileEntitySculkCatalyst>> LOADED=new IdentityHashMap<>();private int charge,misses;
 @Override public void validate(){super.validate();if(worldObj!=null&&!worldObj.isRemote)LOADED.computeIfAbsent(worldObj,w->new HashSet<>()).add(this);}
 private void remove(){Set<TileEntitySculkCatalyst> tiles=LOADED.get(worldObj);if(tiles!=null){tiles.remove(this);if(tiles.isEmpty())LOADED.remove(worldObj);}}
 @Override public void invalidate(){remove();super.invalidate();}@Override public void onChunkUnload(){remove();super.onChunkUnload();}public static void clear(World world){LOADED.remove(world);}
 public static TileEntitySculkCatalyst nearest(World world,double x,double y,double z){TileEntitySculkCatalyst found=null;double distance=64;for(TileEntitySculkCatalyst tile:LOADED.getOrDefault(world,Collections.emptySet()))if(!tile.isInvalid()&&world.blockExists(tile.xCoord,tile.yCoord,tile.zCoord)){double value=tile.getDistanceFrom(x,y,z);if(value<=distance){distance=value;found=tile;}}return found;}
 public void bloomWithExperience(int experience){accept(experience);}
 public int accept(int amount){if(worldObj==null||worldObj.isRemote||!ConfigExperiments.enableSculk)return 0;int accepted=Math.min(Math.max(0,amount),4096-charge);if(accepted>0){charge+=accepted;misses=0;worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,1,3);worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,getBlockType(),8);worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,ganymedes01.etfuturum.Tags.MC_ASSET_VER+":block.sculk_catalyst.bloom",2,1);markDirty();}return accepted;}
 @Override public void updateEntity(){if(worldObj.isRemote||charge==0||!ConfigExperiments.enableSculk)return;for(int i=0;i<8&&charge>0;i++){int x=xCoord+worldObj.rand.nextInt(17)-8,y=yCoord+worldObj.rand.nextInt(5)-2,z=zCoord+worldObj.rand.nextInt(17)-8;if(y<1||y>254||(x-xCoord)*(x-xCoord)+(y-yCoord)*(y-yCoord)+(z-zCoord)*(z-zCoord)>64||!worldObj.blockExists(x,y,z))continue;Block block=worldObj.getBlock(x,y,z);boolean replace=block==Blocks.stone||block==Blocks.dirt||block==Blocks.grass||block==Blocks.sand||block==Blocks.gravel||block==Blocks.netherrack||ganymedes01.etfuturum.api.tags.helpers.BlockTags.hasTag(block,"minecraft:sculk_replaceable");if(replace&&worldObj.getTileEntity(x,y,z)==null&&worldObj.setBlock(x,y,z,ModBlocks.SCULK.get(),0,3)){charge--;misses=0;markDirty();}else if(++misses>=512){charge=0;markDirty();}}}
 @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("Charge",charge);tag.setInteger("Misses",misses);}
 @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);charge=MathHelper.clamp_int(tag.getInteger("Charge"),0,4096);misses=MathHelper.clamp_int(tag.getInteger("Misses"),0,512);}
}
