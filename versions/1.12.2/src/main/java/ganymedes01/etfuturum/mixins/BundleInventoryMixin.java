package ganymedes01.etfuturum.mixins;

import java.util.List;
import ganymedes01.etfuturum.BundleItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public abstract class BundleInventoryMixin {
    @Shadow public List<Slot> inventorySlots;
    @Inject(method="slotClick",at=@At("HEAD"),cancellable=true)
    private void uab$bundle(int id,int button,ClickType type,EntityPlayer player,CallbackInfoReturnable<ItemStack> callback){
        if(type!=ClickType.PICKUP||button!=1||id<0||id>=inventorySlots.size())return;
        Slot slot=inventorySlots.get(id);if(!BundleItem.bundle(player.inventory.getItemStack())&&!BundleItem.bundle(slot.getStack()))return;
        ItemStack before=slot.getStack().copy();
        if(BundleItem.click(player,slot))callback.setReturnValue(before);
    }
}
