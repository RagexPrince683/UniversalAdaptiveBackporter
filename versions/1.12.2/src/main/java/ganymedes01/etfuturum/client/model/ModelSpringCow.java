package ganymedes01.etfuturum.client.model;

import net.minecraft.client.model.*;

/** Released cow geometry and 64x64 UVs, retaining native quadruped walking and baby transforms. */
public final class ModelSpringCow extends ModelCow {
    public ModelSpringCow(int coat) {
        textureWidth = textureHeight = 64;
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4, -4, -6, 8, 8, 6);
        head.setRotationPoint(0, 4, -8);
        head.setTextureOffset(coat == 2 ? 9 : 1, 33).addBox(-3, 1, -7, 6, 3, 1);
        if (coat == 2) {
            ModelRenderer right = new ModelRenderer(this, 0, 40);
            right.addBox(-1.5F, -4.5F, -.5F, 2, 6, 2);
            right.setRotationPoint(-4.5F, -2.5F, -3.5F); right.rotateAngleX = 1.5708F; head.addChild(right);
            ModelRenderer left = new ModelRenderer(this, 0, 32);
            left.addBox(-1.5F, -3, -.5F, 2, 6, 2);
            left.setRotationPoint(5.5F, -2.5F, -5); left.rotateAngleX = 1.5708F; head.addChild(left);
        } else if (coat == 1) {
            head.setTextureOffset(27, 0).addBox(-8, -3, -5, 4, 2, 2);
            head.setTextureOffset(39, 0).addBox(-8, -5, -5, 2, 2, 2);
            head.mirror = true;
            head.setTextureOffset(27, 0).addBox(4, -3, -5, 4, 2, 2);
            head.setTextureOffset(39, 0).addBox(6, -5, -5, 2, 2, 2);
            head.mirror = false;
        } else {
            head.setTextureOffset(22, 0).addBox(-5, -5, -5, 1, 3, 1);
            head.setTextureOffset(22, 0).addBox(4, -5, -5, 1, 3, 1);
        }
        body = new ModelRenderer(this, 18, 4);
        body.addBox(-6, -10, -7, 12, 18, 10);
        if (coat == 2) body.setTextureOffset(20, 32).addBox(-6, -10, -7, 12, 18, 10, .5F);
        body.setTextureOffset(52, 0).addBox(-2, 2, -8, 4, 6, 1);
        body.setRotationPoint(0, 5, 2);
        leg1 = leg(-4, 7, true); leg2 = leg(4, 7, false);
        leg3 = leg(-4, -6, true); leg4 = leg(4, -6, false);
    }
    private ModelRenderer leg(float x, float z, boolean mirror) {
        ModelRenderer leg = new ModelRenderer(this, 0, 16); leg.mirror = mirror;
        leg.addBox(-2, 0, -2, 4, 12, 4); leg.setRotationPoint(x, 12, z); return leg;
    }
}
