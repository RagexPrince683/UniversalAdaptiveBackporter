package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.MapLocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.RecipesMapExtending;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=RecipesMapExtending.class,remap=false)
public class MapScalingRecipeMixin {
    @Inject(method={"matches","func_77569_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private void etfu$locked(InventoryCrafting inventory,World world,CallbackInfoReturnable<Boolean> cir) {
        for(int slot=0;slot<inventory.getSizeInventory();slot++)if(MapLocks.locked(inventory.getStackInSlot(slot),world)){cir.setReturnValue(false);return;}
    }
}
