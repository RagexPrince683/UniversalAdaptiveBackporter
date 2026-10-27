package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.ItemPhysics;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=EntityItem.class,remap=false)
public abstract class ItemPhysicsMixin extends Entity {
 @Unique private boolean uab$ownedImmunity,uab$previousImmunity;
 public ItemPhysicsMixin(World world){super(world);}
 @Inject(method={"onUpdate","func_70071_h_"},at=@At("HEAD"),remap=false)
 private void uab$physics(CallbackInfo ci){EntityItem item=(EntityItem)(Object)this;boolean immune=ItemPhysics.fireproof(item.getItem());if(immune&&!uab$ownedImmunity){uab$previousImmunity=isImmuneToFire;uab$ownedImmunity=true;isImmuneToFire=true;}else if(!immune&&uab$ownedImmunity){isImmuneToFire=uab$previousImmunity;uab$ownedImmunity=false;}ItemPhysics.floatItem(item);}
 @Inject(method={"attackEntityFrom","func_70097_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$fire(DamageSource source,float amount,CallbackInfoReturnable<Boolean> ci){if(source.isFireDamage()&&ItemPhysics.fireproof(((EntityItem)(Object)this).getItem()))ci.setReturnValue(false);}
 @Group(name="itemLava",min=1) @Redirect(method={"onUpdate","func_70071_h_"},at=@At(value="INVOKE",target="Lnet/minecraft/block/state/IBlockState;getMaterial()Lnet/minecraft/block/material/Material;"),require=0,remap=false)
 private Material uab$lava(IBlockState state){return uab$material(state);}
 @Group(name="itemLava",min=1) @Redirect(method={"onUpdate","func_70071_h_"},at=@At(value="INVOKE",target="Lnet/minecraft/block/state/IBlockState;func_185904_a()Lnet/minecraft/block/material/Material;"),require=0,remap=false)
 private Material uab$lavaSrg(IBlockState state){return uab$material(state);}
 @Unique private Material uab$material(IBlockState state){Material material=state.getMaterial();return material==Material.LAVA&&ItemPhysics.fireproof(((EntityItem)(Object)this).getItem())?Material.AIR:material;}
}
