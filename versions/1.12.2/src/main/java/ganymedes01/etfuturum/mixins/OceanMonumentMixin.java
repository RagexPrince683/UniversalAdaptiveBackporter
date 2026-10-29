package ganymedes01.etfuturum.mixins;

import java.util.List;
import ganymedes01.etfuturum.OceanBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Preserve native monument admission when the native ocean layer selects a new climate. */
@Mixin(value=StructureOceanMonument.class,remap=false)
public class OceanMonumentMixin {
    @Group(name="etfu$oceanLists",min=1,max=2)
    @ModifyArg(method={"canSpawnStructureAtCoords","func_75047_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/biome/BiomeProvider;func_76940_a(IIILjava/util/List;)Z"),index=3,require=0,expect=0,remap=false)
    private List<Biome> etfu$runtime(List<Biome> biomes) { return OceanBiomes.monumentBiomes(biomes); }
    @Group(name="etfu$oceanLists",min=1,max=2)
    @ModifyArg(method={"canSpawnStructureAtCoords","func_75047_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/biome/BiomeProvider;areBiomesViable(IIILjava/util/List;)Z"),index=3,require=0,expect=0,remap=false)
    private List<Biome> etfu$development(List<Biome> biomes) { return OceanBiomes.monumentBiomes(biomes); }
}
