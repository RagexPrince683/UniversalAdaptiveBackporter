package ganymedes01.etfuturum.world.structure.surface;

import java.util.*;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.world.RuinedPortalPlan;
import ganymedes01.etfuturum.world.structure.NativeStructureReservations;
import ganymedes01.etfuturum.world.structure.underground.UndergroundLoot;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.*;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public final class RuinedPortals extends WorldSavedData {
    private static final String ID="uab_ruined_portals";private final Map<Long,int[]> sites=new LinkedHashMap<>();
    public RuinedPortals(){super(ID);}public RuinedPortals(String name){super(name);}
    public static RuinedPortals get(World world){RuinedPortals data=(RuinedPortals)world.perWorldStorage.loadData(RuinedPortals.class,ID);if(data==null){data=new RuinedPortals();world.perWorldStorage.setData(ID,data);}return data;}
    public static void generate(World world,int cx,int cz){
        if(world.isRemote||!ContentAvailability.enabled("ruined_portals")||!world.getWorldInfo().isMapFeaturesEnabled()||world.provider.dimensionId!=0&&world.provider.dimensionId!=-1)return;
        int[] candidate=RuinedPortalPlan.candidate(world.getSeed(),Math.floorDiv(cx,40),Math.floorDiv(cz,40));if(candidate[0]!=cx||candidate[1]!=cz)return;
        RuinedPortals data=get(world);long key=ChunkCoordIntPair.chunkXZ2Int(cx,cz);if(data.sites.containsKey(key))return;
        int x=cx*16+8,z=cz*16+8,y=-1;boolean nether=world.provider.dimensionId==-1;Random random=RuinedPortalPlan.random(world.getSeed(),cx,cz);
        if(nether){for(int h=32+random.nextInt(40);h<112;h++)if(world.getBlock(x,h-1,z).isNormalCube()&&world.isAirBlock(x,h,z)&&world.isAirBlock(x,h+6,z)){y=h;break;}}
        else{y=world.getTopSolidOrLiquidBlock(x,z);while(y>8&&!world.getBlock(x,y-1,z).getMaterial().isSolid())y--;if(world.getBiomeGenForCoords(x,z).temperature>1.5F)y-=random.nextInt(3);}
        if(y<10||y>240)return;StructureBoundingBox bounds=new StructureBoundingBox(x-5,y-2,z-5,x+5,y+8,z+5);if(nether?NativeStructureReservations.fortress(world,bounds):NativeStructureReservations.surface(world,bounds))return;
        if(ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world,bounds))return;
        for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++)for(int dy=-1;dy<=7;dy++)if(world.getTileEntity(x+dx,y+dy,z+dz)!=null)return;
        for(RuinedPortalPlan.Block placement:RuinedPortalPlan.blocks(random,nether)){String name=placement.name;Block block;int meta=0;
            if(name.equals("crying_obsidian"))block=ModBlocks.CRYING_OBSIDIAN.isEnabled()?ModBlocks.CRYING_OBSIDIAN.get():Blocks.obsidian;
            else if(name.equals("magma_block"))block=ModBlocks.MAGMA.isEnabled()?ModBlocks.MAGMA.get():Blocks.netherrack;
            else if(name.equals("blackstone"))block=ModBlocks.BLACKSTONE.isEnabled()?ModBlocks.BLACKSTONE.get():Blocks.netherrack;
            else if(name.equals("stone_bricks"))block=Blocks.stonebrick;
            else block=Block.getBlockFromName("minecraft:"+name);
            if(block==null)block=Blocks.netherrack;int px=x+placement.x,py=y+placement.y,pz=z+placement.z;world.setBlock(px,py,pz,block,meta,2);
            TileEntity tile=world.getTileEntity(px,py,pz);if(name.equals("chest")&&tile instanceof TileEntityChest)UndergroundLoot.fill((TileEntityChest)tile,"minecraft:chests/ruined_portal",random);
        }
        data.sites.put(key,new int[]{x,y,z});data.markDirty();
    }
    public static ChunkCoordinates nearest(World world,int x,int z){ChunkCoordinates result=null;double best=Double.MAX_VALUE;for(int[] pos:get(world).sites.values()){double distance=(double)(pos[0]-x)*(pos[0]-x)+(double)(pos[2]-z)*(pos[2]-z);if(distance<best){best=distance;result=new ChunkCoordinates(pos[0],pos[1],pos[2]);}}return result;}
    @Override public void writeToNBT(NBTTagCompound tag){NBTTagList list=new NBTTagList();for(Map.Entry<Long,int[]> entry:sites.entrySet()){NBTTagCompound site=new NBTTagCompound();site.setLong("Chunk",entry.getKey());site.setIntArray("Position",entry.getValue());list.appendTag(site);}tag.setTag("Sites",list);}
    @Override public void readFromNBT(NBTTagCompound tag){sites.clear();NBTTagList list=tag.getTagList("Sites",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound site=list.getCompoundTagAt(i);int[] position=site.getIntArray("Position");if(position.length==3)sites.put(site.getLong("Chunk"),position);}}
}
