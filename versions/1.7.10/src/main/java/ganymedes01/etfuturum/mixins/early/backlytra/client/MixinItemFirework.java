package ganymedes01.etfuturum.mixins.early.backlytra.client;

import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import ganymedes01.etfuturum.elytra.IElytraPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A sent block-use packet already owns the boost; prevent the client's second air-use fallback. */
@Mixin(ItemFirework.class)
public class MixinItemFirework {
    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private void predictBoost(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                              int side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> callback) {
        if (world.isRemote && stack.stackSize > 0 && player instanceof IElytraPlayer
                && ((IElytraPlayer) player).etfu$isElytraFlying() && player.isEntityAlive()
                && !player.isRiding() && !SpectatorUtils.isSpectator(player)) callback.setReturnValue(true);
    }
}
