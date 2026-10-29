package ganymedes01.etfuturum.items;

import java.util.*;
import cpw.mods.fml.common.eventhandler.*;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.mixins.early.shield.ShieldUseAccess;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.oredict.OreDictionary;

/** One-handed native blocking; the server owns damage, wear and player-wide axe cooldowns. */
public final class ItemShield extends BaseItem {
    private static final String DISABLED="UabShieldDisabledUntil";
    private final Map<EntityPlayer,Long> disabled=new WeakHashMap<>();
    public ItemShield(){super("shield");setMaxStackSize(1);setMaxDamage(336);MinecraftForge.EVENT_BUS.register(this);}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 72000;}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.block;}
    private long until(EntityPlayer player){Long end=disabled.get(player);long now=player.worldObj.getTotalWorldTime();if(end==null)return 0;if(end<=now||end-now>100){disabled.remove(player);return 0;}return end;}
    private static boolean tagged(ItemStack stack,World world){long end=stack.hasTagCompound()?stack.getTagCompound().getLong(DISABLED):0;return end>world.getTotalWorldTime()&&end-world.getTotalWorldTime()<=100;}
    private static void synchronize(ItemStack stack,EntityPlayer player,long end){
        NBTTagCompound tag=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();if(tag.getLong(DISABLED)==end)return;
        if(end==0)tag.removeTag(DISABLED);else tag.setLong(DISABLED,end);stack.setTagCompound(tag.hasNoTags()?null:tag);player.inventory.markDirty();
    }
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){
        if(!ContentAvailability.enabled("shields"))return stack;
        if(world.isRemote){if(!tagged(stack,world))player.setItemInUse(stack,getMaxItemUseDuration(stack));}
        else{long end=until(player);synchronize(stack,player,end);if(end==0)player.setItemInUse(stack,getMaxItemUseDuration(stack));else player.clearItemInUse();}return stack;
    }
    @Override public void onUsingTick(ItemStack stack,EntityPlayer player,int remaining){if(!ContentAvailability.enabled("shields")||(player.worldObj.isRemote?tagged(stack,player.worldObj):until(player)>0))player.clearItemInUse();}
    @Override public void onUpdate(ItemStack stack,World world,Entity entity,int slot,boolean selected){if(!world.isRemote&&entity instanceof EntityPlayer)synchronize(stack,(EntityPlayer)entity,until((EntityPlayer)entity));}
    @Override public boolean getIsRepairable(ItemStack shield,ItemStack ingredient){for(int id:OreDictionary.getOreIDs(ingredient))if(OreDictionary.getOreName(id).equals("plankWood"))return true;return false;}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void block(LivingAttackEvent event){
        if(event.entityLiving.worldObj.isRemote||!(event.entityLiving instanceof EntityPlayer)||!ContentAvailability.enabled("shields")||event.ammount<=0||Float.isNaN(event.ammount)||Float.isInfinite(event.ammount)||event.source.isUnblockable())return;
        EntityPlayer player=(EntityPlayer)event.entityLiving;ShieldUseAccess use=(ShieldUseAccess)player;ItemStack shield=use.uab$activeItem();
        if(shield==null||shield.getItem()!=this||player.getCurrentEquippedItem()!=shield||getMaxItemUseDuration(shield)-use.uab$useRemaining()<5||until(player)>0)return;
        Entity source=event.source.getSourceOfDamage();if(source==null||source==player)return;
        double x=source.posX-player.posX,z=source.posZ-player.posZ;
        if(x*x+z*z<1E-8){x=-source.motionX;z=-source.motionZ;}
        double yaw=Math.toRadians(player.rotationYaw);if(x*-Math.sin(yaw)+z*Math.cos(yaw)<=0)return;
        event.setCanceled(true);
        if(event.ammount>=3)shield.damageItem((int)Math.min(32767,1D+Math.floor(event.ammount)),player);
        player.worldObj.playSoundAtEntity(player,"minecraft_1.21.11:item.shield.block",1,.8F+player.getRNG().nextFloat()*.4F);
        Entity attacker=event.source.getEntity();
        if(!event.source.isProjectile()&&attacker instanceof EntityLivingBase){EntityLivingBase living=(EntityLivingBase)attacker;living.knockBack(player,.5F,player.posX-living.posX,player.posZ-living.posZ);living.velocityChanged=true;
            ItemStack weapon=living.getHeldItem();if(weapon!=null&&weapon.getItem() instanceof ItemAxe){float chance=.25F+.05F*EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId,weapon)+(living.isSprinting()?.75F:0);if(player.getRNG().nextFloat()<chance){long end=player.worldObj.getTotalWorldTime()+100;disabled.put(player,end);synchronize(shield,player,end);player.clearItemInUse();}}
        }
        if(shield.stackSize<=0)player.clearItemInUse();
    }
}
