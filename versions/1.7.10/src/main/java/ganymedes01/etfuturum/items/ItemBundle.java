package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.Tags;

import java.util.*;
import net.minecraft.block.Block;
import ganymedes01.etfuturum.blocks.BlockShulkerBox;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import cpw.mods.fml.relauncher.*;


/** Weighted item storage using native inventory clicks and ordinary ItemStack save/network data. */
public final class ItemBundle extends BaseItem {
    public static final String CONTENTS="UabBundleItems";
    public static final String[] COLORS={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
    public ItemBundle(String name){super(name);setMaxStackSize(1);}
    private static boolean empty(ItemStack stack){return stack==null||stack.stackSize<=0;}
    public static boolean bundle(ItemStack stack){return !empty(stack)&&stack.getItem() instanceof ItemBundle;}
    private static List<ItemStack> contents(ItemStack bundle){List<ItemStack> result=new ArrayList<>();if(!bundle.hasTagCompound())return result;NBTTagList list=bundle.getTagCompound().getTagList(CONTENTS,10);for(int i=0;i<Math.min(128,list.tagCount());i++){ItemStack stack=ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));if(!empty(stack))result.add(stack);}return result;}
    private static void store(ItemStack bundle,List<ItemStack> items){NBTTagCompound tag=bundle.hasTagCompound()?bundle.getTagCompound():new NBTTagCompound();if(items.isEmpty())tag.removeTag(CONTENTS);else{NBTTagList list=new NBTTagList();for(ItemStack stack:items)if(!empty(stack))list.appendTag(stack.writeToNBT(new NBTTagCompound()));tag.setTag(CONTENTS,list);}bundle.setTagCompound(tag.hasNoTags()?null:tag);}
    public static int weight(ItemStack bundle){return weight(bundle,0);}
    private static int weight(ItemStack bundle,int depth){if(depth>32)return 64;long result=0;for(ItemStack stack:contents(bundle)){result+=(long)unit(stack,depth+1)*stack.stackSize;if(result>=64)return 64;}return (int)result;}
    private static int unit(ItemStack stack,int depth){return bundle(stack)?4+weight(stack,depth):Math.max(1,64/Math.max(1,Math.min(64,stack.getMaxStackSize())));}
    private static boolean allowed(ItemStack stack){return !empty(stack)&&!(stack.getItem() instanceof ItemBlock&&Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox);}
    public static int room(ItemStack bundle,ItemStack source){return allowed(source)?Math.max(0,64-weight(bundle))/unit(source,0):0;}
    public static int insert(ItemStack bundle,ItemStack source,int maximum){if(bundle==source||!allowed(source))return 0;int amount=Math.min(Math.min(source.stackSize,maximum),room(bundle,source));if(amount<=0)return 0;List<ItemStack> items=contents(bundle);int left=amount;for(ItemStack stored:items)if(stored.isItemEqual(source)&&ItemStack.areItemStackTagsEqual(stored,source)){int moved=Math.min(left,Math.max(0,stored.getMaxStackSize()-stored.stackSize));stored.stackSize+=moved;left-=moved;if(left==0)break;}while(left>0){ItemStack part=source.copy();part.stackSize=Math.min(left,Math.max(1,Math.min(64,source.getMaxStackSize())));items.add(part);left-=part.stackSize;}store(bundle,items);return amount;}
    private static ItemStack peek(ItemStack bundle){List<ItemStack> items=contents(bundle);return items.isEmpty()?null:items.get(items.size()-1).copy();}
    private static ItemStack remove(ItemStack bundle,int maximum){List<ItemStack> items=contents(bundle);if(items.isEmpty())return null;ItemStack last=items.get(items.size()-1),taken=last.splitStack(Math.min(maximum,last.stackSize));if(empty(last))items.remove(items.size()-1);store(bundle,items);return taken;}
    private static void restore(ItemStack bundle,ItemStack stack){if(empty(stack))return;List<ItemStack> items=contents(bundle);items.add(stack.copy());store(bundle,items);}
    private static void sound(EntityPlayer player,String name){if(!player.worldObj.isRemote)player.worldObj.playSoundEffect(player.posX,player.posY,player.posZ,Tags.MC_ASSET_VER+":item.bundle."+name,.8F,1);}
    /** A result/ghost slot cannot be edited as if its displayed bundle were an owned inventory item. */
    public static boolean click(EntityPlayer player,Slot slot){
        ItemStack cursor=player.inventory.getItemStack(),inSlot=slot.getStack();if(!bundle(cursor)&&!bundle(inSlot))return false;
        if(!player.isEntityAlive()||ganymedes01.etfuturum.api.spectator.SpectatorUtils.isSpectator(player)||!slot.canTakeStack(player))return true;
        if(bundle(cursor)){
            if(empty(inSlot)){
                ItemStack top=peek(cursor);if(empty(top)||!slot.isItemValid(top))return true;
                ItemStack taken=remove(cursor,Math.min(slot.getSlotStackLimit(),top.getMaxStackSize()));slot.putStack(taken);sound(player,"remove_one");
            }else{
                int amount=Math.min(inSlot.stackSize,room(cursor,inSlot));if(amount<=0||!slot.isItemValid(inSlot)&&amount<inSlot.stackSize)return true;
                ItemStack taken=slot.decrStackSize(amount);if(empty(taken))return true;
                int accepted=insert(cursor,taken,taken.stackSize);
                if(accepted<taken.stackSize){ItemStack remainder=taken.copy();remainder.stackSize-=accepted;if(!player.inventory.addItemStackToInventory(remainder))restore(cursor,remainder);}
                slot.onPickupFromSlot(player,taken);if(accepted>0)sound(player,"insert");
            }
        }else{
            if(!slot.isItemValid(inSlot))return false;
            if(empty(cursor)){ItemStack taken=remove(inSlot,Integer.MAX_VALUE);if(empty(taken))return true;player.inventory.setItemStack(taken);sound(player,"remove_one");}
            else{int moved=insert(inSlot,cursor,cursor.stackSize);if(moved==0)return true;cursor.stackSize-=moved;if(empty(cursor))player.inventory.setItemStack(null);sound(player,"insert");}
            slot.onSlotChanged();
        }
        player.inventory.markDirty();return true;
    }
    @Override public ItemStack onItemRightClick(ItemStack bundle,World world,EntityPlayer player){
        if(!player.isEntityAlive()||ganymedes01.etfuturum.api.spectator.SpectatorUtils.isSpectator(player))return bundle;
        ItemStack top=peek(bundle);if(empty(top))return bundle;
        if(!world.isRemote){
            EntityItem entity=new EntityItem(world,player.posX,player.posY+player.getEyeHeight()-.3,player.posZ,top.copy());net.minecraft.util.Vec3 look=player.getLookVec();entity.motionX=look.xCoord*.3;entity.motionY=look.yCoord*.3+.1;entity.motionZ=look.zCoord*.3;entity.delayBeforeCanPickup=40;entity.func_145799_b(player.getCommandSenderName());
            ItemTossEvent event=new ItemTossEvent(entity,player);NBTTagCompound before=bundle.hasTagCompound()?(NBTTagCompound)bundle.getTagCompound().copy():null;
            if(MinecraftForge.EVENT_BUS.post(event)||!Objects.equals(before,bundle.getTagCompound()))return bundle;
            ItemStack removed=remove(bundle,Integer.MAX_VALUE);if(!world.spawnEntityInWorld(event.entityItem))restore(bundle,removed);else sound(player,"drop_contents");
            player.inventory.markDirty();
        }
        return bundle;
    }
    @Override public boolean showDurabilityBar(ItemStack stack){return weight(stack)>0;}
    @Override public double getDurabilityForDisplay(ItemStack stack){return 1-weight(stack)/64D;}
    @SideOnly(Side.CLIENT) @Override public void addInformation(ItemStack stack,EntityPlayer player,List lines,boolean advanced){lines.add(net.minecraft.client.resources.I18n.format("item.etfuturum.bundle.weight",weight(stack),64));List<ItemStack> contents=contents(stack);for(int i=contents.size()-1,shown=0;i>=0&&shown<8;i--,shown++){ItemStack stored=contents.get(i);lines.add(stored.getDisplayName()+" x"+stored.stackSize);}if(contents.size()>8)lines.add("...");}
}
