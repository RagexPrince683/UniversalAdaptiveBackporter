package ganymedes01.etfuturum.client.renderer.item;

import ganymedes01.etfuturum.items.ItemCrossbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Native sprite extrusion with the released crossbow's hand-display rotations. */
public final class ItemCrossbowRenderer implements IItemRenderer {
	@Override public boolean handleRenderType(ItemStack stack, ItemRenderType type) { return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON; }
	@Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) { return helper == ItemRendererHelper.EQUIPPED_BLOCK || type == ItemRenderType.EQUIPPED && helper == ItemRendererHelper.BLOCK_3D; }
	@Override public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		EntityLivingBase holder = data.length > 1 && data[1] instanceof EntityLivingBase living ? living : Minecraft.getMinecraft().thePlayer;
		render(stack, holder, type == ItemRenderType.EQUIPPED_FIRST_PERSON, true);
	}
	public void renderModernHand(ItemStack stack, EntityLivingBase holder) { render(stack, holder, false, false); }
	private void render(ItemStack stack, EntityLivingBase holder, boolean first, boolean legacy) {
		IIcon icon = stack.getItem().getIcon(stack, 0);
		if (holder instanceof EntityPlayer player) icon = stack.getItem().getIcon(stack, 0, player, player.getItemInUse(), player.getItemInUseCount());
		else if (holder instanceof ganymedes01.etfuturum.entities.EntityPillager pillager && pillager.getCrossbowChargeTicks() >= 0) icon = ((ItemCrossbow) stack.getItem()).getChargingIcon(stack, pillager.getCrossbowChargeTicks());
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_TEXTURE_BIT);
		GL11.glPushMatrix();
		try {
			GL11.glEnable(GL12.GL_RESCALE_NORMAL); GL11.glEnable(GL11.GL_ALPHA_TEST); GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
			GL11.glColor4f(1, 1, 1, 1);
			// Normalize legacy hand/camera transforms before applying released item display transforms.
			if (legacy) GL11.glTranslatef(.5F, .5F, .5F);
			if (first) {
				GL11.glScalef(2.5F, 2.5F, 2.5F); GL11.glRotatef(-45, 0, 1, 0);
				if (ItemCrossbow.charged(stack)) { GL11.glTranslatef(-.641864F, 0, 0); GL11.glRotatef(10, 0, 1, 0); }
				GL11.glTranslatef(1.13F / 16, 3.2F / 16, 1.13F / 16);
			} else {
				if (legacy) {
					GL11.glScalef(-1 / .375F, -1 / .375F, 1 / .375F);
					GL11.glRotatef(-45, 0, 1, 0); GL11.glRotatef(-20, 1, 0, 0);
					GL11.glTranslatef(.0625F, -.625F, .25F);
				}
				GL11.glRotatef(-90, 1, 0, 0); GL11.glRotatef(180, 0, 1, 0);
				GL11.glTranslatef(1 / 16F, .125F, -.625F);
				GL11.glTranslatef(2 / 16F, .1F / 16F, -3 / 16F);
			}
			// Legacy sprite extrusion has a quarter-turn difference from the generated modern item plane.
			GL11.glRotatef(-90, 1, 0, 0); GL11.glRotatef(first ? 35 : 30, 0, 0, 1);
			float size = first ? 1.02F : 1.35F; GL11.glScalef(size, size, size); GL11.glTranslatef(-.5F, -.5F, 0);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationItemsTexture);
			draw(icon);
			if (stack.hasEffect(0)) {
				GL11.glDepthFunc(GL11.GL_EQUAL); GL11.glDisable(GL11.GL_LIGHTING); GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
				GL11.glColor4f(.38F, .19F, .61F, 1);
				for (int pass = 0; pass < 2; pass++) {
					GL11.glMatrixMode(GL11.GL_TEXTURE); GL11.glPushMatrix();
					try { GL11.glScalef(.125F, .125F, .125F); GL11.glTranslatef((Minecraft.getSystemTime() % (pass == 0 ? 3000L : 4873L)) / (pass == 0 ? 3000F : 4873F) * (pass == 0 ? 8 : -8), 0, 0); GL11.glRotatef(pass == 0 ? -50 : 10, 0, 0, 1); ItemRenderer.renderItemIn2D(Tessellator.instance, 1, 0, 0, 1, 256, 256, .0625F); }
					finally { GL11.glPopMatrix(); GL11.glMatrixMode(GL11.GL_MODELVIEW); }
				}
			}
		} finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
	}
	private void draw(IIcon icon) { if (icon != null) ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), .0625F); }
}
