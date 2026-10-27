package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.block.material.Material;
import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=AbstractHorse.class,remap=false)
public abstract class HorseWaterMixin {
 @Inject(method={"onLivingUpdate","func_70636_d"},at=@At("TAIL"),remap=false)
 private void uab$float(CallbackInfo ci){AbstractHorse horse=(AbstractHorse)(Object)this;if(Features.enabled("mixins.riddenHorsesInWater")&&horse.isBeingRidden()&&horse.world.isMaterialInBB(horse.getEntityBoundingBox().shrink(.001),Material.WATER))horse.motionY+=.03;}
}
