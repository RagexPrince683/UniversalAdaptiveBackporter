package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.client.renderer.ArmorTrimRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(RendererLivingEntity.class)
public abstract class MixinArmorTrims {
	@Unique private int etfu$armorPass;
	@Shadow protected abstract int shouldRenderPass(EntityLivingBase entity, int pass, float partialTicks);
	@Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;shouldRenderPass(Lnet/minecraft/entity/EntityLivingBase;IF)I"))
	private int etfu$rememberPass(RendererLivingEntity renderer, EntityLivingBase entity, int pass, float partialTicks) { etfu$armorPass = pass; return shouldRenderPass(entity, pass, partialTicks); }
	@Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", ordinal = 0))
	private void etfu$renderTrim(ModelBase model, Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
		model.render(entity, swing, amount, age, yaw, pitch, scale);
		ArmorTrimRenderer.render(model, entity, ((EntityLivingBase) entity).getEquipmentInSlot(4 - etfu$armorPass), swing, amount, age, yaw, pitch, scale);
	}
}
