package ganymedes01.etfuturum.mixins.early.lightningrod;

import ganymedes01.etfuturum.blocks.BlockLightningRod;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLightningBolt.class)
public class MixinEntityLightningBolt {
	@Shadow private int lightningState;
	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void etfu$powerRod(CallbackInfo ci) {
		EntityLightningBolt bolt = (EntityLightningBolt) (Object) this;
		if (bolt.worldObj.isRemote || lightningState != 2) return;
		int x = MathHelper.floor_double(bolt.posX), y = MathHelper.floor_double(bolt.posY), z = MathHelper.floor_double(bolt.posZ);
		if (bolt.worldObj.blockExists(x, y, z) && bolt.worldObj.getBlock(x, y, z) instanceof BlockLightningRod rod)
			rod.strike(bolt.worldObj, x, y, z);
	}
}
