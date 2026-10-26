package ganymedes01.etfuturum.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.MathHelper;

/** Released right-handed crossbow poses, shared by native bipeds and the illager model. */
public final class CrossbowPose {
	private CrossbowPose() { }
	public static void charge(ModelRenderer right, ModelRenderer left, float ticks, int duration) {
		float progress = MathHelper.clamp_float(ticks / duration, 0, 1);
		right.rotateAngleY = -.8F; right.rotateAngleX = -.97079635F;
		left.rotateAngleY = .4F + .45F * progress;
		left.rotateAngleX = -.97079635F + (-1.5707964F + .97079635F) * progress;
		right.rotateAngleZ = left.rotateAngleZ = 0;
	}
	public static void hold(ModelRenderer right, ModelRenderer left, ModelRenderer head) {
		right.rotateAngleY = -.3F + head.rotateAngleY; left.rotateAngleY = .6F + head.rotateAngleY;
		right.rotateAngleX = -1.5707964F + head.rotateAngleX + .1F;
		left.rotateAngleX = -1.5F + head.rotateAngleX;
		right.rotateAngleZ = left.rotateAngleZ = 0;
	}
}
