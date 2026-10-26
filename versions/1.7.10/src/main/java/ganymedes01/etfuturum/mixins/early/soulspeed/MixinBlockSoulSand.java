package ganymedes01.etfuturum.mixins.early.soulspeed;

import ganymedes01.etfuturum.enchantment.SoulSpeed;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSoulSand.class)
public class MixinBlockSoulSand {
	@Inject(method = "onEntityCollidedWithBlock", at = @At("HEAD"), cancellable = true)
	private void etfu$soulSpeed(World world, int x, int y, int z, Entity entity, CallbackInfo ci) {
		if (entity instanceof EntityLivingBase && SoulSpeed.level((EntityLivingBase) entity) > 0) ci.cancel();
	}
}
