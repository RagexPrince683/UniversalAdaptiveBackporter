package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.ModernSounds;
import ganymedes01.etfuturum.entities.EntityBee;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
final class BeeClientEffects {
    @SubscribeEvent public void join(EntityJoinWorldEvent event){if(event.getWorld().isRemote&&event.getEntity() instanceof EntityBee)Minecraft.getMinecraft().getSoundHandler().playSound(new Flight((EntityBee)event.getEntity()));}
    private static final class Flight extends MovingSound {
        final EntityBee bee; final boolean angry;
        Flight(EntityBee bee){super(ModernSounds.get("entity.bee.loop"+(bee.isAngry()?"_aggressive":"")),SoundCategory.NEUTRAL);this.bee=bee;angry=bee.isAngry();repeat=true;volume=.001F;xPosF=(float)bee.posX;yPosF=(float)bee.posY;zPosF=(float)bee.posZ;}
        @Override public void update(){if(bee.isDead||bee.world!=Minecraft.getMinecraft().world){donePlaying=true;return;}if(angry!=bee.isAngry()){donePlaying=true;Minecraft.getMinecraft().getSoundHandler().playDelayedSound(new Flight(bee),1);return;}xPosF=(float)bee.posX;yPosF=(float)bee.posY;zPosF=(float)bee.posZ;float speed=(float)Math.sqrt(bee.motionX*bee.motionX+bee.motionZ*bee.motionZ);if(!bee.onGround&&speed>=.01F){float min=bee.isChild()?1.1F:.7F,max=bee.isChild()?1.5F:1.1F;pitch=min+MathHelper.clamp(speed,min,max)*(max-min);volume=MathHelper.clamp(speed,0,.5F)*1.2F;}else{pitch=0;volume=.001F;}}
    }
}
