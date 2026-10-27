package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.ModernStructures;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ChunkGeneratorOverworld.class,remap=false)
public abstract class OverworldStructuresMixin {
 @Shadow(aliases="field_185995_n",remap=false) @Final private World world;
 @Inject(method={"generateChunk","func_185932_a"},at=@At("RETURN"),remap=false)
 private void etfu$prepare(int cx,int cz,CallbackInfoReturnable<Chunk> callback){ModernStructures.prepare(world,cx,cz);}
}
