package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.world.World;
public abstract class EntityIllager extends EntityMob {
    private static final DataParameter<Boolean> CAPTAIN=EntityDataManager.createKey(EntityIllager.class,DataSerializers.BOOLEAN),ATTACKING=EntityDataManager.createKey(EntityIllager.class,DataSerializers.BOOLEAN);
    public int raidId;
    protected EntityIllager(World world){super(world);setSize(.6F,1.95F);tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(5,new EntityAIMoveTowardsRestriction(this,1));tasks.addTask(6,new EntityAIMoveThroughVillage(this,1,false));tasks.addTask(7,new EntityAIWanderAvoidWater(this,.6));tasks.addTask(8,new EntityAIWatchClosest(this,EntityPlayer.class,15));tasks.addTask(9,new EntityAILookIdle(this));targetTasks.addTask(1,new EntityAIHurtByTarget(this,true));targetTasks.addTask(2,new EntityAINearestAttackableTarget<>(this,EntityPlayer.class,true));targetTasks.addTask(3,new EntityAINearestAttackableTarget<>(this,EntityVillager.class,true));targetTasks.addTask(3,new EntityAINearestAttackableTarget<>(this,EntityIronGolem.class,true));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(CAPTAIN,false);dataManager.register(ATTACKING,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.35);getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32);getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5);}
    public boolean isAttacking(){return dataManager.get(ATTACKING);}
    public boolean isCaptain(){return dataManager.get(CAPTAIN);}
    public void setCaptain(boolean value){dataManager.set(CAPTAIN,value);}
    @Override public boolean isOnSameTeam(Entity entity){return super.isOnSameTeam(entity)||getTeam()==null&&entity.getTeam()==null&&(entity instanceof EntityIllager||entity instanceof AbstractIllager);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!world.isRemote){dataManager.set(ATTACKING,getAttackTarget()!=null);if(raidId!=0&&ticksExisted%20==0&&!RaidProgress.get(world).isActive(raidId)){raidId=0;detachHome();}}}
    @Override protected boolean canDespawn(){return raidId==0&&super.canDespawn();}
    @Override protected void dropFewItems(boolean player,int looting){if(player){int count=rand.nextInt(2+looting);if(count>0)dropItem(Items.EMERALD,count);}if(isCaptain()&&raidId==0&&Content.available("ominous_bottle"))entityDropItem(Content.stack("ominous_bottle"),0);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("EFRRaid",raidId);tag.setBoolean("PatrolLeader",isCaptain());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);raidId=Math.max(0,tag.getInteger("EFRRaid"));setCaptain(tag.getBoolean("PatrolLeader"));}
}
