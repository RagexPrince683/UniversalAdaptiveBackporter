package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.BoatSettings;
import net.minecraft.entity.item.EntityBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=EntityBoat.class,remap=false)
public abstract class BoatSteeringMixin {
 @ModifyConstant(method={"controlBoat","func_184443_x"},constant={@Constant(floatValue=.04F),@Constant(floatValue=.005F)},remap=false)
 private float uab$speed(float value){return value*BoatSettings.speed;}
}
