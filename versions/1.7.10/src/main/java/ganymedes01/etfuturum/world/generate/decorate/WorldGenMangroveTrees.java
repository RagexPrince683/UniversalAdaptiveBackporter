package ganymedes01.etfuturum.world.generate.decorate;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.api.blocksanditems.utils.BlockMetaPair;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import java.util.*;

/** Shallow-water mangroves with raised branching trunks and grounded roots; no waterlogged states. */
public class WorldGenMangroveTrees extends WorldGenAbstractTree {
    private static final int[] DX={1,-1,0,0}, DZ={0,0,1,-1}, VINE={2,8,4,1}, AGES={2,4,6,10,12};
    public WorldGenMangroveTrees(boolean notify) { super(notify); }
    @Override public boolean generate(World world,Random random,int x,int y,int z) {
        if(y<2||!world.checkChunksExist(x-9,0,z-9,x+9,world.getHeight()-1,z+9))return false;
        while(y>1&&world.getBlock(x,y-1,z).getMaterial()==Material.water)y--;
        Block soil=world.getBlock(x,y-1,z);
        if(soil!=Blocks.grass&&soil!=Blocks.dirt&&soil!=Blocks.clay&&soil!=ModBlocks.MUD.get())return false;
        return new TreePlan(world,random).grow(new ChunkPosition(x,y,z));
    }
    private final class TreePlan {
        final World world;final Random random;final Map<ChunkPosition,BlockMetaPair> plan=new LinkedHashMap<>();
        TreePlan(World world,Random random){this.world=world;this.random=random;}
        ChunkPosition add(ChunkPosition p,int x,int y,int z){return new ChunkPosition(p.chunkPosX+x,p.chunkPosY+y,p.chunkPosZ+z);}
        Block old(ChunkPosition p){return world.getBlock(p.chunkPosX,p.chunkPosY,p.chunkPosZ);}
        boolean replaceable(ChunkPosition p){Block block=old(p);return block.isAir(world,p.chunkPosX,p.chunkPosY,p.chunkPosZ)||block.isLeaves(world,p.chunkPosX,p.chunkPosY,p.chunkPosZ)||block.getMaterial()==Material.water||block.isReplaceable(world,p.chunkPosX,p.chunkPosY,p.chunkPosZ);}
        boolean free(ChunkPosition p){return !plan.containsKey(p)&&world.isAirBlock(p.chunkPosX,p.chunkPosY,p.chunkPosZ);}
        void put(ChunkPosition p,Block block,int meta){plan.put(p,BlockMetaPair.intern(block,meta));}
        void log(ChunkPosition p,int meta){put(p,ModBlocks.MANGROVE_LOG.get(),meta);}
        void leaf(ChunkPosition p){if(!plan.containsKey(p)&&replaceable(p))put(p,ModBlocks.LEAVES.get(),0);}
        boolean root(ChunkPosition cursor,int direction,ChunkPosition trunk){
            for(int length=0;length<15&&cursor.chunkPosY>0;length++){
                Block block=old(cursor);
                if(block==ModBlocks.MUD.get()||block==ModBlocks.MUDDY_MANGROVE_ROOTS.get()){put(cursor,ModBlocks.MUDDY_MANGROVE_ROOTS.get(),0);return true;}
                if(!replaceable(cursor)&&block!=ModBlocks.MANGROVE_ROOTS.get())return length>0;
                put(cursor,ModBlocks.MANGROVE_ROOTS.get(),0);
                int distance=Math.abs(cursor.chunkPosX-trunk.chunkPosX)+Math.abs(cursor.chunkPosZ-trunk.chunkPosZ);
                cursor=distance<6&&random.nextFloat()<.2F?add(cursor,DX[direction],0,DZ[direction]):add(cursor,0,-1,0);
            }
            return false;
        }
        boolean grow(ChunkPosition pos){
            boolean tall=random.nextFloat()<.85F;
            ChunkPosition trunk=add(pos,0,tall?3+random.nextInt(5):1+random.nextInt(3),0);
            int height=(tall?4:2)+random.nextInt(2)+random.nextInt(tall?10:5);
            put(add(trunk,0,-1,0),ModBlocks.MANGROVE_ROOTS.get(),0);
            for(int d=0;d<4;d++)if(!root(add(trunk,DX[d],-1,DZ[d]),d,trunk))return false;
            List<ChunkPosition> crowns=new ArrayList<>();
            for(int y=0;y<height;y++){
                log(add(trunk,0,y,0),0);
                if(y>0&&y<height-1&&random.nextFloat()<.5F){
                    int direction=random.nextInt(4),steps=Math.min(height-y,1+random.nextInt(tall?6:4));
                    ChunkPosition branch=add(trunk,0,y,0);
                    for(int step=0;step<steps;step++){
                        branch=add(branch,0,1,0);log(branch,0);
                        branch=add(branch,DX[direction],0,DZ[direction]);log(branch,DX[direction]!=0?4:8);
                    }
                    crowns.add(add(branch,0,1,0));
                }
            }
            crowns.add(add(trunk,0,height,0));
            for(ChunkPosition crown:crowns)for(int i=0;i<70;i++)leaf(add(crown,random.nextInt(3)-random.nextInt(3),random.nextInt(2)-random.nextInt(2),random.nextInt(3)-random.nextInt(3)));
            for(Map.Entry<ChunkPosition,BlockMetaPair> entry:new ArrayList<>(plan.entrySet())){
                ChunkPosition at=entry.getKey();
                if(entry.getValue().get()==ModBlocks.MANGROVE_ROOTS.get()&&ModBlocks.MOSS_CARPET.isEnabled()&&random.nextFloat()<.5F&&free(add(at,0,1,0)))put(add(at,0,1,0),ModBlocks.MOSS_CARPET.get(),0);
                if(entry.getValue().get()!=ModBlocks.LEAVES.get())continue;
                if(random.nextFloat()<.14F&&free(add(at,0,-1,0))&&free(add(at,0,-2,0)))put(add(at,0,-1,0),ModBlocks.SAPLING.get(),AGES[random.nextInt(5)]);
                for(int d=0;d<4;d++)if(random.nextFloat()<.125F){
                    ChunkPosition trailing=add(at,DX[d],0,DZ[d]);
                    for(int length=0;length<4&&free(trailing);length++,trailing=add(trailing,0,-1,0))put(trailing,Blocks.vine,VINE[d]);
                }
            }
            // Validate every planned block before writing, including roots and decorators.
            for(Map.Entry<ChunkPosition,BlockMetaPair> entry:plan.entrySet()){
                ChunkPosition p=entry.getKey();Block block=old(p);
                if(p.chunkPosY<1||p.chunkPosY>=world.getHeight()||world.getTileEntity(p.chunkPosX,p.chunkPosY,p.chunkPosZ)!=null)return false;
                if(!replaceable(p)&&block!=ModBlocks.MANGROVE_ROOTS.get()&&!(entry.getValue().get()==ModBlocks.MUDDY_MANGROVE_ROOTS.get()&&(block==ModBlocks.MUD.get()||block==ModBlocks.MUDDY_MANGROVE_ROOTS.get())))return false;
            }
            for(Map.Entry<ChunkPosition,BlockMetaPair> entry:plan.entrySet()){
                ChunkPosition p=entry.getKey();setBlockAndNotifyAdequately(world,p.chunkPosX,p.chunkPosY,p.chunkPosZ,entry.getValue().get(),entry.getValue().getMeta());
            }
            return true;
        }
    }
}
