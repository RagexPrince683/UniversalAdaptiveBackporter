package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.client.model.ModelAquaticMob;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

final class FishRenderer extends RenderLiving<EntityFish> {
    private final int kind;
    private final ModelBase[] models;
    FishRenderer(RenderManager manager, int kind) {
        super(manager, new ModelAquaticMob(kind == 10 ? 4 : kind), .25F); this.kind = kind;
        models = kind == 10 ? new ModelBase[]{mainModel, new ModelAquaticMob(5), new ModelAquaticMob(6)} : kind == 2 ? new ModelBase[]{mainModel, new ModelAquaticMob(3)} : new ModelBase[]{mainModel};
        if (kind == 2) addLayer(new LayerRenderer<EntityFish>() {
            @Override public void doRenderLayer(EntityFish fish, float swing, float amount, float partial, float age, float yaw, float pitch, float scale) {
                int variant = fish.getVariant();
                bindTexture(texture("tropical_" + ((variant & 1) == 0 ? "a" : "b") + "_pattern_" + (((variant >>> 8) & 255) + 1)));
                color(variant >>> 24);
                mainModel.render(fish, swing, amount, age, yaw, pitch, scale); GlStateManager.color(1, 1, 1, 1);
            }
            @Override public boolean shouldCombineTextures() { return true; }
        });
    }
    private static void color(int dye) { float[] rgb = EnumDyeColor.byMetadata(dye & 15).getColorComponentValues(); GlStateManager.color(rgb[0], rgb[1], rgb[2], 1); }
    private static ResourceLocation texture(String name) { return new ResourceLocation("etfuturum", "textures/entity/fish/" + name + ".png"); }
    @Override protected ResourceLocation getEntityTexture(EntityFish fish) {
        return texture(kind == 1 ? "salmon" : kind == 2 ? "tropical_" + ((fish.getVariant() & 1) == 0 ? "a" : "b") : kind == 10 ? "pufferfish" : "cod");
    }
    @Override public void doRender(EntityFish fish, double x, double y, double z, float yaw, float partial) {
        if (fish instanceof EntityPufferfish) mainModel = models[((EntityPufferfish) fish).getPuffState()];
        else if (fish instanceof EntityTropicalFish) mainModel = models[fish.getVariant() & 1];
        super.doRender(fish, x, y, z, yaw, partial);
    }
    @Override protected void renderModel(EntityFish fish, float swing, float amount, float age, float yaw, float pitch, float scale) {
        if (kind == 2) color(fish.getVariant() >>> 16);
        try { super.renderModel(fish, swing, amount, age, yaw, pitch, scale); } finally { GlStateManager.color(1, 1, 1, 1); }
    }
    @Override protected void preRenderCallback(EntityFish fish, float partial) { if (kind == 1) { float scale = fish.getVariant() == 0 ? .5F : fish.getVariant() == 2 ? 1.5F : 1; GlStateManager.scale(scale, scale, scale); } }
    @Override protected void applyRotations(EntityFish fish, float age, float yaw, float partial) { super.applyRotations(fish, age, yaw, partial); if (!fish.isInWater()) { GlStateManager.translate(.15F, .1F, 0); GlStateManager.rotate(90, 0, 0, 1); } }
}
