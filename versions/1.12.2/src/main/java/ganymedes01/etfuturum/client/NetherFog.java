package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.NetherBiomes;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.ForgeModContainer;

/** The maintained fractional biome blend, including native darkness and potion post-processing. */
final class NetherFog {
    private NetherFog() { }
    static void apply(EntityViewRenderEvent.FogColors event){
        if(!(event.getEntity() instanceof EntityLivingBase))return;
        EntityLivingBase player=(EntityLivingBase)event.getEntity();World world=player.world;
        if(world.provider.getDimension()!=-1||player.isInsideOfMaterial(Material.LAVA)||player.isInsideOfMaterial(Material.WATER))return;
        GameSettings settings=Minecraft.getMinecraft().gameSettings;int[] ranges=ForgeModContainer.blendRanges;int distance=settings.fancyGraphics&&settings.renderDistanceChunks>=0&&settings.renderDistanceChunks<ranges.length?ranges[settings.renderDistanceChunks]:0;if(distance<=0)return;
        int px=MathHelper.floor(player.posX),pz=MathHelper.floor(player.posZ);double fx=player.posX-px,fz=player.posZ-pz;float r=0,g=0,b=0,weight=0;
        for(int x=-distance;x<=distance;x++)for(int z=-distance;z<=distance;z++){
            BlockPos pos=new BlockPos(px+x,player.posY,pz+z);if(!world.isBlockLoaded(pos))continue;int color=NetherBiomes.fog(world.getBiome(pos));if(color<0)continue;
            float part=(float)((x==-distance?1-fx:x==distance?fx:1)*(z==-distance?1-fz:z==distance?fz:1));
            r+=(color>>16&255)*part;g+=(color>>8&255)*part;b+=(color&255)*part;weight+=part;
        }
        if(weight==0)return;r*=.3F/(255*weight);g*=.3F/(255*weight);b*=.3F/(255*weight);
        double partial=event.getRenderPartialTicks(),dark=(player.lastTickPosY+(player.posY-player.lastTickPosY)*partial)*world.provider.getVoidFogYFactor();
        if(player.isPotionActive(MobEffects.BLINDNESS)){int duration=player.getActivePotionEffect(MobEffects.BLINDNESS).getDuration();dark*=duration<20?1-duration/20F:0;}
        if(dark<1){float shade=(float)Math.max(0,dark);shade*=shade;r*=shade;g*=shade;b*=shade;}
        if(player.isPotionActive(MobEffects.NIGHT_VISION)){int duration=player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();float brightness=duration>200?1:.7F+MathHelper.sin((float)((duration-partial)*Math.PI*.2))*.3F,max=Math.max(r,Math.max(g,b));if(max>0){float scale=1/max;r=r*(1-brightness)+r*scale*brightness;g=g*(1-brightness)+g*scale*brightness;b=b*(1-brightness)+b*scale*brightness;}}
        if(settings.anaglyph){float red=(r*30+g*59+b*11)/100,green=(r*30+g*70)/100,blue=(r*30+b*70)/100;r=red;g=green;b=blue;}
        float total=4*distance*distance,base=total-weight;event.setRed((r*weight+event.getRed()*base)/total);event.setGreen((g*weight+event.getGreen()*base)/total);event.setBlue((b*weight+event.getBlue()*base)/total);
    }
}
