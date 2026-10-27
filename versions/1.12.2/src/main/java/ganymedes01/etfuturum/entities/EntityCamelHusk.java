package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.*;
public class EntityCamelHusk extends EntityCamel implements IMob {
    private boolean jockeyInitialized;
    public EntityCamelHusk(World world){super(world);}
    @Override public boolean isCreatureType(EnumCreatureType type,boolean count){return type==EnumCreatureType.MONSTER;}
    @Override public EnumCreatureAttribute getCreatureAttribute(){return EnumCreatureAttribute.UNDEAD;}
    @Override public boolean isBreedingItem(ItemStack stack){return false;}
    @Override public EntityAgeable createChild(EntityAgeable mate){return null;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote)return;if(world.getDifficulty()==EnumDifficulty.PEACEFUL){setDead();return;}if(!isAIDisabled()&&!jockeyInitialized){jockeyInitialized=true;if(!isBeingRidden()){EntityHusk husk=new EntityHusk(world);husk.copyLocationAndAnglesFrom(this);husk.onInitialSpawn(world.getDifficultyForLocation(getPosition()),null);husk.setChild(false);if(Content.available("iron_spear"))husk.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,Content.stack("iron_spear"));if(world.spawnEntity(husk))husk.startRiding(this);if(MobRegistry.enabled(EntityParched.class)){EntityParched parched=new EntityParched(world);parched.copyLocationAndAnglesFrom(this);parched.onInitialSpawn(world.getDifficultyForLocation(getPosition()),null);if(world.spawnEntity(parched))parched.startRiding(this);}}}Entity rider=getControllingPassenger();if(rider instanceof EntityMob&&((EntityMob)rider).getAttackTarget()!=null&&ticksExisted%10==0)getNavigator().tryMoveToEntityLiving(((EntityMob)rider).getAttackTarget(),1.2);}
    @Override protected void dropFewItems(boolean player,int looting){super.dropFewItems(player,looting);dropItem(Items.ROTTEN_FLESH,2+rand.nextInt(2)+rand.nextInt(looting+1));}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("JockeyInitialized",jockeyInitialized);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);jockeyInitialized=tag.getBoolean("JockeyInitialized");}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.camel_husk.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.camel_husk.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.camel_husk.death");}
}
