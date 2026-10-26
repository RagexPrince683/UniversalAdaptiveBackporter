package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.BiomeDictionary;

/** Native feline taming and owner AI, with independently persisted modern coat and collar. */
public class EntityCat extends EntityOcelot {
    private static final DataParameter<Byte> COAT=EntityDataManager.createKey(EntityCat.class,DataSerializers.BYTE),COLLAR=EntityDataManager.createKey(EntityCat.class,DataSerializers.BYTE);
    private boolean sleptWithOwner;
    public EntityCat(World world){super(world);}
    @Override protected void entityInit(){super.entityInit();dataManager.register(COAT,(byte)0);dataManager.register(COLLAR,(byte)14);}
    public int getVariant(){return dataManager.get(COAT)&15;}
    public int getCollarColor(){return dataManager.get(COLLAR)&15;}
    @Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,IEntityLivingData data){data=super.onInitialSpawn(difficulty,data);dataManager.set(COAT,(byte)rand.nextInt(11));return data;}
    @Override public boolean getCanSpawnHere(){BlockPos pos=new BlockPos(this);return MobRegistry.enabled(getClass())&&world.provider.getDimension()==0&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&!world.containsAnyLiquid(getEntityBoundingBox())&&world.getBlockState(pos.down()).isNormalCube()&&(world.getVillageCollection().getNearestVillage(pos,32)!=null||BiomeDictionary.hasType(world.getBiome(pos),BiomeDictionary.Type.SWAMP))&&world.getEntitiesWithinAABB(EntityCat.class,getEntityBoundingBox().grow(48,16,48)).size()<5;}
    @Override public boolean isBreedingItem(ItemStack stack){return stack.getItem()==Items.FISH&&stack.getMetadata()<2;}
    @Override public boolean canMateWith(EntityAnimal other){return other instanceof EntityCat&&super.canMateWith(other);}
    @Override public EntityOcelot createChild(EntityAgeable other){EntityCat child=new EntityCat(world);child.dataManager.set(COAT,(byte)(rand.nextBoolean()?getVariant():((EntityCat)other).getVariant()));if(isTamed()){child.setOwnerId(getOwnerId());child.setTamed(true);child.setTameSkin(1);}return child;}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);int color=ModernDye.color(held);if(isTamed()&&isOwner(player)&&color>=0){if(!world.isRemote){dataManager.set(COLLAR,(byte)color);if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}return super.processInteract(player,hand);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||ticksExisted%20!=0||!isTamed()||!(getOwner() instanceof EntityPlayer))return;EntityPlayer owner=(EntityPlayer)getOwner();if(owner.isPlayerSleeping()&&getDistanceSq(owner)<16)sleptWithOwner=true;else if(sleptWithOwner&&!owner.isPlayerSleeping()){sleptWithOwner=false;if(rand.nextFloat()<.7F&&getDistanceSq(owner)<16){java.util.List<Item> gifts=new java.util.ArrayList<>(java.util.Arrays.asList(Items.STRING,Items.FEATHER,Items.ROTTEN_FLESH,Items.BONE));if(Content.available("phantom_membrane"))gifts.add(Content.item("phantom_membrane"));entityDropItem(new ItemStack(gifts.get(rand.nextInt(gifts.size()))),0);}}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("CatType",getVariant());tag.setInteger("CollarColor",getCollarColor());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);dataManager.set(COAT,(byte)MathHelper.clamp(tag.getInteger("CatType"),0,10));dataManager.set(COLLAR,(byte)(tag.hasKey("CollarColor")?tag.getInteger("CollarColor")&15:14));}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.cat.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.cat.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.cat.death");}
}
