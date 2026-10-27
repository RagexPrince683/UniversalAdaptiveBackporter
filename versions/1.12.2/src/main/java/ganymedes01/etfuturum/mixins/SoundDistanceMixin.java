package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.client.SoundDistances;
import net.minecraft.client.audio.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=SoundManager.class,remap=false)
public abstract class SoundDistanceMixin {
 @ModifyConstant(method={"playSound","func_148611_c"},constant=@Constant(floatValue=16,ordinal=0),remap=false)
 private float uab$distance(float original,ISound sound){return SoundDistances.distance(original,sound);}
}
