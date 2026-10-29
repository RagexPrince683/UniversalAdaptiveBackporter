package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.enchantment.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Uses the existing atomic workstation extraction paths, including shift/hotbar/drop operations. */
public final class GrindstoneContainer extends WorkstationContainer {
    private int experience;
    public GrindstoneContainer(InventoryPlayer inventory,World world,BlockPos pos){super(inventory,world,pos,Content.block("grindstone"),2);}
    @Override protected boolean accepts(int slot,ItemStack stack){return stack.isItemStackDamageable()||stack.isItemEnchanted()||stack.getItem()==Items.ENCHANTED_BOOK;}
    @Override public String title(){return "tile.etfuturum.grindstone.name";}
    @Override public List<String> choices(){return Collections.emptyList();}
    @Override protected void refresh(){
        experience=0;ItemStack a=inputs.getStackInSlot(0),b=inputs.getStackInSlot(1),result=ItemStack.EMPTY;
        if(!a.isEmpty()||!b.isEmpty()){
            ItemStack source=a.isEmpty()?b:a;
            if(accepts(0,source)&&(a.isEmpty()||b.isEmpty()||a.getItem()==b.getItem()&&(a.isItemStackDamageable()||simpleBook(a)&&simpleBook(b)))){
                result=source.copy();result.setCount(!a.isEmpty()&&!b.isEmpty()&&!source.isItemStackDamageable()?2:1);
                if(!a.isEmpty()&&!b.isEmpty()&&source.isItemStackDamageable())result.setItemDamage(Math.max(0,source.getMaxDamage()-(a.getMaxDamage()-a.getItemDamage()+b.getMaxDamage()-b.getItemDamage()+source.getMaxDamage()*5/100)));
                Map<Enchantment,Integer> curses=new LinkedHashMap<>();List<net.minecraft.nbt.NBTTagCompound> unknown=new ArrayList<>();
                for(ItemStack input:new ItemStack[]{a,b})if(!input.isEmpty()){net.minecraft.nbt.NBTTagList list=input.getItem()==Items.ENCHANTED_BOOK?ItemEnchantedBook.getEnchantments(input):input.getEnchantmentTagList();for(int i=0;i<list.tagCount();i++){net.minecraft.nbt.NBTTagCompound entry=list.getCompoundTagAt(i);Enchantment enchantment=Enchantment.getEnchantmentByID(entry.getShort("id"));int level=entry.getShort("lvl");if(enchantment==null)unknown.add(entry.copy());else if(enchantment.isCurse())curses.merge(enchantment,level,Math::max);else experience+=enchantment.getMinEnchantability(level);}}
                if(result.hasTagCompound()){result.getTagCompound().removeTag("ench");result.getTagCompound().removeTag("StoredEnchantments");}
                if(result.getItem()==Items.ENCHANTED_BOOK&&curses.isEmpty()&&unknown.isEmpty()){ItemStack book=new ItemStack(Items.BOOK,result.getCount());if(result.hasTagCompound())book.setTagCompound(result.getTagCompound().copy());result=book;}
                EnchantmentHelper.setEnchantments(curses,result);if(!unknown.isEmpty()){net.minecraft.nbt.NBTTagCompound tag=result.hasTagCompound()?result.getTagCompound():new net.minecraft.nbt.NBTTagCompound();String key=result.getItem()==Items.ENCHANTED_BOOK?"StoredEnchantments":"ench";net.minecraft.nbt.NBTTagList list=tag.getTagList(key,10);for(net.minecraft.nbt.NBTTagCompound entry:unknown)list.appendTag(entry);tag.setTag(key,list);result.setTagCompound(tag);}result.setRepairCost(0);
            }
        }
        output.setInventorySlotContents(0,result);
    }
    private static boolean simpleBook(ItemStack stack){if(stack.getItem()!=Items.ENCHANTED_BOOK)return false;net.minecraft.nbt.NBTTagList list=ItemEnchantedBook.getEnchantments(stack);for(int i=0;i<list.tagCount();i++){Enchantment enchantment=Enchantment.getEnchantmentByID(list.getCompoundTagAt(i).getShort("id"));if(enchantment==null||enchantment.isCurse())return false;}return true;}
    @Override protected ItemStack craft(EntityPlayer player){ItemStack result=output.getStackInSlot(0).copy();int reward=experience;if(reward>0)player.addExperience((reward+1)/2+world.rand.nextInt((reward+1)/2));for(int i=0;i<2;i++)inputs.decrStackSize(i,1);world.playEvent(1042,player.getPosition(),0);return result;}
}
