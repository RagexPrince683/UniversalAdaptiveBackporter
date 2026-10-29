package ganymedes01.etfuturum.mixins;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import ganymedes01.etfuturum.RespawnAnchors;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** A valid anchor affects only this native death respawn; provider instances stay authoritative elsewhere. */
@Mixin(value=PlayerList.class,remap=false)
public final class AnchorRespawnMixin {
    @WrapMethod(method={"recreatePlayerEntity","func_72368_a"},remap=false)
    private EntityPlayerMP uab$respawn(EntityPlayerMP old,int dimension,boolean end,Operation<EntityPlayerMP> original){RespawnAnchors.Context selected=RespawnAnchors.begin(old,end),previous=RespawnAnchors.push(selected);try{EntityPlayerMP result=original.call(old,selected==null?dimension:selected.dimension,end);RespawnAnchors.finish(selected,result);return result;}finally{RespawnAnchors.push(previous);}}
    // Only one naming variant exists at runtime; the group requires a match, even with injection counting enabled.
    @Group(name="anchorProvider",min=1) @WrapOperation(method={"recreatePlayerEntity","func_72368_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldProvider;canRespawnHere()Z"),require=0,expect=0,remap=false)
    private boolean uab$provider(WorldProvider provider,Operation<Boolean> original){return RespawnAnchors.allow(provider,original.call(provider));}
    @Group(name="anchorProvider",min=1) @WrapOperation(method={"recreatePlayerEntity","func_72368_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldProvider;func_76567_e()Z"),require=0,expect=0,remap=false)
    private boolean uab$providerSrg(WorldProvider provider,Operation<Boolean> original){return RespawnAnchors.allow(provider,original.call(provider));}
    @WrapOperation(method={"recreatePlayerEntity","func_72368_a"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/EntityPlayerMP;getBedLocation(I)Lnet/minecraft/util/math/BlockPos;"),remap=false)
    private BlockPos uab$exit(EntityPlayerMP player,int dimension,Operation<BlockPos> original){return RespawnAnchors.bed(player,dimension,original.call(player,dimension));}
    @WrapOperation(method={"recreatePlayerEntity","func_72368_a"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/EntityPlayerMP;isSpawnForced(I)Z"),remap=false)
    private boolean uab$forced(EntityPlayerMP player,int dimension,Operation<Boolean> original){return RespawnAnchors.forced(player,dimension,original.call(player,dimension));}
    @Group(name="anchorStoredPoint",min=1) @WrapOperation(method={"recreatePlayerEntity","func_72368_a"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/EntityPlayerMP;setSpawnPoint(Lnet/minecraft/util/math/BlockPos;Z)V"),require=0,expect=0,remap=false)
    private void uab$store(EntityPlayerMP player,BlockPos pos,boolean forced,Operation<Void> original){BlockPos target=RespawnAnchors.stored(player,pos);RespawnAnchors.SpawnRestore previous=RespawnAnchors.beforeRestore(player,target,forced);try{original.call(player,target,forced);}finally{RespawnAnchors.afterRestore(previous);}}
    @Group(name="anchorStoredPoint",min=1) @WrapOperation(method={"recreatePlayerEntity","func_72368_a"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/EntityPlayerMP;func_180473_a(Lnet/minecraft/util/math/BlockPos;Z)V"),require=0,expect=0,remap=false)
    private void uab$storeSrg(EntityPlayerMP player,BlockPos pos,boolean forced,Operation<Void> original){BlockPos target=RespawnAnchors.stored(player,pos);RespawnAnchors.SpawnRestore previous=RespawnAnchors.beforeRestore(player,target,forced);try{original.call(player,target,forced);}finally{RespawnAnchors.afterRestore(previous);}}
}
