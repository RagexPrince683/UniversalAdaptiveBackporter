package ganymedes01.etfuturum.client.renderer.entity;
import ganymedes01.etfuturum.client.model.ModelModernMob;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
/** Native retained models; existing registration names and renderer entry points remain unchanged. */
public class NetherMobRenderer extends RenderLiving {
 private final ResourceLocation texture;
 private final ModelModernMob model;
 private static final ResourceLocation COLD = texture("strider_cold"), SADDLE = texture("strider_saddle");
 private static ResourceLocation texture(String name) { return new ResourceLocation("etfuturum", "textures/entity/" + name + ".png"); }
 public NetherMobRenderer(int kind, String texture) { super(new ModelModernMob(kind == 0 ? "piglin" : kind == 1 ? "hoglin" : kind == 3 ? "creaking" : "strider"), .6F); model = (ModelModernMob) mainModel; this.texture = texture(texture); setRenderPassModel(mainModel); }
 @Override protected ResourceLocation getEntityTexture(Entity entity) { return entity instanceof EntityStrider strider && strider.isCold() ? COLD : texture; }
 @Override protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) { if (pass == 0 && entity instanceof EntityStrider strider && strider.isSaddled()) { bindTexture(SADDLE); GL11.glColor4f(1, 1, 1, 1); return 1; } return -1; }
 @Override protected void renderEquippedItems(EntityLivingBase entity, float partial) { ItemStack held = entity.getHeldItem(); if (!(entity instanceof EntityPiglin) || held == null) return; GL11.glPushMatrix(); if (entity.isChild()) { GL11.glTranslatef(0, .75F, 0); GL11.glScalef(.5F, .5F, .5F); } model.poseArm(.0625F); GL11.glTranslatef(-.05F, .5F, -.1F); GL11.glRotatef(-90, 1, 0, 0); GL11.glScalef(.65F, .65F, .65F); renderManager.itemRenderer.renderItem(entity, held, 0); GL11.glPopMatrix(); }
}
