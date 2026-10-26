package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public class EntityCreaking extends EntityMob {
    private BlockPos heart;
    private boolean watched;
    public EntityCreaking(World world){super(world);setSize(.9F,2.7F);isImmuneToFire=true;experienceValue=0;enablePersistence();tasks.addTask(1,new EntityAIAttackMelee(this,1,true));tasks.addTask(6,new EntityAIWanderAvoidWater(this,.7));targetTasks.addTask(1,new EntityAINearestAttackableTarget<>(this,EntityPlayer.class,true));}
    public void bind(BlockPos pos){heart=pos.toImmutable();}
    public boolean belongsTo(BlockPos pos){return pos.equals(heart);}
    @Override public boolean canBePushed(){return false;}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.4);}
    @Override public void onLivingUpdate(){if(!world.isRemote&&ticksExisted%5==0){if(heart!=null){if(!world.isBlockLoaded(heart)){motionX=motionZ=0;return;}if(!CreakingHeart.isActive(world,heart)||getDistanceSqToCenter(heart)>32*32){setDead();return;}}watched=false;for(EntityPlayer player:world.playerEntities){if(player.isDead||player.isSpectator()||getDistanceSq(player)>32*32||player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()==Item.getItemFromBlock(Blocks.PUMPKIN))continue;Vec3d toward=new Vec3d(posX-player.posX,posY+height*.75-player.posY-player.getEyeHeight(),posZ-player.posZ);double distance=toward.length();if(distance>0&&player.getLook(1).dotProduct(toward.normalize())>1-.5/distance&&player.canEntityBeSeen(this)){watched=true;break;}}}super.onLivingUpdate();}
    @Override protected void updateAITasks(){if(watched){getNavigator().clearPath();motionX=motionZ=0;setAttackTarget(null);}else super.updateAITasks();}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){if(heart==null||source==DamageSource.OUT_OF_WORLD)return super.attackEntityFrom(source,amount);if(!world.isRemote&&world.isBlockLoaded(heart)){TileEntity tile=world.getTileEntity(heart);if(tile instanceof CreakingHeartTile&&source.getTrueSource() instanceof EntityPlayer)((CreakingHeartTile)tile).createResin();}return false;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("HeartX",heart==null?0:heart.getX());tag.setInteger("HeartY",heart==null?-1:heart.getY());tag.setInteger("HeartZ",heart==null?0:heart.getZ());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);heart=tag.hasKey("HeartY")&&tag.getInteger("HeartY")>=0?new BlockPos(tag.getInteger("HeartX"),tag.getInteger("HeartY"),tag.getInteger("HeartZ")):null;}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.creaking.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.creaking.attack");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.creaking.death");}
}
