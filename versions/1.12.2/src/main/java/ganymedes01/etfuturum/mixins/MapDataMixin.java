package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.LockedMapData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value = MapData.class, remap = false)
public class MapDataMixin implements LockedMapData {
    @Unique private boolean etfu$locked;
    @Override public boolean etfu$isLocked() { return etfu$locked; }
    @Override public void etfu$setLocked(boolean value) { etfu$locked = value; ((MapData) (Object) this).markDirty(); }
    @Inject(method = {"readFromNBT", "func_76184_a"}, at = @At("RETURN"), remap = false)
    private void etfu$read(NBTTagCompound tag, CallbackInfo ci) { etfu$locked = tag.getBoolean("etfuturumLocked"); }
    @Inject(method = {"writeToNBT", "func_189551_b"}, at = @At("RETURN"), remap = false)
    private void etfu$write(NBTTagCompound tag, CallbackInfoReturnable<NBTTagCompound> cir) { if (etfu$locked) cir.getReturnValue().setBoolean("etfuturumLocked", true); }
}
