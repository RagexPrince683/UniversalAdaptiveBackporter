package ganymedes01.etfuturum.mixins.early.potionids;

import java.util.Arrays;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Potion.class)
public abstract class MixinPotion {
    @Shadow @Final @Mutable public static Potion[] potionTypes;

    @Inject(method = "<clinit>", at = @At("RETURN"), require = 1)
    private static void uab$ensureNativeSlots(CallbackInfo ci) {
        // 0..127 round-trip through vanilla's signed-byte effect packets and NBT.
        // Never shrink an extender's array or discard existing registrations.
        if (potionTypes.length < 128) potionTypes = Arrays.copyOf(potionTypes, 128);
    }
}
