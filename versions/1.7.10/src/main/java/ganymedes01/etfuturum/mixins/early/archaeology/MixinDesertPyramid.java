package ganymedes01.etfuturum.mixins.early.archaeology;

import ganymedes01.etfuturum.world.structure.ArchaeologySites;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;

@Mixin(ComponentScatteredFeaturePieces.DesertPyramid.class)
public abstract class MixinDesertPyramid extends StructureComponent {
	@Inject(method = "addComponentParts", at = @At("RETURN"))
	private void etfu$excavation(World world, Random random, StructureBoundingBox clip, CallbackInfoReturnable<Boolean> result) {
		if (result.getReturnValue()) ArchaeologySites.pyramid(world, getBoundingBox(), clip);
	}
}
