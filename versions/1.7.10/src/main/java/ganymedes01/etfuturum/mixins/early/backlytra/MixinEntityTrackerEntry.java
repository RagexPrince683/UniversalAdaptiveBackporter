package ganymedes01.etfuturum.mixins.early.backlytra;

import ganymedes01.etfuturum.elytra.IElytraEntityTrackerEntry;
import ganymedes01.etfuturum.elytra.IElytraPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(EntityTrackerEntry.class)
public class MixinEntityTrackerEntry implements IElytraEntityTrackerEntry {
	@Unique
	private boolean etfu$wasSendingVelUpdates;
	@Shadow public Entity myEntity;
	@Shadow public boolean sendVelocityUpdates;

	@Inject(method = "sendLocationToAllClients", at = @At("HEAD"))
	private void etfu$elytraVelocity(List<EntityPlayer> players, CallbackInfo ci) {
		if (!(myEntity instanceof IElytraPlayer player)) return;
		if (player.etfu$isElytraFlying()) {
			if (!sendVelocityUpdates) etfu$wasSendingVelUpdates = true;
			sendVelocityUpdates = true;
		} else if (etfu$wasSendingVelUpdates) {
			sendVelocityUpdates = false;
			etfu$wasSendingVelUpdates = false;
		}
	}

	@Override
	public boolean etfu$getWasSendingVelUpdates() {
		return etfu$wasSendingVelUpdates;
	}

	@Override
	public void etfu$setWasSendingVelUpdates(boolean b) {
		etfu$wasSendingVelUpdates = b;
	}
}
