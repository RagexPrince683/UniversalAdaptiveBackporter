package ganymedes01.etfuturum;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public final class MapLocks {
    private MapLocks() { }
    public static boolean locked(ItemStack stack,World world) {
        if(stack.isEmpty()||stack.getItem()!=Items.FILLED_MAP)return false;
        if(stack.hasTagCompound()&&stack.getTagCompound().getBoolean("etfuturumLocked"))return true;
        MapData data=world==null?null:Items.FILLED_MAP.getMapData(stack,world);
        return data instanceof LockedMapData&&((LockedMapData)data).etfu$isLocked();
    }
}
