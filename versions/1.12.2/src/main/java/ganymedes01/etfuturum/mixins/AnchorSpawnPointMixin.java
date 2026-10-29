package ganymedes01.etfuturum.mixins;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ganymedes01.etfuturum.RespawnAnchors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.injection.At;
/** Observe actual successful native spawn changes, including dimension-aware Forge setters. */
@Mixin(value=EntityPlayer.class,remap=false)
public final class AnchorSpawnPointMixin {
    @WrapMethod(method={"setSpawnPoint(Lnet/minecraft/util/math/BlockPos;Z)V","func_180473_a(Lnet/minecraft/util/math/BlockPos;Z)V"},remap=false)
    private void uab$spawn(BlockPos pos,boolean forced,Operation<Void> original){EntityPlayer player=(EntityPlayer)(Object)this;int dimension=player.dimension;RespawnAnchors.SpawnSnapshot before=RespawnAnchors.snapshot(player,dimension);original.call(pos,forced);RespawnAnchors.changed(player,dimension,pos,forced,before);}
    @WrapMethod(method="setSpawnChunk(Lnet/minecraft/util/math/BlockPos;ZI)V",remap=false)
    private void uab$dimension(BlockPos pos,boolean forced,int dimension,Operation<Void> original){EntityPlayer player=(EntityPlayer)(Object)this;RespawnAnchors.SpawnSnapshot before=RespawnAnchors.snapshot(player,dimension);original.call(pos,forced,dimension);RespawnAnchors.changed(player,dimension,pos,forced,before);}
    @ModifyExpressionValue(method={"setSpawnPoint","func_180473_a"},at=@At(value="INVOKE",target="Lnet/minecraftforge/event/ForgeEventFactory;onPlayerSpawnSet(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;Z)Z"),remap=false)
    private boolean uab$permission(boolean canceled){RespawnAnchors.permission((EntityPlayer)(Object)this,canceled);return canceled;}
}
