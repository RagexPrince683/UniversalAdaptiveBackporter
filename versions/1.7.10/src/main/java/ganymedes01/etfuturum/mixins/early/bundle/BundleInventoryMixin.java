package ganymedes01.etfuturum.mixins.early.bundle;

import java.util.List;
import ganymedes01.etfuturum.items.ItemBundle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public abstract class BundleInventoryMixin {
    @Shadow public List inventorySlots;
    @Inject(method="slotClick",at=@At("HEAD"),cancellable=true)
    private void uab$bundle(int id,int button,int type,EntityPlayer player,CallbackInfoReturnable<ItemStack> callback){
        if(type!=0||button!=1||id<0||id>=inventorySlots.size())return;
        Slot slot=(Slot)inventorySlots.get(id);if(!ItemBundle.bundle(player.inventory.getItemStack())&&!ItemBundle.bundle(slot.getStack()))return;
        ItemStack before=slot.getStack()==null?null:slot.getStack().copy();
        if(ItemBundle.click(player,slot))callback.setReturnValue(before);
    }
}
