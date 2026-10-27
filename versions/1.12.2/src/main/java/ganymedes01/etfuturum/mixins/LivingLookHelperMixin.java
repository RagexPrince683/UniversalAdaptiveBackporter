package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.entities.ai.LookHelperControl;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityLookHelper;
import org.spongepowered.asm.mixin.*;
@Mixin(value=EntityLiving.class,remap=false)
public class LivingLookHelperMixin implements LookHelperControl {
    @Shadow(aliases="field_70749_g") @Final @Mutable private EntityLookHelper lookHelper;
    @Override public void etfu$setLookHelper(EntityLookHelper helper){lookHelper=java.util.Objects.requireNonNull(helper);}
}
