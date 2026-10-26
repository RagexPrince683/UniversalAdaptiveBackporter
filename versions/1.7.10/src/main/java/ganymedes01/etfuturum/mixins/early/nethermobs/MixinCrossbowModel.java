package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.items.ItemCrossbow;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public class MixinCrossbowModel {
	@Inject(method = "setRotationAngles", at = @At("RETURN"))
	private void etfu$holdCrossbow(float limb, float amount, float age, float yaw, float pitch, float scale, Entity entity, CallbackInfo ci) {
		if (!(entity instanceof EntityLivingBase living)) return;
		ItemStack held = living.getHeldItem();
		if (held == null || !(held.getItem() instanceof ItemCrossbow) || !ItemCrossbow.charged(held) && (!(living instanceof net.minecraft.entity.player.EntityPlayer player) || player.getItemInUse() != held)) return;
		ModelBiped model = (ModelBiped) (Object) this;
		if (living instanceof net.minecraft.entity.player.EntityPlayer player && player.getItemInUse() == held)
			ganymedes01.etfuturum.client.model.CrossbowPose.charge(model.bipedRightArm, model.bipedLeftArm, player.getItemInUseDuration() + MathHelper.clamp_float(age - living.ticksExisted, 0, 1), ItemCrossbow.chargeTime(held));
		else ganymedes01.etfuturum.client.model.CrossbowPose.hold(model.bipedRightArm, model.bipedLeftArm, model.bipedHead);
	}
}
