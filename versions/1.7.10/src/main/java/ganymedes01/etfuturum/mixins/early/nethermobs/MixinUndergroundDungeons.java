package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.structure.underground.UndergroundStructures;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;
@Mixin(WorldGenDungeons.class)
public class MixinUndergroundDungeons {
	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void etfu$reservation(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		if (UndergroundStructures.reserved(world, new StructureBoundingBox(x - 4, y - 1, z - 4, x + 4, y + 4, z + 4))) cir.setReturnValue(false);
	}
}
