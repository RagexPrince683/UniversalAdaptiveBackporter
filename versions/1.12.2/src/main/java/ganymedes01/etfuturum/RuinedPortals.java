package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.world.RuinedPortalPlan;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public final class RuinedPortals extends WorldSavedData {
    private static final String ID="uab_ruined_portals";private final Map<Long,BlockPos> sites=new LinkedHashMap<>();
    public RuinedPortals(){super(ID);}public RuinedPortals(String name){super(name);}
    public static RuinedPortals get(World world){RuinedPortals data=(RuinedPortals)world.getPerWorldStorage().getOrLoadData(RuinedPortals.class,ID);if(data==null){data=new RuinedPortals();world.getPerWorldStorage().setData(ID,data);}return data;}
    public static void generate(World world,int cx,int cz){
        if(world.isRemote||!Features.enabled("content.ruined_portals")||!world.getWorldInfo().isMapFeaturesEnabled()||world.provider.getDimension()!=0&&world.provider.getDimension()!=-1)return;
        int[] candidate=RuinedPortalPlan.candidate(world.getSeed(),Math.floorDiv(cx,40),Math.floorDiv(cz,40));if(candidate[0]!=cx||candidate[1]!=cz)return;
        RuinedPortals data=get(world);long key=ChunkPos.asLong(cx,cz);if(data.sites.containsKey(key))return;
        int x=cx*16+8,z=cz*16+8,y=-1;boolean nether=world.provider.getDimension()==-1;Random random=RuinedPortalPlan.random(world.getSeed(),cx,cz);
        if(nether){for(int h=32+random.nextInt(40);h<112;h++)if(world.getBlockState(new BlockPos(x,h-1,z)).isFullCube()&&world.isAirBlock(new BlockPos(x,h,z))&&world.isAirBlock(new BlockPos(x,h+6,z))){y=h;break;}}
        else{y=world.getTopSolidOrLiquidBlock(new BlockPos(x,0,z)).getY();while(y>8&&!world.getBlockState(new BlockPos(x,y-1,z)).getMaterial().isSolid())y--;if(world.getBiome(new BlockPos(x,y,z)).getTemperature(new BlockPos(x,y,z))>1.5F)y-=random.nextInt(3);}
        if(y<10||y>240)return;BlockPos origin=new BlockPos(x,y,z);StructureBoundingBox bounds=new StructureBoundingBox(x-5,y-2,z-5,x+5,y+8,z+5);if(StructureReservations.vanilla(world,bounds)||OceanStructures.reserved(world,bounds)||ModernStructures.reserved(world,bounds))return;
        for(BlockPos pos:BlockPos.getAllInBox(x-5,y-1,z-5,x+5,y+7,z+5))if(world.getTileEntity(pos)!=null)return;
        for(RuinedPortalPlan.Block block:RuinedPortalPlan.blocks(random,nether)){BlockPos pos=origin.add(block.x,block.y,block.z);IBlockState state=block.name.equals("crying_obsidian")&&Content.available("crying_obsidian")?Content.block("crying_obsidian").getDefaultState():block.name.equals("magma_block")?Blocks.MAGMA.getDefaultState():block.name.equals("stone_bricks")?Blocks.STONEBRICK.getDefaultState():block.name.equals("blackstone")&&Content.available("blackstone")?Content.block("blackstone").getDefaultState():Block.getBlockFromName("minecraft:"+block.name)==null?Blocks.NETHERRACK.getDefaultState():Block.getBlockFromName("minecraft:"+block.name).getDefaultState();
            if(block.name.equals("crying_obsidian")&&!Content.available("crying_obsidian"))state=Blocks.OBSIDIAN.getDefaultState();world.setBlockState(pos,state,18);TileEntity tile=world.getTileEntity(pos);if(block.name.equals("chest")&&tile instanceof TileEntityChest)((TileEntityChest)tile).setLootTable(new ResourceLocation(UAB.ID,"chests/ruined_portal"),random.nextLong());}
        data.sites.put(key,origin);data.markDirty();
    }
    public static BlockPos nearest(World world,BlockPos origin){BlockPos nearest=null;double best=Double.MAX_VALUE;for(BlockPos pos:get(world).sites.values()){double distance=origin.distanceSq(pos);if(distance<best){best=distance;nearest=pos;}}return nearest;}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){NBTTagList list=new NBTTagList();for(Map.Entry<Long,BlockPos> entry:sites.entrySet()){NBTTagCompound site=new NBTTagCompound();site.setLong("Chunk",entry.getKey());site.setLong("Position",entry.getValue().toLong());list.appendTag(site);}tag.setTag("Sites",list);return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){sites.clear();NBTTagList list=tag.getTagList("Sites",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound site=list.getCompoundTagAt(i);sites.put(site.getLong("Chunk"),BlockPos.fromLong(site.getLong("Position")));}}
}
