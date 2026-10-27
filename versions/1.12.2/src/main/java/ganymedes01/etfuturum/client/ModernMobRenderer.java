package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.client.model.ModelModernMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.passive.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

final class ModernMobRenderer extends RenderLiving<EntityLiving> {
    private final String kind,path;
    private final ModelModernMob model,baby,coral;
    ModernMobRenderer(RenderManager manager,String kind,String path,float shadow){super(manager,new ModelModernMob(kind),shadow);this.kind=kind;this.path=path;model=(ModelModernMob)mainModel;baby=kind.equals("nautilus")?new ModelModernMob("nautilus_baby"):null;coral=kind.equals("nautilus")?new ModelModernMob("nautilus_coral"):null;if(kind.equals("camel")||kind.equals("happy_ghast")||kind.equals("nautilus"))addLayer(new MountEquipment());if(kind.equals("creaking"))addLayer(new ModelLayer("creaking","creaking/creaking_eyes",false));if(kind.equals("allay")||kind.equals("illager")||kind.equals("copper_golem"))addLayer(new Equipment());if(kind.equals("warden"))addLayer(new ModelLayer("warden","warden/warden_bioluminescent_layer",false));if(kind.equals("breeze")){addLayer(new ModelLayer("breeze_wind","breeze/breeze_wind",true));addLayer(new ModelLayer("breeze","breeze/breeze_eyes",false));}}
    static ResourceLocation texture(String path){return new ResourceLocation(UAB.ID,"textures/entity/"+path+".png");}
    @Override protected void preRenderCallback(EntityLiving entity,float partial){mainModel=coral!=null&&entity instanceof EntityZombieNautilus&&((EntityZombieNautilus)entity).getVariant()==1?coral:baby!=null&&entity.isChild()?baby:model;}
    @Override protected ResourceLocation getEntityTexture(EntityLiving entity){if(entity instanceof EntityHappyGhast)return texture("ghast/"+(entity.isChild()?"ghastling":"happy_ghast"));if(entity instanceof EntityZombieNautilus)return texture("nautilus/"+(((EntityZombieNautilus)entity).getVariant()==1?"zombie_nautilus_coral":"zombie_nautilus"));if(entity instanceof EntityNautilus&&entity.isChild())return texture("nautilus/nautilus_baby");if(entity instanceof EntityCopperGolem)return texture("copper_golem/"+new String[]{"copper_golem","exposed_copper_golem","weathered_copper_golem","oxidized_copper_golem"}[((EntityCopperGolem)entity).getOxidation()]);if(entity instanceof EntityPanda){String[] genes={"panda","lazy_panda","worried_panda","playful_panda","brown_panda","weak_panda","aggressive_panda"};return texture("panda/"+genes[((EntityPanda)entity).getGene()]);}return texture(path);}
    private final class Equipment implements LayerRenderer<EntityLiving> {
        @Override public void doRenderLayer(EntityLiving entity,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(entity.getHeldItemMainhand().isEmpty())return;GlStateManager.pushMatrix();if(entity.isChild()){GlStateManager.translate(0,.75,0);GlStateManager.scale(.5,.5,.5);}model.poseArm(scale);GlStateManager.translate(-.07,.5,-.1);GlStateManager.rotate(-90,1,0,0);GlStateManager.scale(.6,.6,.6);Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity,entity.getHeldItemMainhand(),ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,false);GlStateManager.popMatrix();}
        @Override public boolean shouldCombineTextures(){return false;}
    }
    private final class MountEquipment implements LayerRenderer<EntityLiving> {
        private final ModelModernMob saddle=new ModelModernMob(kind.equals("happy_ghast")?"harness":kind.equals("camel")?"camel_saddle":"nautilus_saddle");
        private final ModelModernMob armor=kind.equals("nautilus")?new ModelModernMob("nautilus_armor"):null;
        @Override public void doRenderLayer(EntityLiving entity,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){EntityRideableMob mount=(EntityRideableMob)entity;if(mount.isSaddled()){String path=mount instanceof EntityHappyGhast?"equipment/happy_ghast_body/"+EnumDyeColor.byMetadata(mount.getVariant()).getName()+"_harness":mount instanceof EntityCamel?"equipment/"+(mount instanceof EntityCamelHusk?"camel_husk":"camel")+"_saddle/saddle":"equipment/nautilus_saddle/saddle";bindTexture(texture(path));saddle.render(entity,limb,amount,age,yaw,pitch,scale);}if(armor!=null&&!mount.getBodyEquipment().isEmpty()){int material=net.minecraft.util.math.MathHelper.clamp(mount.getBodyEquipment().getMetadata(),0,4);bindTexture(texture("equipment/nautilus_body/"+new String[]{"copper","gold","iron","diamond","netherite"}[material]));armor.render(entity,limb,amount,age,yaw,pitch,scale);}}
        @Override public boolean shouldCombineTextures(){return false;}
    }
    private final class ModelLayer implements LayerRenderer<EntityLiving> {
        private final ModelModernMob layer;private final String texture;private final boolean wind;
        ModelLayer(String kind,String texture,boolean wind){layer=new ModelModernMob(kind);this.texture=texture;this.wind=wind;}
        @Override public void doRenderLayer(EntityLiving entity,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){bindTexture(texture(texture));boolean luminous=texture.endsWith("_eyes")||texture.endsWith("bioluminescent_layer");if(luminous){GlStateManager.disableLighting();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240,240);}GlStateManager.enableBlend();GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);if(wind){GlStateManager.matrixMode(5890);GlStateManager.loadIdentity();GlStateManager.translate(age*.02,0,0);GlStateManager.matrixMode(5888);}layer.render(entity,limb,amount,age,yaw,pitch,scale);if(wind){GlStateManager.matrixMode(5890);GlStateManager.loadIdentity();GlStateManager.matrixMode(5888);}GlStateManager.disableBlend();if(luminous){int light=entity.getBrightnessForRender();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>16);GlStateManager.enableLighting();}GlStateManager.color(1,1,1,1);}
        @Override public boolean shouldCombineTextures(){return false;}
    }
    static void register(){
        RenderingRegistry.registerEntityRenderingHandler(EntityCamel.class,manager->new ModernMobRenderer(manager,"camel","camel/camel",.9F));
        RenderingRegistry.registerEntityRenderingHandler(EntityCamelHusk.class,manager->new ModernMobRenderer(manager,"camel","camel/camel_husk",.9F));
        RenderingRegistry.registerEntityRenderingHandler(EntityHappyGhast.class,manager->new ModernMobRenderer(manager,"happy_ghast","ghast/happy_ghast",1.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityNautilus.class,manager->new ModernMobRenderer(manager,"nautilus","nautilus/nautilus",.6F));
        RenderingRegistry.registerEntityRenderingHandler(EntityZombieNautilus.class,manager->new ModernMobRenderer(manager,"nautilus","nautilus/zombie_nautilus",.6F));
        RenderingRegistry.registerEntityRenderingHandler(EntityFrog.class,manager->new AmphibianRenderer(manager,false));
        RenderingRegistry.registerEntityRenderingHandler(EntityTadpole.class,manager->new AmphibianRenderer(manager,true));
        RenderingRegistry.registerEntityRenderingHandler(EntityCopperGolem.class,manager->new ModernMobRenderer(manager,"copper_golem","copper_golem/copper_golem",.4F));
        RenderingRegistry.registerEntityRenderingHandler(EntityCreaking.class,manager->new ModernMobRenderer(manager,"creaking","creaking/creaking",.6F));
        RenderingRegistry.registerEntityRenderingHandler(EntityWarden.class,manager->new ModernMobRenderer(manager,"warden","warden/warden",.8F));
        RenderingRegistry.registerEntityRenderingHandler(EntityPanda.class,manager->new ModernMobRenderer(manager,"panda","panda/panda",.9F));
        RenderingRegistry.registerEntityRenderingHandler(EntityArmadillo.class,manager->new ModernMobRenderer(manager,"armadillo","armadillo",.4F));
        RenderingRegistry.registerEntityRenderingHandler(EntityAllay.class,manager->new ModernMobRenderer(manager,"allay","allay/allay",.2F));
        RenderingRegistry.registerEntityRenderingHandler(EntitySniffer.class,manager->new ModernMobRenderer(manager,"sniffer","sniffer/sniffer",1));
        RenderingRegistry.registerEntityRenderingHandler(EntityBreeze.class,manager->new ModernMobRenderer(manager,"breeze","breeze/breeze",.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityPillager.class,manager->new ModernMobRenderer(manager,"illager","illager/pillager",.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityRavager.class,manager->new ModernMobRenderer(manager,"ravager","illager/ravager",1.1F));
        RenderingRegistry.registerEntityRenderingHandler(EntityCat.class,Cat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBogged.class,manager->new Skeleton(manager,true));
        RenderingRegistry.registerEntityRenderingHandler(EntityParched.class,manager->new Skeleton(manager,false));
        RenderingRegistry.registerEntityRenderingHandler(EntityWanderingTrader.class,manager->new RenderVillager(manager){@Override protected ResourceLocation getEntityTexture(EntityVillager entity){return texture("wandering_trader");}});
        RenderingRegistry.registerEntityRenderingHandler(EntityTraderLlama.class,manager->new RenderLlama(manager){@Override protected ResourceLocation getEntityTexture(EntityLlama entity){return texture("llama/creamy");}});
        RenderingRegistry.registerEntityRenderingHandler(EntityBrownMooshroom.class,BrownMooshroomRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityWindCharge.class,manager->new RenderSnowball<EntityWindCharge>(manager,Content.item("wind_charge"),Minecraft.getMinecraft().getRenderItem()));
    }
    private static final class Cat extends RenderOcelot {
        Cat(RenderManager manager){super(manager);addLayer(new LayerRenderer<EntityOcelot>(){
            @Override public void doRenderLayer(EntityOcelot entity,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(!entity.isTamed())return;bindTexture(texture("cat/cat_collar"));float[] color=EnumDyeColor.byMetadata(((EntityCat)entity).getCollarColor()).getColorComponentValues();GlStateManager.color(color[0],color[1],color[2]);getMainModel().render(entity,limb,amount,age,yaw,pitch,scale);GlStateManager.color(1,1,1,1);}
            @Override public boolean shouldCombineTextures(){return false;}
        });}
        @Override protected ResourceLocation getEntityTexture(EntityOcelot entity){String[] coats={"tabby","black","red","siamese","british_shorthair","calico","persian","ragdoll","white","jellie","all_black"};return texture("cat/"+coats[((EntityCat)entity).getVariant()]);}
    }
    private static final class Skeleton extends RenderSkeleton {
        private final boolean bogged;
        Skeleton(RenderManager manager,boolean bogged){super(manager);this.bogged=bogged;if(bogged)addLayer(new LayerRenderer<AbstractSkeleton>(){
            private final ModelZombie overlay=new ModelZombie(.25F,true);
            @Override public void doRenderLayer(AbstractSkeleton entity,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(((EntityBogged)entity).isSheared())return;bindTexture(texture("skeleton/bogged_overlay"));overlay.setModelAttributes(getMainModel());overlay.setLivingAnimations(entity,limb,amount,partial);overlay.render(entity,limb,amount,age,yaw,pitch,scale);}
            @Override public boolean shouldCombineTextures(){return false;}
        });}
        @Override protected ResourceLocation getEntityTexture(AbstractSkeleton entity){return texture("skeleton/"+(bogged?"bogged":"parched"));}
    }
}
