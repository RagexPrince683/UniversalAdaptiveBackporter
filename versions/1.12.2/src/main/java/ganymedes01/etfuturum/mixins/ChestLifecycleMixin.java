package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.MobChestIndex;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=TileEntity.class,remap=false)
public abstract class ChestLifecycleMixin {
    @Inject(method="onLoad",at=@At("RETURN"),remap=false)
    private void uab$loadedChest(CallbackInfo callback){MobChestIndex.add((TileEntity)(Object)this);}
    @Inject(method={"invalidate","func_145843_s","onChunkUnload"},at=@At("HEAD"),remap=false)
    private void uab$removedChest(CallbackInfo callback){MobChestIndex.remove((TileEntity)(Object)this);}
}
