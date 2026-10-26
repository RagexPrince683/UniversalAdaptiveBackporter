package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.LockedMapData;
import ganymedes01.etfuturum.MapLocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemMap.class, remap = false)
public class ItemMapMixin {
    @Inject(method = {"updateMapData", "func_77872_a"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void etfu$locked(World world, Entity entity, MapData map, CallbackInfo ci) { if (((LockedMapData) map).etfu$isLocked()) ci.cancel(); }
    @Inject(method={"scaleMap","func_185063_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private static void etfu$scaling(ItemStack stack,World world,int direction,CallbackInfo ci) { if(MapLocks.locked(stack,world))ci.cancel(); }
}
