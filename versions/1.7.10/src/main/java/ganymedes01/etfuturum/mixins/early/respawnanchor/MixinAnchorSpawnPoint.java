package ganymedes01.etfuturum.mixins.early.respawnanchor;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ganymedes01.etfuturum.RespawnAnchors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import org.spongepowered.asm.mixin.Mixin;
/** Observe actual successful native spawn changes, including dimension-aware Forge setters. */
@Mixin(value=EntityPlayer.class,remap=false)
public final class MixinAnchorSpawnPoint {
    @WrapMethod(method={"setSpawnChunk(Lnet/minecraft/util/ChunkCoordinates;Z)V","func_71063_a(Lnet/minecraft/util/ChunkCoordinates;Z)V"},remap=false)
    private void uab$spawn(ChunkCoordinates pos,boolean forced,Operation<Void> original){EntityPlayer player=(EntityPlayer)(Object)this;int dimension=player.dimension;RespawnAnchors.SpawnSnapshot before=RespawnAnchors.snapshot(player,dimension);original.call(pos,forced);RespawnAnchors.changed(player,dimension,pos,forced,before);}
    @WrapMethod(method="setSpawnChunk(Lnet/minecraft/util/ChunkCoordinates;ZI)V",remap=false)
    private void uab$dimension(ChunkCoordinates pos,boolean forced,int dimension,Operation<Void> original){EntityPlayer player=(EntityPlayer)(Object)this;RespawnAnchors.SpawnSnapshot before=RespawnAnchors.snapshot(player,dimension);original.call(pos,forced,dimension);RespawnAnchors.changed(player,dimension,pos,forced,before);}
}
