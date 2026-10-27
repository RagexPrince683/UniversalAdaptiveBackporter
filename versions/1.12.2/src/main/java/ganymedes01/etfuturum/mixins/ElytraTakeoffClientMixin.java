package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityPlayerSP.class,remap=false)
public abstract class ElytraTakeoffClientMixin {
 @Unique private boolean uab$lastJump,uab$wasGround;
 @Inject(method={"onLivingUpdate","func_70636_d"},at=@At("HEAD"),remap=false)
 private void uab$ground(CallbackInfo ci){uab$wasGround=((EntityPlayerSP)(Object)this).onGround;}
 @Inject(method={"onLivingUpdate","func_70636_d"},at=@At("TAIL"),remap=false)
 private void uab$takeoff(CallbackInfo ci){EntityPlayerSP player=(EntityPlayerSP)(Object)this;boolean jump=player.movementInput.jump;if(jump&&!uab$lastJump&&!uab$wasGround&&!player.onGround&&player.motionY>=0&&!player.isElytraFlying()&&!player.isInWater()&&!player.capabilities.isFlying&&Features.enabled("mixins.enableNewElytraTakeoffLogic")){ItemStack chest=player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);if(chest.getItem()==Items.ELYTRA&&ItemElytra.isUsable(chest))player.connection.sendPacket(new CPacketEntityAction(player,CPacketEntityAction.Action.START_FALL_FLYING));}uab$lastJump=jump;}
}
