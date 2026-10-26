package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=MovementInputFromOptions.class,remap=false)
public class SwiftSneakMixin {
    @ModifyConstant(method={"updatePlayerMoveState","func_78898_a"},constant=@Constant(doubleValue=.3D),remap=false)
    private double uab$sneakSpeed(double speed){return Minecraft.getMinecraft().player==null||!Features.enabled("enchantspotions.swiftSneak")?speed:speed+.15*ModEnchantments.level(ModEnchantments.swiftSneak,Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.LEGS));}
}
