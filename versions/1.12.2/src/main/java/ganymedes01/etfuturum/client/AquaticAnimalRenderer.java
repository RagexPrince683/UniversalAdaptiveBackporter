package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.UAB;
import ganymedes01.etfuturum.entities.EntityAquaticAnimal;
import ganymedes01.etfuturum.client.model.ModelAquaticMob;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;

final class AquaticAnimalRenderer extends RenderLiving<EntityAquaticAnimal> {
    private final ResourceLocation texture;private final int kind;
    private static final String[] AXOLOTLS={"lucy","wild","gold","cyan","blue"};
    AquaticAnimalRenderer(RenderManager manager,int kind){super(manager,new ModelAquaticMob(kind),.45F);this.kind=kind;texture=new ResourceLocation(UAB.ID,"textures/entity/"+(kind==7?"dolphin.png":"turtle/big_sea_turtle.png"));}
    @Override protected ResourceLocation getEntityTexture(EntityAquaticAnimal animal){return kind==8?new ResourceLocation(UAB.ID,"textures/entity/axolotl/axolotl_"+AXOLOTLS[((ganymedes01.etfuturum.entities.EntityAxolotl)animal).getVariant()]+".png"):texture;}
    @Override protected void preRenderCallback(EntityAquaticAnimal animal,float partial){if(animal.isChild()){float scale=kind==9?.3F:.5F;GlStateManager.scale(scale,scale,scale);}}
}
