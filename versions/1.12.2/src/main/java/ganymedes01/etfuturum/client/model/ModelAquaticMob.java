package ganymedes01.etfuturum.client.model;

import ganymedes01.etfuturum.entities.*;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/** Retained native geometry using the 1.21.11 model dimensions and texture coordinates. */
public class ModelAquaticMob extends ModelBase {
	private final List<ModelRenderer> roots = new ArrayList<>();
	private final List<ModelRenderer> fins = new ArrayList<>();
	private ModelRenderer tail, body, eggBelly;
	private final int kind;
	public ModelAquaticMob(int kind) {
		this.kind = kind;
		textureWidth = textureHeight = kind == 7 || kind == 8 ? 64 : 32;
		if (kind == 9) { textureWidth = 128; textureHeight = 64; }
        switch (kind) {
            case 0: cod(); break;
            case 1: salmon(); break;
            case 2: case 3: tropical(kind == 3); break;
            case 4: case 5: case 6: puffer(kind - 4); break;
            case 7: dolphin(); break;
            case 8: axolotl(); break;
            case 9: turtle(); break;
            default: throw new IllegalArgumentException("Unknown fish model " + kind);
        }
	}
	private ModelRenderer part(ModelRenderer parent, int u, int v, float x, float y, float z, int w, int h, int d, float px, float py, float pz) {
		ModelRenderer part = new ModelRenderer(this, u, v); part.addBox(x, y, z, w, h, d); part.setRotationPoint(px, py, pz);
		if (parent == null) roots.add(part); else parent.addChild(part); return part;
	}
	private void cod() {
		part(null, 0, 0, -1, -2, 0, 2, 4, 7, 0, 22, 0); part(null, 11, 0, -1, -2, -3, 2, 4, 3, 0, 22, 0); part(null, 0, 0, -1, -2, -1, 2, 3, 1, 0, 22, -3);
		part(null, 22, 1, -2, 0, -1, 2, 0, 2, -1, 23, 0).rotateAngleZ = -.7854F;
		part(null, 22, 4, 0, 0, -1, 2, 0, 2, 1, 23, 0).rotateAngleZ = .7854F;
		tail = part(null, 22, 3, 0, -2, 0, 0, 4, 4, 0, 22, 7); part(null, 20, -6, 0, -1, -1, 0, 1, 6, 0, 20, 0);
	}
	private void salmon() {
		ModelRenderer front = part(null, 0, 0, -1.5F, -2.5F, 0, 3, 5, 8, 0, 20, -7.2F);
		tail = part(null, 0, 13, -1.5F, -2.5F, 0, 3, 5, 8, 0, 20, .8F);
		part(null, 22, 0, -1, -2, -3, 2, 4, 3, 0, 20, -7.2F); part(tail, 20, 10, 0, -2.5F, 0, 0, 5, 6, 0, 0, 8);
		part(front, 2, 1, 0, 0, 0, 0, 2, 3, 0, -4.5F, 5); part(tail, 0, 2, 0, 0, 0, 0, 2, 4, 0, -4.5F, -1);
		part(null, -4, 0, -2, 0, 0, 2, 0, 2, -1.5F, 21.5F, -7.2F).rotateAngleZ = -.7854F;
		part(null, 0, 0, 0, 0, 0, 2, 0, 2, 1.5F, 21.5F, -7.2F).rotateAngleZ = .7854F;
	}
	private void tropical(boolean large) {
		int height = large ? 6 : 3; float y = large ? 19 : 22, half = height / 2F;
		part(null, 0, large ? 20 : 0, -1, -half, -3, 2, height, 6, 0, y, 0);
		tail = part(null, large ? 21 : 22, large ? 16 : -6, 0, -half, 0, 0, height, large ? 5 : 6, 0, y, 3);
		part(null, 2, 16, -2, large ? 0 : -1, 0, 2, 2, 0, -1, y + (large ? 1 : .5F), 0).rotateAngleY = .7854F;
		part(null, 2, 12, 0, large ? 0 : -1, 0, 2, 2, 0, 1, y + (large ? 1 : .5F), 0).rotateAngleY = -.7854F;
		part(null, large ? 20 : 10, large ? 11 : -5, 0, large ? -4 : -3, 0, 0, large ? 4 : 3, 6, 0, y - (large ? 3 : 1.5F), -3);
		if (large) part(null, 20, 21, 0, 0, 0, 0, 4, 6, 0, 22, -3);
	}
	private void puffer(int state) {
		if (state == 0) {
			part(null, 0, 27, -1.5F, -2, -1.5F, 3, 2, 3, 0, 23, 0);
			part(null, 24, 6, -1.5F, 0, -1.5F, 1, 1, 1, 0, 20, 0); part(null, 28, 6, .5F, 0, -1.5F, 1, 1, 1, 0, 20, 0);
			part(null, -3, 0, -1.5F, 0, 0, 3, 0, 3, 0, 22, 1.5F);
			fins.add(part(null, 25, 0, -1, 0, 0, 1, 0, 2, -1.5F, 22, -1.5F)); fins.add(part(null, 25, 0, 0, 0, 0, 1, 0, 2, 1.5F, 22, -1.5F));
			return;
		}
		int width = state == 1 ? 5 : 8; float half = width / 2F, top = 22 - width;
		part(null, state == 1 ? 12 : 0, state == 1 ? 22 : 0, -half, -width, -half, width, width, width, 0, 22, 0);
		fins.add(part(null, 24, 0, -2, 0, -1, 2, state == 1 ? 0 : 1, 2, -half, top + 1, -half + 2));
		fins.add(part(null, 24, 3, 0, 0, -1, 2, state == 1 ? 0 : 1, 2, half, top + 1, -half + 2));
		for (int side : new int[]{-1, 1}) {
			part(null, 15, 17, -half, -1, 0, width, 1, 0, 0, top, side * half).rotateAngleX = -side * .7854F;
			part(null, 15, 20, -half, 0, 0, width, 1, 0, 0, 22, side * half).rotateAngleX = side * .7854F;
			part(null, 5, 17, -1, -width, 0, 1, width, 0, -half, 22, side * half).rotateAngleY = side * .7854F;
			part(null, 9, 17, 0, -width, 0, 1, width, 0, half, 22, side * half).rotateAngleY = -side * .7854F;
		}
		if (state == 2) { part(null, 14, 16, -4, -1, 0, 8, 1, 1, 0, 14, 0); part(null, 15, 20, -4, 0, 0, 8, 1, 0, 0, 22, 0); }
	}
	private void dolphin() {
		body = part(null, 22, 0, -4, -7, 0, 8, 7, 13, 0, 22, -5);
		part(body, 51, 0, -.5F, 0, 8, 1, 4, 5, 0, 0, 0).rotateAngleX = 1.0472F;
		ModelRenderer left = part(body, 48, 20, -.5F, -4, 0, 1, 4, 7, 2, -2, 4); left.rotateAngleX = 1.0472F; left.rotateAngleZ = 2.0944F;
		ModelRenderer right = part(body, 48, 20, -.5F, -4, 0, 1, 4, 7, -2, -2, 4); right.rotateAngleX = 1.0472F; right.rotateAngleZ = -2.0944F;
		tail = part(body, 0, 19, -2, -2.5F, 0, 4, 5, 11, 0, -2.5F, 11);
		part(tail, 19, 20, -5, -.5F, 0, 10, 1, 6, 0, 0, 9);
		ModelRenderer head = part(body, 0, 0, -4, -3, -3, 8, 7, 6, 0, -4, -3); part(head, 0, 13, -1, 2, -7, 2, 2, 4, 0, 0, 0);
	}
	private void axolotl() {
		body = part(null, 0, 11, -4, -2, -9, 8, 4, 10, 0, 20, 5); part(body, 2, 17, 0, -3, -8, 0, 5, 9, 0, 0, 0);
		ModelRenderer head = part(body, 0, 1, -4, -3, -5, 8, 5, 5, 0, 0, -9);
		part(head, 3, 37, -4, -3, 0, 8, 3, 0, 0, -3, -1);
		part(head, 0, 40, -3, -5, 0, 3, 7, 0, -4, 0, -1); part(head, 11, 40, 0, -5, 0, 3, 7, 0, 4, 0, -1);
		for (int side : new int[]{-1, 1}) for (int z : new int[]{-1, -8}) fins.add(part(body, 2, 13, side < 0 ? -2 : -1, 0, 0, 3, 5, 0, side * 3.5F, 1, z));
		tail = part(body, 2, 19, 0, -3, 0, 0, 5, 12, 0, 0, 1);
	}
	private void turtle() {
		part(null, 3, 0, -3, -1, -3, 6, 5, 6, 0, 19, -10);
		body = part(null, 7, 37, -9.5F, 3, -10, 19, 20, 6, 0, 11, -10); body.rotateAngleX = (float) Math.PI / 2;
		part(body, 31, 1, -5.5F, 3, -13, 11, 18, 3, 0, 0, 0);
		eggBelly = part(body, 70, 33, -4.5F, 3, -14, 9, 18, 1, 0, 0, 0);
		fins.add(part(null, 1, 23, -2, 0, 0, 4, 1, 10, -3.5F, 22, 11)); fins.add(part(null, 1, 12, -2, 0, 0, 4, 1, 10, 3.5F, 22, 11));
		fins.add(part(null, 27, 30, -13, 0, -2, 13, 1, 5, -5, 21, -4)); fins.add(part(null, 27, 24, 0, 0, -2, 13, 1, 5, 5, 21, -4));
	}
	@Override public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
		if(body!=null&&kind==8)body.rotateAngleZ=entity instanceof EntityAxolotl&&((EntityAxolotl)entity).isPlayingDead()?-.2F:0;
		if(eggBelly!=null)eggBelly.showModel=entity instanceof EntityTurtle&&((EntityTurtle)entity).hasEgg()&&!((EntityTurtle)entity).isChild();
		if (tail != null) { tail.rotateAngleY = -.45F * MathHelper.sin(age * .6F); if (kind == 7) { tail.rotateAngleY = 0; tail.rotateAngleX = -.1F * MathHelper.cos(age * .3F); } }
		for (int i = 0; i < fins.size(); i++) {
			ModelRenderer fin = fins.get(i); float side = i % 2 == 0 ? 1 : -1;
			if (kind >= 4 && kind <= 6) fin.rotateAngleZ = side * (-.2F + .4F * MathHelper.sin(age * .2F));
			else if (kind == 8) { fin.rotateAngleX = .7F + MathHelper.sin(age * .2F + i) * .3F; fin.rotateAngleY = side * .7F; }
			else if (kind == 9) { fin.rotateAngleY = entity.isInWater() ? 0 : side * MathHelper.cos(swing * 5) * amount * 2; fin.rotateAngleX = entity.isInWater() ? side * MathHelper.cos(swing * .4F) * amount : 0; }
		}
		for (ModelRenderer root : roots) root.render(scale);
	}
}
