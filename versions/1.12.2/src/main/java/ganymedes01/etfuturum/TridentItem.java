package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.EntityTrident;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

final class TridentItem extends Item {
    TridentItem() { setMaxStackSize(1); setMaxDamage(250); }
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.BOW; }
    @Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }
    @Override public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItemDamage() < stack.getMaxDamage() - 1) player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
    @Override public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase user, int remaining) {
        if (world.isRemote || !(user instanceof EntityPlayer) || getMaxItemUseDuration(stack) - remaining < 10 || stack.getItemDamage() >= stack.getMaxDamage() - 1) return;
        EntityPlayer player = (EntityPlayer) user;
        int level = ModEnchantments.level(ModEnchantments.riptide, stack);
        if (level > 0) {
            if (!player.isWet()) return;
            Vec3d look = player.getLookVec(); float speed = .6F + .6F * level;
            player.addVelocity(look.x * speed, look.y * speed + (player.onGround ? .4 : 0), look.z * speed); player.velocityChanged = true; stack.damageItem(1, player); return;
        }
        ItemStack thrown = stack.copy(); thrown.setCount(1);
        if (!player.capabilities.isCreativeMode) thrown.damageItem(1, player);
        EntityTrident projectile = new EntityTrident(world, player, player.capabilities.isCreativeMode ? null : thrown);
        projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, 2.5F, 1);
        if (world.spawnEntity(projectile) && !player.capabilities.isCreativeMode) stack.shrink(1);
    }
    @Override public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) { stack.damageItem(1, attacker); return true; }
    @Override public int getItemEnchantability() { return 1; }
    @Override public com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot, ItemStack stack) {
        com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> attributes = super.getAttributeModifiers(slot, stack);
        if (slot == net.minecraft.inventory.EntityEquipmentSlot.MAINHAND) attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new net.minecraft.entity.ai.attributes.AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 8, 0));
        return attributes;
    }
}
