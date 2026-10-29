package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.FluidPhysics;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=BlockLiquid.class,remap=false)
public abstract class FluidLevelMixin {
 @Group(name="fluidLevel",min=1) @Redirect(method={"modifyAcceleration","func_176197_a"},at=@At(value="INVOKE",target="Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"),require=0,expect=0,remap=false)
 private Vec3d uab$level(Vec3d velocity,Vec3d flow,World world,BlockPos pos,Entity entity,Vec3d initial){return velocity.add(FluidPhysics.level(flow,world.getBlockState(pos)));}
 @Group(name="fluidLevel",min=1) @Redirect(method={"modifyAcceleration","func_176197_a"},at=@At(value="INVOKE",target="Lnet/minecraft/util/math/Vec3d;func_178787_e(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"),require=0,expect=0,remap=false)
 private Vec3d uab$levelSrg(Vec3d velocity,Vec3d flow,World world,BlockPos pos,Entity entity,Vec3d initial){return velocity.add(FluidPhysics.level(flow,world.getBlockState(pos)));}
}
