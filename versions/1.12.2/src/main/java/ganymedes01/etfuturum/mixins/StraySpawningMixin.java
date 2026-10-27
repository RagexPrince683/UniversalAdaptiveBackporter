package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.entity.monster.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=EntityStray.class,remap=false)
public abstract class StraySpawningMixin extends AbstractSkeleton {
 protected StraySpawningMixin(World world){super(world);}
 @Inject(method={"getCanSpawnHere","func_70601_bi"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$legacy(CallbackInfoReturnable<Boolean> ci){if(Features.enabled("world.oldStraySpawning"))ci.setReturnValue(super.getCanSpawnHere());}
}
