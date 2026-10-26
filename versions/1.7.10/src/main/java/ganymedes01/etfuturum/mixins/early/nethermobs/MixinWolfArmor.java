package ganymedes01.etfuturum.mixins.early.nethermobs;
import ganymedes01.etfuturum.ModItems;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(RenderWolf.class)
public abstract class MixinWolfArmor extends RenderLiving {
	protected MixinWolfArmor() { super(new ModelWolf(), .5F); }
	@Inject(method = "shouldRenderPass(Lnet/minecraft/entity/passive/EntityWolf;IF)I", at = @At("HEAD"), cancellable = true)
	private void etfu$armor(EntityWolf wolf, int pass, float partial, CallbackInfoReturnable<Integer> cir) { ItemStack armor = wolf.getEquipmentInSlot(3); if (pass == 2 && armor != null && armor.getItem() == ModItems.WOLF_ARMOR.get()) { setRenderPassModel(mainModel); bindTexture(new ResourceLocation("etfuturum:textures/entity/equipment/wolf_body/armadillo_scute.png")); org.lwjgl.opengl.GL11.glColor4f(1, 1, 1, 1); cir.setReturnValue(1); } }
}
