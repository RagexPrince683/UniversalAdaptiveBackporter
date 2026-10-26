package ganymedes01.etfuturum;

import net.minecraft.enchantment.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = UAB.ID)
public final class ModEnchantments {
    public static final Enchantment loyalty = new TridentEnchantment("loyalty", 3);
    public static final Enchantment impaling = new TridentEnchantment("impaling", 5);
    public static final Enchantment channeling = new TridentEnchantment("channeling", 1);
    public static final Enchantment riptide = new TridentEnchantment("riptide", 3);
    public static final Enchantment quickCharge=new CrossbowEnchantment("quick_charge",3),multishot=new CrossbowEnchantment("multishot",1),piercing=new CrossbowEnchantment("piercing",4);
    public static final Enchantment swiftSneak=new Enchantment(Enchantment.Rarity.VERY_RARE,EnumEnchantmentType.ARMOR_LEGS,new EntityEquipmentSlot[]{EntityEquipmentSlot.LEGS}){
        {setRegistryName(UAB.ID,"swift_sneak");setName("swift_sneak");}
        @Override public int getMaxLevel(){return 3;}
        @Override public int getMinEnchantability(int level){return level*25;}
        @Override public int getMaxEnchantability(int level){return getMinEnchantability(level)+50;}
        @Override public boolean isTreasureEnchantment(){return true;}
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack){return Features.enabled("enchantspotions.swiftSneak")&&stack.getItem()==net.minecraft.init.Items.BOOK;}
        @Override public boolean isAllowedOnBooks(){return Features.enabled("enchantspotions.swiftSneak");}
    };
    public static final Enchantment soulSpeed=new Enchantment(Enchantment.Rarity.VERY_RARE,EnumEnchantmentType.ARMOR_FEET,new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET}){
        {setRegistryName(UAB.ID,"soul_speed");setName("soul_speed");}
        @Override public int getMaxLevel(){return 3;}
        @Override public int getMinEnchantability(int level){return level*10;}
        @Override public int getMaxEnchantability(int level){return getMinEnchantability(level)+15;}
        @Override public boolean isTreasureEnchantment(){return true;}
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack){return false;}
        @Override public boolean isAllowedOnBooks(){return Features.enabled("enchantspotions.soulSpeed");}
    };
    private ModEnchantments() { }
    public static boolean available(Enchantment enchantment){if(enchantment==soulSpeed)return Features.enabled("enchantspotions.soulSpeed");if(enchantment==swiftSneak)return Features.enabled("enchantspotions.swiftSneak");if(enchantment==quickCharge||enchantment==multishot||enchantment==piercing)return Content.available("crossbow");if(enchantment==loyalty||enchantment==impaling||enchantment==channeling||enchantment==riptide)return Content.available("trident");return true;}
    public static int level(Enchantment enchantment, ItemStack stack) { return stack == null || stack.isEmpty() ? 0 : EnchantmentHelper.getEnchantmentLevel(enchantment, stack); }
    @SubscribeEvent public static void register(RegistryEvent.Register<Enchantment> event) { Features.enabled("enchantspotions.soulSpeed");event.getRegistry().registerAll(loyalty, impaling, channeling, riptide,quickCharge,multishot,piercing,soulSpeed,swiftSneak); }
    private static final class CrossbowEnchantment extends Enchantment {
        final int maximum;
        CrossbowEnchantment(String name,int maximum){super(Rarity.RARE,EnumEnchantmentType.BOW,new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});this.maximum=maximum;setRegistryName(UAB.ID,name);setName(name);}
        @Override public int getMaxLevel(){return maximum;}
        @Override public int getMinEnchantability(int level){return 12+(level-1)*20;}
        @Override public int getMaxEnchantability(int level){return getMinEnchantability(level)+30;}
        @Override public boolean canApply(ItemStack stack){return stack.getItem() instanceof CrossbowItem;}
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack){return canApply(stack)&&Content.available("crossbow");}
        @Override public boolean isAllowedOnBooks(){return Content.available("crossbow");}
        @Override protected boolean canApplyTogether(Enchantment other){return !((this==multishot&&other==piercing)||(this==piercing&&other==multishot))&&super.canApplyTogether(other);}
    }
    private static final class TridentEnchantment extends Enchantment {
        private final int maximum;
        TridentEnchantment(String name, int maximum) { super(Rarity.RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}); this.maximum = maximum; setRegistryName(UAB.ID, name); setName(name); }
        @Override public int getMaxLevel() { return maximum; }
        @Override public int getMinEnchantability(int level) { return 10 + (level - 1) * 7; }
        @Override public int getMaxEnchantability(int level) { return getMinEnchantability(level) + 30; }
        @Override public boolean canApply(ItemStack stack) { return stack.getItem() == Content.item("trident"); }
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return canApply(stack) && Content.available("trident"); }
        @Override public boolean isAllowedOnBooks(){return Content.available("trident");}
        @Override protected boolean canApplyTogether(Enchantment other) {
            if ((this == riptide && (other == loyalty || other == channeling)) || (other == riptide && (this == loyalty || this == channeling))) return false;
            return super.canApplyTogether(other);
        }
    }
}
