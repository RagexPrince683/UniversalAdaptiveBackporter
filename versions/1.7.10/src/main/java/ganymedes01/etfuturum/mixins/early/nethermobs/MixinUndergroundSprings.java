package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.structure.underground.UndergroundStructures;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;

@Mixin(WorldGenLiquids.class)
public class MixinUndergroundSprings {
	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void etfu$reservation(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		// Immediate vanilla spring flow can reach a room through a neighboring, unpopulated cave.
		if (UndergroundStructures.reserved(world, new StructureBoundingBox(x - 8, 6, z - 8, x + 8, y + 1, z + 8))) cir.setReturnValue(false);
	}
}
