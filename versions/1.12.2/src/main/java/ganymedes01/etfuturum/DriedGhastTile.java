package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntityHappyGhast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
/** Loaded game ticks, with exact progress retained across state changes and chunk reloads. */
public final class DriedGhastTile extends TileEntity implements ITickable {
    private int progress=-1,ticks,dryTicks;
    private boolean lastWet;
    @Override public void update(){if(world.isRemote)return;IBlockState state=world.getBlockState(pos);if(!(state.getBlock() instanceof DriedGhast))return;int stage=state.getValue(DriedGhast.HYDRATION);if(progress<0)progress=stage*6000;boolean wet=state.getValue(DriedGhast.WATERLOGGED);if(wet!=lastWet){progress=stage*6000;dryTicks=0;}if(wet)progress=Math.min(24000,progress+1);else if(progress>0&&++dryTicks>=6000){progress=Math.max(0,stage*6000-6000);dryTicks=0;}lastWet=wet;int next=Math.min(3,progress/6000);if(next!=stage){world.setBlockState(pos,state.withProperty(DriedGhast.HYDRATION,next),2);world.playSound(null,pos,ModernSounds.get("block.dried_ghast.transition"),SoundCategory.BLOCKS,1,1);}if(++ticks%20==0)markDirty();if(wet&&progress==24000&&ticks%20==0&&MobRegistry.enabled(EntityHappyGhast.class)&&Content.available("dried_ghast")){EntityHappyGhast ghast=new EntityHappyGhast(world);ghast.setGrowingAge(-24000);ghast.setLocationAndAngles(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);if(world.spawnEntity(ghast)){world.setBlockState(pos,Blocks.WATER.getDefaultState(),3);world.playSound(null,pos,ModernSounds.get("entity.ghastling.spawn"),SoundCategory.BLOCKS,1,1);}}}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("HydrationTicks",progress);tag.setInteger("DryTicks",dryTicks);tag.setBoolean("LastWaterlogged",lastWet);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);progress=tag.hasKey("HydrationTicks")?MathHelper.clamp(tag.getInteger("HydrationTicks"),-1,24000):-1;dryTicks=MathHelper.clamp(tag.getInteger("DryTicks"),0,5999);lastWet=tag.getBoolean("LastWaterlogged");}
}
