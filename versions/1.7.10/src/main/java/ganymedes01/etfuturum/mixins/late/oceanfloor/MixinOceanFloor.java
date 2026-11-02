package ganymedes01.etfuturum.mixins.late.oceanfloor;

import ganymedes01.etfuturum.world.surface.OceanBiomeLayer;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Translate only the optional generator's vanilla identity test; terrain and world biomes are untouched. */
@Pseudo
@Mixin(targets = "com.lothrazar.samsocean.WorldGeneratorOcean", remap = false)
public abstract class MixinOceanFloor {
    @ModifyVariable(method = "run", at = @At("STORE"), ordinal = 0, remap = false)
    private BiomeGenBase uab$ocean(BiomeGenBase biome) {
        return OceanBiomeLayer.isOcean(biome) ? OceanBiomeLayer.isDeepOcean(biome) ? BiomeGenBase.deepOcean : BiomeGenBase.ocean : biome;
    }
}
