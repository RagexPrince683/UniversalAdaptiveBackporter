package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntityCreaking;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.*;
public final class CreakingHeartTile extends TileEntity implements ITickable {
    private UUID creakingId;
    private long nextResinTime;
    @Override public void update(){if(world.isRemote||(world.getTotalWorldTime()+pos.getX()+pos.getZ())%20!=0)return;IBlockState old=world.getBlockState(pos),actual=old.getBlock().getActualState(old,world,pos);if(!old.equals(actual))world.setBlockState(pos,actual,3);if(world.getDifficulty()==EnumDifficulty.PEACEFUL||!MobRegistry.enabled(EntityCreaking.class)||!CreakingHeart.isActive(world,pos))return;boolean nearby=false;for(EntityPlayer player:world.playerEntities)if(!player.isDead&&!player.capabilities.disableDamage&&!player.isSpectator()&&player.getDistanceSqToCenter(pos)<=256){nearby=true;break;}if(!nearby)return;for(EntityCreaking mob:world.getEntitiesWithinAABB(EntityCreaking.class,new AxisAlignedBB(pos).grow(32)))if(!mob.isDead&&mob.belongsTo(pos)){if(!mob.getUniqueID().equals(creakingId)){creakingId=mob.getUniqueID();markDirty();}return;}if(creakingId!=null&&!world.isAreaLoaded(new BlockPos(pos.getX()-32,0,pos.getZ()-32),new BlockPos(pos.getX()+32,255,pos.getZ()+32)))return;for(int attempt=0;attempt<12;attempt++){int x=pos.getX()+world.rand.nextInt(17)-8,z=pos.getZ()+world.rand.nextInt(17)-8;if(!world.isBlockLoaded(new BlockPos(x,pos.getY(),z)))continue;for(int y=Math.min(253,pos.getY()+8);y>=Math.max(1,pos.getY()-16);y--){BlockPos at=new BlockPos(x,y,z);if(!world.getBlockState(at.down()).isNormalCube()||!world.isAirBlock(at)||!world.isAirBlock(at.up())||!world.isAirBlock(at.up(2)))continue;EntityCreaking mob=new EntityCreaking(world);mob.bind(pos);mob.setLocationAndAngles(x+.5,y,z+.5,world.rand.nextFloat()*360,0);if(!world.checkNoEntityCollision(mob.getEntityBoundingBox())||!world.getCollisionBoxes(mob,mob.getEntityBoundingBox()).isEmpty())continue;if(world.spawnEntity(mob)){creakingId=mob.getUniqueID();markDirty();return;}}}}
    public void createResin(){long now=world.getTotalWorldTime();if(now<nextResinTime||!Content.available("resin_clump")||!world.getGameRules().getBoolean("mobGriefing"))return;nextResinTime=now+100;markDirty();int count=1+world.rand.nextInt(3);for(int tries=0;tries<48&&count>0;tries++){BlockPos log=pos.add(world.rand.nextInt(9)-4,world.rand.nextInt(9)-4,world.rand.nextInt(9)-4);if(!world.isBlockLoaded(log)||world.getBlockState(log).getBlock()!=Content.block("pale_oak_log"))continue;EnumFacing side=EnumFacing.HORIZONTALS[world.rand.nextInt(4)];BlockPos at=log.offset(side);if(world.isBlockLoaded(at)&&world.isAirBlock(at)){IBlockState state=Content.block("resin_clump").getDefaultState().withProperty(BlockVine.getPropertyFor(side.getOpposite()),true);if(world.setBlockState(at,state,2))count--;}}}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);if(creakingId!=null)tag.setString("Creaking",creakingId.toString());tag.setLong("NextResinTime",nextResinTime);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);nextResinTime=tag.getLong("NextResinTime");try{creakingId=tag.hasKey("Creaking",8)?UUID.fromString(tag.getString("Creaking")):null;}catch(IllegalArgumentException ignored){creakingId=null;}}
}
