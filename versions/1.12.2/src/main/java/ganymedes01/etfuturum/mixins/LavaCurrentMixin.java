package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Entity.class,remap=false)
public abstract class LavaCurrentMixin {
 @Group(name="lavaCurrent",min=1) @Inject(method={"onEntityUpdate","func_70030_z"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;setOnFireFromLava()V",shift=At.Shift.AFTER),require=0,remap=false)
 private void uab$lava(CallbackInfo ci){uab$current();}
 @Group(name="lavaCurrent",min=1) @Inject(method={"onEntityUpdate","func_70030_z"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;func_70044_A()V",shift=At.Shift.AFTER),require=0,remap=false)
 private void uab$lavaSrg(CallbackInfo ci){uab$current();}
 private void uab$current(){if(Features.enabled("mixins.adjustedLiquidPhysics")){Entity entity=(Entity)(Object)this;entity.world.handleMaterialAcceleration(entity.getEntityBoundingBox(),Material.LAVA,entity);}}
}
