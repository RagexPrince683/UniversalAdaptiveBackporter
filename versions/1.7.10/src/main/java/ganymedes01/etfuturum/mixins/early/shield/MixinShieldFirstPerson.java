package ganymedes01.etfuturum.mixins.early.shield;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import ganymedes01.etfuturum.items.ItemShield;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** The shield item renderer supplies its own raised pose instead of the old sword's rotation. */
@Mixin(ItemRenderer.class)
public class MixinShieldFirstPerson {
    @WrapOperation(method="renderItemInFirstPerson",at=@At(value="INVOKE",target="Lnet/minecraft/item/ItemStack;getItemUseAction()Lnet/minecraft/item/EnumAction;"),require=0)
    private EnumAction uab$shieldPose(ItemStack stack,Operation<EnumAction> original){return stack.getItem() instanceof ItemShield?EnumAction.none:original.call(stack);}
}
