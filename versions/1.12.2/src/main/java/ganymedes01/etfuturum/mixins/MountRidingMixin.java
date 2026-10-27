package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.entities.EntityStrider;
import ganymedes01.etfuturum.entities.EntityRideableMob;
import ganymedes01.etfuturum.MountInput;
import net.minecraft.network.play.client.CPacketInput;
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
public class MountRidingMixin {
    @Inject(method={"processVehicleMove","func_184338_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private void etfu$movement(CPacketVehicleMove packet,CallbackInfo ci){EntityPlayerMP player=((NetHandlerPlayServer)(Object)this).player;if(!player.getServerWorld().isCallingFromMinecraftThread())return;Entity vehicle=player.getLowestRidingEntity();if((vehicle instanceof EntityStrider||vehicle instanceof EntityRideableMob)&&vehicle.getControllingPassenger()==player){player.connection.sendPacket(new SPacketMoveVehicle(vehicle));ci.cancel();}}
    @Inject(method={"processInput","func_147358_a"},at=@At("RETURN"),remap=false)
    private void uab$riderInput(CPacketInput packet,CallbackInfo ci){EntityPlayerMP player=((NetHandlerPlayServer)(Object)this).player;if(player.getServerWorld().isCallingFromMinecraftThread())MountInput.record(player,packet.isJumping());}
}

