package ganymedes01.etfuturum.mixins.early.copper;
import java.util.Arrays;
import ganymedes01.etfuturum.items.ItemCopperHorseArmor;
import ganymedes01.etfuturum.items.ItemNetheriteHorseArmor;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityHorse.class)
public class MixinHorseArmorTextures {
    @Shadow @Final @Mutable private static String[] horseArmorTextures;
    @Shadow @Final @Mutable private static String[] field_110273_bx;
    @Inject(method="setHorseTexturePaths",at=@At("HEAD")) private void uab$textures(CallbackInfo info){int index=ItemNetheriteHorseArmor.nativeIndex;if(horseArmorTextures.length<=index)horseArmorTextures=Arrays.copyOf(horseArmorTextures,index+1);if(field_110273_bx.length<=index)field_110273_bx=Arrays.copyOf(field_110273_bx,index+1);horseArmorTextures[ItemCopperHorseArmor.nativeIndex]="etfuturum:textures/entity/horse/armor/horse_armor_copper.png";field_110273_bx[ItemCopperHorseArmor.nativeIndex]="uab_copper";horseArmorTextures[index]="etfuturum:textures/entity/horse/armor/horse_armor_netherite.png";field_110273_bx[index]="uab_netherite";}
}
