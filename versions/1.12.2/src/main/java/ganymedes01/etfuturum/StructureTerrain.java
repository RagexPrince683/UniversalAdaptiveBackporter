package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/** Independent native density sampling. No chunk request, decoration or population occurs here. */
final class StructureTerrain {
    private final ChunkGeneratorOverworld noise;
    private final Map<Long,ChunkPrimer> chunks=new LinkedHashMap<Long,ChunkPrimer>(16,.75F,true){@Override protected boolean removeEldestEntry(Map.Entry<Long,ChunkPrimer> entry){return size()>16;}};
    StructureTerrain(World world){noise=new ChunkGeneratorOverworld(world,world.getSeed(),false,world.getWorldInfo().getGeneratorOptions());}
    IBlockState state(int x,int y,int z){long key=ChunkPos.asLong(x>>4,z>>4);ChunkPrimer primer=chunks.get(key);if(primer==null){primer=new ChunkPrimer();noise.setBlocksInChunk(x>>4,z>>4,primer);chunks.put(key,primer);}return primer.getBlockState(x&15,y,z&15);}
    int height(int x,int z){for(int y=250;y>1;y--)if(state(x,y,z).getMaterial().isSolid())return y+1;return 1;}
    boolean roof(StructureBoundingBox box){int y=box.maxY+8;if(y>=256)return false;for(int x=box.minX;x<=box.maxX;x++)for(int z=box.minZ;z<=box.maxZ;z++)if(!state(x,y,z).getMaterial().isSolid())return false;return true;}
    static IChunkGenerator generator(World world){return world instanceof WorldServer?((WorldServer)world).getChunkProvider().chunkGenerator:null;}
    static boolean overworld(World world){IChunkGenerator generator=generator(world);return world.provider.getDimension()==0&&generator!=null&&generator.getClass()==ChunkGeneratorOverworld.class;}
    static boolean nether(World world){IChunkGenerator generator=generator(world);return world.provider.getDimension()==-1&&generator!=null&&generator.getClass()==ChunkGeneratorHell.class;}
    static boolean fresh(World world,StructureBoundingBox box){if(!(world instanceof WorldServer))return false;ChunkProviderServer provider=((WorldServer)world).getChunkProvider();for(int x=box.minX>>4;x<=box.maxX>>4;x++)for(int z=box.minZ>>4;z<=box.maxZ>>4;z++){if(provider.chunkLoader.isChunkGeneratedAt(x,z))return false;Chunk loaded=provider.getLoadedChunk(x,z);if(loaded!=null&&loaded.isTerrainPopulated())return false;}return true;}
}
