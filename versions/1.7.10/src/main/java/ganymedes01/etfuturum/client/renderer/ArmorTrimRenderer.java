package ganymedes01.etfuturum.client.renderer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.recipes.ArmorTrims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.opengl.GL11;

public final class ArmorTrimRenderer {
	private static String palette(ItemArmor armor, String material) {
		String type = armor.getArmorMaterial() == ganymedes01.etfuturum.ModMaterials.NETHERITE_ARMOUR ? "netherite" : armor.getArmorMaterial().name().toLowerCase(java.util.Locale.ROOT);
		return material.equals(type) ? material + "_darker" : material;
	}
	public static void renderIcon(ItemStack stack, int x, int y, float depth) {
		NBTTagCompound trim = ArmorTrims.trim(stack); if (trim == null || !(stack.getItem() instanceof ItemArmor)) return;
		ItemArmor armor = (ItemArmor) stack.getItem();
		String material = palette(armor, trim.getString("material").replace("minecraft:", ""));
		ResourceLocation texture = new ResourceLocation("etfuturum", "textures/trims/items/" + material + "/" + new String[]{"helmet", "chestplate", "leggings", "boots"}[armor.armorType] + ".png");
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT);
		try {
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture); GL11.glDisable(GL11.GL_LIGHTING); GL11.glEnable(GL11.GL_ALPHA_TEST); GL11.glColor4f(1, 1, 1, 1);
			net.minecraft.client.renderer.Tessellator t = net.minecraft.client.renderer.Tessellator.instance;
			t.startDrawingQuads(); t.addVertexWithUV(x, y + 16, depth + 1, 0, 1); t.addVertexWithUV(x + 16, y + 16, depth + 1, 1, 1); t.addVertexWithUV(x + 16, y, depth + 1, 1, 0); t.addVertexWithUV(x, y, depth + 1, 0, 0); t.draw();
		} finally { GL11.glPopAttrib(); }
	}
	public static void render(ModelBase model, Entity entity, ItemStack armor, float swing, float amount, float age, float yaw, float pitch, float scale) {
		NBTTagCompound trim = ArmorTrims.trim(armor);
		if (trim == null || !(model instanceof ModelBiped) || !(armor.getItem() instanceof ItemArmor)) return;
		String pattern = trim.getString("pattern").replace("minecraft:", ""), material = trim.getString("material").replace("minecraft:", "");
		ItemArmor item = (ItemArmor) armor.getItem(); material = palette(item, material);
		ResourceLocation texture = new ResourceLocation("etfuturum", "textures/trims/" + material + "/" + pattern + (item.armorType == 2 ? "_leggings" : "") + ".png");
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_POLYGON_BIT);
		try {
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture); GL11.glColor4f(1, 1, 1, 1);
			GL11.glEnable(GL11.GL_ALPHA_TEST); GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
			GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL); GL11.glPolygonOffset(-1, -1);
			model.render(entity, swing, amount, age, yaw, pitch, scale);
		} finally { GL11.glPopAttrib(); }
	}
	@SubscribeEvent public void tooltip(ItemTooltipEvent event) {
		NBTTagCompound trim = ArmorTrims.trim(event.itemStack); if (trim == null) return;
		event.toolTip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("uab.trim"));
		event.toolTip.add(" " + StatCollector.translateToLocal("uab.trim.pattern." + trim.getString("pattern").replace("minecraft:", "")));
		event.toolTip.add(" " + StatCollector.translateToLocal("uab.trim.material." + trim.getString("material").replace("minecraft:", "")));
	}
}
