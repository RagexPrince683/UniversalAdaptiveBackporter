package ganymedes01.etfuturum;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;

final class TurtleHelmet extends ItemArmor {
    private static final ArmorMaterial SHELL=EnumHelper.addArmorMaterial("EFR_TURTLE","etfuturum:turtle",25,new int[]{2,5,6,2},9,SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,0);
    TurtleHelmet(){super(SHELL,0,EntityEquipmentSlot.HEAD);setMaxDamage(275);}
    @Override public String getArmorTexture(ItemStack stack,Entity entity,EntityEquipmentSlot slot,String type){return "etfuturum:textures/models/armor/turtle_layer_1.png";}
    @Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return ingredient.getItem()==Content.item("turtle_scute")||super.getIsRepairable(stack,ingredient);}
    @Override public void onArmorTick(World world,EntityPlayer player,ItemStack stack){if(!world.isRemote&&!player.isInsideOfMaterial(Material.WATER)&&!player.isPotionActive(MobEffects.WATER_BREATHING))player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING,200));}
}
