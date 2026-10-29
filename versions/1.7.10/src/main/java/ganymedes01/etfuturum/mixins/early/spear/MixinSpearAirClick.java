package ganymedes01.etfuturum.mixins.early.spear;

import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge 1.7.10 has no left-click-empty event. Observe only accepted empty-air clicks. */
@Mixin(Minecraft.class)
public class MixinSpearAirClick {
    @Inject(method = "func_147116_af", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;swingItem()V"), require = 0)
    private void uab$lungeAir(CallbackInfo callback) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS)
            EtFuturum.proxy.spearLunge(mc.thePlayer);
    }
}
