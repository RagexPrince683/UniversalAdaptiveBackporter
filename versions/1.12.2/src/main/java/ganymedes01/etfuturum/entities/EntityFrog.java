package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.BiomeDictionary;
public class EntityFrog extends EntityAnimal {
    private static final DataParameter<Byte> VARIANT=EntityDataManager.createKey(EntityFrog.class,DataSerializers.BYTE);
    private boolean pregnant;
    public EntityFrog(World world){super(world);setSize(.5F,.5F);tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIPanic(this,1.5));tasks.addTask(2,new EntityAIMate(this,1));tasks.addTask(3,new EntityAITempt(this,1.2,Items.SLIME_BALL,false));tasks.addTask(4,new EntityAIAttackMelee(this,1.2,true));tasks.addTask(5,new EntityAIWander(this,1));tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,6));targetTasks.addTask(1,new EntityAINearestAttackableTarget<EntitySlime>(this,EntitySlime.class,20,true,false,slime->slime!=null&&slime.getSlimeSize()==1));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(VARIANT,(byte)0);}
    public int getVariant(){return dataManager.get(VARIANT);}
    public void chooseVariant(){net.minecraft.world.biome.Biome biome=world.getBiome(new BlockPos(this));dataManager.set(VARIANT,(byte)(biome.getTemperature(new BlockPos(this))<.2F?2:BiomeDictionary.hasType(biome,BiomeDictionary.Type.HOT)||world.provider.isNether()?1:0));}
    @Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,IEntityLivingData data){chooseVariant();return super.onInitialSpawn(difficulty,data);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.2);}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean isBreedingItem(ItemStack stack){return !pregnant&&stack.getItem()==Items.SLIME_BALL;}
    @Override public EntityAgeable createChild(EntityAgeable other){pregnant=true;setGrowingAge(6000);other.setGrowingAge(6000);resetInLove();((EntityAnimal)other).resetInLove();return null;}
    @Override public boolean attackEntityAsMob(Entity target){if(!(target instanceof EntitySlime)||((EntitySlime)target).getSlimeSize()!=1)return false;boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(this),10);if(hit&&((EntitySlime)target).getHealth()<=0&&target instanceof EntityMagmaCube){String light=getVariant()==0?"ochre_froglight":getVariant()==1?"pearlescent_froglight":"verdant_froglight";if(Content.available(light)&&world.getGameRules().getBoolean("doMobLoot"))entityDropItem(Content.stack(light),.1F);}return hit;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isAIDisabled())return;if(onGround&&getNavigator().getPath()!=null&&ticksExisted%40==0)jump();if(pregnant&&ticksExisted%40==0&&Content.available("frogspawn")&&net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,this)){BlockPos origin=new BlockPos(this);for(BlockPos at:BlockPos.getAllInBox(origin.add(-3,-1,-3),origin.add(3,1,3)))if(world.isBlockLoaded(at)&&world.isAirBlock(at)&&world.getBlockState(at.down()).getMaterial()==Material.WATER&&world.setBlockState(at,Content.block("frogspawn").getDefaultState(),2)){pregnant=false;return;}}}
    @Override public boolean getCanSpawnHere(){BlockPos below=new BlockPos(this).down();net.minecraft.block.Block floor=world.getBlockState(below).getBlock();return MobRegistry.enabled(getClass())&&(floor==Blocks.GRASS||floor==Blocks.DIRT||floor==Content.block("mud"))&&world.getLight(new BlockPos(this))>8&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&!world.containsAnyLiquid(getEntityBoundingBox());}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Variant",getVariant());tag.setBoolean("Pregnant",pregnant);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);dataManager.set(VARIANT,(byte)MathHelper.clamp(tag.getInteger("Variant"),0,2));pregnant=tag.getBoolean("Pregnant");}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.frog.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.frog.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.frog.death");}
}
