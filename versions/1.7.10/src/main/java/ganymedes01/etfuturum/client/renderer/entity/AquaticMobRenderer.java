package ganymedes01.etfuturum.client.renderer.entity;

import ganymedes01.etfuturum.client.model.ModelAquaticMob;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class AquaticMobRenderer extends RenderLiving {
	private final int kind;
	private final ModelBase[] models;
	private static final ResourceLocation COD = texture("fish/cod"), SALMON = texture("fish/salmon"), PUFFER = texture("fish/pufferfish"), DOLPHIN = texture("dolphin"), TURTLE = texture("turtle/big_sea_turtle");
	private static final ResourceLocation[] TROPICAL = {texture("fish/tropical_a"), texture("fish/tropical_b")};
	private static final ResourceLocation[][] PATTERNS = new ResourceLocation[2][6];
	private static final ResourceLocation[] AXOLOTLS = {texture("axolotl/axolotl_lucy"), texture("axolotl/axolotl_wild"), texture("axolotl/axolotl_gold"), texture("axolotl/axolotl_cyan"), texture("axolotl/axolotl_blue")};
	static { for (int shape = 0; shape < 2; shape++) for (int pattern = 0; pattern < 6; pattern++) PATTERNS[shape][pattern] = texture("fish/tropical_" + (shape == 0 ? "a" : "b") + "_pattern_" + (pattern + 1)); }
	private static ResourceLocation texture(String path) { return new ResourceLocation("etfuturum", "textures/entity/" + path + ".png"); }
	public AquaticMobRenderer(int kind) {
		super(new ModelAquaticMob(kind == 10 ? 4 : kind), .25F); this.kind = kind;
		models = kind == 10 ? new ModelBase[]{mainModel, new ModelAquaticMob(5), new ModelAquaticMob(6)} : kind == 2 ? new ModelBase[]{mainModel, new ModelAquaticMob(3)} : new ModelBase[]{mainModel};
	}
	@Override public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partial) {
		if (entity instanceof EntityPufferfish puffer) mainModel = models[puffer.getPuffState()];
		else if (entity instanceof EntityTropicalFish fish) mainModel = models[fish.getVariant() & 1];
		setRenderPassModel(mainModel); super.doRender(entity, x, y, z, yaw, partial);
	}
	@Override protected ResourceLocation getEntityTexture(Entity entity) {
		if (entity instanceof EntityTropicalFish fish) return TROPICAL[fish.getVariant() & 1];
		if (entity instanceof EntityAxolotl axolotl) return AXOLOTLS[axolotl.getVariant()];
		return switch (kind) { case 1 -> SALMON; case 10 -> PUFFER; case 7 -> DOLPHIN; case 9 -> TURTLE; default -> COD; };
	}
	private static void color(int dye) { float[] color = EntitySheep.fleeceColorTable[dye & 15]; GL11.glColor4f(color[0], color[1], color[2], 1); }
	@Override protected void renderModel(EntityLivingBase entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
		if (entity instanceof EntityTropicalFish fish) color(fish.getVariant() >>> 16);
		try { super.renderModel(entity, swing, amount, age, yaw, pitch, scale); } finally { GL11.glColor4f(1, 1, 1, 1); }
	}
	@Override protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) {
		if (pass == 0 && entity instanceof EntityTropicalFish fish) { bindTexture(PATTERNS[fish.getVariant() & 1][(fish.getVariant() >>> 8) & 255]); color(fish.getVariant() >>> 24); return 1; }
		GL11.glColor4f(1, 1, 1, 1); return -1;
	}
	@Override protected void preRenderCallback(EntityLivingBase entity, float partial) {
		float scale = entity.isChild() ? entity instanceof EntityTurtle ? .3F : .5F : 1;
		if (entity instanceof EntitySalmon salmon) scale *= salmon.getVariant() == 0 ? .5F : salmon.getVariant() == 2 ? 1.5F : 1;
		GL11.glScalef(scale, scale, scale);
	}
	@Override protected void rotateCorpse(EntityLivingBase entity, float age, float yaw, float partial) {
		super.rotateCorpse(entity, age, yaw, partial);
		if (entity instanceof EntityFish && !entity.isInWater()) { GL11.glTranslatef(.15F, .1F, 0); GL11.glRotatef(90, 0, 0, 1); }
	}
}
