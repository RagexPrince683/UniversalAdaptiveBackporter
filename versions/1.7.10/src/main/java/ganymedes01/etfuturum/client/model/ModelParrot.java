package ganymedes01.etfuturum.client.model;
import net.minecraft.client.model.*;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import ganymedes01.etfuturum.entities.EntityParrot;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelParrot extends ModelBase {
   ModelRenderer body;
   ModelRenderer tail;
   ModelRenderer wingLeft;
   ModelRenderer wingRight;
   ModelRenderer head;
   ModelRenderer head2;
   ModelRenderer beak1;
   ModelRenderer beak2;
   ModelRenderer feather;
   ModelRenderer legLeft;
   ModelRenderer legRight;
   private enum State { STANDING, FLYING, SITTING, PARTY }
   private State state = State.STANDING;

   public ModelParrot() {
      this.textureWidth = 32;
      this.textureHeight = 32;
      this.body = new ModelRenderer(this, 2, 8);
      this.body.addBox(-1.5F, 0.0F, -1.5F, 3, 6, 3);
      this.body.setRotationPoint(0.0F, 16.5F, -3.0F);
      this.tail = new ModelRenderer(this, 22, 1);
      this.tail.addBox(-1.5F, -1.0F, -1.0F, 3, 4, 1);
      this.tail.setRotationPoint(0.0F, 21.07F, 1.16F);
      this.wingLeft = new ModelRenderer(this, 19, 8);
      this.wingLeft.addBox(-0.5F, 0.0F, -1.5F, 1, 5, 3);
      this.wingLeft.setRotationPoint(1.5F, 16.94F, -2.76F);
      this.wingRight = new ModelRenderer(this, 19, 8);
      this.wingRight.addBox(-0.5F, 0.0F, -1.5F, 1, 5, 3);
      this.wingRight.setRotationPoint(-1.5F, 16.94F, -2.76F);
      this.head = new ModelRenderer(this, 2, 2);
      this.head.addBox(-1.0F, -1.5F, -1.0F, 2, 3, 2);
      this.head.setRotationPoint(0.0F, 15.69F, -2.76F);
      this.head2 = new ModelRenderer(this, 10, 0);
      this.head2.addBox(-1.0F, -0.5F, -2.0F, 2, 1, 4);
      this.head2.setRotationPoint(0.0F, -2.0F, -1.0F);
      this.head.addChild(this.head2);
      this.beak1 = new ModelRenderer(this, 11, 7);
      this.beak1.addBox(-0.5F, -1.0F, -0.5F, 1, 2, 1);
      this.beak1.setRotationPoint(0.0F, -0.5F, -1.5F);
      this.head.addChild(this.beak1);
      this.beak2 = new ModelRenderer(this, 16, 7);
      this.beak2.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
      this.beak2.setRotationPoint(0.0F, -1.75F, -2.45F);
      this.head.addChild(this.beak2);
      this.feather = new ModelRenderer(this, 2, 18);
      this.feather.addBox(0.0F, -4.0F, -2.0F, 0, 5, 4);
      this.feather.setRotationPoint(0.0F, -2.15F, 0.15F);
      this.head.addChild(this.feather);
      this.legLeft = new ModelRenderer(this, 14, 18);
      this.legLeft.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
      this.legLeft.setRotationPoint(1.0F, 22.0F, -1.05F);
      this.legRight = new ModelRenderer(this, 14, 18);
      this.legRight.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
      this.legRight.setRotationPoint(-1.0F, 22.0F, -1.05F);
   }

   public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
      this.body.render(scale);
      this.wingLeft.render(scale);
      this.wingRight.render(scale);
      this.tail.render(scale);
      this.head.render(scale);
      this.legLeft.render(scale);
      this.legRight.render(scale);
   }

   public void setRotationAngles(
      float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn
   ) {
      float l = ageInTicks * 0.3F;
      this.head.rotateAngleX = headPitch * (float) (Math.PI / 180.0);
      this.head.rotateAngleY = netHeadYaw * (float) (Math.PI / 180.0);
      this.head.rotateAngleZ = 0.0F;
      this.head.rotationPointX = 0.0F;
      this.body.rotationPointX = 0.0F;
      this.tail.rotationPointX = 0.0F;
      this.wingRight.rotationPointX = -1.5F;
      this.wingLeft.rotationPointX = 1.5F;
      if (this.state != State.FLYING) {
         if (this.state == State.SITTING) {
            return;
         }

         if (this.state == State.PARTY) {
            float m = MathHelper.cos(entityIn.ticksExisted);
            float n = MathHelper.sin(entityIn.ticksExisted);
            this.head.rotationPointX = m;
            this.head.rotationPointY = 15.69F + n;
            this.head.rotateAngleX = 0.0F;
            this.head.rotateAngleY = 0.0F;
            this.head.rotateAngleZ = MathHelper.sin(entityIn.ticksExisted) * 0.4F;
            this.body.rotationPointX = m;
            this.body.rotationPointY = 16.5F + n;
            this.wingLeft.rotateAngleZ = -0.0873F - ageInTicks;
            this.wingLeft.rotationPointX = 1.5F + m;
            this.wingLeft.rotationPointY = 16.94F + n;
            this.wingRight.rotateAngleZ = 0.0873F + ageInTicks;
            this.wingRight.rotationPointX = -1.5F + m;
            this.wingRight.rotationPointY = 16.94F + n;
            this.tail.rotationPointX = m;
            this.tail.rotationPointY = 21.07F + n;
            return;
         }

         this.legLeft.rotateAngleX = this.legLeft.rotateAngleX + MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         this.legRight.rotateAngleX = this.legRight.rotateAngleX + MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
      }

      this.head.rotationPointY = 15.69F + l;
      this.tail.rotateAngleX = 1.015F + MathHelper.cos(limbSwing * 0.6662F) * 0.3F * limbSwingAmount;
      this.tail.rotationPointY = 21.07F + l;
      this.body.rotationPointY = 16.5F + l;
      this.wingLeft.rotateAngleZ = -0.0873F - ageInTicks;
      this.wingLeft.rotationPointY = 16.94F + l;
      this.wingRight.rotateAngleZ = 0.0873F + ageInTicks;
      this.wingRight.rotationPointY = 16.94F + l;
      this.legLeft.rotationPointY = 22.0F + l;
      this.legRight.rotationPointY = 22.0F + l;
   }

   public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime) {
      this.feather.rotateAngleX = -0.2214F;
      this.body.rotateAngleX = 0.4937F;
      this.wingLeft.rotateAngleX = -0.6981F;
      this.wingLeft.rotateAngleY = (float) -Math.PI;
      this.wingRight.rotateAngleX = -0.6981F;
      this.wingRight.rotateAngleY = (float) -Math.PI;
      this.legLeft.rotateAngleX = -0.0299F;
      this.legRight.rotateAngleX = -0.0299F;
      this.legLeft.rotationPointY = 22.0F;
      this.legRight.rotationPointY = 22.0F;
      if (entitylivingbaseIn instanceof EntityParrot) {
         EntityParrot entityParrot = (EntityParrot)entitylivingbaseIn;
         if (entityParrot.isPartying()) {
            this.legLeft.rotateAngleZ = (float) (-Math.PI / 9);
            this.legRight.rotateAngleZ = (float) (Math.PI / 9);
            this.state = State.PARTY;
            return;
         }

         if (entityParrot.isSitting()) {
            float i = 1.9F;
            this.head.rotationPointY = 17.59F;
            this.tail.rotateAngleX = 1.5388988F;
            this.tail.rotationPointY = 22.97F;
            this.body.rotationPointY = 18.4F;
            this.wingLeft.rotateAngleZ = -0.0873F;
            this.wingLeft.rotationPointY = 18.84F;
            this.wingRight.rotateAngleZ = 0.0873F;
            this.wingRight.rotationPointY = 18.84F;
            this.legLeft.rotationPointY++;
            this.legRight.rotationPointY++;
            this.legLeft.rotateAngleX++;
            this.legRight.rotateAngleX++;
            this.state = State.SITTING;
         } else if (entityParrot.isFlying()) {
            this.legLeft.rotateAngleX += (float) (Math.PI * 2.0 / 9.0);
            this.legRight.rotateAngleX += (float) (Math.PI * 2.0 / 9.0);
            this.state = State.FLYING;
         } else {
            this.state = State.STANDING;
         }

         this.legLeft.rotateAngleZ = 0.0F;
         this.legRight.rotateAngleZ = 0.0F;
      }
   }
}
