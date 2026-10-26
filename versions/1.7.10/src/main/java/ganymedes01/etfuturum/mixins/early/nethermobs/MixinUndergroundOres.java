package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.structure.underground.UndergroundStructures;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;

@Mixin(WorldGenMinable.class)
public class MixinUndergroundOres {
	@Shadow public int numberOfBlocks;
	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void etfu$reservation(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		if (UndergroundStructures.oreReserved(world, x, y, z, numberOfBlocks)) cir.setReturnValue(false);
	}
}
