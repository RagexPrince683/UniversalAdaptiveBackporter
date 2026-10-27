package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.LegacySpawns;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(value=EntityShulker.class,remap=false)
public abstract class NaturalShulkerMixin extends EntityGolem {
 protected NaturalShulkerMixin(World world){super(world);}
 @Override protected boolean canDespawn(){return getEntityData().getBoolean(LegacySpawns.NATURAL_SHULKER)&&!hasCustomName()&&!isRiding();}
}
