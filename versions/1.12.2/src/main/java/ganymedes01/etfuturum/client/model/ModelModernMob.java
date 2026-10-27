package ganymedes01.etfuturum.client.model;

import com.google.gson.*;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
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

    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        for(Map.Entry<String,ModelRenderer> entry:parts.entrySet()){ModelRenderer part=entry.getValue();float[] pose=poses.get(entry.getKey());part.setRotationPoint(pose[0],pose[1],pose[2]);part.rotateAngleX=pose[3];part.rotateAngleY=pose[4];part.rotateAngleZ=pose[5];part.showModel=true;}
        rotate("head",pitch*.0174533F,yaw*.0174533F,0);float walk=MathHelper.cos(limb*.6662F)*1.2F*amount;
        rotate("right_leg",walk,0,0);rotate("left_leg",-walk,0,0);rotate("right_hind_leg",walk,0,0);rotate("left_hind_leg",-walk,0,0);rotate("right_front_leg",-walk,0,0);rotate("left_front_leg",walk,0,0);
        if(entity instanceof EntityPiglin){EntityPiglin piglin=(EntityPiglin)entity;boolean crossbow=piglin.getHeldItemMainhand().getItem() instanceof ganymedes01.etfuturum.CrossbowItem;rotate("right_arm",piglin.isAdmiring()?-.9F:piglin.isAttacking()?-1.3F:-walk,0,0);rotate("left_arm",piglin.isAdmiring()?-.9F:crossbow&&piglin.isAttacking()?-1.3F:walk,0,0);rotate("left_ear",0,0,-MathHelper.cos(age*.12F)*.1F);rotate("right_ear",0,0,MathHelper.cos(age*.1F)*.1F);}
        for(String name:parts.keySet())if(name.contains("tentacle"))parts.get(name).rotateAngleX=.2F+.3F*MathHelper.sin(age*.15F+name.hashCode()%9);
        if(entity instanceof EntityNautilus)rotate("body",MathHelper.sin(age*.12F)*.05F,MathHelper.sin(age*.1F)*.04F,0);
        if(entity instanceof EntityCamel&&((EntityCamel)entity).isSitting()){rotate("left_front_leg",-1.5F,0,0);rotate("right_front_leg",-1.5F,0,0);rotate("left_hind_leg",-1.5F,0,0);rotate("right_hind_leg",-1.5F,0,0);}
        if(entity instanceof EntityCopperGolem){boolean holding=!((EntityCopperGolem)entity).getHeldItemMainhand().isEmpty();rotate("right_arm",holding?-1:-walk,0,0);rotate("left_arm",holding?-1:walk,0,0);}
        if(entity instanceof EntityCreaking){rotate("right_arm",-walk*.5F,0,0);rotate("left_arm",walk*.5F,0,0);}
        if(entity instanceof EntityWarden){int state=((EntityWarden)entity).getAnimationState();rotate("right_arm",state==1?-1.5F:-walk*.5F,0,0);rotate("left_arm",state==1?-1.5F:walk*.5F,0,0);if(state>=2){rotate("body",.6F,0,0);rotate("head",-.5F,0,0);}rotate("left_tendril",0,0,MathHelper.cos(age*.15F)*.15F);rotate("right_tendril",0,0,-MathHelper.cos(age*.15F)*.15F);}
        if(entity instanceof EntityAllay){rotate("right_wing",0,.6F+MathHelper.cos(age*.8F)*.5F,0);rotate("left_wing",0,-.6F-MathHelper.cos(age*.8F)*.5F,0);rotate("right_arm",-.7F,0,0);rotate("left_arm",-.7F,0,0);if(((EntityAllay)entity).isDancing())rotate("head",0,0,MathHelper.sin(age*.4F)*.35F);}
        if(entity instanceof EntityPanda&&((EntityPanda)entity).isEating()){rotate("body",-.9F,0,0);rotate("head",.5F+MathHelper.sin(age*.3F)*.1F,0,0);rotate("right_front_leg",-1.1F,0,0);rotate("left_front_leg",-1.1F,0,0);}
        if(entity instanceof EntityArmadillo){boolean rolled=((EntityArmadillo)entity).isRolled();show("body",!rolled);show("head",!rolled);show("right_hind_leg",!rolled);show("left_hind_leg",!rolled);show("right_front_leg",!rolled);show("left_front_leg",!rolled);show("cube",rolled);}
        if(entity instanceof EntitySniffer&&((EntitySniffer)entity).isDigging())rotate("head",.5F+MathHelper.sin(age*.5F)*.15F,0,0);
        if(entity instanceof EntityPillager){EntityPillager mob=(EntityPillager)entity;show("arms",false);show("right_arm",true);show("left_arm",true);if(mob.getCrossbowChargeTicks()>=0){float charge=MathHelper.clamp((float)mob.getCrossbowChargeTicks()/ganymedes01.etfuturum.CrossbowItem.chargeTime(mob.getHeldItemMainhand()),0,1);rotate("right_arm",-.97F,-.8F,0);rotate("left_arm",-.97F-charge*.6F,.4F+charge*.45F,0);}else if(mob.isAttacking()&&ganymedes01.etfuturum.CrossbowItem.charged(mob.getHeldItemMainhand())){rotate("right_arm",-(float)Math.PI/2+pitch*.0174533F,yaw*.0174533F-.3F,0);rotate("left_arm",-1.5F+pitch*.0174533F,yaw*.0174533F+.6F,0);}else{rotate("right_arm",-walk*.5F-.314159F,0,0);rotate("left_arm",walk,0,0);}}
        if(kind.startsWith("breeze")){rotate("rods",0,age*.1F,0);rotate("wind_mid",0,-age*.08F,0);rotate("wind_top",0,age*.06F,0);}
        if(entity instanceof EntityStrider){rotate("body",pitch*.0174533F,yaw*.0174533F,MathHelper.sin(limb*1.5F)*amount*.1F);for(String name:parts.keySet())if(name.contains("bristle"))rotate(name.substring(name.lastIndexOf('/')+1),0,0,MathHelper.sin(age*.2F+name.hashCode()%5)*.1F);}
        GL11.glPushMatrix();if(entity instanceof EntityHappyGhast){float size=((EntityHappyGhast)entity).isChild()?.95F:4;GL11.glTranslatef(0,24*(1-size)*scale,0);GL11.glScalef(size,size,size);}else if(entity instanceof net.minecraft.entity.EntityLivingBase&&((net.minecraft.entity.EntityLivingBase)entity).isChild()&&!(entity instanceof EntityNautilus)){GL11.glTranslatef(0,12*scale,0);GL11.glScalef(.5F,.5F,.5F);}else if(entity instanceof EntityCamel&&((EntityCamel)entity).isSitting())GL11.glTranslatef(0,10*scale,0);root.render(scale);GL11.glPopMatrix();
    }
}
