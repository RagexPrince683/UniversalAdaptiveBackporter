package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
final class CompassView {
 private CompassView(){}
 static void initialize(){for(String name:new String[]{"lodestone_compass","recovery_compass"})Content.item(name).addPropertyOverride(new ResourceLocation("angle"),CompassView::angle);}
 static float angle(ItemStack stack,World world,EntityLivingBase entity){if(entity==null)entity=Minecraft.getMinecraft().player;if(entity==null)return 0;if(world==null)world=entity.world;NBTTagCompound target=stack.hasTagCompound()?stack.getTagCompound().getCompoundTag(NavigationItems.TARGET):new NBTTagCompound();return (float)(NavigationItems.valid(target)&&target.getInteger("Dimension")==world.provider.getDimension()?CompassRules.rotation(entity.posX,entity.posZ,entity.rotationYaw,target.getInteger("X")+.5,target.getInteger("Z")+.5):CompassRules.spin(world.getTotalWorldTime(),entity.getEntityId()));}
}
