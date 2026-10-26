package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.structure.underground.UndergroundStructures;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockTorch;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BlockChest.class, BlockTorch.class})
public class MixinUndergroundOrientation {
	@Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
	private void etfu$templateOrientation(World world, int x, int y, int z, CallbackInfo ci) {
		// A neighboring chunk may not contain its template wall/chest yet. The template owns facing.
		if (UndergroundStructures.placingTemplate() || ganymedes01.etfuturum.world.structure.surface.SurfaceStructures.placingTemplate()) ci.cancel();
	}
}
