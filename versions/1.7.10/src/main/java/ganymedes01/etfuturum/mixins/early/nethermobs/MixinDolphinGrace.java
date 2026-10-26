package ganymedes01.etfuturum.mixins.early.nethermobs;
import ganymedes01.etfuturum.potion.ModPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Changes only horizontal water drag while the native potion is active. No swimming poses or controls. */
@Mixin(EntityLivingBase.class)
public class MixinDolphinGrace {
	@ModifyConstant(method = "moveEntityWithHeading", constant = {@Constant(doubleValue = 0.800000011920929D, ordinal = 0), @Constant(doubleValue = 0.800000011920929D, ordinal = 2)}, require = 2)
	private double etfu$grace(double drag) { EntityLivingBase living = (EntityLivingBase) (Object) this; return living instanceof EntityPlayer && ModPotions.dolphinGrace != null && living.isPotionActive(ModPotions.dolphinGrace) ? .96 : drag; }
}
