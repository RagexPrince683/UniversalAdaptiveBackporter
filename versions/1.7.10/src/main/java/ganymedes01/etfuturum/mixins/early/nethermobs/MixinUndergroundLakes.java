package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.structure.underground.UndergroundStructures;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;
@Mixin(WorldGenLakes.class)
public class MixinUndergroundLakes {
	// The arguments now contain the native ground search's normalized 16x8x16 origin.
	@Inject(method = "generate", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0), cancellable = true)
	private void etfu$reservation(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		if (UndergroundStructures.reserved(world, new StructureBoundingBox(x, y, z, x + 15, y + 7, z + 15))) cir.setReturnValue(false);
	}
}
