package ganymedes01.etfuturum;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ganymedes01.etfuturum.combat.MaceRules;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class MaceItem extends Item {
    MaceItem() { setMaxStackSize(1); setMaxDamage(MaceRules.DURABILITY); }
    @Override public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) { return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand)); }
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.NONE; }
    @Override public int getItemEnchantability() { return 15; }
    @Override public boolean getIsRepairable(ItemStack stack, ItemStack ingredient) { return ingredient.getItem() == Content.item("breeze_rod"); }
    @Override public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) { MaceCombat.impact(attacker, target); stack.damageItem(1, attacker); return true; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { return MaceCombat.allowed(enchantment); }
    @Override public boolean onBlockDestroyed(ItemStack stack, World world, net.minecraft.block.state.IBlockState state, net.minecraft.util.math.BlockPos pos, EntityLivingBase entity) { if (state.getBlockHardness(world,pos) != 0) stack.damageItem(2,entity); return true; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        for (Enchantment enchantment : EnchantmentHelper.getEnchantments(book).keySet()) if (!MaceCombat.allowed(enchantment)) return false;
        return true;
    }
    @Override public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> attributes = HashMultimap.create();
        if (slot == EntityEquipmentSlot.MAINHAND) {
            attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5, 0));
            attributes.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3.4, 0));
        }
        return attributes;
    }
}
