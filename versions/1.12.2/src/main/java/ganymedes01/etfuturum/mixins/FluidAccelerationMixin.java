package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=World.class,remap=false)
public abstract class FluidAccelerationMixin {
 // The native bytecode inlines this value into each axis; changing only its unused local does nothing.
 @ModifyConstant(method={"handleMaterialAcceleration","func_72918_a"},constant=@Constant(doubleValue=.014D),remap=false)
 private double uab$itemFlow(double value,AxisAlignedBB bounds,Material material,Entity entity){return entity instanceof EntityItem&&Features.enabled("mixins.adjustedLiquidPhysics")?value*4:value;}
 @Group(name="fluidLength",min=1) @Redirect(method={"handleMaterialAcceleration","func_72918_a"},at=@At(value="INVOKE",target="Lnet/minecraft/util/math/Vec3d;normalize()Lnet/minecraft/util/math/Vec3d;"),require=0,expect=0,remap=false)
 private Vec3d uab$length(Vec3d flow,AxisAlignedBB bounds,Material material,Entity entity){return FluidPhysics.normalize(flow,entity);}
 @Group(name="fluidLength",min=1) @Redirect(method={"handleMaterialAcceleration","func_72918_a"},at=@At(value="INVOKE",target="Lnet/minecraft/util/math/Vec3d;func_72432_b()Lnet/minecraft/util/math/Vec3d;"),require=0,expect=0,remap=false)
 private Vec3d uab$lengthSrg(Vec3d flow,AxisAlignedBB bounds,Material material,Entity entity){return FluidPhysics.normalize(flow,entity);}
}
