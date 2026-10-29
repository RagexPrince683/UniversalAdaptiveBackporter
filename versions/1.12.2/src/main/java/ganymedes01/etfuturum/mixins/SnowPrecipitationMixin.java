package ganymedes01.etfuturum.mixins;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import ganymedes01.etfuturum.PowderSnow;
import net.minecraft.world.WorldServer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=WorldServer.class,remap=false)
public final class SnowPrecipitationMixin {
    @Group(name="snowPrecipitation",min=1) @WrapOperation(method={"updateBlocks","func_147456_g"},at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldServer;canSnowAt(Lnet/minecraft/util/math/BlockPos;Z)Z"),require=0,remap=false)
    private boolean uab$snow(WorldServer world,BlockPos pos,boolean light,Operation<Boolean> original){boolean result=original.call(world,pos,light);PowderSnow.precipitation(world,pos);return result;}
    @Group(name="snowPrecipitation",min=1) @WrapOperation(method={"updateBlocks","func_147456_g"},at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldServer;func_175708_f(Lnet/minecraft/util/math/BlockPos;Z)Z"),require=0,remap=false)
    private boolean uab$snowSrg(WorldServer world,BlockPos pos,boolean light,Operation<Boolean> original){boolean result=original.call(world,pos,light);PowderSnow.precipitation(world,pos);return result;}
}
