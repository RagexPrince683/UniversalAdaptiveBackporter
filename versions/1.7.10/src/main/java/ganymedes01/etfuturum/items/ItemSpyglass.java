package ganymedes01.etfuturum.items;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
public final class ItemSpyglass extends BaseItem {
    public ItemSpyglass(){super("spyglass");setMaxStackSize(1);}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 72000;}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.none;}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){if(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("spyglasses")){player.setItemInUse(stack,getMaxItemUseDuration(stack));world.playSoundAtEntity(player,"minecraft_1.21.11:item.spyglass.use",1,1);}return stack;}
    @Override public void onPlayerStoppedUsing(ItemStack stack,World world,EntityPlayer player,int remaining){world.playSoundAtEntity(player,"minecraft_1.21.11:item.spyglass.stop_using",1,1);}
}
