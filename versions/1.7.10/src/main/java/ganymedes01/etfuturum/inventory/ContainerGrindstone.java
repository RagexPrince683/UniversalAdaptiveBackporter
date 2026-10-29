package ganymedes01.etfuturum.inventory;

import java.util.*;
import ganymedes01.etfuturum.ModernAdditions;
import net.minecraft.enchantment.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.world.World;

public final class ContainerGrindstone extends ContainerSelectableWorkstation {
    private int experience;
    public ContainerGrindstone(InventoryPlayer inventory,World world,int x,int y,int z){super(inventory,world,x,y,z,ModernAdditions.block("grindstone"),2);}
    @Override protected boolean accepts(int slot,ItemStack stack){return stack!=null&&(stack.isItemStackDamageable()||stack.isItemEnchanted()||stack.getItem()==Items.enchanted_book);}
    @Override public String title(){return "tile.etfuturum.grindstone.name";}
    @Override public List<String> choices(){return Collections.emptyList();}
    @Override protected void refresh(){
        experience=0;ItemStack a=inputs.getStackInSlot(0),b=inputs.getStackInSlot(1),result=null;
        if(a!=null||b!=null){ItemStack source=a==null?b:a;
            if(accepts(0,source)&&(a==null||b==null||a.getItem()==b.getItem()&&(a.isItemStackDamageable()||simpleBook(a)&&simpleBook(b)))){
                result=source.copy();result.stackSize=a!=null&&b!=null&&!source.isItemStackDamageable()?2:1;
                if(a!=null&&b!=null&&source.isItemStackDamageable())result.setItemDamage(Math.max(0,source.getMaxDamage()-(a.getMaxDamage()-a.getItemDamage()+b.getMaxDamage()-b.getItemDamage()+source.getMaxDamage()*5/100)));
                Map<Integer,Integer> curses=new LinkedHashMap<>();
                for(ItemStack input:new ItemStack[]{a,b})if(input!=null){Map<Integer,Integer> entries=EnchantmentHelper.getEnchantments(input);for(Map.Entry<Integer,Integer> entry:entries.entrySet()){
                    int id=entry.getKey();Enchantment enchantment=id>=0&&id<Enchantment.enchantmentsList.length?Enchantment.enchantmentsList[id]:null;
                    // Preserve unknown external enchantments, and legacy mods' named curses.
                    if(enchantment==null||enchantment.getName().toLowerCase(Locale.ROOT).contains("curse"))curses.merge(id,entry.getValue(),Math::max);
                    else experience+=enchantment.getMinEnchantability(entry.getValue());
                }}
                if(result.hasTagCompound()){result.getTagCompound().removeTag("ench");result.getTagCompound().removeTag("StoredEnchantments");}
                if(result.getItem()==Items.enchanted_book&&curses.isEmpty()){ItemStack book=new ItemStack(Items.book,result.stackSize);if(result.hasTagCompound())book.setTagCompound((net.minecraft.nbt.NBTTagCompound)result.getTagCompound().copy());result=book;}
                if(!curses.isEmpty()){net.minecraft.nbt.NBTTagCompound tag=result.hasTagCompound()?result.getTagCompound():new net.minecraft.nbt.NBTTagCompound();net.minecraft.nbt.NBTTagList list=new net.minecraft.nbt.NBTTagList();for(Map.Entry<Integer,Integer> entry:curses.entrySet()){net.minecraft.nbt.NBTTagCompound stored=new net.minecraft.nbt.NBTTagCompound();stored.setShort("id",entry.getKey().shortValue());stored.setShort("lvl",entry.getValue().shortValue());list.appendTag(stored);}tag.setTag(result.getItem()==Items.enchanted_book?"StoredEnchantments":"ench",list);result.setTagCompound(tag);}result.setRepairCost(0);
            }
        }
        output.setInventorySlotContents(0,result);
    }
    private static boolean simpleBook(ItemStack stack){if(stack.getItem()!=Items.enchanted_book)return false;Map<Integer,Integer> entries=EnchantmentHelper.getEnchantments(stack);for(int id:entries.keySet()){Enchantment enchantment=id>=0&&id<Enchantment.enchantmentsList.length?Enchantment.enchantmentsList[id]:null;if(enchantment==null||enchantment.getName().toLowerCase(Locale.ROOT).contains("curse"))return false;}return true;}
    @Override protected ItemStack craft(EntityPlayer player){ItemStack result=output.getStackInSlot(0).copy();int reward=experience;if(reward>0)player.addExperience((reward+1)/2+world.rand.nextInt((reward+1)/2));for(int i=0;i<2;i++)inputs.decrStackSize(i,1);world.playSoundAtEntity(player,"random.anvil_use",.5F,1.5F);return result;}
}
