package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.entities.ai.FoodEffectControl;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemFood;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.*;
@Mixin(value=ItemFood.class,remap=false)
public class FoodEffectMixin implements FoodEffectControl {
    @Shadow(aliases="field_77851_ca") private PotionEffect potionId;
    @Shadow(aliases="field_77858_cd") private float potionEffectProbability;
    @Override public void etfu$applyFoodEffect(EntityLivingBase consumer){if(!consumer.world.isRemote&&potionId!=null&&consumer.getRNG().nextFloat()<potionEffectProbability)consumer.addPotionEffect(new PotionEffect(potionId));}
}
