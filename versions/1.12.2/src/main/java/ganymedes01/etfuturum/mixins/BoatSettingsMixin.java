package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.BoatSettings;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityBoat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
/** Retain native physics and network validation; apply the maintained configurable controls at their owners. */
@Mixin(value=EntityBoat.class,remap=false)
public abstract class BoatSettingsMixin {
 @Shadow(aliases="field_184474_h",remap=false) private float outOfControlTicks;
 @Shadow(aliases="field_184475_as",remap=false) private float deltaRotation;
 @Shadow(aliases="field_184472_g",remap=false) private float momentum;
 @Shadow(aliases="field_184465_aD",remap=false) private double waterLevel;
 @Shadow(aliases="field_184469_aF",remap=false) private EntityBoat.Status status;
 @Unique private Entity uab$passenger;@Unique private float uab$yaw,uab$previousYaw,uab$head,uab$previousHead,uab$body,uab$previousBody;
 @Inject(method={"onUpdate","func_70071_h_"},at=@At("HEAD"),remap=false)
 private void uab$keepRiders(CallbackInfo callback){if(BoatSettings.legacyBuoyancy)outOfControlTicks=-1;}
 @ModifyConstant(method={"updateFallState","func_184231_a"},constant=@Constant(floatValue=3F),remap=false)
 private float uab$fall(float value){return BoatSettings.fallBreak<=0?Float.MAX_VALUE:BoatSettings.fallBreak;}
 @Inject(method={"getBoatGlide","func_184441_l"},at=@At("RETURN"),cancellable=true,remap=false)
 private void uab$glide(CallbackInfoReturnable<Float> callback){callback.setReturnValue(Math.min(callback.getReturnValue(),BoatSettings.landGlide));}
 @Inject(method={"canFitPassenger","func_184219_q"},at=@At("RETURN"),cancellable=true,remap=false)
 private void uab$passenger(Entity passenger,CallbackInfoReturnable<Boolean> callback){EntityBoat boat=(EntityBoat)(Object)this;if(callback.getReturnValue()&&(!BoatSettings.allowed(passenger)||!BoatSettings.secondSeat&&!boat.getPassengers().isEmpty()))callback.setReturnValue(false);}
 @Inject(method={"updateMotion","func_184450_w"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$buoyancy(CallbackInfo callback){if(!BoatSettings.legacyBuoyancy||status!=EntityBoat.Status.UNDER_WATER&&status!=EntityBoat.Status.UNDER_FLOWING_WATER)return;EntityBoat boat=(EntityBoat)(Object)this;waterLevel=boat.getWaterLevelAbove();double lift=(waterLevel-boat.getEntityBoundingBox().minY)/boat.height*BoatSettings.buoyancy;momentum=.9F;boat.motionX*=momentum;boat.motionZ*=momentum;deltaRotation*=momentum;if(lift>0){boat.motionY+=lift*(.03999999910593033/.65);boat.motionY*=.75;}double surface=boat.getWaterLevelAbove()-boat.height+.101;if(boat.posY<surface-.05)boat.motionY+=.06*BoatSettings.buoyancy;callback.cancel();}
 @Inject(method={"updatePassenger","func_184232_k"},at=@At("HEAD"),remap=false)
 private void uab$rememberRotation(Entity passenger,CallbackInfo callback){if(!BoatSettings.freeRotation)return;uab$passenger=passenger;uab$yaw=passenger.rotationYaw;uab$previousYaw=passenger.prevRotationYaw;if(passenger instanceof EntityLivingBase){EntityLivingBase living=(EntityLivingBase)passenger;uab$head=living.rotationYawHead;uab$previousHead=living.prevRotationYawHead;uab$body=living.renderYawOffset;uab$previousBody=living.prevRenderYawOffset;}}
 @Inject(method={"applyYawToEntity","func_184454_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$freeLook(Entity passenger,CallbackInfo callback){if(BoatSettings.freeRotation)callback.cancel();}
 @Inject(method={"updatePassenger","func_184232_k"},at=@At("RETURN"),remap=false)
 private void uab$restoreRotation(Entity passenger,CallbackInfo callback){if(!BoatSettings.freeRotation||uab$passenger!=passenger)return;passenger.rotationYaw=uab$yaw;passenger.prevRotationYaw=uab$previousYaw;if(passenger instanceof EntityLivingBase){EntityLivingBase living=(EntityLivingBase)passenger;living.rotationYawHead=uab$head;living.prevRotationYawHead=uab$previousHead;living.renderYawOffset=uab$body;living.prevRenderYawOffset=uab$previousBody;}uab$passenger=null;}
}
