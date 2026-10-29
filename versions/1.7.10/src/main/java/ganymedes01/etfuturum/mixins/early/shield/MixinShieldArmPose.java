package ganymedes01.etfuturum.mixins.early.shield;

import ganymedes01.etfuturum.items.ItemShield;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shield-only visual arm yaw; other items and the native attack/blocking rules remain unchanged. */
@Mixin(ModelBiped.class)
public class MixinShieldArmPose {
    @Shadow public ModelRenderer bipedRightArm;
    @Shadow public int heldItemRight;
    @Inject(method="setRotationAngles",at=@At("RETURN"),require=0,expect=0)
    private void uab$shieldArm(float limbSwing,float limbAmount,float age,float yaw,float pitch,float scale,Entity entity,CallbackInfo callback){
        if(heldItemRight!=3||!(entity instanceof EntityPlayer))return;
        EntityPlayer player=(EntityPlayer)entity;ItemStack active=player.getItemInUse();
        if(player.getItemInUseCount()>0&&active!=null&&active.getItem() instanceof ItemShield)bipedRightArm.rotateAngleY=-(float)Math.PI/6;
    }
}
