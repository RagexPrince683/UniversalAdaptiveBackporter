package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Maintained stem/cap geometry is planned and validated before any native world mutation. */
public final class HugeFungus {
    private final World world;private final Random random;private final boolean crimson,planted;
    private final Map<BlockPos,IBlockState> plan=new LinkedHashMap<>();
    private final IBlockState stem,hat;
    private HugeFungus(World world,Random random,boolean crimson,boolean planted){this.world=world;this.random=random;this.crimson=crimson;this.planted=planted;stem=Content.block(crimson?"crimson_stem":"warped_stem").getDefaultState();hat=crimson?Blocks.NETHER_WART_BLOCK.getDefaultState():Content.block("warped_wart_block").getDefaultState();}
    public static boolean generate(World world,Random random,BlockPos pos,boolean crimson,boolean planted){
        if(world.isRemote||!Content.available(crimson?"crimson_fungus":"warped_fungus")||world.getBlockState(pos.down()).getBlock()!=Content.block(crimson?"crimson_nylium":"warped_nylium"))return false;
        int height=4+random.nextInt(10);if(random.nextInt(12)==0)height*=2;
        if(pos.getY()<1||pos.getY()+height+1>=world.getHeight()||!world.isAreaLoaded(pos.add(-4,0,-4),pos.add(4,height+1,4)))return false;
        for(int y=0;y<=height;y++)for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){BlockPos at=pos.add(x,y,z);IBlockState old=world.getBlockState(at);if(world.getTileEntity(at)!=null||old.getMaterial()!=Material.AIR&&old.getMaterial()!=Material.LEAVES&&!(old.getBlock() instanceof BlockBush)&&!old.getBlock().isReplaceable(world,at))return false;}
        HugeFungus fungus=new HugeFungus(world,random,crimson,planted);fungus.plan(pos,height,!planted&&random.nextFloat()<.06F);
        if(!planted)fungus.plan.put(pos.down(),Blocks.NETHERRACK.getDefaultState());
        for(Map.Entry<BlockPos,IBlockState> entry:fungus.plan.entrySet())world.setBlockState(entry.getKey(),entry.getValue(),planted?3:18);return true;
    }
    private void plan(BlockPos pos,int height,boolean thick){
        int radius=thick?1:0;
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)for(int y=0;y<height;y++)if(!thick||Math.abs(x)!=radius||Math.abs(z)!=radius||random.nextFloat()<.1F)plan.put(pos.add(x,y,z),stem);
        int depth=Math.min(random.nextInt(1+height/3)+5,height),bottom=height-depth;
        for(int y=bottom;y<=height;y++){
            int r=y<height-random.nextInt(3)?2:1;if(depth>8&&y<bottom+4)r=3;if(thick)r++;
            for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
                boolean edgeX=x==-r||x==r,edgeZ=z==-r||z==r,inside=!edgeX&&!edgeZ&&y!=height,corner=edgeX&&edgeZ;BlockPos at=pos.add(x,y,z);
                if(plan.containsKey(at))continue;
                if(y<bottom+3){if(!inside){IBlockState below=plan.get(at.down());if(below==hat||random.nextFloat()<.15F){plan.put(at,hat);if(crimson&&random.nextInt(11)==0)vine(at);}}}
                else if(inside)cap(at,.1F,.2F,crimson?.1F:0);
                else if(corner)cap(at,.01F,.7F,crimson?.083F:0);
                else cap(at,.0005F,.98F,crimson?.07F:0);
            }
        }
    }
    private void cap(BlockPos pos,float light,float wart,float vines){if(Content.available("shroomlight")&&random.nextFloat()<light)plan.put(pos,Content.block("shroomlight").getDefaultState());else if(random.nextFloat()<wart){plan.put(pos,hat);if(random.nextFloat()<vines)vine(pos);}}
    private void vine(BlockPos pos){if(!Content.available("weeping_vines"))return;int length=1+random.nextInt(5);if(random.nextInt(7)==0)length*=2;for(int y=1;y<=length&&pos.getY()-y>0;y++){BlockPos at=pos.down(y);if(plan.containsKey(at)||!world.isAirBlock(at))break;plan.put(at,Content.block("weeping_vines").getDefaultState());}}
}
