package ganymedes01.etfuturum.mixins.late.undergroundbiomes;

import ganymedes01.etfuturum.compat.CompatUndergroundBiomes.WrappedProvider;
import net.minecraft.world.chunk.IChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "exterminatorJeff.undergroundBiomes.worldGen.ChunkProviderWrapper", remap = false)
public abstract class MixinChunkProviderWrapper implements WrappedProvider {
	@Shadow public abstract IChunkProvider wrappee();

	@Override
	public IChunkProvider uab$terrainProvider() {
		return wrappee();
	}
}
