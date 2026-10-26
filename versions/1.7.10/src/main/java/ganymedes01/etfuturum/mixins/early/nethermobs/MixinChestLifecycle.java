package ganymedes01.etfuturum.mixins.early.nethermobs;
import ganymedes01.etfuturum.world.MobChestIndex;
import net.minecraft.tileentity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(TileEntity.class)
public abstract class MixinChestLifecycle {
	@Inject(method = "validate", at = @At("RETURN")) private void etfu$index(CallbackInfo ci) { TileEntity tile = (TileEntity) (Object) this; if (tile.getWorldObj() != null && MobChestIndex.chest(tile)) MobChestIndex.add(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord); }
	@Inject(method = "invalidate", at = @At("HEAD")) private void etfu$remove(CallbackInfo ci) { TileEntity tile = (TileEntity) (Object) this; if (tile.getWorldObj() != null && MobChestIndex.chest(tile)) MobChestIndex.remove(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord); }
}
