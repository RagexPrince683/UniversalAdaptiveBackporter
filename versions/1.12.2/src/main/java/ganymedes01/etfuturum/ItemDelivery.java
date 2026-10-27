package ganymedes01.etfuturum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
/** Inventory ownership changes only after the native entity-join hook accepts a delivery. */
public final class ItemDelivery {
    private ItemDelivery(){}
    public static boolean drop(Entity source,ItemStack stack,float offset){return drop(source.world,source.posX,source.posY+offset,source.posZ,stack);}
    public static boolean drop(Entity source,ItemStack stack,float offset,int delay){return drop(source.world,source.posX,source.posY+offset,source.posZ,stack,delay);}
    public static boolean drop(net.minecraft.world.World world,double x,double y,double z,ItemStack stack){return drop(world,x,y,z,stack,10);}
    private static boolean drop(net.minecraft.world.World world,double x,double y,double z,ItemStack stack,int delay){if(world.isRemote||stack.isEmpty())return false;EntityItem item=new EntityItem(world,x,y,z,stack.copy());item.setPickupDelay(delay);return world.spawnEntity(item);}
}
