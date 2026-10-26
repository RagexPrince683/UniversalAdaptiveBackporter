package ganymedes01.etfuturum;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureData;

/** Only saved sites are consulted. Feeding a dolphin never starts terrain generation or a search. */
public final class OceanGuidance {
    private OceanGuidance(){ }
    public static BlockPos nearest(World world,BlockPos origin){
        BlockPos found=OceanTreasure.nearest(world,origin);if(found!=null)return found;
        BlockPos wreck=OceanStructures.nearest(world,origin,"shipwreck"),ruin=OceanStructures.nearest(world,origin,"underwater_ruin");if(wreck!=null||ruin!=null)return wreck==null?ruin:ruin==null||wreck.distanceSq(origin)<ruin.distanceSq(origin)?wreck:ruin;
        MapGenStructureData monuments=(MapGenStructureData)world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class,"Monument");if(monuments==null)return null;double distance=Double.MAX_VALUE;NBTTagCompound starts=monuments.getTagCompound();
        for(String key:starts.getKeySet()){int[] bounds=starts.getCompoundTag(key).getIntArray("BB");if(bounds.length!=6)continue;BlockPos pos=new BlockPos((bounds[0]+bounds[3])/2,bounds[1],(bounds[2]+bounds[5])/2);if(pos.distanceSq(origin)<distance){distance=pos.distanceSq(origin);found=pos;}}return found;
    }
}
