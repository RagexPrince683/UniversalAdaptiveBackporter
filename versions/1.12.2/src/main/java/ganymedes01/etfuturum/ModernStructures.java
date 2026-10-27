package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Generation plans are discovered before population; all placement is restricted to the population chunk. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class ModernStructures {
    private static final Map<World,BastionStructures> BASTIONS=new IdentityHashMap<>();
    private static final Map<World,TrailStructures> TRAILS=new IdentityHashMap<>();
    private ModernStructures(){}
    static void initialize(){BastionStructures.spacing=Features.integer("bastionSpacing",27,8,128);MapGenStructureIO.registerStructure(BastionStructures.Start.class,"EFRBastion");MapGenStructureIO.registerStructureComponent(BastionStructures.Piece.class,"EFRBastionPiece");MapGenStructureIO.registerStructure(UndergroundStructures.Start.class,"EFRUnderground");MapGenStructureIO.registerStructureComponent(UndergroundStructures.Piece.class,"EFRUndergroundPiece");MapGenStructureIO.registerStructure(TrailStructures.Start.class,"UABSurfaceStructures");MapGenStructureIO.registerStructureComponent(TrailStructures.Piece.class,"UABSurfacePiece");}
    public static void prepare(World world,int cx,int cz){if(world.isRemote||!world.getWorldInfo().isMapFeaturesEnabled())return;if(StructureTerrain.overworld(world)){if(Features.enabled("world.enableAncientCities")||Features.enabled("world.enableTrialChambers"))UndergroundStructures.get(world).prepare(world,cx,cz);if(TrailStructures.enabled())TRAILS.computeIfAbsent(world,key->new TrailStructures()).prepare(world,cx,cz);}else if(StructureTerrain.nether(world)&&Features.enabled("world.enableBastions")&&Features.enabled("experiments.netherDimensionProvider"))BASTIONS.computeIfAbsent(world,key->new BastionStructures()).prepare(world,cx,cz);}
    static void populate(World world,int cx,int cz){if(world.isRemote||!world.getWorldInfo().isMapFeaturesEnabled())return;Random random=new Random(world.getSeed()^(long)cx*341873128712L^(long)cz*132897987541L);ChunkPos chunk=new ChunkPos(cx,cz);if(StructureTerrain.overworld(world)){UndergroundStructures generator=UndergroundStructures.get(world);generator.generateStructure(world,random,chunk);TrailStructures trails=TRAILS.get(world);if(trails!=null)trails.generateStructure(world,random,chunk);OutpostStructures.populate(world,random,cx,cz);}else{BastionStructures bastions=BASTIONS.get(world);if(bastions!=null)bastions.generateStructure(world,random,chunk);}}
    public static boolean reserved(World world,StructureBoundingBox box){if(UndergroundStructures.reserved(world,box))return true;BastionStructures bastions=BASTIONS.get(world);TrailStructures trails=TRAILS.get(world);return bastions!=null&&bastions.intersects(box)||trails!=null&&trails.intersects(box);}
    public static boolean occupied(World world,BlockPos pos){if(UndergroundStructures.occupied(world,pos))return true;BastionStructures bastions=BASTIONS.get(world);TrailStructures trails=TRAILS.get(world);StructureBoundingBox point=new StructureBoundingBox(pos,pos);return bastions!=null&&bastions.intersects(point)||trails!=null&&trails.intersects(point);}
    public static boolean placing(){return StructureTemplate.placing();}
    @SubscribeEvent public static void unload(WorldEvent.Unload event){BASTIONS.remove(event.getWorld());TRAILS.remove(event.getWorld());UndergroundStructures.unload(event.getWorld());}
    static BlockPos nearest(World world,BlockPos origin,String type){if(!world.getWorldInfo().isMapFeaturesEnabled())return null;if(type.equals("bastion"))return StructureTerrain.nether(world)&&Features.enabled("world.enableBastions")&&Features.enabled("experiments.netherDimensionProvider")?BASTIONS.computeIfAbsent(world,key->new BastionStructures()).nearest(world,origin):null;if(type.equals("trail_ruins"))return StructureTerrain.overworld(world)&&TrailStructures.enabled()?TRAILS.computeIfAbsent(world,key->new TrailStructures()).nearest(world,origin):null;return UndergroundStructures.nearest(world,origin,type.equals("ancient_city"));}
}
