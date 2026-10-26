package ganymedes01.etfuturum.mixins.early.cartography;

import ganymedes01.etfuturum.inventory.LockedMapData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapData.class)
public class MixinMapData implements LockedMapData {
	@Unique private boolean etfu$locked;
	@Override public boolean etfu$isLocked() { return etfu$locked; }
	@Override public void etfu$setLocked(boolean locked) { etfu$locked = locked; ((MapData) (Object) this).markDirty(); }
	@Inject(method = "readFromNBT", at = @At("RETURN"))
	private void etfu$read(NBTTagCompound tag, CallbackInfo ci) { etfu$locked = tag.getBoolean("etfuturumLocked"); }
	@Inject(method = "writeToNBT", at = @At("RETURN"))
	private void etfu$write(NBTTagCompound tag, CallbackInfo ci) { if (etfu$locked) tag.setBoolean("etfuturumLocked", true); }
}
