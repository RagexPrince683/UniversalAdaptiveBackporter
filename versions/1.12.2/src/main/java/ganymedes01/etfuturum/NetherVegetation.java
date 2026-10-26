package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.event.terraingen.*;

/** Native population stays inside its owning chunk and never forces an unfinished neighbor. */
public final class NetherVegetation {
    private NetherVegetation() { }
    static void generate(World world,Random random,int cx,int cz){
        NetherBiomes.Kind kind=NetherBiomes.kind(world.getBiome(new BlockPos((cx<<4)+8,64,(cz<<4)+8)));if(kind==null)return;
        BlockPos origin=new BlockPos(cx<<4,0,cz<<4);
        boolean plants=TerrainGen.decorate(world,random,origin,DecorateBiomeEvent.Decorate.EventType.GRASS);
        if(kind==NetherBiomes.Kind.CRIMSON||kind==NetherBiomes.Kind.WARPED){
            boolean crimson=kind==NetherBiomes.Kind.CRIMSON;
            if(TerrainGen.decorate(world,random,origin,DecorateBiomeEvent.Decorate.EventType.TREE))for(int i=0;i<12;i++){
                BlockPos pos=floor(world,new BlockPos((cx<<4)+4+random.nextInt(8),32+random.nextInt(73),(cz<<4)+4+random.nextInt(8)),32);
                if(pos!=null&&NetherBiomes.kind(world.getBiome(pos))==kind&&!reserved(world,pos.add(-4,0,-4),pos.add(4,27,4)))HugeFungus.generate(world,random,pos,crimson,false);
            }
            if(plants){
                for(int i=0;i<12;i++){BlockPos pos=floor(world,new BlockPos((cx<<4)+random.nextInt(16),32+random.nextInt(89),(cz<<4)+random.nextInt(16)),32);if(pos!=null)scatter(world,random,pos,crimson,true);}
                for(int i=0;i<48;i++)vines(world,random,cx,cz,crimson);
            }
        }else if(kind==NetherBiomes.Kind.SOUL){
            if(plants&&Content.available("crimson_roots")&&random.nextInt(3)==0){BlockPos pos=floor(world,new BlockPos((cx<<4)+8,32+random.nextInt(89),(cz<<4)+8),32);if(pos!=null)scatter(world,random,pos,true,true);}
            if(Content.available("basalt")&&random.nextInt(6)==0)pillar(world,random,new BlockPos((cx<<4)+4+random.nextInt(8),32+random.nextInt(89),(cz<<4)+4+random.nextInt(8)));
            if(world.getWorldInfo().isMapFeaturesEnabled())for(int i=0;i<3;i++)fossil(world,random,cx,cz);
        }else if(Content.available("basalt")&&Content.available("blackstone")){
            for(int i=0;i<15;i++){BlockPos pos=floor(world,new BlockPos((cx<<4)+random.nextInt(16),32+random.nextInt(73),(cz<<4)+random.nextInt(16)),32);if(pos!=null)delta(world,random,pos.down(),cx,cz);}
            for(int segment=0;segment<5;segment++)for(int i=0;i<12;i++){
                BlockPos pos=floor(world,new BlockPos((cx<<4)+random.nextInt(16),32+segment*18+random.nextInt(23),(cz<<4)+random.nextInt(16)),32);
                if(pos!=null&&NetherBiomes.kind(world.getBiome(pos))==kind)columns(world,random,pos,cx,cz,random.nextInt(4)!=0);
            }
        }
    }
    private static boolean reserved(World world,BlockPos min,BlockPos max){return StructureReservations.vanilla(world,new StructureBoundingBox(min,max));}
    private static BlockPos floor(World world,BlockPos pos,int min){for(;pos.getY()>min;pos=pos.down()){if(!world.isBlockLoaded(pos))return null;if(world.isAirBlock(pos)&&world.getBlockState(pos.down()).getMaterial().isSolid())return pos;}return null;}
    public static void scatter(World world,Random random,BlockPos origin,boolean crimson,boolean natural){
        int cx=origin.getX()>>4,cz=origin.getZ()>>4;
        for(int i=0;i<128;i++){
            BlockPos pos=origin.add(random.nextInt(8)-random.nextInt(8),random.nextInt(4)-random.nextInt(4),random.nextInt(8)-random.nextInt(8));
            if(pos.getY()<1||pos.getY()>=world.getHeight()-1||natural&&((pos.getX()>>4)!=cx||(pos.getZ()>>4)!=cz)||!world.isAreaLoaded(pos.down(),pos)||!world.isAirBlock(pos))continue;
            String name;
            if(random.nextInt(32)==0)name=(random.nextInt(16)==0?!crimson:crimson)?"crimson_fungus":"warped_fungus";
            else if(!crimson&&random.nextInt(3)==0)name="nether_sprouts";
            else name=(!crimson&&random.nextInt(32)==0||crimson)?"crimson_roots":"warped_roots";
            if(!Content.available(name))continue;NetherPlant plant=(NetherPlant)Content.block(name);
            if(plant.canBlockStay(world,pos,plant.getDefaultState()))world.setBlockState(pos,plant.getDefaultState(),natural?18:3);
        }
    }
    private static void vines(World world,Random random,int cx,int cz,boolean crimson){
        String name=crimson?"weeping_vines":"twisting_vines";if(!Content.available(name))return;
        BlockPos pos=new BlockPos((cx<<4)+random.nextInt(16),32+random.nextInt(89),(cz<<4)+random.nextInt(16));EnumFacing growth=crimson?EnumFacing.DOWN:EnumFacing.UP;
        if(crimson){while(pos.getY()<123&&world.isAirBlock(pos.up()))pos=pos.up();}else{pos=floor(world,pos,32);if(pos==null)return;}
        NetherVines vine=(NetherVines)Content.block(name);int length=1+random.nextInt(crimson?8:12);
        for(int i=0;i<length&&vine.canPlaceBlockAt(world,pos);i++,pos=pos.offset(growth))world.setBlockState(pos,vine.getDefaultState(),18);
    }
    private static boolean rock(World world,BlockPos pos){if(!world.isBlockLoaded(pos)||world.getTileEntity(pos)!=null)return false;Block b=world.getBlockState(pos).getBlock();return b==Content.block("blackstone")||b==Content.block("basalt");}
    private static boolean space(World world,BlockPos pos){return world.isBlockLoaded(pos)&&world.getTileEntity(pos)==null&&(world.isAirBlock(pos)||pos.getY()<=32&&world.getBlockState(pos).getMaterial()==Material.LAVA);}
    private static void columns(World world,Random random,BlockPos origin,int cx,int cz,boolean large){
        int height=large?5+random.nextInt(6):1+random.nextInt(4),size=Math.min(height,random.nextFloat()<.9F?5:8),reach=large?2+random.nextInt(2):1;
        for(int n=0;n<50;n++){
            int x=origin.getX()+random.nextInt(size*2+1)-size,z=origin.getZ()+random.nextInt(size*2+1)-size,h=height-Math.abs(x-origin.getX())-Math.abs(z-origin.getZ());if(h<0)continue;
            for(int dx=-reach;dx<=reach;dx++)for(int dz=-reach;dz<=reach;dz++){
                BlockPos pos=new BlockPos(x+dx,origin.getY(),z+dz);if((pos.getX()>>4)!=cx||(pos.getZ()>>4)!=cz)continue;
                int distance=Math.abs(dx)+Math.abs(dz)+1;
                if(space(world,pos)){for(int d=0;d<distance&&pos.getY()>1&&!rock(world,pos.down());d++)pos=pos.down();}
                else {for(int d=0;d<distance&&pos.getY()<125&&rock(world,pos);d++)pos=pos.up();}
                if(!space(world,pos)||!rock(world,pos.down()))continue;
                for(int dy=0,limit=h-(Math.abs(dx)+Math.abs(dz))/2;dy<=limit&&pos.getY()<125;dy++,pos=pos.up()){
                    if(space(world,pos))world.setBlockState(pos,Content.block("basalt").getDefaultState(),18);else if(world.getBlockState(pos).getBlock()!=Content.block("basalt"))break;
                }
            }
        }
    }
    private static boolean deltaClear(World world,BlockPos pos,int cx,int cz){
        if((pos.getX()>>4)!=cx||(pos.getZ()>>4)!=cz||!rock(world,pos))return false;
        for(EnumFacing face:EnumFacing.values()){BlockPos next=pos.offset(face);if(!world.isBlockLoaded(next)||world.isAirBlock(next)!=(face==EnumFacing.UP))return false;}return true;
    }
    private static void delta(World world,Random random,BlockPos origin,int cx,int cz){
        boolean rim=random.nextDouble()<.9;int rx=rim?3+random.nextInt(5):0,rz=rim?3+random.nextInt(5):0,xs=random.nextInt(3),zs=random.nextInt(3),size=Math.max(xs,zs);
        for(int x=-xs;x<=xs;x++)for(int z=-zs;z<=zs;z++)if(Math.abs(x)+Math.abs(z)<=size){BlockPos pos=origin.add(x,0,z);if(!deltaClear(world,pos,cx,cz))continue;if(rim)world.setBlockState(pos,Blocks.MAGMA.getDefaultState(),18);BlockPos lava=pos.add(rx,0,rz);if(deltaClear(world,lava,cx,cz))world.setBlockState(lava,Blocks.LAVA.getDefaultState(),18);}
    }
    private static void pillar(World world,Random random,BlockPos pos){
        while(pos.getY()<120&&world.isAirBlock(pos.up()))pos=pos.up();if(!world.isAirBlock(pos)||!world.getBlockState(pos.up()).isOpaqueCube()||reserved(world,pos.add(-3,-120,-3),pos.add(3,1,3)))return;
        IBlockState basalt=Content.block("basalt").getDefaultState();boolean[] arms={true,true,true,true};EnumFacing[] sides=EnumFacing.HORIZONTALS;
        while(pos.getY()>1&&world.isAirBlock(pos)){world.setBlockState(pos,basalt,18);for(int i=0;i<4;i++){BlockPos side=pos.offset(sides[i]);arms[i]&=random.nextInt(10)!=0;if(arms[i]&&world.isAirBlock(side))world.setBlockState(side,basalt,18);}pos=pos.down();}
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)if(random.nextInt(10)<10-Math.abs(x)*Math.abs(z)){BlockPos at=pos.add(x,1,z);for(int d=0;d<3&&at.getY()>1&&world.isAirBlock(at.down());d++)at=at.down();if(world.isAirBlock(at)&&world.getBlockState(at.down()).getMaterial().isSolid())world.setBlockState(at,basalt,18);}
    }
    private static void fossil(World world,Random random,int cx,int cz){
        String name="nether_fossils/fossil_"+(1+random.nextInt(14));OceanTemplate template=OceanTemplate.get(name);int rotation=random.nextInt(4),width=template.width(rotation),depth=template.depth(rotation);
        if(!template.available()||width>16||depth>16)return;
        int x=(cx<<4)+random.nextInt(17-width),z=(cz<<4)+random.nextInt(17-depth);
        for(int y=32+random.nextInt(89);y>=32;y--){BlockPos origin=new BlockPos(x,y,z),max=origin.add(width-1,template.sy-1,depth-1);if(!world.isAreaLoaded(origin.down(),max))continue;int support=0;boolean clear=true;
            for(BlockPos at:BlockPos.getAllInBox(origin,max)){if(!world.isAirBlock(at)||world.getTileEntity(at)!=null){clear=false;break;}if(at.getY()==y){Block soil=world.getBlockState(at.down()).getBlock();if(soil==Blocks.SOUL_SAND||soil==Content.block("soul_soil"))support++;}}
            if(!clear||support*2<width*depth||reserved(world,origin,max))continue;template.place(world,name,origin,rotation,cx,cz,random.nextLong(),1);return;
        }
    }
}
