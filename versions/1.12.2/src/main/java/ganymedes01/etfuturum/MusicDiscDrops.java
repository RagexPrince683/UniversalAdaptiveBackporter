package ganymedes01.etfuturum;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class MusicDiscDrops {
 private MusicDiscDrops(){}
 @SubscribeEvent public static void death(LivingDeathEvent event){EntityLivingBase mob=event.getEntityLiving();if(mob.world.isRemote)return;Entity killer=event.getSource().getTrueSource();if(mob instanceof EntityZombie&&mob.isChild()&&mob.getRidingEntity() instanceof EntityChicken&&(killer instanceof EntityPlayer||killer instanceof EntityWolf&&((EntityWolf)killer).isTamed())&&Content.available("lava_chicken_record"))mob.entityDropItem(Content.stack("lava_chicken_record"),0);if(mob instanceof EntityGhast&&event.getSource().getDamageType().equals("fireball")&&killer instanceof EntityPlayer&&Content.available("tears_record"))mob.entityDropItem(Content.stack("tears_record"),0);}
}
