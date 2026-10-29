package ganymedes01.etfuturum.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.combat.MaceRules;
import ganymedes01.etfuturum.combat.MaceCombat;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.world.World;

public final class ItemMace extends Item {
    public ItemMace() { setMaxStackSize(1); setMaxDamage(MaceRules.DURABILITY); setUnlocalizedName("etfuturum.mace"); setTextureName("mace"); setCreativeTab(EtFuturum.creativeTabItems); }
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) { return stack; }
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.none; }
    @Override public int getItemEnchantability() { return 15; }
    @Override public boolean getIsRepairable(ItemStack stack, ItemStack ingredient) { return ingredient.getItem() == ModItems.BREEZE_ROD.get(); }
    @Override public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) { MaceCombat.impact(attacker, target); stack.damageItem(1, attacker); return true; }
    @Override public boolean isFull3D() { return true; }
    @Override public boolean onBlockDestroyed(ItemStack stack, World world, net.minecraft.block.Block block, int x, int y, int z, EntityLivingBase entity) { if (block.getBlockHardness(world,x,y,z) != 0) stack.damageItem(2,entity); return true; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        for (Object key : EnchantmentHelper.getEnchantments(book).keySet()) { int id=(Integer)key; if(id<0||id>=Enchantment.enchantmentsList.length||!MaceCombat.allowed(Enchantment.enchantmentsList[id]))return false; }
        return true;
    }
    @Override public Multimap getItemAttributeModifiers() {
        Multimap attributes = HashMultimap.create();
        attributes.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", 5, 0));
        return attributes;
    }
}
