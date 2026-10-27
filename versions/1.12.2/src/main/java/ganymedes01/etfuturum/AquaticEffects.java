package ganymedes01.etfuturum;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.*;

@Mod.EventBusSubscriber(modid=UAB.ID)
public final class AquaticEffects {
    public static final Potion DOLPHINS_GRACE=new Grace();
    public static final Potion NAUTILUS_BREATH=new Status("nautilus_breath",false,0x61A5BF);
    public static final Potion DARKNESS=new Status("darkness",true,0x292721);
    private AquaticEffects(){ }
    @SubscribeEvent public static void register(RegistryEvent.Register<Potion> event){event.getRegistry().registerAll(DOLPHINS_GRACE,DARKNESS,NAUTILUS_BREATH);}
    private static final class Status extends Potion {
        private final String name;
        Status(String name,boolean bad,int color){super(bad,color);this.name=name;setRegistryName(UAB.ID,name);setPotionName("potion."+name);}
        @Override public boolean hasStatusIcon(){return false;}
        @SideOnly(Side.CLIENT) private void icon(int x,int y,Minecraft mc){mc.getTextureManager().bindTexture(new ResourceLocation(UAB.ID,"textures/mob_effect/"+name+".png"));Gui.drawModalRectWithCustomSizedTexture(x,y,0,0,18,18,18,18);}
        @Override @SideOnly(Side.CLIENT) public void renderInventoryEffect(int x,int y,PotionEffect effect,Minecraft mc){icon(x+6,y+7,mc);}
        @Override @SideOnly(Side.CLIENT) public void renderHUDEffect(int x,int y,PotionEffect effect,Minecraft mc,float alpha){net.minecraft.client.renderer.GlStateManager.color(1,1,1,alpha);icon(x+3,y+3,mc);}
    }
    private static final class Grace extends Potion {
        Grace(){super(false,0x88A3BE);setRegistryName(UAB.ID,"dolphins_grace");setPotionName("potion.dolphins_grace");setBeneficial();}
        @Override public boolean hasStatusIcon(){return false;}
        @SideOnly(Side.CLIENT) private void icon(int x,int y,Minecraft mc){mc.getTextureManager().bindTexture(new ResourceLocation(UAB.ID,"textures/mob_effect/dolphins_grace.png"));Gui.drawModalRectWithCustomSizedTexture(x,y,0,0,18,18,18,18);}
        @Override @SideOnly(Side.CLIENT) public void renderInventoryEffect(int x,int y,PotionEffect effect,Minecraft mc){icon(x+6,y+7,mc);}
        @Override @SideOnly(Side.CLIENT) public void renderHUDEffect(int x,int y,PotionEffect effect,Minecraft mc,float alpha){net.minecraft.client.renderer.GlStateManager.color(1,1,1,alpha);icon(x+3,y+3,mc);}
    }
}
