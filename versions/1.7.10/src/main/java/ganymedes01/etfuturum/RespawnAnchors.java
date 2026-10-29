package ganymedes01.etfuturum;

import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;

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
    private static ChunkCoordinates point(NBTTagCompound tag){return new ChunkCoordinates(tag.getInteger("X"),tag.getInteger("Y"),tag.getInteger("Z"));}
    private static boolean matches(NBTTagCompound tag,int dimension,ChunkCoordinates pos){return valid(tag)&&tag.getInteger("Dimension")==dimension&&point(tag).equals(pos);}
    private static final class Binding {final EntityPlayer player;Binding(EntityPlayer player){this.player=player;}}
    public static boolean bind(EntityPlayer player,World world,ChunkCoordinates pos){
        if(world.isRemote||player.worldObj!=world||!world.provider.isHellWorld)return false;
        int dimension=world.provider.dimensionId;if(matches(saved(player),dimension,pos)&&pos.equals(player.getBedLocation(dimension))&&player.isSpawnForced(dimension))return true;
        Binding attempt=new Binding(player),previous=BINDING.get();BINDING.set(attempt);
        try{player.setSpawnChunk(pos,true);}finally{if(previous==null)BINDING.remove();else BINDING.set(previous);}
        if(!pos.equals(player.getBedLocation(dimension))||!player.isSpawnForced(dimension))return false;
        NBTTagCompound tag=new NBTTagCompound();tag.setInteger("Dimension",dimension);tag.setInteger("X",pos.posX);tag.setInteger("Y",pos.posY);tag.setInteger("Z",pos.posZ);save(player,tag);return true;
    }
    public static final class SpawnSnapshot {private final ChunkCoordinates pos;private final boolean forced;private SpawnSnapshot(EntityPlayer player,int dimension){ChunkCoordinates current=player.getBedLocation(dimension);pos=current==null?null:new ChunkCoordinates(current);forced=player.isSpawnForced(dimension);}}
    public static SpawnSnapshot snapshot(EntityPlayer player,int dimension){return new SpawnSnapshot(player,dimension);}
    public static void changed(EntityPlayer player,int dimension,ChunkCoordinates requested,boolean forced,SpawnSnapshot before){
        if(player.worldObj==null||player.worldObj.isRemote)return;Binding binding=BINDING.get();if(binding!=null&&binding.player==player)return;
        SpawnRestore restoring=RESTORING.get();if(restoring!=null&&restoring.player==player&&restoring.dimension==dimension&&Objects.equals(restoring.pos,requested)&&restoring.forced==forced)return;
        if(!Objects.equals(before.pos,player.getBedLocation(dimension))||before.forced!=player.isSpawnForced(dimension))clear(player);
    }
    /** Native respawn restores a stored point; only genuine later spawn changes replace a binding. */
    public static final class SpawnRestore {private final EntityPlayer player;private final int dimension;private final ChunkCoordinates pos;private final boolean forced;private SpawnRestore(EntityPlayer player,ChunkCoordinates pos,boolean forced){this.player=player;this.dimension=player.dimension;this.pos=pos;this.forced=forced;}}
    public static SpawnRestore beforeRestore(EntityPlayer player,ChunkCoordinates pos,boolean forced){SpawnRestore previous=RESTORING.get();RESTORING.set(new SpawnRestore(player,pos,forced));return previous;}
    public static void afterRestore(SpawnRestore previous){if(previous==null)RESTORING.remove();else RESTORING.set(previous);}
    public static final class Context {
        public final int dimension;public final WorldServer worldObj;public final ChunkCoordinates anchor,exit;private final EntityPlayerMP owner;private boolean consumed;
        private Context(EntityPlayerMP owner,WorldServer world,ChunkCoordinates anchor,ChunkCoordinates exit){this.owner=owner;this.worldObj=world;this.dimension=world.provider.dimensionId;this.anchor=anchor;this.exit=exit;}
    }
    public static Context begin(EntityPlayerMP player,boolean endReturn){
        if(endReturn||player.worldObj==null||player.worldObj.isRemote||player.getHealth()>0)return null;
        NBTTagCompound tag=saved(player);if(!valid(tag))return null;int dimension=tag.getInteger("Dimension");ChunkCoordinates anchor=point(tag);
        if(!anchor.equals(player.getBedLocation(dimension))||!player.isSpawnForced(dimension)){clear(player);return null;}
        MinecraftServer server=FMLCommonHandler.instance().getMinecraftServerInstance();if(server==null||!DimensionManager.isDimensionRegistered(dimension)||dimension==-1&&!server.getAllowNether())return null;
        WorldServer world=DimensionManager.getWorld(dimension);if(world==null){DimensionManager.initDimension(dimension);world=DimensionManager.getWorld(dimension);}if(world==null||!world.provider.isHellWorld||anchor.posY>=world.getHeight()-1)return null;
        if(!(world.getBlock(anchor.posX,anchor.posY,anchor.posZ) instanceof ganymedes01.etfuturum.blocks.BlockRespawnAnchor)||world.getBlockMetadata(anchor.posX,anchor.posY,anchor.posZ)==0)return null;
        ChunkCoordinates exit=findExit(world,anchor,player);return exit==null?null:new Context(player,world,anchor,exit);
    }
    private static boolean harmless(World world,int x,int y,int z){Material material=world.getBlock(x,y,z).getMaterial();return !material.isLiquid()&&material!=Material.fire&&material!=Material.portal;}
    private static ChunkCoordinates findExit(WorldServer world,ChunkCoordinates anchor,EntityPlayer player){
        for(int dy:new int[]{0,1,-1})for(int radius=1;radius<=2;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)if(Math.max(Math.abs(dx),Math.abs(dz))==radius){ChunkCoordinates pos=new ChunkCoordinates(anchor.posX+dx,anchor.posY+dy,anchor.posZ+dz);if(safe(world,pos,player))return pos;}
        ChunkCoordinates top=new ChunkCoordinates(anchor.posX,anchor.posY+1,anchor.posZ);return safe(world,top,player)?top:null;
    }
    private static boolean safe(WorldServer world,ChunkCoordinates pos,EntityPlayer player){
        if(pos.posY<1||pos.posY>world.getHeight()-2||Math.abs((long)pos.posX)>=30000000||Math.abs((long)pos.posZ)>=30000000)return false;
        Block below=world.getBlock(pos.posX,pos.posY-1,pos.posZ);String name=Block.blockRegistry.getNameForObject(below);
        if(!below.isSideSolid(world,pos.posX,pos.posY-1,pos.posZ,ForgeDirection.UP)||name!=null&&name.contains("magma")||!harmless(world,pos.posX,pos.posY,pos.posZ)||!harmless(world,pos.posX,pos.posY+1,pos.posZ))return false;
        return world.getCollidingBoundingBoxes(player,AxisAlignedBB.getBoundingBox(pos.posX+.2,pos.posY,pos.posZ+.2,pos.posX+.8,pos.posY+1.8,pos.posZ+.8)).isEmpty();
    }
    public static Context push(Context context){Context previous=RESPAWN.get();if(context==null)RESPAWN.remove();else RESPAWN.set(context);return previous;}
    public static boolean allow(WorldProvider provider,boolean original){Context context=RESPAWN.get();return original||context!=null&&context.worldObj.provider==provider;}
    public static ChunkCoordinates bed(EntityPlayerMP player,int dimension,ChunkCoordinates original){Context context=RESPAWN.get();return context!=null&&context.owner==player&&context.dimension==dimension?context.exit:original;}
    public static boolean forced(EntityPlayerMP player,int dimension,boolean original){Context context=RESPAWN.get();return original||context!=null&&context.owner==player&&context.dimension==dimension;}
    public static ChunkCoordinates stored(EntityPlayerMP player,ChunkCoordinates original){Context context=RESPAWN.get();return context!=null&&player.dimension==context.dimension&&context.owner.getUniqueID().equals(player.getUniqueID())&&context.exit.equals(original)?context.anchor:original;}
    public static void finish(Context context,EntityPlayerMP player){
        if(context==null||context.consumed||player==null||player.worldObj!=context.worldObj||player.dimension!=context.dimension||!player.isEntityAlive()||!context.worldObj.playerEntities.contains(player)||!matches(saved(player),context.dimension,context.anchor)||!context.anchor.equals(player.getBedLocation(context.dimension))||!player.isSpawnForced(context.dimension))return;
        if(Math.abs(player.posX-context.exit.posX-.5)>1||Math.abs(player.posZ-context.exit.posZ-.5)>1||player.posY<context.exit.posY||player.posY>context.exit.posY+2)return;
        int charges=Math.min(4,context.worldObj.getBlockMetadata(context.anchor.posX,context.anchor.posY,context.anchor.posZ));if(!(context.worldObj.getBlock(context.anchor.posX,context.anchor.posY,context.anchor.posZ) instanceof ganymedes01.etfuturum.blocks.BlockRespawnAnchor)||charges==0)return;
        context.consumed=true;if(ganymedes01.etfuturum.blocks.BlockRespawnAnchor.charge(context.worldObj,context.anchor.posX,context.anchor.posY,context.anchor.posZ,charges-1))context.worldObj.playSoundEffect(context.anchor.posX+.5,context.anchor.posY+.5,context.anchor.posZ+.5,Tags.MC_ASSET_VER+":block.respawn_anchor.deplete",1,1);
    }
}
