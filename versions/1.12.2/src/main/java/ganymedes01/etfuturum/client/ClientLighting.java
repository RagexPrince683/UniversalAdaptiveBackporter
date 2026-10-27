package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Native float light calculation. Provider colors run before vision and gamma, as in Forge. */
public final class ClientLighting {
 private ClientLighting(){}
 public static boolean calculate(Minecraft mc,int[] output,float partial,float flicker,float boss,float previousBoss){
  World world=mc.world;if(world==null||mc.player==null||!ModernLightmap.supports(world.provider))return false;
  boolean tint=Features.enabled("world.modernBlockLightTint"),gamma=Features.enabled("world.modernLightmapGamma"),vision=Features.enabled("world.modernNightVision"),end=ModernLightmap.isEnd(world.provider),ambient=end&&Features.enabled("world.modernEndAmbientColor");
  float flash=end&&Features.enabled("world.endFlashes")?EndFlashState.INSTANCE.getIntensity(partial):0;
  if(!tint&&!gamma&&!vision&&!ambient&&flash<=0)return false;
  float sun=world.getSunBrightness(1),skyFactor=sun*.95F+.05F,night=0;
  if(mc.player.isPotionActive(MobEffects.NIGHT_VISION)){int ticks=mc.player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();night=ticks>200?1:.7F+MathHelper.sin((ticks-partial)*(float)Math.PI*.2F)*.3F;}
  float[] table=world.provider.getLightBrightnessTable(),rgb=new float[3];
  for(int i=0;i<256;i++){
   float sky=table[i/16]*(world.getLastLightningBolt()>0?1:skyFactor),block=table[i%16]*(flicker*.1F+1.5F);
   float br=block,bg=block*((block*.6F+.4F)*.6F+.4F),bb=block*(block*block*.6F+.4F);
   if(tint){br=ModernLightmap.blockTint(i&15,0,flicker);bg=ModernLightmap.blockTint(i&15,1,flicker);bb=ModernLightmap.blockTint(i&15,2,flicker);}
   rgb[0]=(sky*(sun*.65F+.35F)+br)*.96F+.03F;rgb[1]=(sky*(sun*.65F+.35F)+bg)*.96F+.03F;rgb[2]=(sky+bb)*.96F+.03F;
   if(boss>0){float amount=previousBoss+(boss-previousBoss)*partial;rgb[0]*=1-.3F*amount;rgb[1]*=1-.4F*amount;rgb[2]*=1-.4F*amount;}
   if(end){rgb[0]=.22F+br*.75F;rgb[1]=.28F+bg*.75F;rgb[2]=.25F+bb*.75F;}
   world.provider.getLightmapColors(partial,sun,sky,block,rgb);
   for(int c=0;c<3;c++){rgb[c]=ModernLightmap.clamp(rgb[c]);if(ambient)rgb[c]=Math.max(rgb[c],ModernLightmap.END_AMBIENT[c]);}
   if(flash>0){rgb[0]+=(172/255F-rgb[0])*flash;rgb[1]+=(96/255F-rgb[1])*flash;rgb[2]+=(205/255F-rgb[2])*flash;}
   if(night>0){float max=Math.max(rgb[0],Math.max(rgb[1],rgb[2]));for(int c=0;c<3;c++)rgb[c]=vision?ModernLightmap.nightVisionFloor(rgb[c],c,night):rgb[c]*(1-night)+(max>0?rgb[c]/max:0)*night;}
   for(int c=0;c<3;c++)rgb[c]=ModernLightmap.clamp(rgb[c]);
   if(gamma){float max=Math.max(rgb[0],Math.max(rgb[1],rgb[2])),scale=max>0?ModernLightmap.modernGamma(max,mc.gameSettings.gammaSetting)/max:1;for(int c=0;c<3;c++)rgb[c]*=scale;}
   else for(int c=0;c<3;c++)rgb[c]=ModernLightmap.modernGamma(rgb[c],mc.gameSettings.gammaSetting);
   output[i]=0xFF000000|(int)(ModernLightmap.clamp(rgb[0]*.96F+.03F)*255)<<16|(int)(ModernLightmap.clamp(rgb[1]*.96F+.03F)*255)<<8|(int)(ModernLightmap.clamp(rgb[2]*.96F+.03F)*255);
  }
  return true;
 }
}
