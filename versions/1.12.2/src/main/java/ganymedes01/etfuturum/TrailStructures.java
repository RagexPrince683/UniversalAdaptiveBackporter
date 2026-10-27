package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.*;
import net.minecraft.world.gen.structure.*;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.BiomeDictionary;
/** Native woodland mansions remain native; only the maintained trail-ruin topology is added here. */
public final class TrailStructures extends NativeStructureGenerator {
    private final Map<Long,Start> candidates=new LinkedHashMap<Long,Start>(64,.75F,true){@Override protected boolean removeEldestEntry(Map.Entry<Long,Start> entry){return size()>128;}};
    private StructureTerrain terrain;
    TrailStructures(){range=8;}
    static StructureTemplate template(String name){return StructureTemplate.get(name);}
    @Override public String getStructureName(){return "UABSurfaceStructures";}
    static boolean enabled(){return Features.enabled("content.trail_ruins")&&Content.available("suspicious_gravel");}
    @Override protected boolean enabled(StructureStart start){return enabled();}
    static int[] candidate(long seed,int gx,int gz){Random r=new Random(seed+gx*341873128712L+gz*132897987541L+10387319L+71287L);return new int[]{gx*34+r.nextInt(26),gz*34+r.nextInt(26)};}
    @Override protected boolean canSpawnStructureAtCoords(int cx,int cz){if(!enabled())return false;int[] p=candidate(world.getSeed(),Math.floorDiv(cx,34),Math.floorDiv(cz,34));if(p[0]!=cx||p[1]!=cz)return false;Biome biome=world.getBiomeProvider().getBiome(new BlockPos(cx*16+8,64,cz*16+8));if(!BiomeDictionary.hasType(biome,BiomeDictionary.Type.CONIFEROUS)&&biome!=Biomes.BIRCH_FOREST&&biome!=Biomes.BIRCH_FOREST_HILLS&&biome!=Biomes.JUNGLE&&biome!=Biomes.JUNGLE_HILLS)return false;long key=ChunkPos.asLong(cx,cz);if(!candidates.containsKey(key))candidates.put(key,create(cx,cz));return candidates.get(key)!=null;}
    BlockPos nearest(World world,BlockPos origin){this.world=world;initializeStructureData(world);BlockPos result=null;double distance=Double.MAX_VALUE;int gx=Math.floorDiv(origin.getX()>>4,34),gz=Math.floorDiv(origin.getZ()>>4,34);for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){int[] p=candidate(world.getSeed(),gx+dx,gz+dz);long key=ChunkPos.asLong(p[0],p[1]);StructureStart start=structureMap.get(key);if(start==null&&canSpawnStructureAtCoords(p[0],p[1]))start=candidates.get(key);if(start==null)continue;BlockPos at=new BlockPos(p[0]*16+8,start.getBoundingBox().minY,p[1]*16+8);double d=at.distanceSq(origin);if(d<distance){distance=d;result=at;}}return result;}
    @Override protected StructureStart getStructureStart(int cx,int cz){return candidates.get(ChunkPos.asLong(cx,cz));}
    private Start create(int cx,int cz){if(terrain==null)terrain=new StructureTerrain(world);int x=cx*16+8,z=cz*16+8,y=terrain.height(x,z);if(y<63)return null;Random random=new Random(world.getSeed()^cx*341873128712L^cz*132897987541L^131L);random.nextInt(4);List<Piece> pieces=new ArrayList<>();TrailRuins.generate(random,x,y,z,pieces);if(pieces.isEmpty())return null;Start start=new Start(cx,cz,pieces);StructureBoundingBox box=start.getBoundingBox();return box.minY>=5&&box.maxY<250&&StructureTerrain.fresh(world,box)&&!NativeStructureReservations.surface(world,box)?start:null;}
    public static class Start extends StructureStart {
        private final Set<Long> placed=new HashSet<>();
        public Start(){} Start(int cx,int cz,List<Piece> pieces){super(cx,cz);components.addAll(pieces);updateBoundingBox();}
        @Override public void generateStructure(World world,Random random,StructureBoundingBox clip){long key=ChunkPos.asLong(clip.minX>>4,clip.minZ>>4);if(placed.contains(key))return;for(StructureComponent component:components)if(component.getBoundingBox().intersectsWith(clip))component.addComponentParts(world,random,clip);placed.add(key);}
        @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("Kind",1);NBTTagList list=new NBTTagList();for(long key:placed){NBTTagCompound entry=new NBTTagCompound();entry.setLong("Chunk",key);list.appendTag(entry);}tag.setTag("PlacedChunks",list);}
        @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);placed.clear();NBTTagList list=tag.getTagList("PlacedChunks",10);for(int i=0;i<list.tagCount();i++)placed.add(list.getCompoundTagAt(i).getLong("Chunk"));}
    }
    public static class Piece extends StructureComponent {
        String name,processors="";int x,y,z,rotation;transient TrailProcessors.Result processed;
        public Piece(){} public Piece(String name,int x,int y,int z,int rotation,int mirror,int kind){if(mirror!=0||kind!=1)throw new IllegalArgumentException("Non-trail layout");this.name=name;this.x=x;this.y=y;this.z=z;this.rotation=rotation;setCoordBaseMode(null);boundingBox=template(name).box(x,y,z,rotation);}
        @Override protected void writeStructureToNBT(NBTTagCompound tag){tag.setString("Template",name);tag.setString("Processors",processors);tag.setInteger("X",x);tag.setInteger("Y",y);tag.setInteger("Z",z);tag.setInteger("Rotation",rotation);tag.setInteger("Kind",1);}
        @Override protected void readStructureFromNBT(NBTTagCompound tag,TemplateManager manager){name=tag.getString("Template");processors=tag.getString("Processors");x=tag.getInteger("X");y=tag.getInteger("Y");z=tag.getInteger("Z");rotation=tag.getInteger("Rotation")&3;processed=null;}
        @Override public boolean addComponentParts(World world,Random random,StructureBoundingBox clip){StructureTemplate template=template(name);if(processed==null)processed=TrailProcessors.prepare(template,processors,x,y,z,world.getSeed());template.place(world,x,y,z,rotation,clip,world.getSeed(),processors,processed);return true;}
    }
}
