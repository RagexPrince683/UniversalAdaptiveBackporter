package ganymedes01.etfuturum.mixins.early.powdersnow;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import ganymedes01.etfuturum.PowderSnow;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Reuse the native sampled snowfall position, without scans or changing other weather decisions. */
@Mixin(WorldServer.class)
public final class MixinSnowPrecipitation {
    @WrapOperation(method="func_147456_g",at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldServer;func_147478_e(IIIZ)Z"))
    private boolean uab$collectSnow(WorldServer world,int x,int y,int z,boolean light,Operation<Boolean> original){boolean result=original.call(world,x,y,z,light);PowderSnow.precipitation(world,x,y,z);return result;}
}
