package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.entities.EntityStrider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Rider input remains native; this server-owned vehicle never accepts client coordinates. */
@Mixin(value=NetHandlerPlayServer.class,remap=false)
public class StriderRidingMixin {
    @Inject(method={"processVehicleMove","func_184338_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private void etfu$movement(CPacketVehicleMove packet,CallbackInfo ci){EntityPlayerMP player=((NetHandlerPlayServer)(Object)this).player;if(!player.getServerWorld().isCallingFromMinecraftThread())return;Entity vehicle=player.getLowestRidingEntity();if(vehicle instanceof EntityStrider&&vehicle.getControllingPassenger()==player){player.connection.sendPacket(new SPacketMoveVehicle(vehicle));ci.cancel();}}
}
