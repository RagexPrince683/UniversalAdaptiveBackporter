package ganymedes01.etfuturum.client.model;

import com.google.gson.*;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import java.io.*;
import java.util.*;

/** Retained native boxes and pivots from the released vanilla models; no mesh rebuilding per frame. */
public class ModelModernMob extends ModelBase {
	private static JsonObject definitions;
	private final ModelRenderer root;
	private final Map<String, ModelRenderer> parts = new LinkedHashMap<>();
	private final Map<String, float[]> poses = new LinkedHashMap<>();
	private final String kind;
	public ModelModernMob(String kind) {
		this.kind = kind;
		if (definitions == null) try (Reader reader = new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("etfuturum:models/mobs.json")).getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) { definitions = new JsonParser().parse(reader).getAsJsonObject(); } catch (IOException exception) { throw new IllegalStateException("Missing native mob model definitions", exception); }
		JsonObject definition = definitions.getAsJsonObject(kind);
		if (definition == null) throw new IllegalArgumentException("Unknown mob model " + kind);
		textureWidth = definition.get("width").getAsInt(); textureHeight = definition.get("height").getAsInt();
		root = new ModelRenderer(this); JsonArray offset = definition.getAsJsonArray("offset"); root.setRotationPoint(offset.get(0).getAsFloat(), offset.get(1).getAsFloat(), offset.get(2).getAsFloat());
		for (JsonElement element : definition.getAsJsonArray("parts")) {
			JsonObject part = element.getAsJsonObject(); String name = part.get("name").getAsString(), parent = part.get("parent").getAsString(); ModelRenderer node = new ModelRenderer(this); float[] pose = new float[6];
			for (int i = 0; i < 6; i++) pose[i] = part.getAsJsonArray("pose").get(i).getAsFloat();
			node.setRotationPoint(pose[0], pose[1], pose[2]); node.rotateAngleX = pose[3]; node.rotateAngleY = pose[4]; node.rotateAngleZ = pose[5];
			for (JsonElement box : part.getAsJsonArray("cubes")) { JsonArray cube = box.getAsJsonArray(); node.mirror = cube.get(9).getAsBoolean(); node.setTextureOffset(cube.get(0).getAsInt(), cube.get(1).getAsInt()); node.addBox(cube.get(2).getAsFloat(), cube.get(3).getAsFloat(), cube.get(4).getAsFloat(), cube.get(5).getAsInt(), cube.get(6).getAsInt(), cube.get(7).getAsInt(), cube.get(8).getAsFloat()); }
			(parent.isEmpty() ? root : parts.get(parent)).addChild(node); parts.put(name, node); poses.put(name, pose);
		}
	}
	private ModelRenderer find(String name) { for (Map.Entry<String, ModelRenderer> part : parts.entrySet()) if (part.getKey().equals(name) || part.getKey().endsWith("/" + name)) return part.getValue(); return null; }
	public void poseArm(float scale) { root.postRender(scale); String path = null; for (String name : parts.keySet()) if (name.equals("right_arm") || name.endsWith("/right_arm")) { path = name; break; } if (path != null) { String current = ""; for (String segment : path.split("/")) { current += (current.isEmpty() ? "" : "/") + segment; parts.get(current).postRender(scale); } } }
	private void rotate(String name, float x, float y, float z) { ModelRenderer part = find(name); if (part != null) { part.rotateAngleX += x; part.rotateAngleY += y; part.rotateAngleZ += z; } }
	private void show(String name, boolean show) { ModelRenderer part = find(name); if (part != null) part.showModel = show; }
	@Override public void render(Entity entity, float limb, float amount, float age, float yaw, float pitch, float scale) {
		for (Map.Entry<String, ModelRenderer> entry : parts.entrySet()) { ModelRenderer part = entry.getValue(); float[] pose = poses.get(entry.getKey()); part.setRotationPoint(pose[0], pose[1], pose[2]); part.rotateAngleX = pose[3]; part.rotateAngleY = pose[4]; part.rotateAngleZ = pose[5]; part.showModel = true; }
		rotate("head", pitch * .0174533F, yaw * .0174533F, 0);
		float walk = MathHelper.cos(limb * .6662F) * 1.2F * amount;
		rotate("right_leg", walk, 0, 0); rotate("left_leg", -walk, 0, 0); rotate("right_hind_leg", walk, 0, 0); rotate("left_hind_leg", -walk, 0, 0); rotate("right_front_leg", -walk, 0, 0); rotate("left_front_leg", walk, 0, 0);
		for (String name : parts.keySet()) if (name.contains("tentacle")) parts.get(name).rotateAngleX = .2F + .3F * MathHelper.sin(age * .15F + name.hashCode() % 9);
		if (entity instanceof EntityLlama llama) { show("right_chest", llama.hasChest()); show("left_chest", llama.hasChest()); }
		if (entity instanceof EntityIllager illager && !(entity instanceof EntityRavager)) {
			boolean crossbow = illager.getHeldItem() != null && illager.getHeldItem().getItem() instanceof ganymedes01.etfuturum.items.ItemCrossbow;
			boolean armed = illager.isAttacking() || crossbow;
			show("arms", !armed); show("right_arm", armed); show("left_arm", armed);
			if (crossbow && entity instanceof EntityPillager pillager) {
				if (pillager.getCrossbowChargeTicks() >= 0) CrossbowPose.charge(find("right_arm"), find("left_arm"), pillager.getCrossbowChargeTicks() + MathHelper.clamp_float(age - entity.ticksExisted, 0, 1), ganymedes01.etfuturum.items.ItemCrossbow.chargeTime(illager.getHeldItem()));
				else if (illager.isAttacking() && ganymedes01.etfuturum.items.ItemCrossbow.charged(illager.getHeldItem())) CrossbowPose.hold(find("right_arm"), find("left_arm"), find("head"));
				else { rotate("right_arm", -walk * .5F - .314159F, 0, 0); rotate("left_arm", walk, 0, 0); }
			} else {
				rotate("right_arm", armed ? -1.3F : -walk, 0, 0);
				rotate("left_arm", entity instanceof EntityIllusioner ? -1.3F : walk, 0, 0);
			}
		}
		if (entity instanceof EntityVex || entity instanceof EntityAllay) { rotate("right_wing", 0, .6F + MathHelper.cos(age * .8F) * .5F, 0); rotate("left_wing", 0, -.6F - MathHelper.cos(age * .8F) * .5F, 0); rotate("right_arm", -.6F, 0, -.2F); rotate("left_arm", -.6F, 0, .2F); }
		if (entity instanceof EntityAllay allay && allay.isDancing()) rotate("body", 0, age * .15F, MathHelper.sin(age * .3F) * .3F);
		if (entity instanceof EntityPiglin piglin) { rotate("right_arm", piglin.isAdmiring() ? -.9F : piglin.isAttacking() ? -1.3F : -walk, 0, 0); rotate("left_arm", piglin.isAdmiring() ? -.9F : walk, 0, 0); rotate("left_ear", 0, 0, -MathHelper.cos(age * .12F) * .1F); rotate("right_ear", 0, 0, MathHelper.cos(age * .1F) * .1F); }
		if (entity instanceof EntityCreaking) { rotate("right_arm", -walk * .5F, 0, 0); rotate("left_arm", walk * .5F, 0, 0); }
		if (entity instanceof EntityStrider) { rotate("body", pitch * .0174533F, yaw * .0174533F, MathHelper.sin(limb * 1.5F) * amount * .1F); for (String name : parts.keySet()) if (name.contains("bristle")) rotate(name.substring(name.lastIndexOf('/') + 1), 0, 0, MathHelper.sin(age * .2F + name.hashCode() % 5) * .1F); }
		if (entity instanceof EntityWarden warden) { int state = warden.getAnimationState(); rotate("right_arm", state == 1 ? -1.5F : -walk * .5F, 0, 0); rotate("left_arm", state == 1 ? -1.5F : walk * .5F, 0, 0); if (state >= 2) { rotate("body", .6F, 0, 0); rotate("head", -.5F, 0, 0); } rotate("left_tendril", 0, 0, MathHelper.cos(age * .15F) * .15F); rotate("right_tendril", 0, 0, -MathHelper.cos(age * .15F) * .15F); }
		if (entity instanceof EntityCopperGolem golem) { rotate("right_arm", golem.getHeldItem() == null ? -walk : -1, 0, 0); rotate("left_arm", golem.getHeldItem() == null ? walk : -1, 0, 0); }
		if (entity instanceof EntityPanda panda && panda.isEating()) { rotate("body", -.9F, 0, 0); rotate("head", .5F + MathHelper.sin(age * .3F) * .1F, 0, 0); rotate("right_front_leg", -1.1F, 0, 0); rotate("left_front_leg", -1.1F, 0, 0); }
		if (entity instanceof EntityArmadillo armadillo && armadillo.isRolled()) { show("body", false); show("right_hind_leg", false); show("left_hind_leg", false); show("right_front_leg", false); show("left_front_leg", false); show("cube", true); } else if (kind.equals("armadillo")) show("cube", false);
		if (entity instanceof EntityCamel camel && camel.isSitting()) { rotate("left_front_leg", -1.5F, 0, 0); rotate("right_front_leg", -1.5F, 0, 0); rotate("left_hind_leg", -1.5F, 0, 0); rotate("right_hind_leg", -1.5F, 0, 0); }
		if (entity instanceof EntitySniffer sniffer && sniffer.isDigging()) { rotate("head", .5F + MathHelper.sin(age * .5F) * .15F, 0, 0); }
		if (entity instanceof EntityNautilus) { rotate("body", MathHelper.sin(age * .12F) * .05F, MathHelper.sin(age * .1F) * .04F, 0); }
		if (kind.startsWith("breeze")) { rotate("rods", 0, age * .1F, 0); rotate("wind_mid", 0, -age * .08F, 0); rotate("wind_top", 0, age * .06F, 0); }
		GL11.glPushMatrix();
		if (entity instanceof EntityHappyGhast) { float size = entity instanceof net.minecraft.entity.EntityAgeable ageable && ageable.isChild() ? .95F : 4; GL11.glTranslatef(0, 24 * (1 - size) * scale, 0); GL11.glScalef(size, size, size); }
		else if (entity instanceof net.minecraft.entity.EntityLivingBase living && living.isChild() && !(entity instanceof EntityNautilus)) { GL11.glTranslatef(0, 12 * scale, 0); GL11.glScalef(.5F, .5F, .5F); }
		else if (entity instanceof EntityCamel camel && camel.isSitting()) GL11.glTranslatef(0, 10 * scale, 0);
		root.render(scale); GL11.glPopMatrix();
	}
}
