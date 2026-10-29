package ganymedes01.etfuturum.world;

import java.util.*;

/** Chunk-local ruined frames: deterministic native-height adaptation without a terrain replacement. */
public final class RuinedPortalPlan {
    public static final long SALT=34222645L;
    public static int[] candidate(long seed,int rx,int rz){Random random=new Random(seed+rx*341873128712L+rz*132897987541L+SALT);return new int[]{rx*40+random.nextInt(28),rz*40+random.nextInt(28)};}
    public static Random random(long seed,int cx,int cz){return new Random(seed^cx*341873128712L^cz*132897987541L^SALT);}
    public static final class Block {public final int x,y,z;public final String name;Block(int x,int y,int z,String name){this.x=x;this.y=y;this.z=z;this.name=name;}}
    public static List<Block> blocks(Random random,boolean nether){
        List<Block> blocks=new ArrayList<>();int rotation=random.nextInt(4),height=5+random.nextInt(2),width=4+random.nextInt(2);
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)if(x*x+z*z<=25&&random.nextInt(6)!=0){String material=random.nextInt(13)==0?"magma_block":"netherrack";put(blocks,x,-1,z,material,rotation);if(random.nextInt(12)==0)put(blocks,x,0,z,nether?"blackstone":"stone_bricks",rotation);}
        for(int x=0;x<width;x++)for(int y=0;y<height;y++){
            if(x>0&&x<width-1&&y>0&&y<height-1){put(blocks,x-width/2,y,0,"air",rotation);continue;}
            if(y==height-1&&x==width-2||y==height-2&&x==width-1)continue;
            put(blocks,x-width/2,y,0,random.nextInt(5)==0?"crying_obsidian":"obsidian",rotation);
        }
        put(blocks,-width/2-1,0,0,"obsidian",rotation);put(blocks,width/2+1,0,-2,"gold_block",rotation);
        put(blocks,3,0,3,"chest",rotation);return blocks;
    }
    private static void put(List<Block> out,int x,int y,int z,String block,int rotation){for(int i=0;i<rotation;i++){int previous=x;x=-z;z=previous;}out.add(new Block(x,y,z,block));}
    private RuinedPortalPlan(){}
}
