package ganymedes01.etfuturum.mixins.early.archaeology;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityThrowable.class)
public abstract class MixinPotProjectile {
	@Shadow protected abstract void onImpact(MovingObjectPosition hit);
	@Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/EntityThrowable;onImpact(Lnet/minecraft/util/MovingObjectPosition;)V"))
	private void etfu$shatterPot(EntityThrowable entity, MovingObjectPosition hit) {
		World world = entity.worldObj;
		if (!world.isRemote && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && world.getBlock(hit.blockX, hit.blockY, hit.blockZ) == ModBlocks.DECORATED_POT.get()) {
			world.setBlockMetadataWithNotify(hit.blockX, hit.blockY, hit.blockZ, world.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ) | 4, 2);
			world.func_147480_a(hit.blockX, hit.blockY, hit.blockZ, true);
		}
		onImpact(hit);
	}
}
