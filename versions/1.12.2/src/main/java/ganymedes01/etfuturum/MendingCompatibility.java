package ganymedes01.etfuturum;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.enchantment.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;
/** Extends vanilla's own selection; vanilla retains XP cost, item repair ratios and pickup authority. */
public final class MendingCompatibility {
 private static Method inventory,changed;
 private static boolean warned;
 private MendingCompatibility(){}
 static void initialize(){if(!Loader.isModLoaded("baubles")||!Features.enabled("modcompat.baublesMending"))return;try{inventory=Class.forName("baubles.api.BaublesApi").getMethod("getBaublesHandler",EntityPlayer.class);if(!IItemHandler.class.isAssignableFrom(inventory.getReturnType()))throw new NoSuchMethodException("Baubles inventory is not an item handler");changed=inventory.getReturnType().getMethod("setChanged",int.class,boolean.class);}catch(ReflectiveOperationException|LinkageError failure){inventory=null;org.apache.logging.log4j.LogManager.getLogger("UAB").warn("Baubles Mending could not bind the installed inventory API",failure);}}
 public static Selection select(Enchantment enchantment,EntityLivingBase entity){if(inventory==null||enchantment!=Enchantments.MENDING||!(entity instanceof EntityPlayer))return null;try{Object owner=inventory.invoke(null,entity);if(!(owner instanceof IItemHandler))return null;IItemHandler handler=(IItemHandler)owner;List<Selection> choices=new ArrayList<>();for(int slot=0;slot<handler.getSlots();slot++){ItemStack stack=handler.getStackInSlot(slot);if(!stack.isEmpty()&&EnchantmentHelper.getEnchantmentLevel(enchantment,stack)>0)choices.add(new Selection(stack,owner,slot));}if(choices.isEmpty())return null;for(ItemStack stack:enchantment.getEntityEquipment(entity))if(!stack.isEmpty()&&EnchantmentHelper.getEnchantmentLevel(enchantment,stack)>0)choices.add(new Selection(stack,null,-1));return choices.get(entity.getRNG().nextInt(choices.size()));}catch(ReflectiveOperationException failure){if(!warned){warned=true;org.apache.logging.log4j.LogManager.getLogger("UAB").warn("Baubles Mending inventory lookup failed; native equipment selection remains available",failure);}return null;}}
 public static final class Selection {
  public final ItemStack stack;private final Object owner;private final int slot,previousDamage;
  Selection(ItemStack stack,Object owner,int slot){this.stack=stack;this.owner=owner;this.slot=slot;previousDamage=stack.getItemDamage();}
  // Baubles passes these same equipped references to worn-item callbacks. Mark its native sync flag after repair.
  public void finish(){if(owner!=null&&stack.getItemDamage()!=previousDamage)try{changed.invoke(owner,slot,true);}catch(ReflectiveOperationException failure){throw new IllegalStateException("Cannot synchronize repaired Baubles equipment",failure);}}
 }
}
