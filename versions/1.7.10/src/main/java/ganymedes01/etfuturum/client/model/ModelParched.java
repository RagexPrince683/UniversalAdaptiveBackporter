package ganymedes01.etfuturum.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeleton;

/** Released Parched geometry and 64x64 UVs, retaining native skeleton animation/equipment. */
public final class ModelParched extends ModelSkeleton {
    public ModelParched() {
        textureWidth = textureHeight = 64;
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4, -8, -4, 8, 8, 8);
        bipedHead.setTextureOffset(0, 32).addBox(-4, -8, -4, 8, 8, 8, .2F);
        bipedHeadwear = new ModelRenderer(this);
        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4, 0, -2, 8, 12, 4);
        bipedBody.setTextureOffset(28, 0).addBox(-4, 10, -2, 8, 1, 4);
        bipedBody.setTextureOffset(16, 48).addBox(-4, 0, -2, 8, 12, 4, .025F);
        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-1, -2, -1, 2, 12, 2);
        bipedRightArm.setTextureOffset(42, 33).addBox(-1.55F, -2.025F, -1.5F, 3, 12, 3);
        bipedRightArm.setRotationPoint(-5.5F, 2, 0);
        bipedLeftArm = new ModelRenderer(this, 56, 16);
        bipedLeftArm.addBox(-1, -2, -1, 2, 12, 2);
        bipedLeftArm.setTextureOffset(40, 48).addBox(-1.45F, -2.025F, -1.5F, 3, 12, 3);
        bipedLeftArm.setRotationPoint(5.5F, 2, 0);
        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-1, 0, -1, 2, 12, 2);
        bipedRightLeg.setTextureOffset(0, 49).addBox(-1.5F, 0, -1.5F, 3, 12, 3);
        bipedRightLeg.setRotationPoint(-2, 12, 0);
        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-1, 0, -1, 2, 12, 2);
        bipedLeftLeg.setTextureOffset(4, 49).addBox(-1.5F, 0, -1.5F, 3, 12, 3);
        bipedLeftLeg.setRotationPoint(2, 12, 0);
    }
}
