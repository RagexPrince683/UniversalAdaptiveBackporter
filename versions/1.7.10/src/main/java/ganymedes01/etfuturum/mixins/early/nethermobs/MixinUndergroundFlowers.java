package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.structure.underground.UndergroundStructures;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;

@Mixin(WorldGenFlowers.class)
public class MixinUndergroundFlowers {
	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void etfu$reservation(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		if (UndergroundStructures.reserved(world, new StructureBoundingBox(x - 7, y - 3, z - 7, x + 7, y + 3, z + 7))) cir.setReturnValue(false);
	}
}
