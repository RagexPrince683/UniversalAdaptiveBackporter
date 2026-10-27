package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.entities.EntityNewBoatWithChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=NetHandlerPlayServer.class,remap=false)
public abstract class ChestBoatInventoryMixin {
 @Inject(method={"processEntityAction","func_147357_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$open(CPacketEntityAction packet,CallbackInfo callback){NetHandlerPlayServer handler=(NetHandlerPlayServer)(Object)this;EntityPlayerMP player=handler.player;if(packet.getAction()!=CPacketEntityAction.Action.OPEN_INVENTORY)return;PacketThreadUtil.checkThreadAndEnqueue(packet,handler,player.getServerWorld());Entity vehicle=player.getRidingEntity();if(!(vehicle instanceof EntityNewBoatWithChest))return;EntityNewBoatWithChest boat=(EntityNewBoatWithChest)vehicle;if(boat.isUsableByPlayer(player))player.displayGUIChest(boat);callback.cancel();}
}
