package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.entities.EntityAxolotl;
import ganymedes01.etfuturum.entities.EntityFish;
import ganymedes01.etfuturum.entities.EntityTadpole;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Preserve vanilla fluid currents, entry effects and fire clearing with a non-inverted query box. */
@Mixin(Entity.class)
public abstract class MixinSmallAquaticWater {
    @ModifyArg(method = "handleWaterMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;handleMaterialAcceleration(Lnet/minecraft/util/AxisAlignedBB;Lnet/minecraft/block/material/Material;Lnet/minecraft/entity/Entity;)Z"), index = 0)
    private AxisAlignedBB uab$smallWaterBox(AxisAlignedBB vanilla) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof EntityFish || entity instanceof EntityAxolotl || entity instanceof EntityTadpole)
            return entity.boundingBox.contract(.001, .001, .001);
        return vanilla;
    }
}
