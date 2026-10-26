package ganymedes01.etfuturum.client.renderer.entity;

import cpw.mods.fml.client.registry.RenderingRegistry;
import ganymedes01.etfuturum.client.model.ModelModernMob;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ModernMobRenderer extends RenderLiving {
	private final String kind, texture;
	private final ModelModernMob model;
	private ModelModernMob layer;
	private static final ganymedes01.etfuturum.client.renderer.item.ItemCrossbowRenderer CROSSBOW = new ganymedes01.etfuturum.client.renderer.item.ItemCrossbowRenderer();
	private final ModelModernMob baby;
	private final ModelModernMob coral;
	private static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
	private static ResourceLocation texture(String path) { return new ResourceLocation("etfuturum", "textures/entity/" + path + ".png"); }
	public ModernMobRenderer(String model, String texture, float shadow) { super(new ModelModernMob(model), shadow); kind = model; this.texture = texture; this.model = (ModelModernMob) mainModel; coral = model.equals("nautilus") ? new ModelModernMob("nautilus_coral") : null; baby = model.equals("nautilus") ? new ModelModernMob("nautilus_baby") : null; }
	@Override protected ResourceLocation getEntityTexture(Entity entity) {
		if (entity instanceof EntityPanda panda) return texture("panda/" + new String[]{"panda", "lazy_panda", "worried_panda", "playful_panda", "brown_panda", "weak_panda", "aggressive_panda"}[panda.getGene()]);
		if (entity instanceof EntityLlama llama) return texture("llama/" + new String[]{"creamy", "white", "brown", "gray"}[llama.getVariant()]);
		if (entity instanceof EntityCopperGolem golem) return texture("copper_golem/" + new String[]{"copper_golem", "exposed_copper_golem", "weathered_copper_golem", "oxidized_copper_golem"}[golem.getOxidation()]);
		if (entity instanceof EntityHappyGhast ghast) return texture("ghast/" + (ghast.isChild() ? "ghastling" : "happy_ghast"));
		if (entity instanceof EntityZombieNautilus nautilus) return texture("nautilus/" + (nautilus.getVariant() == 1 ? "zombie_nautilus_coral" : "zombie_nautilus"));
		if (entity instanceof EntityNautilus nautilus && nautilus.isChild()) return texture("nautilus/nautilus_baby");
		if (entity instanceof EntityVex vex && vex.getAttackTarget() != null) return texture("illager/vex_charging");
		return texture(texture);
	}
	@Override protected void preRenderCallback(EntityLivingBase entity, float partial) { mainModel = coral != null && entity instanceof EntityZombieNautilus nautilus && nautilus.getVariant() == 1 ? coral : baby != null && entity.isChild() ? baby : model; }
	@Override protected void renderModel(EntityLivingBase entity, float limb, float amount, float age, float yaw, float pitch, float scale) {
		if (entity instanceof EntityIllusioner && entity.isInvisible()) {
			bindEntityTexture(entity); GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT); GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); GL11.glColor4f(1, 1, 1, .65F);
			for (int i = 0; i < 4; i++) { GL11.glPushMatrix(); GL11.glTranslatef((float) Math.sin(i * 1.5708 + entity.getEntityId()) * 1.4F, 0, (float) Math.cos(i * 1.5708 + entity.getEntityId()) * 1.4F); mainModel.render(entity, limb, amount, age, yaw, pitch, scale); GL11.glPopMatrix(); }
			GL11.glColor4f(1, 1, 1, 1); GL11.glPopAttrib(); return;
		}
		super.renderModel(entity, limb, amount, age, yaw, pitch, scale);
	}
	@Override protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) {
		String path = null, layerKind = kind;
		if (entity instanceof EntityLlama llama && pass == 0) { if (llama.getDecor() >= 0) path = "equipment/llama_body/" + COLORS[llama.getDecor()]; else if (llama instanceof EntityTraderLlama) path = "equipment/llama_body/trader_llama"; }
		if (entity instanceof EntityRideableMob mount) {
			if (pass == 0 && mount.isSaddled()) {
				if (mount instanceof EntityHappyGhast) { path = "equipment/happy_ghast_body/" + COLORS[mount.getVariant()] + "_harness"; layerKind = "harness"; }
				else if (mount instanceof EntityCamel) { path = "equipment/" + (mount instanceof EntityCamelHusk ? "camel_husk" : "camel") + "_saddle/saddle"; layerKind = "camel_saddle"; }
				else if (mount instanceof EntityNautilus) { path = "equipment/nautilus_saddle/saddle"; layerKind = "nautilus_saddle"; }
			} else if (pass == 1 && mount instanceof EntityNautilus && mount.getBodyEquipment() != null) { path = "equipment/nautilus_body/" + new String[]{"copper", "gold", "iron", "diamond", "netherite"}[Math.min(4, mount.getBodyEquipment().getItemDamage())]; layerKind = "nautilus_armor"; }
		}
		if (entity instanceof EntityBreeze && pass == 0) { path = "breeze/breeze_wind"; layerKind = "breeze_wind"; }
		if (entity instanceof EntityWarden && pass == 0) path = "warden/warden_bioluminescent_layer";
		if (path == null) return -1;
		// Equipment models are retained independently because saddle geometry differs from the base body.
		if (!layerKind.equals(kind)) { layer = Layers.get(layerKind); setRenderPassModel(layer); } else setRenderPassModel(mainModel);
		bindTexture(texture(path)); GL11.glColor4f(1, 1, 1, 1); return 1;
	}
	private static class Layers { private static final java.util.Map<String, ModelModernMob> MODELS = new java.util.HashMap<>(); static ModelModernMob get(String kind) { ModelModernMob model = MODELS.get(kind); if (model == null) { model = new ModelModernMob(kind); MODELS.put(kind, model); } return model; } }
	@Override protected void renderEquippedItems(EntityLivingBase entity, float partial) {
		ItemStack stack = entity.getHeldItem(); if (stack == null || !(entity instanceof EntityIllager || entity instanceof EntityVex || entity instanceof EntityAllay || entity instanceof EntityCopperGolem)) return;
		GL11.glPushMatrix(); model.poseArm(.0625F);
		if (stack.getItem() instanceof ganymedes01.etfuturum.items.ItemCrossbow) CROSSBOW.renderModernHand(stack, entity);
		else { GL11.glTranslatef(-.07F, .5F, -.1F); GL11.glRotatef(-90, 1, 0, 0); GL11.glScalef(.6F, .6F, .6F); renderManager.itemRenderer.renderItem(entity, stack, 0); }
		GL11.glPopMatrix();
	}
	public static void register() {
		register(EntityPillager.class, "illager", "illager/pillager", .5F); register(EntityVindicator.class, "illager", "illager/vindicator", .5F); register(EntityEvoker.class, "illager", "illager/evoker", .5F); register(EntityIllusioner.class, "illager", "illager/illusioner", .5F); register(EntityVex.class, "vex", "illager/vex", .3F); register(EntityRavager.class, "ravager", "illager/ravager", 1.1F);
		register(EntityPanda.class, "panda", "panda/panda", .9F); register(EntityLlama.class, "llama", "llama/creamy", .7F); register(EntityTraderLlama.class, "llama", "llama/creamy", .7F); register(EntityAllay.class, "allay", "allay/allay", .2F); register(EntityWarden.class, "warden", "warden/warden", .8F); register(EntityBreeze.class, "breeze", "breeze/breeze", .5F); register(EntityCamel.class, "camel", "camel/camel", .9F); register(EntityCamelHusk.class, "camel", "camel/camel_husk", .9F); register(EntitySniffer.class, "sniffer", "sniffer/sniffer", 1); register(EntityArmadillo.class, "armadillo", "armadillo", .4F); register(EntityCopperGolem.class, "copper_golem", "copper_golem/copper_golem", .4F); register(EntityHappyGhast.class, "happy_ghast", "ghast/happy_ghast", 1.5F); register(EntityNautilus.class, "nautilus", "nautilus/nautilus", .6F); register(EntityZombieNautilus.class, "nautilus", "nautilus/zombie_nautilus", .6F);
		RenderingRegistry.registerEntityRenderingHandler(EntityWanderingTrader.class, new RenderVillager() { @Override protected ResourceLocation getEntityTexture(Entity entity) { return texture("wandering_trader"); } });
		RenderingRegistry.registerEntityRenderingHandler(EntityCat.class, new CatRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityBogged.class, new MobSkeletonRenderer(true)); RenderingRegistry.registerEntityRenderingHandler(EntityParched.class, new MobSkeletonRenderer(false));
		RenderingRegistry.registerEntityRenderingHandler(EntityCrossbowBolt.class, new RenderArrow()); RenderingRegistry.registerEntityRenderingHandler(EntityStatusArrow.class, new RenderArrow()); RenderingRegistry.registerEntityRenderingHandler(EntityWindCharge.class, new RenderSnowball(ganymedes01.etfuturum.ModItems.WIND_CHARGE.get())); RenderingRegistry.registerEntityRenderingHandler(EntityLlamaSpit.class, new LlamaSpitRenderer()); RenderingRegistry.registerEntityRenderingHandler(EntityEvokerFangs.class, new EvokerFangsRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityMobSeat.class, new Render() { @Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) { } @Override protected ResourceLocation getEntityTexture(Entity entity) { return null; } });
	}
	private static void register(Class<? extends Entity> type, String model, String texture, float shadow) { RenderingRegistry.registerEntityRenderingHandler(type, new ModernMobRenderer(model, texture, shadow)); }
	private static class LlamaSpitRenderer extends Render {
		private final ModelRenderer spit;
		LlamaSpitRenderer() { ModelBase model = new ModelBase() { }; model.textureWidth = 64; model.textureHeight = 32; spit = new ModelRenderer(model, 0, 0); spit.addBox(-4, 0, 0, 2, 2, 2); spit.addBox(0, -4, 0, 2, 2, 2); spit.addBox(0, 0, -4, 2, 2, 2); spit.addBox(0, 0, 0, 2, 2, 2); spit.addBox(2, 0, 0, 2, 2, 2); spit.addBox(0, 2, 0, 2, 2, 2); spit.addBox(0, 0, 2, 2, 2, 2); }
		@Override protected ResourceLocation getEntityTexture(Entity entity) { return texture("llama/spit"); }
		@Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) { bindEntityTexture(entity); GL11.glPushMatrix(); GL11.glTranslated(x, y, z); GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partial - 90, 0, 1, 0); GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partial, 0, 0, 1); spit.render(.0625F); GL11.glPopMatrix(); }
	}
	private static class CatRenderer extends RenderOcelot {
		CatRenderer() { super(new ModelOcelot(), .4F); }
		private static final String[] SKINS = {"tabby", "black", "red", "siamese", "british_shorthair", "calico", "persian", "ragdoll", "white", "jellie", "all_black"};
		@Override protected ResourceLocation getEntityTexture(Entity entity) { return texture("cat/" + SKINS[((EntityCat) entity).getVariant()]); }
		@Override protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) { if (pass != 0 || !((EntityCat) entity).isTamed()) return -1; setRenderPassModel(mainModel); bindTexture(texture("cat/cat_collar")); int color = net.minecraft.item.ItemDye.field_150922_c[15 - ((EntityCat) entity).getCollarColor()]; GL11.glColor3f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F); return 1; }
	}
	private static class MobSkeletonRenderer extends RenderSkeleton {
		private final boolean bogged; private final ModelZombie overlay = new ModelZombie(.25F, true);
		MobSkeletonRenderer(boolean bogged) { this.bogged = bogged; }
		@Override protected ResourceLocation getEntityTexture(Entity entity) { return texture("skeleton/" + (bogged ? "bogged" : "parched")); }
		@Override protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) { if (bogged && pass == 1 && !((EntityBogged) entity).isSheared()) { setRenderPassModel(overlay); bindTexture(texture("skeleton/bogged_overlay")); return 1; } return super.shouldRenderPass(entity, pass, partial); }
	}
}
