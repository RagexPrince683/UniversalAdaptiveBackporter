package ganymedes01.etfuturum.client.model;

import net.minecraft.client.model.ModelBox;

/** Native cuboid and animation regression; no replacement model implementation or graphics context. */
public final class SpringCowModelTest {
    public static void main(String[] args) throws Exception {
        for(String coat:new String[]{"temperate_cow","warm_cow","cold_cow"}) {
            java.awt.image.BufferedImage texture=javax.imageio.ImageIO.read(SpringCowModelTest.class.getResourceAsStream("/assets/etfuturum/textures/entity/cow/"+coat+".png"));
            if(texture.getWidth()!=64||texture.getHeight()!=64)throw new AssertionError("Cow snout UVs require the uncropped released sheet: "+coat);
        }
        for (int coat=0;coat<3;coat++) {
            ModelSpringCow cow=new ModelSpringCow(coat);
            if(cow.textureHeight!=64 || cow.head.textureHeight!=64)throw new AssertionError("Modern cow UV sheet");
            ModelBox snout=(ModelBox)cow.head.cubeList.get(1);
            if(snout.posX1!=-3 || snout.posY1!=1 || snout.posZ1!=-7 || snout.posX2!=3 || snout.posY2!=4 || snout.posZ2!=-6)
                throw new AssertionError("Missing or misplaced snout for coat "+coat);
            cow.setRotationAngles(0,1,0,30,20,1F/16,null);
            if(cow.head.rotateAngleX<=0 || cow.head.rotateAngleY<=0)throw new AssertionError("Head rotation lost");
            float before=cow.leg1.rotateAngleX;
            cow.setRotationAngles(2,1,0,0,0,1F/16,null);
            if(cow.leg1.rotateAngleX==before || cow.leg1.rotateAngleX==cow.leg2.rotateAngleX)throw new AssertionError("Native walk animation lost");
        }
        System.out.println("Native spring cow models: all three coats, snout geometry, UV dimensions and walking verified");
    }
}
