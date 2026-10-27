package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.client.renderer.ArmorTrimRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=LayerArmorBase.class,remap=false)
public class ArmorTrimLayerMixin {
    @Group(name="etfu$trim",min=1)
    @Redirect(method={"renderArmorLayer","func_188361_a"},at=@At(value="INVOKE",target="Lnet/minecraft/client/model/ModelBase;func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V"),require=0,remap=false)
    private void etfu$srg(ModelBase model,Entity drawn,float swing,float amount,float age,float yaw,float pitch,float scale,EntityLivingBase entity,float limb,float limbAmount,float partial,float ticks,float headYaw,float headPitch,float size,EntityEquipmentSlot slot){etfu$draw(model,drawn,swing,amount,age,yaw,pitch,scale,entity,slot);}
    @Group(name="etfu$trim",min=1)
    @Redirect(method={"renderArmorLayer","func_188361_a"},at=@At(value="INVOKE",target="Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"),require=0,remap=false)
    private void etfu$mcp(ModelBase model,Entity drawn,float swing,float amount,float age,float yaw,float pitch,float scale,EntityLivingBase entity,float limb,float limbAmount,float partial,float ticks,float headYaw,float headPitch,float size,EntityEquipmentSlot slot){etfu$draw(model,drawn,swing,amount,age,yaw,pitch,scale,entity,slot);}
    @Unique private static void etfu$draw(ModelBase model,Entity drawn,float swing,float amount,float age,float yaw,float pitch,float scale,EntityLivingBase entity,EntityEquipmentSlot slot){model.render(drawn,swing,amount,age,yaw,pitch,scale);ArmorTrimRenderer.render(model,drawn,entity.getItemStackFromSlot(slot),swing,amount,age,yaw,pitch,scale);}
}
