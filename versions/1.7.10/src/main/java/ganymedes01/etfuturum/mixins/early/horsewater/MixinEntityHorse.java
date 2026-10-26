package ganymedes01.etfuturum.mixins.early.horsewater;

import net.minecraft.block.material.Material;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHorse.class)
public abstract class MixinEntityHorse extends EntityAnimal {

	public MixinEntityHorse(World world) {
		super(world);
	}

	@Inject(method = "onLivingUpdate", at = @At("TAIL"))
	private void floatWhileRiddenInWater(CallbackInfo ci) {
		if (riddenByEntity != null && worldObj.isMaterialInBB(boundingBox.contract(0.001D, 0.001D, 0.001D), Material.water)) {
			motionY += 0.03D;
		}
	}
}
