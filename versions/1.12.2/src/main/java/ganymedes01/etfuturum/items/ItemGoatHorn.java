package ganymedes01.etfuturum.items;
import ganymedes01.etfuturum.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.*;
import java.util.List;
public class ItemGoatHorn extends Item {
    public static final int VARIANT_COUNT=8,USE_DURATION=140;
    private static final String COOLDOWN="EtFuturumGoatHornCooldown";
    private static final String[] INSTRUMENTS={"ponder_goat_horn","sing_goat_horn","seek_goat_horn","feel_goat_horn","admire_goat_horn","call_goat_horn","yearn_goat_horn","dream_goat_horn"};
    public ItemGoatHorn(){setHasSubtypes(true);setMaxStackSize(1);addPropertyOverride(new ResourceLocation("tooting"),(stack,world,entity)->entity!=null&&entity.isHandActive()&&entity.getActiveItemStack()==stack?1:0);}
    @Override public int getMetadata(int meta){return getVariant(meta);}
    @Override public void getSubItems(CreativeTabs tab,NonNullList<ItemStack> items){if(isInCreativeTab(tab)&&Content.available("goat_horn"))for(int meta=0;meta<VARIANT_COUNT;meta++)items.add(new ItemStack(this,1,meta));}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);NBTTagCompound data=player.getEntityData();if(data.getLong(COOLDOWN)>world.getTotalWorldTime()||player.getCooldownTracker().hasCooldown(this))return new ActionResult<>(EnumActionResult.FAIL,held);player.resetActiveHand();player.setActiveHand(hand);data.setLong(COOLDOWN,world.getTotalWorldTime()+USE_DURATION);player.getCooldownTracker().setCooldown(this,USE_DURATION);if(!world.isRemote)world.playSound(null,player.posX,player.posY,player.posZ,ModernSounds.get("item.goat_horn.sound."+getVariant(held)),SoundCategory.PLAYERS,16,1);return new ActionResult<>(EnumActionResult.SUCCESS,held);}
    @Override public int getMaxItemUseDuration(ItemStack stack){return USE_DURATION;}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.NONE;}
    @Override public EnumRarity getRarity(ItemStack stack){return EnumRarity.UNCOMMON;}
    @Override @SideOnly(Side.CLIENT) public void addInformation(ItemStack stack,World world,List<String> lines,net.minecraft.client.util.ITooltipFlag flags){lines.add(net.minecraft.util.text.TextFormatting.GRAY+net.minecraft.client.resources.I18n.format("instrument.minecraft."+getInstrumentName(stack.getMetadata())));}
    public static int getVariant(ItemStack stack){return stack==null?0:getVariant(stack.getMetadata());}
    public static int getVariant(int meta){return meta<0?0:meta%VARIANT_COUNT;}
    public static String getInstrumentName(int meta){return INSTRUMENTS[getVariant(meta)];}
    public static boolean isGoatHorn(ItemStack stack){return stack!=null&&!stack.isEmpty()&&stack.getItem() instanceof ItemGoatHorn;}
}
