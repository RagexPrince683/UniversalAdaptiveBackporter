package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.*;
public class EntityZombieNautilus extends EntityNautilus implements IMob {
    private boolean jockeyInitialized;
    public EntityZombieNautilus(World world){super(world);}
    @Override public EnumCreatureAttribute getCreatureAttribute(){return EnumCreatureAttribute.UNDEAD;}
    @Override public boolean isBreedingItem(ItemStack stack){return false;}
    @Override public EntityAgeable createChild(EntityAgeable mate){return null;}
    @Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,IEntityLivingData data){data=super.onInitialSpawn(difficulty,data);dataManager.set(VARIANT,world.getBiome(getPosition()).getDefaultTemperature()>.8F?1:0);return data;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote)return;if(world.getDifficulty()==EnumDifficulty.PEACEFUL){setDead();return;}if(!isAIDisabled()&&!jockeyInitialized){jockeyInitialized=true;if(!isBeingRidden()&&Content.available("trident")&&MobRegistry.enabled(EntityDrowned.class)){EntityDrowned drowned=new EntityDrowned(world);drowned.copyLocationAndAnglesFrom(this);drowned.onInitialSpawn(world.getDifficultyForLocation(getPosition()),null);drowned.setChild(false);drowned.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,Content.stack("trident"));if(world.spawnEntity(drowned))drowned.startRiding(this);}}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("JockeyInitialized",jockeyInitialized);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);jockeyInitialized=tag.getBoolean("JockeyInitialized");}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.zombie_nautilus.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.zombie_nautilus.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.zombie_nautilus.death");}
}
