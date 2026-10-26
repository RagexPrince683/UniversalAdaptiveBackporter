package ganymedes01.etfuturum.entities;
import net.minecraft.entity.ai.*;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.*;
import net.minecraft.world.World;
public class EntityParched extends EntityStatusSkeleton {
    private boolean checkingSun;
    public EntityParched(World world){super(world);for(EntityAITasks.EntityAITaskEntry entry:new java.util.ArrayList<>(tasks.taskEntries))if(entry.action instanceof EntityAIRestrictSun||entry.action instanceof EntityAIFleeSun)tasks.removeTask(entry.action);}
    @Override protected Potion effect(){return MobEffects.WEAKNESS;}
    @Override protected int duration(){return 600;}
    @Override protected String kind(){return "parched";}
    @Override public boolean isPotionApplicable(PotionEffect effect){return effect.getPotion()!=MobEffects.WEAKNESS&&super.isPotionApplicable(effect);}
    @Override public void onLivingUpdate(){checkingSun=true;try{super.onLivingUpdate();}finally{checkingSun=false;}}
    @Override public float getBrightness(){return checkingSun?0:super.getBrightness();}
}
