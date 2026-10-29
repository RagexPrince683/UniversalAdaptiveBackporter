package ganymedes01.etfuturum;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.world.World;
public final class TargetCompass extends Item {
 public final boolean recovery;TargetCompass(boolean recovery){this.recovery=recovery;setMaxStackSize(64);}
 @Override public void onUpdate(ItemStack stack,World world,Entity entity,int slot,boolean held){NavigationItems.update(stack,world,entity,recovery);}
 @Override public EnumRarity getRarity(ItemStack stack){return recovery?EnumRarity.UNCOMMON:EnumRarity.COMMON;}
 @Override public boolean hasEffect(ItemStack stack){return stack.isItemEnchanted()||!recovery&&stack.hasTagCompound()&&NavigationItems.valid(stack.getTagCompound().getCompoundTag(NavigationItems.TARGET));}
}
