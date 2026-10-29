package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Client-owned world lifecycle, flash timing and biome ambience. No world or audio I/O. */
public final class ClientAtmosphere {
 private World previousWorld;private AmbientLoop loop;private int flashDelay=-1;private int additionDelay=80;
 @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
  Minecraft mc=Minecraft.getMinecraft();if(event.phase!=TickEvent.Phase.END)return;
  if(mc.world!=previousWorld){if(loop!=null)mc.getSoundHandler().stopSound(loop);loop=null;previousWorld=mc.world;flashDelay=-1;EndFlashState.INSTANCE.reset(mc.world==null?0:mc.world.getTotalWorldTime());}
  if(mc.world==null||mc.player==null||mc.isGamePaused())return;
  if(Features.enabled("functions.enableGamemodeSwitcher")&&mc.currentScreen==null&&Keyboard.isCreated()&&Keyboard.isKeyDown(Keyboard.KEY_F3)&&Keyboard.isKeyDown(Keyboard.KEY_F4))mc.displayGuiScreen(new ganymedes01.etfuturum.client.gui.GuiGamemodeSwitcher());
  if(Features.enabled("world.endFlashes")&&mc.world.provider instanceof WorldProviderEnd){
   if(EndFlashState.INSTANCE.tick(mc.world.getTotalWorldTime())&&!(mc.currentScreen instanceof GuiWinGame))flashDelay=EndFlashState.SOUND_DELAY_IN_TICKS;
   if(flashDelay>=0&&flashDelay--==0){double pitch=Math.toRadians(EndFlashState.INSTANCE.getXAngle()),yaw=Math.toRadians(EndFlashState.INSTANCE.getYAngle()),horizontal=Math.sin(pitch)*10;play("weather.end_flash",SoundCategory.WEATHER,(float)(mc.player.posX+Math.cos(yaw)*horizontal),(float)(mc.player.posY+Math.cos(pitch)*10),(float)(mc.player.posZ+Math.sin(yaw)*horizontal));}
  }else flashDelay=-1;
  String biome=netherBiome();
  if(biome==null){if(loop!=null){loop.stop=true;loop=null;}return;}
  NetherParticles.tick(mc);
  if(loop==null||!loop.biome.equals(biome)||!mc.getSoundHandler().isSoundPlaying(loop)){if(loop!=null)loop.stop=true;loop=new AmbientLoop(biome);mc.getSoundHandler().playSound(loop);additionDelay=80;}
  if(--additionDelay<=0){additionDelay=40+mc.world.rand.nextInt(41);play("ambient."+biome+".additions",SoundCategory.AMBIENT,0,0,0);}
 }
 public static String netherBiome(){Minecraft mc=Minecraft.getMinecraft();if(mc.world==null||mc.player==null||!Features.enabled("sounds.netherAmbience")||mc.world.provider.getClass()!=WorldProviderHell.class||!mc.world.isBlockLoaded(mc.player.getPosition()))return null;NetherBiomes.Kind kind=NetherBiomes.kind(mc.world.getBiome(mc.player.getPosition()));return kind==null?"nether_wastes":kind==NetherBiomes.Kind.CRIMSON?"crimson_forest":kind==NetherBiomes.Kind.WARPED?"warped_forest":kind==NetherBiomes.Kind.SOUL?"soul_sand_valley":"basalt_deltas";}
 private static void play(String sound,SoundCategory category,float x,float y,float z){Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(ModernSounds.DOMAIN,sound),category,1,1,false,0,ISound.AttenuationType.NONE,x,y,z));}
 @SubscribeEvent public void sound(PlaySoundEvent event){ISound original=event.getSound();if(original==null)return;String id=original.getSoundLocation().toString(),biome=netherBiome(),replacement=null;
  if(id.equals("minecraft:ambient.cave")){if(biome!=null)replacement="ambient."+biome+".mood";else if(Features.enabled("sounds.caveAmbience"))replacement="ambient.cave";}
  else if(biome!=null&&id.equals("minecraft:music.nether"))replacement="music.nether."+biome;
  if(id.startsWith("minecraft:block.note.")&&Features.enabled("sounds.noteBlockNotes")){Minecraft mc=Minecraft.getMinecraft();if(mc.world!=null){net.minecraft.util.math.BlockPos pos=new net.minecraft.util.math.BlockPos(original.getXPosF(),original.getYPosF(),original.getZPosF());if(mc.world.isBlockLoaded(pos)&&mc.world.getBlockState(pos).getBlock() instanceof net.minecraft.block.BlockNote)replacement=ClientSoundRules.instrument(mc.world.getBlockState(pos.down()).getBlock());}}
  if(replacement!=null){
   // Forge fires PlaySoundEvent before SoundManager resolves the sample. Native
   // PositionedSound volume/pitch accessors require that sample to exist.
   original.createAccessor(Minecraft.getMinecraft().getSoundHandler());
   event.setResultSound(new PositionedSoundRecord(new ResourceLocation(ModernSounds.DOMAIN,replacement),original.getCategory(),original.getVolume(),original.getPitch(),original.canRepeat(),original.getRepeatDelay(),original.getAttenuationType(),original.getXPosF(),original.getYPosF(),original.getZPosF()));
  }
 }
 @SubscribeEvent public void fog(EntityViewRenderEvent.FogColors event){if(event.getEntity().world.provider instanceof WorldProviderEnd&&Features.enabled("world.endFlashes")){float flash=EndFlashState.INSTANCE.getIntensity((float)event.getRenderPartialTicks());event.setRed(event.getRed()+(172/255F-event.getRed())*flash);event.setGreen(event.getGreen()+(96/255F-event.getGreen())*flash);event.setBlue(event.getBlue()+(205/255F-event.getBlue())*flash);}}
 public static void renderFlash(float partial){Minecraft mc=Minecraft.getMinecraft();if(mc.world==null||!(mc.world.provider instanceof WorldProviderEnd)||mc.world.provider.getSkyRenderer()!=null||!Features.enabled("world.endFlashes"))return;float intensity=EndFlashState.INSTANCE.getIntensity(partial);if(intensity<=.01F)return;
  boolean fog=GL11.glIsEnabled(GL11.GL_FOG),alpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),write=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);int src=GL11.glGetInteger(GL11.GL_BLEND_SRC),dst=GL11.glGetInteger(GL11.GL_BLEND_DST);
  GlStateManager.pushMatrix();try{GlStateManager.disableFog();GlStateManager.disableAlpha();GlStateManager.disableCull();GlStateManager.disableDepth();GlStateManager.depthMask(false);GlStateManager.enableBlend();GlStateManager.blendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE);mc.getTextureManager().bindTexture(new ResourceLocation(UAB.ID,"textures/environment/end_flash.png"));GlStateManager.rotate(EndFlashState.INSTANCE.getYAngle(),0,1,0);GlStateManager.rotate(EndFlashState.INSTANCE.getXAngle(),1,0,0);
   BufferBuilder buffer=Tessellator.getInstance().getBuffer();buffer.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX_COLOR);buffer.pos(-100,-100,-100).tex(0,0).color(1F,1F,1F,intensity).endVertex();buffer.pos(-100,-100,100).tex(0,1).color(1F,1F,1F,intensity).endVertex();buffer.pos(100,-100,100).tex(1,1).color(1F,1F,1F,intensity).endVertex();buffer.pos(100,-100,-100).tex(1,0).color(1F,1F,1F,intensity).endVertex();Tessellator.getInstance().draw();
  }finally{GlStateManager.popMatrix();if(fog)GlStateManager.enableFog();if(alpha)GlStateManager.enableAlpha();if(cull)GlStateManager.enableCull();if(depth)GlStateManager.enableDepth();if(!blend)GlStateManager.disableBlend();GlStateManager.depthMask(write);GlStateManager.blendFunc(src,dst);GlStateManager.color(1,1,1,1);}
 }
 private static final class AmbientLoop extends MovingSound {final String biome;boolean stop;AmbientLoop(String biome){super(ModernSounds.get("ambient."+biome+".loop"),SoundCategory.AMBIENT);this.biome=biome;repeat=true;repeatDelay=0;attenuationType=ISound.AttenuationType.NONE;volume=.01F;}
  @Override public void update(){volume=stop?volume-.0175F:Math.min(1,volume+.0175F);if(volume<=0){donePlaying=true;volume=0;}}
 }
}
