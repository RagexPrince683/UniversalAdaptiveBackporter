package ganymedes01.etfuturum;

import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

/** Persisted player binding, scoped strictly to native death-respawn execution. */
public final class RespawnAnchors {
    private static final String KEY="UabRespawnAnchor";
    private static final ThreadLocal<Context> RESPAWN=new ThreadLocal<>();
    private static final ThreadLocal<Binding> BINDING=new ThreadLocal<>();
    private static final ThreadLocal<SpawnRestore> RESTORING=new ThreadLocal<>();
    private RespawnAnchors(){}
    private static NBTTagCompound saved(EntityPlayer player){return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag(KEY);}
    private static void save(EntityPlayer player,NBTTagCompound binding){NBTTagCompound root=player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);root.setTag(KEY,binding.copy());player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG,root);}
    private static void clear(EntityPlayer player){player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).removeTag(KEY);}
    private static boolean valid(NBTTagCompound tag){return tag.hasKey("Dimension",3)&&tag.hasKey("X",3)&&tag.hasKey("Y",3)&&tag.hasKey("Z",3)&&Math.abs((long)tag.getInteger("X"))<30000000&&Math.abs((long)tag.getInteger("Z"))<30000000&&tag.getInteger("Y")>=0&&tag.getInteger("Y")<256;}
    private static BlockPos point(NBTTagCompound tag){return new BlockPos(tag.getInteger("X"),tag.getInteger("Y"),tag.getInteger("Z"));}
    private static boolean matches(NBTTagCompound tag,int dimension,BlockPos pos){return valid(tag)&&tag.getInteger("Dimension")==dimension&&point(tag).equals(pos);}
    private static final class Binding {final EntityPlayer player;boolean allowed;Binding(EntityPlayer player){this.player=player;}}
    public static void permission(EntityPlayer player,boolean canceled){Binding binding=BINDING.get();if(binding!=null&&binding.player==player)binding.allowed=!canceled;}
    public static boolean bind(EntityPlayer player,World world,BlockPos pos){
        if(world.isRemote||player.world!=world||!world.provider.isNether())return false;
        int dimension=world.provider.getDimension();if(matches(saved(player),dimension,pos)&&pos.equals(player.getBedLocation(dimension))&&player.isSpawnForced(dimension))return true;
        Binding attempt=new Binding(player),previous=BINDING.get();BINDING.set(attempt);
        try{player.setSpawnPoint(pos,true);}finally{if(previous==null)BINDING.remove();else BINDING.set(previous);}
        if(!attempt.allowed||!pos.equals(player.getBedLocation(dimension))||!player.isSpawnForced(dimension))return false;
        NBTTagCompound tag=new NBTTagCompound();tag.setInteger("Dimension",dimension);tag.setInteger("X",pos.getX());tag.setInteger("Y",pos.getY());tag.setInteger("Z",pos.getZ());save(player,tag);return true;
    }
    public static final class SpawnSnapshot {private final BlockPos pos;private final boolean forced;private SpawnSnapshot(EntityPlayer player,int dimension){BlockPos current=player.getBedLocation(dimension);pos=current==null?null:new BlockPos(current);forced=player.isSpawnForced(dimension);}}
    public static SpawnSnapshot snapshot(EntityPlayer player,int dimension){return new SpawnSnapshot(player,dimension);}
    public static void changed(EntityPlayer player,int dimension,BlockPos requested,boolean forced,SpawnSnapshot before){
        if(player.world==null||player.world.isRemote)return;Binding binding=BINDING.get();if(binding!=null&&binding.player==player)return;
        SpawnRestore restoring=RESTORING.get();if(restoring!=null&&restoring.player==player&&restoring.dimension==dimension&&Objects.equals(restoring.pos,requested)&&restoring.forced==forced)return;
        if(!Objects.equals(before.pos,player.getBedLocation(dimension))||before.forced!=player.isSpawnForced(dimension))clear(player);
    }
    /** Native respawn restores a stored point; only genuine later spawn changes replace a binding. */
    public static final class SpawnRestore {private final EntityPlayer player;private final int dimension;private final BlockPos pos;private final boolean forced;private SpawnRestore(EntityPlayer player,BlockPos pos,boolean forced){this.player=player;this.dimension=player.dimension;this.pos=pos;this.forced=forced;}}
    public static SpawnRestore beforeRestore(EntityPlayer player,BlockPos pos,boolean forced){SpawnRestore previous=RESTORING.get();RESTORING.set(new SpawnRestore(player,pos,forced));return previous;}
    public static void afterRestore(SpawnRestore previous){if(previous==null)RESTORING.remove();else RESTORING.set(previous);}
    public static final class Context {
        public final int dimension;public final WorldServer world;public final BlockPos anchor,exit;private final EntityPlayerMP owner;private boolean consumed;
        private Context(EntityPlayerMP owner,WorldServer world,BlockPos anchor,BlockPos exit){this.owner=owner;this.world=world;this.dimension=world.provider.getDimension();this.anchor=anchor;this.exit=exit;}
    }
    public static Context begin(EntityPlayerMP player,boolean endReturn){
        if(endReturn||player.world==null||player.world.isRemote||player.getHealth()>0)return null;
        NBTTagCompound tag=saved(player);if(!valid(tag))return null;int dimension=tag.getInteger("Dimension");BlockPos anchor=point(tag);
        if(!anchor.equals(player.getBedLocation(dimension))||!player.isSpawnForced(dimension)){clear(player);return null;}
        MinecraftServer server=FMLCommonHandler.instance().getMinecraftServerInstance();if(server==null||!DimensionManager.isDimensionRegistered(dimension)||dimension==-1&&!server.getAllowNether())return null;
        WorldServer world=DimensionManager.getWorld(dimension);if(world==null){DimensionManager.initDimension(dimension);world=DimensionManager.getWorld(dimension);}if(world==null||!world.provider.isNether()||anchor.getY()>=world.getHeight()-1)return null;
        net.minecraft.block.state.IBlockState state=world.getBlockState(anchor);if(!(state.getBlock() instanceof RespawnAnchorBlock)||state.getValue(RespawnAnchorBlock.CHARGES)==0)return null;
        BlockPos exit=findExit(world,anchor,player);return exit==null?null:new Context(player,world,anchor,exit);
    }
    private static boolean harmless(World world,BlockPos pos){Material material=world.getBlockState(pos).getMaterial();return !material.isLiquid()&&material!=Material.FIRE&&material!=Material.PORTAL;}
    private static BlockPos findExit(WorldServer world,BlockPos anchor,EntityPlayer player){
        for(int dy:new int[]{0,1,-1})for(int radius=1;radius<=2;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)if(Math.max(Math.abs(dx),Math.abs(dz))==radius){BlockPos pos=anchor.add(dx,dy,dz);if(safe(world,pos,player))return pos;}
        return safe(world,anchor.up(),player)?anchor.up():null;
    }
    private static boolean safe(WorldServer world,BlockPos pos,EntityPlayer player){
        if(pos.getY()<1||pos.getY()>world.getHeight()-2||!world.getWorldBorder().contains(pos))return false;
        Block below=world.getBlockState(pos.down()).getBlock();if(!world.isSideSolid(pos.down(),EnumFacing.UP)||below.getRegistryName()!=null&&below.getRegistryName().getPath().contains("magma")||!harmless(world,pos)||!harmless(world,pos.up()))return false;
        return world.getCollisionBoxes(player,new AxisAlignedBB(pos.getX()+.2,pos.getY(),pos.getZ()+.2,pos.getX()+.8,pos.getY()+1.8,pos.getZ()+.8)).isEmpty();
    }
    public static Context push(Context context){Context previous=RESPAWN.get();if(context==null)RESPAWN.remove();else RESPAWN.set(context);return previous;}
    public static boolean allow(WorldProvider provider,boolean original){Context context=RESPAWN.get();return original||context!=null&&context.world.provider==provider;}
    public static BlockPos bed(EntityPlayerMP player,int dimension,BlockPos original){Context context=RESPAWN.get();return context!=null&&context.owner==player&&context.dimension==dimension?context.exit:original;}
    public static boolean forced(EntityPlayerMP player,int dimension,boolean original){Context context=RESPAWN.get();return original||context!=null&&context.owner==player&&context.dimension==dimension;}
    public static BlockPos stored(EntityPlayerMP player,BlockPos original){Context context=RESPAWN.get();return context!=null&&player.dimension==context.dimension&&context.owner.getUniqueID().equals(player.getUniqueID())&&context.exit.equals(original)?context.anchor:original;}
    public static void finish(Context context,EntityPlayerMP player){
        if(context==null||context.consumed||player==null||player.world!=context.world||player.dimension!=context.dimension||!player.isEntityAlive()||!context.world.playerEntities.contains(player)||!matches(saved(player),context.dimension,context.anchor)||!context.anchor.equals(player.getBedLocation(context.dimension))||!player.isSpawnForced(context.dimension))return;
        if(Math.abs(player.posX-context.exit.getX()-.5)>1||Math.abs(player.posZ-context.exit.getZ()-.5)>1||player.posY<context.exit.getY()||player.posY>context.exit.getY()+2)return;
        net.minecraft.block.state.IBlockState state=context.world.getBlockState(context.anchor);if(!(state.getBlock() instanceof RespawnAnchorBlock)||state.getValue(RespawnAnchorBlock.CHARGES)==0)return;
        context.consumed=true;if(RespawnAnchorBlock.charge(context.world,context.anchor,state.getValue(RespawnAnchorBlock.CHARGES)-1))context.world.playSound(null,context.anchor,ModernSounds.get("block.respawn_anchor.deplete"),net.minecraft.util.SoundCategory.BLOCKS,1,1);
    }
}
