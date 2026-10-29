package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=NetHandlerPlayServer.class,remap=false)
public abstract class ElytraTakeoffMixin {
 @Inject(method={"processEntityAction","func_147357_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$takeoff(CPacketEntityAction packet,CallbackInfo ci){if(packet.getAction()!=CPacketEntityAction.Action.START_FALL_FLYING||!Features.enabled("mixins.enableNewElytraTakeoffLogic"))return;NetHandlerPlayServer handler=(NetHandlerPlayServer)(Object)this;EntityPlayerMP player=handler.player;PacketThreadUtil.checkThreadAndEnqueue(packet,handler,player.getServerWorld());ItemStack chest=player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);if(player.isEntityAlive()&&!player.isSpectator()&&!player.isRiding()&&!player.capabilities.isFlying&&!player.onGround&&!player.isElytraFlying()&&!player.isInWater()&&chest.getItem()==Items.ELYTRA&&ItemElytra.isUsable(chest))player.setElytraFlying();else player.clearElytraFlying();ci.cancel();}
}
