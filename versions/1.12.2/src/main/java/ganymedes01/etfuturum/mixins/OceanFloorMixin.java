package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.OceanBiomes;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.lothrazar.samsocean.WorldGeneratorOcean", remap = false)
public abstract class OceanFloorMixin {
    @ModifyVariable(method = "run", at = @At("STORE"), ordinal = 0, remap = false)
    private Biome uab$ocean(Biome biome) {
        return OceanBiomes.isModernOcean(biome) ? biome.getBaseHeight() < -1.5F ? Biomes.DEEP_OCEAN : Biomes.OCEAN : biome;
    }
}
