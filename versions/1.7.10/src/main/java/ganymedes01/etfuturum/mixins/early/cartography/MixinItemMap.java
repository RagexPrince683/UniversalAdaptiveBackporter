package ganymedes01.etfuturum.mixins.early.cartography;

import ganymedes01.etfuturum.inventory.LockedMapData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemMap;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemMap.class)
public class MixinItemMap {
	@Inject(method = "updateMapData", at = @At("HEAD"), cancellable = true)
	private void etfu$locked(World world, Entity entity, MapData map, CallbackInfo ci) {
		if (((LockedMapData) map).etfu$isLocked()) ci.cancel();
	}
}
