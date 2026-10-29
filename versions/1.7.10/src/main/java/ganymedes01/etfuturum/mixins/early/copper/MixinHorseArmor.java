package ganymedes01.etfuturum.mixins.early.copper;
import java.util.Arrays;
import ganymedes01.etfuturum.items.ItemCopperHorseArmor;
import ganymedes01.etfuturum.items.ItemNetheriteHorseArmor;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(EntityHorse.class)
public class MixinHorseArmor {
    @Shadow @Final @Mutable private static int[] armorValues;
    @Inject(method="<clinit>",at=@At("RETURN")) private static void uab$copper(CallbackInfo info){ItemCopperHorseArmor.nativeIndex=armorValues.length;ItemNetheriteHorseArmor.nativeIndex=armorValues.length+1;armorValues=Arrays.copyOf(armorValues,armorValues.length+2);armorValues[ItemCopperHorseArmor.nativeIndex]=4;armorValues[ItemNetheriteHorseArmor.nativeIndex]=19;}
    @Inject(method="getHorseArmorIndex",at=@At("HEAD"),cancellable=true) private void uab$index(ItemStack stack,CallbackInfoReturnable<Integer> result){if(stack!=null&&stack.getItem() instanceof ItemCopperHorseArmor)result.setReturnValue(ItemCopperHorseArmor.nativeIndex);else if(stack!=null&&stack.getItem() instanceof ItemNetheriteHorseArmor)result.setReturnValue(ItemNetheriteHorseArmor.nativeIndex);}
    @Inject(method="func_146085_a",at=@At("HEAD"),cancellable=true) private static void uab$accept(Item item,CallbackInfoReturnable<Boolean> result){if(item instanceof ItemCopperHorseArmor || item instanceof ItemNetheriteHorseArmor)result.setReturnValue(true);}
}
