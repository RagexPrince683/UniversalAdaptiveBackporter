package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;

/** Native saved starts with an explicit population-chunk clip, independent of vanilla's +8 clip. */
abstract class NativeStructureGenerator extends MapGenStructure {
    final void prepare(World world,int cx,int cz){generate(world,cx,cz,null);}
    protected boolean enabled(StructureStart start){return true;}
    final boolean intersects(StructureBoundingBox box){for(StructureStart start:structureMap.values())if(start.getBoundingBox().intersectsWith(box))return true;return false;}
    @Override public synchronized boolean generateStructure(World world,Random random,ChunkPos chunk){
        this.world=world;initializeStructureData(world);
        StructureBoundingBox clip=new StructureBoundingBox(chunk.x<<4,0,chunk.z<<4,(chunk.x<<4)+15,255,(chunk.z<<4)+15);
        MapGenStructureData data=(MapGenStructureData)world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class,getStructureName());
        boolean changed=false;
        for(StructureStart start:new ArrayList<>(structureMap.values()))if(enabled(start)&&start.isSizeableStructure()&&start.getBoundingBox().intersectsWith(clip)){
            start.generateStructure(world,random,clip);
            data.writeInstance(start.writeStructureComponentsToNBT(start.getChunkPosX(),start.getChunkPosZ()),start.getChunkPosX(),start.getChunkPosZ());changed=true;
        }
        if(changed)data.markDirty();return changed;
    }
    @Override public BlockPos getNearestStructurePos(World world,BlockPos origin,boolean unexplored){
        this.world=world;initializeStructureData(world);BlockPos nearest=null;double distance=Double.MAX_VALUE;
        for(StructureStart start:structureMap.values()){StructureBoundingBox box=start.getBoundingBox();BlockPos pos=new BlockPos((box.minX+box.maxX)/2,box.minY,(box.minZ+box.maxZ)/2);double value=pos.distanceSq(origin);if(value<distance){nearest=pos;distance=value;}}
        return nearest;
    }
}
