package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.items.ItemHandlerHelper;

/** Weighted item storage using native inventory clicks and ordinary ItemStack save/network data. */
public final class BundleItem extends Item {
    public static final String CONTENTS="UabBundleItems";
    public static final String[] COLORS={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
    BundleItem(){setMaxStackSize(1);}
    public static boolean bundle(ItemStack stack){return !stack.isEmpty()&&stack.getItem() instanceof BundleItem;}
    private static List<ItemStack> contents(ItemStack bundle){List<ItemStack> result=new ArrayList<>();if(!bundle.hasTagCompound())return result;NBTTagList list=bundle.getTagCompound().getTagList(CONTENTS,10);for(int i=0;i<Math.min(128,list.tagCount());i++){ItemStack stack=new ItemStack(list.getCompoundTagAt(i));if(!stack.isEmpty())result.add(stack);}return result;}
    private static void store(ItemStack bundle,List<ItemStack> items){NBTTagCompound tag=bundle.hasTagCompound()?bundle.getTagCompound():new NBTTagCompound();if(items.isEmpty())tag.removeTag(CONTENTS);else{NBTTagList list=new NBTTagList();for(ItemStack stack:items)if(!stack.isEmpty())list.appendTag(stack.writeToNBT(new NBTTagCompound()));tag.setTag(CONTENTS,list);}bundle.setTagCompound(tag.getKeySet().isEmpty()?null:tag);}
    public static int weight(ItemStack bundle){return weight(bundle,0);}
    private static int weight(ItemStack bundle,int depth){if(depth>32)return 64;long result=0;for(ItemStack stack:contents(bundle)){result+=(long)unit(stack,depth+1)*stack.getCount();if(result>=64)return 64;}return (int)result;}
    private static int unit(ItemStack stack,int depth){return bundle(stack)?4+weight(stack,depth):Math.max(1,64/Math.max(1,Math.min(64,stack.getMaxStackSize())));}
    private static boolean allowed(ItemStack stack){return !stack.isEmpty()&&!(stack.getItem() instanceof ItemBlock&&Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox);}
    public static int room(ItemStack bundle,ItemStack source){return allowed(source)?Math.max(0,64-weight(bundle))/unit(source,0):0;}
    public static int insert(ItemStack bundle,ItemStack source,int maximum){if(bundle==source||!allowed(source))return 0;int amount=Math.min(Math.min(source.getCount(),maximum),room(bundle,source));if(amount<=0)return 0;List<ItemStack> items=contents(bundle);int left=amount;for(ItemStack stored:items)if(ItemHandlerHelper.canItemStacksStack(stored,source)){int moved=Math.min(left,Math.max(0,stored.getMaxStackSize()-stored.getCount()));stored.grow(moved);left-=moved;if(left==0)break;}while(left>0){ItemStack part=source.copy();part.setCount(Math.min(left,Math.max(1,Math.min(64,source.getMaxStackSize()))));items.add(part);left-=part.getCount();}store(bundle,items);return amount;}
    private static ItemStack peek(ItemStack bundle){List<ItemStack> items=contents(bundle);return items.isEmpty()?ItemStack.EMPTY:items.get(items.size()-1).copy();}
    private static ItemStack remove(ItemStack bundle,int maximum){List<ItemStack> items=contents(bundle);if(items.isEmpty())return ItemStack.EMPTY;ItemStack last=items.get(items.size()-1),taken=last.splitStack(Math.min(maximum,last.getCount()));if(last.isEmpty())items.remove(items.size()-1);store(bundle,items);return taken;}
    private static void restore(ItemStack bundle,ItemStack stack){if(stack.isEmpty())return;List<ItemStack> items=contents(bundle);items.add(stack.copy());store(bundle,items);}
    private static void sound(EntityPlayer player,String name){if(!player.world.isRemote)player.world.playSound(null,player.posX,player.posY,player.posZ,ModernSounds.get("item.bundle."+name),SoundCategory.PLAYERS,.8F,1);}
    /** A result/ghost slot cannot be edited as if its displayed bundle were an owned inventory item. */
    public static boolean click(EntityPlayer player,Slot slot){
        ItemStack cursor=player.inventory.getItemStack(),inSlot=slot.getStack();if(!bundle(cursor)&&!bundle(inSlot))return false;
        if(!player.isEntityAlive()||player.isSpectator()||!slot.canTakeStack(player))return true;
        if(bundle(cursor)){
            if(inSlot.isEmpty()){
                ItemStack top=peek(cursor);if(top.isEmpty()||!slot.isItemValid(top))return true;
                ItemStack taken=remove(cursor,Math.min(slot.getItemStackLimit(top),top.getMaxStackSize()));slot.putStack(taken);sound(player,"remove_one");
            }else{
                int amount=Math.min(inSlot.getCount(),room(cursor,inSlot));if(amount<=0||!slot.isItemValid(inSlot)&&amount<inSlot.getCount())return true;
                ItemStack taken=slot.decrStackSize(amount);if(taken.isEmpty())return true;
                int accepted=insert(cursor,taken,taken.getCount());
                if(accepted<taken.getCount()){ItemStack remainder=taken.copy();remainder.shrink(accepted);if(!player.inventory.addItemStackToInventory(remainder))restore(cursor,remainder);}
                slot.onTake(player,taken);if(accepted>0)sound(player,"insert");
            }
        }else{
            if(!slot.isItemValid(inSlot))return false;
            if(cursor.isEmpty()){ItemStack taken=remove(inSlot,Integer.MAX_VALUE);if(taken.isEmpty())return true;player.inventory.setItemStack(taken);sound(player,"remove_one");}
            else{int moved=insert(inSlot,cursor,cursor.getCount());if(moved==0)return true;cursor.shrink(moved);if(cursor.isEmpty())player.inventory.setItemStack(ItemStack.EMPTY);sound(player,"insert");}
            slot.onSlotChanged();
        }
        player.inventory.markDirty();return true;
    }
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){
        ItemStack bundle=player.getHeldItem(hand);if(!player.isEntityAlive()||player.isSpectator())return new ActionResult<>(EnumActionResult.FAIL,bundle);ItemStack top=peek(bundle);if(top.isEmpty())return new ActionResult<>(EnumActionResult.PASS,bundle);
        if(!world.isRemote){
            EntityItem entity=new EntityItem(world,player.posX,player.posY+player.getEyeHeight()-.3,player.posZ,top.copy());net.minecraft.util.math.Vec3d look=player.getLookVec();entity.motionX=look.x*.3;entity.motionY=look.y*.3+.1;entity.motionZ=look.z*.3;entity.setPickupDelay(40);entity.setThrower(player.getName());
            ItemTossEvent event=new ItemTossEvent(entity,player);NBTTagCompound before=bundle.hasTagCompound()?bundle.getTagCompound().copy():null;
            if(MinecraftForge.EVENT_BUS.post(event)||!Objects.equals(before,bundle.getTagCompound()))return new ActionResult<>(EnumActionResult.FAIL,bundle);
            ItemStack removed=remove(bundle,Integer.MAX_VALUE);if(!world.spawnEntity(event.getEntityItem()))restore(bundle,removed);else sound(player,"drop_contents");
            player.inventory.markDirty();
        }
        return new ActionResult<>(EnumActionResult.SUCCESS,bundle);
    }
    @Override public boolean showDurabilityBar(ItemStack stack){return weight(stack)>0;}
    @Override public double getDurabilityForDisplay(ItemStack stack){return 1-weight(stack)/64D;}
    @SideOnly(Side.CLIENT) @Override public void addInformation(ItemStack stack,World world,List<String> lines,ITooltipFlag flag){lines.add(net.minecraft.client.resources.I18n.format("item.etfuturum.bundle.weight",weight(stack),64));List<ItemStack> contents=contents(stack);for(int i=contents.size()-1,shown=0;i>=0&&shown<8;i--,shown++){ItemStack stored=contents.get(i);lines.add(stored.getDisplayName()+" x"+stored.getCount());}if(contents.size()>8)lines.add("...");}
}
