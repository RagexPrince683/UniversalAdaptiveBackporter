package ganymedes01.etfuturum.mixins.early.nethermobs;
import ganymedes01.etfuturum.entities.RiderInput;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(EntityLivingBase.class)
public interface MixinRiderInput extends RiderInput {
	@Override @Accessor("isJumping") boolean etfuturum$isJumping();
}
