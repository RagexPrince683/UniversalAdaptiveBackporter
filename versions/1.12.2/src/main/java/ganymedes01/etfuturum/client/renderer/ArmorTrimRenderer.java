package ganymedes01.etfuturum.client.renderer;

import ganymedes01.etfuturum.Content;
import ganymedes01.etfuturum.recipes.ArmorTrims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Retained trim geometry and palettes; armor attributes and enchantments remain native. */
public final class ArmorTrimRenderer {
    private static String palette(ItemArmor armor,String material){String type=armor.getArmorMaterial()==Content.NETHERITE_ARMOR?"netherite":armor.getArmorMaterial().name().toLowerCase(java.util.Locale.ROOT);return material.equals(type)?material+"_darker":material;}
    public static void renderIcon(ItemStack stack,int x,int y,float depth){NBTTagCompound trim=ArmorTrims.trim(stack);if(trim==null||!(stack.getItem() instanceof ItemArmor))return;ItemArmor armor=(ItemArmor)stack.getItem();String material=palette(armor,trim.getString("material").replace("minecraft:",""));String slot=armor.armorType==EntityEquipmentSlot.HEAD?"helmet":armor.armorType==EntityEquipmentSlot.CHEST?"chestplate":armor.armorType==EntityEquipmentSlot.LEGS?"leggings":"boots";int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        try{Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("etfuturum","textures/trims/items/"+material+"/"+slot+".png"));GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glColor4f(1,1,1,1);BufferBuilder buffer=Tessellator.getInstance().getBuffer();buffer.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX);buffer.pos(x,y+16,depth+1).tex(0,1).endVertex();buffer.pos(x+16,y+16,depth+1).tex(1,1).endVertex();buffer.pos(x+16,y,depth+1).tex(1,0).endVertex();buffer.pos(x,y,depth+1).tex(0,0).endVertex();Tessellator.getInstance().draw();}finally{GL11.glPopAttrib();GlStateManager.bindTexture(texture);}
    }
    public static void render(ModelBase model,Entity entity,ItemStack stack,float swing,float amount,float age,float yaw,float pitch,float scale){NBTTagCompound trim=ArmorTrims.trim(stack);if(trim==null||!(model instanceof ModelBiped)||!(stack.getItem() instanceof ItemArmor))return;ItemArmor armor=(ItemArmor)stack.getItem();String pattern=trim.getString("pattern").replace("minecraft:",""),material=palette(armor,trim.getString("material").replace("minecraft:",""));int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_POLYGON_BIT);
        try{Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("etfuturum","textures/trims/"+material+"/"+pattern+(armor.armorType==EntityEquipmentSlot.LEGS?"_leggings":"")+".png"));GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1F);GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);GL11.glPolygonOffset(-1,-1);model.render(entity,swing,amount,age,yaw,pitch,scale);}finally{GL11.glPopAttrib();GlStateManager.bindTexture(texture);}
    }
    @SubscribeEvent public void tooltip(ItemTooltipEvent event){NBTTagCompound trim=ArmorTrims.trim(event.getItemStack());if(trim==null)return;event.getToolTip().add(TextFormatting.GRAY+net.minecraft.client.resources.I18n.format("uab.trim"));event.getToolTip().add(" "+net.minecraft.client.resources.I18n.format("uab.trim.pattern."+trim.getString("pattern").replace("minecraft:","")));event.getToolTip().add(" "+net.minecraft.client.resources.I18n.format("uab.trim.material."+trim.getString("material").replace("minecraft:","")));}
}
