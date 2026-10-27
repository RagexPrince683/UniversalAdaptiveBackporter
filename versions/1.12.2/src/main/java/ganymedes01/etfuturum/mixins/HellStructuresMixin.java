package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.ModernStructures;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGeneratorHell;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ChunkGeneratorHell.class,remap=false)
public abstract class HellStructuresMixin {
 @Shadow(aliases="field_185952_n",remap=false) @Final private World world;
 @Inject(method={"generateChunk","func_185932_a"},at=@At("RETURN"),remap=false)
 private void etfu$prepare(int cx,int cz,CallbackInfoReturnable<Chunk> callback){ModernStructures.prepare(world,cx,cz);}
}
