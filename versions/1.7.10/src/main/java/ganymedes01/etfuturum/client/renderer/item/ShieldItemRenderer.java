package ganymedes01.etfuturum.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** A native 3D item model; no player or global item renderer replacement. */
public final class ShieldItemRenderer implements IItemRenderer {
    private final ModelRenderer plate,handle;
    public ShieldItemRenderer(){ModelBase model=new ModelBase(){};model.textureWidth=64;model.textureHeight=64;plate=new ModelRenderer(model,0,0);plate.addBox(-6,-11,-2,12,22,1);handle=new ModelRenderer(model,26,0);handle.addBox(-1,-3,-1,2,6,6);}
    @Override public boolean handleRenderType(ItemStack stack,ItemRenderType type){return true;}
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,ItemRendererHelper helper){return helper==ItemRendererHelper.EQUIPPED_BLOCK||helper==ItemRendererHelper.ENTITY_BOBBING||helper==ItemRendererHelper.ENTITY_ROTATION;}
    @Override public void renderItem(ItemRenderType type,ItemStack stack,Object...data){
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_LIGHTING_BIT);GL11.glPushMatrix();try{
            Minecraft client=Minecraft.getMinecraft();client.getTextureManager().bindTexture(new ResourceLocation("etfuturum:textures/entity/shield_base_nopattern.png"));GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_ALPHA_TEST);
            if(type==ItemRenderType.INVENTORY){GL11.glTranslatef(8,8,0);GL11.glScalef(9,-9,9);GL11.glRotatef(15,1,0,0);GL11.glRotatef(-30,0,1,0);RenderHelper.enableGUIStandardItemLighting();}
            else if(type==ItemRenderType.ENTITY){GL11.glTranslatef(0,.35F,0);GL11.glScalef(.5F,-.5F,-.5F);}
            else{
                GL11.glTranslatef(.5F,.5F,.5F);
                EntityLivingBase holder=data.length>1&&data[1] instanceof EntityLivingBase?(EntityLivingBase)data[1]:null;
                boolean raised=holder instanceof EntityPlayer&&((EntityPlayer)holder).getItemInUseCount()>0
                        &&((EntityPlayer)holder).getItemInUse()!=null&&((EntityPlayer)holder).getItemInUse().getItem()==stack.getItem();
                if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)firstPerson(raised);
                else thirdPerson(raised);
            }
            plate.render(1/16F);handle.render(1/16F);
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
    private static void firstPerson(boolean raised){
        // Undo the legacy item's scale/yaw, retaining its camera, equip and swing movement.
        GL11.glScalef(2.5F,2.5F,2.5F);GL11.glRotatef(-45,0,1,0);
        // Native right-hand shield display. There is no left-hand inventory or pose here.
        GL11.glTranslatef((raised?-15:-10)/16F,(raised?5:2)/16F,(raised?-11:-10)/16F);
        GL11.glRotatef(180,0,1,0);GL11.glRotatef(raised?-5:5,0,0,1);
        GL11.glScalef(1.25F,1.25F,1.25F);modelOrigin();
    }
    private static void thirdPerson(boolean raised){
        // RenderPlayer's generic sprite transform precedes the Forge item renderer.
        GL11.glRotatef(-20,0,0,1);GL11.glRotatef(90,1,0,0);GL11.glRotatef(-60,0,0,1);
        GL11.glScalef(1/.375F,1/.375F,1/.375F);GL11.glTranslatef(-.25F,-.1875F,.1875F);
        // Return to the arm attachment, then use the native right-hand held-item frame.
        GL11.glTranslatef(.0625F,-.4375F,-.0625F);
        GL11.glRotatef(-90,1,0,0);GL11.glRotatef(180,0,1,0);GL11.glTranslatef(.0625F,.125F,-.625F);
        GL11.glTranslatef((raised?3.51F:10)/16F,(raised?11:6)/16F,(raised?-2:-4)/16F);
        if(raised)GL11.glRotatef(45,1,0,0);
        GL11.glRotatef(raised?135:90,0,1,0);modelOrigin();
    }
    private static void modelOrigin(){GL11.glTranslatef(-.5F,-.5F,-.5F);GL11.glScalef(1,-1,-1);}
}
