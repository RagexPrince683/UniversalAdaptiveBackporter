package ganymedes01.etfuturum.world.structure.surface;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;
import java.util.*;

/** Each rotated footprint fits its owning chunk, so custom generators need no terrain probe or neighboring load. */
public final class Igloos extends WorldSavedData {
    private static final String KEY = "uab_igloos";
    private final Map<Long, int[]> sites = new LinkedHashMap<>();
    public Igloos() { super(KEY); }
    public Igloos(String key) { super(key); }
    public static Igloos get(World world) {
        Igloos data = (Igloos) world.perWorldStorage.loadData(Igloos.class, KEY);
        if (data == null) { data = new Igloos(); world.perWorldStorage.setData(KEY, data); }
        return data;
    }
    public static int[] candidate(long seed, int regionX, int regionZ) {
        Random random = new Random(seed + regionX * 341873128712L + regionZ * 132897987541L + 14357618L);
        return new int[]{regionX * 32 + random.nextInt(24), regionZ * 32 + random.nextInt(24)};
    }
    private static boolean biome(World world, int x, int z) {
        BiomeGenBase biome = world.getWorldChunkManager().getBiomeGenAt(x, z);
        return BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SNOWY)
                && (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.PLAINS) || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.CONIFEROUS))
                && !BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.OCEAN);
    }
    public static void generate(World world, int cx, int cz) {
        if (world.isRemote || world.provider.dimensionId != 0 || !world.getWorldInfo().isMapFeaturesEnabled() || !ContentAvailability.enabled("igloos")) return;
        int[] candidate = candidate(world.getSeed(), Math.floorDiv(cx,32), Math.floorDiv(cz,32));
        if (candidate[0] != cx || candidate[1] != cz || !biome(world,cx*16+8,cz*16+8)) return;
        Igloos data = get(world); long key = ChunkCoordIntPair.chunkXZ2Int(cx,cz);
        if (data.sites.containsKey(key)) return;
        Random random = new Random(world.getSeed() ^ cx*341873128712L ^ cz*132897987541L ^ 14357618L);
        int rotation = random.nextInt(4), x=cx*16+(rotation==1||rotation==2?10:4), z=cz*16+(rotation>=2?10:4);
        int min=255,max=0;
        for (int dx : new int[]{3,12}) for (int dz : new int[]{3,12}) {
            int y=world.getTopSolidOrLiquidBlock(cx*16+dx,cz*16+dz);
            while(y>1 && world.getBlock(cx*16+dx,y-1,cz*16+dz)==Blocks.snow_layer)y--;
            min=Math.min(min,y);max=Math.max(max,y);
        }
        if (min<63 || max-min>4) return;
        int y=min-1, levels=random.nextDouble()<.5?random.nextInt(8)+4:0;
        if (y-6-levels*3<5) levels=0;
        StructureBoundingBox clip=new StructureBoundingBox(cx*16,0,cz*16,cx*16+15,255,cz*16+15);
        new SurfaceStructures.Piece("igloo/top",x,y,z,rotation,0,2).addComponentParts(world,random,clip);
        if (levels>0) {
            for(int i=0;i<levels;i++) {
                BlockPos offset=SurfaceTemplate.rotate(2,-3-i*3,4,rotation,0);
                new SurfaceStructures.Piece("igloo/middle",x+offset.getX(),y+offset.getY(),z+offset.getZ(),rotation,0,2).addComponentParts(world,random,clip);
            }
            BlockPos offset=SurfaceTemplate.rotate(0,-6-levels*3,-2,rotation,0);
            SurfaceStructures.Piece bottom=new SurfaceStructures.Piece("igloo/bottom",x+offset.getX(),y+offset.getY(),z+offset.getZ(),rotation,0,2);
            bottom.addComponentParts(world,random,clip);
            for(int i=0;i<2;i++) {
                EntityLiving mob;
                if(i==0) mob=new EntityVillager(world,2);
                else { EntityZombie zombie=new EntityZombie(world);zombie.setVillager(true);zombie.setChild(false);mob=zombie; }
                BlockPos pos=SurfaceTemplate.rotate(i==0?2:4,1,1,rotation,0);
                mob.setLocationAndAngles(bottom.x+pos.getX()+.5,bottom.y+1,bottom.z+pos.getZ()+.5,rotation*90,0);
                mob.func_110163_bv();world.spawnEntityInWorld(mob);
            }
        } else {
            BlockPos trapdoor=SurfaceTemplate.rotate(3,0,5,rotation,0);
            world.setBlock(x+trapdoor.getX(),y,z+trapdoor.getZ(),Blocks.snow,0,2);
        }
        data.sites.put(key,new int[]{x,y,z,rotation,levels});data.markDirty();
    }
    public static ChunkCoordinates nearest(World world,int x,int z) {
        ChunkCoordinates result=null;double best=Double.MAX_VALUE;
        for(int[] p:get(world).sites.values()){double d=(double)(p[0]-x)*(p[0]-x)+(double)(p[2]-z)*(p[2]-z);if(d<best){best=d;result=new ChunkCoordinates(p[0],p[1],p[2]);}}
        return result;
    }
    @Override public void writeToNBT(NBTTagCompound tag) { NBTTagList list=new NBTTagList();for(Map.Entry<Long,int[]> site:sites.entrySet()){NBTTagCompound entry=new NBTTagCompound();entry.setLong("Chunk",site.getKey());entry.setIntArray("Site",site.getValue());list.appendTag(entry);}tag.setTag("Sites",list); }
    @Override public void readFromNBT(NBTTagCompound tag) { sites.clear();NBTTagList list=tag.getTagList("Sites",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound entry=list.getCompoundTagAt(i);int[] site=entry.getIntArray("Site");if(site.length==5)sites.put(entry.getLong("Chunk"),site);} }
}
