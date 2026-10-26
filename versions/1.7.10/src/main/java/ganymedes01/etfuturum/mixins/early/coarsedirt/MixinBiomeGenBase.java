package ganymedes01.etfuturum.mixins.early.coarsedirt;

import ganymedes01.etfuturum.world.CoarseDirtGeneration;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(BiomeGenBase.class)
public class MixinBiomeGenBase {
	@Inject(method = "genBiomeTerrain", at = @At("RETURN"))
	private void etfu$coarseDirt(World world, Random random, Block[] blocks, byte[] metadata, int x, int z, double noise, CallbackInfo ci) {
		CoarseDirtGeneration.replaceColumn(blocks, metadata, x, z);
	}
}
