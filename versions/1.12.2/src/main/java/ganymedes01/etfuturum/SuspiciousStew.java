package ganymedes01.etfuturum;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

final class SuspiciousStew extends ItemSoup {
    SuspiciousStew(){super(6);setAlwaysEdible();}
    static ItemStack effect(Potion effect,int ticks){ItemStack stack=Content.stack("suspicious_stew");NBTTagCompound tag=new NBTTagCompound(),entry=new NBTTagCompound();NBTTagList list=new NBTTagList();entry.setInteger("EffectId",Potion.getIdFromPotion(effect));entry.setString("EffectName",effect.getRegistryName().toString());entry.setInteger("EffectDuration",ticks);list.appendTag(entry);tag.setTag("Effects",list);stack.setTagCompound(tag);return stack;}
    @Override public ItemStack onItemUseFinish(ItemStack stack,World world,EntityLivingBase living){if(!world.isRemote&&stack.hasTagCompound()){NBTTagList list=stack.getTagCompound().getTagList("Effects",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound entry=list.getCompoundTagAt(i);int id=entry.hasKey("EffectId",1)?entry.getByte("EffectId")&255:entry.getInteger("EffectId");Potion effect=entry.hasKey("EffectName",8)?Potion.REGISTRY.getObject(new ResourceLocation(entry.getString("EffectName"))):Potion.getPotionById(id);if(effect!=null)living.addPotionEffect(new PotionEffect(effect,Math.max(1,entry.hasKey("EffectDuration")?entry.getInteger("EffectDuration"):160),Math.max(0,entry.getByte("EffectLevel"))));}}return super.onItemUseFinish(stack,world,living);}
}
