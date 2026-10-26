package ganymedes01.etfuturum.entities;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.potion.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/** Native arrow impacts retain potion ammunition and skip bodies already pierced. */
public final class EntityCrossbowBolt extends EntityArrow {
    private int pierces;private ItemStack ammunition=new ItemStack(Items.ARROW);private final Set<UUID> struck=new HashSet<>();
    public EntityCrossbowBolt(World world){super(world);}
    public EntityCrossbowBolt(World world,EntityLivingBase shooter,ItemStack ammo,int piercing){super(world,shooter);ammunition=ammo.copy();ammunition.setCount(1);pierces=MathHelper.clamp(piercing,0,4);setDamage(3);}
    @Override protected ItemStack getArrowStack(){return ammunition.copy();}
    @Override protected void arrowHit(EntityLivingBase target){super.arrowHit(target);if(ammunition.getItem()==Items.SPECTRAL_ARROW)target.addPotionEffect(new PotionEffect(MobEffects.GLOWING,200));else if(ammunition.getItem()==Items.TIPPED_ARROW)for(PotionEffect effect:PotionUtils.getEffectsFromStack(ammunition))target.addPotionEffect(new PotionEffect(effect.getPotion(),Math.max(1,effect.getDuration()/8),effect.getAmplifier(),effect.getIsAmbient(),effect.doesShowParticles()));}
    @Override protected void onHit(RayTraceResult hit){double x=motionX,y=motionY,z=motionZ;super.onHit(hit);if(!world.isRemote&&hit.entityHit!=null&&isDead&&pierces>0){struck.add(hit.entityHit.getUniqueID());pierces--;isDead=false;motionX=x;motionY=y;motionZ=z;}}
    @Override protected Entity findEntityOnPath(Vec3d start,Vec3d end){Entity closest=null;double distance=Double.MAX_VALUE;for(Entity entity:world.getEntitiesWithinAABBExcludingEntity(this,getEntityBoundingBox().expand(motionX,motionY,motionZ).grow(1))){if(!entity.canBeCollidedWith()||entity==shootingEntity&&ticksExisted<5||struck.contains(entity.getUniqueID()))continue;RayTraceResult hit=entity.getEntityBoundingBox().grow(.3).calculateIntercept(start,end);if(hit!=null&&start.squareDistanceTo(hit.hitVec)<distance){distance=start.squareDistanceTo(hit.hitVec);closest=entity;}}return closest;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Pierces",pierces);tag.setTag("Ammunition",ammunition.writeToNBT(new NBTTagCompound()));NBTTagList ids=new NBTTagList();for(UUID id:struck)ids.appendTag(new NBTTagString(id.toString()));tag.setTag("Pierced",ids);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);pierces=MathHelper.clamp(tag.getInteger("Pierces"),0,4);ammunition=tag.hasKey("Ammunition",10)?new ItemStack(tag.getCompoundTag("Ammunition")):new ItemStack(Items.ARROW);if(ammunition.isEmpty())ammunition=new ItemStack(Items.ARROW);ammunition.setCount(1);struck.clear();NBTTagList ids=tag.getTagList("Pierced",8);for(int i=0;i<Math.min(5,ids.tagCount());i++)try{struck.add(UUID.fromString(ids.getStringTagAt(i)));}catch(IllegalArgumentException ignored){}}
}
