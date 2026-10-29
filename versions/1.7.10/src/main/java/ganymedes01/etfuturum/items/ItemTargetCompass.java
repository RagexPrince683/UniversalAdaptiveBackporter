package ganymedes01.etfuturum.items;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import ganymedes01.etfuturum.NavigationItems;
public class ItemTargetCompass extends BaseItem {
 public final boolean recovery;
 @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private net.minecraft.util.IIcon[] frames;
 public ItemTargetCompass(boolean recovery){super(recovery?"recovery_compass":"lodestone_compass");this.recovery=recovery;setMaxStackSize(64);}
 @Override public void onUpdate(ItemStack stack,World world,Entity entity,int slot,boolean held){NavigationItems.update(stack,world,entity,recovery);}
 @Override public EnumRarity getRarity(ItemStack stack){return recovery?EnumRarity.uncommon:EnumRarity.common;}
 @Override public boolean hasEffect(ItemStack stack){return stack.isItemEnchanted()||!recovery&&stack.hasTagCompound()&&NavigationItems.valid(stack.getTagCompound().getCompoundTag(NavigationItems.TARGET));}
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister atlas){frames=new net.minecraft.util.IIcon[32];for(int i=0;i<32;i++)frames[i]=atlas.registerIcon((recovery?"recovery_compass":"compass")+"_"+String.format(java.util.Locale.ROOT,"%02d",i));itemIcon=frames[16];}
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getIconIndex(ItemStack stack){return getIcon(stack,0);}
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getIcon(ItemStack stack,int pass){net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();if(mc.thePlayer==null||mc.theWorld==null)return frames[16];net.minecraft.nbt.NBTTagCompound target=stack.hasTagCompound()?stack.getTagCompound().getCompoundTag(NavigationItems.TARGET):new net.minecraft.nbt.NBTTagCompound();int frame=NavigationItems.valid(target)&&target.getInteger("Dimension")==mc.theWorld.provider.dimensionId?ganymedes01.etfuturum.client.CompassRules.frame(mc.thePlayer.posX,mc.thePlayer.posZ,mc.thePlayer.rotationYaw,target.getInteger("X")+.5,target.getInteger("Z")+.5):(int)(ganymedes01.etfuturum.client.CompassRules.spin(mc.theWorld.getTotalWorldTime(),mc.thePlayer.getEntityId())*32);return frames[frame];}
}
