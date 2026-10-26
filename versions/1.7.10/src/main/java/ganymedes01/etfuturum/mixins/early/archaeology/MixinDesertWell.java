package ganymedes01.etfuturum.mixins.early.archaeology;

import ganymedes01.etfuturum.world.structure.ArchaeologySites;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;

@Mixin(WorldGenDesertWells.class)
public class MixinDesertWell {
	@Inject(method = "generate", at = @At("RETURN"))
	private void etfu$excavation(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> result) { if (result.getReturnValue()) ArchaeologySites.well(world, x, y, z); }
}
