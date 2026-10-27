package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/** Server-owned movement and native passenger lists replace the legacy auxiliary seat entities. */
public abstract class EntityRideableMob extends EntityAnimal {
    private static final DataParameter<Byte> FLAGS=EntityDataManager.createKey(EntityRideableMob.class,DataSerializers.BYTE);
    protected static final DataParameter<Integer> VARIANT=EntityDataManager.createKey(EntityRideableMob.class,DataSerializers.VARINT);
    private static final DataParameter<ItemStack> ARMOR=EntityDataManager.createKey(EntityRideableMob.class,DataSerializers.ITEM_STACK);
    protected int dashCooldown;
    private boolean jumpWasDown,hasFlightHome;
    private EntityRideableMob swimmingMate;
    private double flightHomeX,flightHomeY,flightHomeZ,flyX,flyY,flyZ;
    private int flyTicks;
    protected EntityRideableMob(World world){super(world);tasks.addTask(1,new EntityAIMate(this,1));tasks.addTask(3,new EntityAIFollowParent(this,1));tasks.addTask(6,new EntityAIWander(this,.7));tasks.addTask(7,new EntityAIWatchClosest(this,EntityPlayer.class,8));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(FLAGS,(byte)0);dataManager.register(VARIANT,0);dataManager.register(ARMOR,ItemStack.EMPTY);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(32);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.2);}
    public abstract int passengerCount();
    public boolean isSaddled(){return flag(1);}
    protected void flag(int bit,boolean value){byte flags=dataManager.get(FLAGS);dataManager.set(FLAGS,(byte)(value?flags|bit:flags&~bit));}
    protected boolean flag(int bit){return (dataManager.get(FLAGS)&bit)!=0;}
    public int getVariant(){return dataManager.get(VARIANT);}
    public ItemStack getBodyEquipment(){return dataManager.get(ARMOR);}
    protected void bodyEquipment(ItemStack stack){dataManager.set(ARMOR,stack.copy());}
    protected boolean saddleItem(ItemStack held){return held.getItem()==Items.SADDLE;}
    protected boolean mayRide(EntityPlayer player){return !isChild()&&isSaddled();}
    protected void consume(EntityPlayer player,ItemStack held){if(!player.capabilities.isCreativeMode)held.shrink(1);}
    @Override protected boolean canFitPassenger(Entity passenger){return getPassengers().size()<passengerCount();}
    @Override public Entity getControllingPassenger(){return getPassengers().isEmpty()?null:getPassengers().get(0);}
    @Override public boolean canBeSteered(){return getControllingPassenger() instanceof EntityPlayer&&mayRide((EntityPlayer)getControllingPassenger());}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(isBreedingItem(held))return super.processInteract(player,hand);if(!isChild()&&!isSaddled()&&saddleItem(held)){if(!world.isRemote){flag(1,true);if(this instanceof EntityHappyGhast)dataManager.set(VARIANT,held.getMetadata()&15);consume(player,held);}return true;}if(mayRide(player)&&!player.isSneaking()){if(!world.isRemote)player.startRiding(this);return true;}return super.processInteract(player,hand);}
    protected boolean flyingMount(){return false;}
    protected boolean aquaticMount(){return false;}
    protected double mountedSpeed(){return .3;}
    protected void dash(){if(dashCooldown==0&&(onGround||aquaticMount()&&isInWater())){double yaw=rotationYaw*Math.PI/180;motionX+=-Math.sin(yaw)*(aquaticMount()?1.2:1.5);motionZ+=Math.cos(yaw)*(aquaticMount()?1.2:1.5);if(!aquaticMount())motionY=.4;dashCooldown=aquaticMount()?40:55;velocityChanged=true;}}
    @Override public void travel(float strafe,float vertical,float forward){Entity passenger=getControllingPassenger();if(world.isRemote&&(passenger instanceof EntityPlayer||flyingMount()||aquaticMount()))return;if(isAIDisabled()){super.travel(strafe,vertical,forward);return;}
        if(!world.isRemote&&passenger instanceof EntityPlayer&&mayRide((EntityPlayer)passenger)){EntityPlayer rider=(EntityPlayer)passenger;if(flyingMount()){flightHomeX=posX;flightHomeY=posY;flightHomeZ=posZ;hasFlightHome=true;}if(Float.isFinite(rider.rotationYaw))rotationYaw=rider.rotationYaw;renderYawOffset=rotationYaw;rotationYawHead=rotationYaw;strafe=Float.isFinite(rider.moveStrafing)?MathHelper.clamp(rider.moveStrafing,-1,1)*.5F:0;forward=Float.isFinite(rider.moveForward)?MathHelper.clamp(rider.moveForward,-1,1):0;boolean jumping=MountInput.jumping(rider);if(jumping&&!jumpWasDown)dash();jumpWasDown=jumping;
            if(flyingMount()||aquaticMount()&&isInWater()){double yaw=rotationYaw*Math.PI/180,speed=mountedSpeed();motionX+=((-Math.sin(yaw)*forward+Math.cos(yaw)*strafe)*speed-motionX)*.15;motionZ+=((Math.cos(yaw)*forward+Math.sin(yaw)*strafe)*speed-motionZ)*.15;double rise=Float.isFinite(rider.rotationPitch)?-Math.sin(MathHelper.clamp(rider.rotationPitch,-80,80)*Math.PI/180)*Math.abs(forward)*speed:0;motionY+=(rise-motionY)*.15;move(MoverType.SELF,motionX,motionY,motionZ);return;}setAIMoveSpeed((float)mountedSpeed());super.travel(strafe,0,forward);return;}
        jumpWasDown=false;
        if(flyingMount()){if(world.isRemote)return;if(!hasFlightHome){flightHomeX=posX;flightHomeY=posY;flightHomeZ=posZ;hasFlightHome=true;}if(!isChild()&&!world.getEntitiesWithinAABB(EntityPlayer.class,top()).isEmpty()){motionX=motionY=motionZ=0;rotationYaw=Math.round(rotationYaw/90)*90;return;}if(--flyTicks<=0||collidedHorizontally){flyTicks=60+rand.nextInt(100);flyX=flightHomeX+(rand.nextDouble()-.5)*24;flyY=MathHelper.clamp(flightHomeY+(rand.nextDouble()-.5)*12,8,240);flyZ=flightHomeZ+(rand.nextDouble()-.5)*24;EntityPlayer player=world.getClosestPlayerToEntity(this,16);if(player!=null){ItemStack held=player.getHeldItemMainhand();if(held.getItem()==Items.SNOWBALL||!isChild()&&Content.available("harness")&&held.getItem()==Content.item("harness")){flyX=player.posX;flyY=player.posY+3;flyZ=player.posZ;flyTicks=20;}}}double dx=flyX-posX,dy=flyY-posY,dz=flyZ-posZ,length=Math.max(1,Math.sqrt(dx*dx+dy*dy+dz*dz));if(world.isBlockLoaded(new BlockPos(flyX,flyY,flyZ))&&world.rayTraceBlocks(new Vec3d(posX,posY+height/2,posZ),new Vec3d(flyX,flyY+height/2,flyZ),false,true,false)==null){motionX+=dx/length*.006;motionY+=dy/length*.006;motionZ+=dz/length*.006;rotationYaw=(float)(Math.atan2(motionZ,motionX)*180/Math.PI)-90;renderYawOffset=rotationYaw;}else{flyTicks=0;motionY+=.015;}move(MoverType.SELF,motionX,motionY,motionZ);motionX*=.92;motionY*=.92;motionZ*=.92;return;}
        if(aquaticMount()&&isInWater()){if(!world.isRemote){EntityLivingBase target=swimmingMate!=null&&swimmingMate.isEntityAlive()&&isInLove()?swimmingMate:passenger instanceof EntityMob?((EntityMob)passenger).getAttackTarget():null;if(target!=null){double dx=target.posX-posX,dy=target.posY-posY,dz=target.posZ-posZ,length=Math.max(1,Math.sqrt(dx*dx+dy*dy+dz*dz));double speed=target==swimmingMate?.12:.2;motionX+=(dx/length*speed-motionX)*.15;motionY+=(dy/length*speed-motionY)*.15;motionZ+=(dz/length*speed-motionZ)*.15;}else if(ticksExisted%40==0){motionX=(rand.nextDouble()-.5)*.15;motionY=(rand.nextDouble()-.5)*.08;motionZ=(rand.nextDouble()-.5)*.15;}if(collidedHorizontally)motionY+=.03;move(MoverType.SELF,motionX,motionY,motionZ);motionX*=.98;motionY*=.98;motionZ*=.98;}return;}super.travel(strafe,vertical,forward);}
    protected AxisAlignedBB top(){AxisAlignedBB box=getEntityBoundingBox();return new AxisAlignedBB(box.minX,box.maxY-.1,box.minZ,box.maxX,box.maxY+.6,box.maxZ);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote)return;if(dashCooldown>0)--dashCooldown;if(aquaticMount()&&ticksExisted%20==0){swimmingMate=null;if(isInLove())for(EntityRideableMob other:world.getEntitiesWithinAABB(EntityRideableMob.class,getEntityBoundingBox().grow(8,4,8)))if(canMateWith(other)){swimmingMate=other;break;}}}
    @Override public void updatePassenger(Entity passenger){if(!isPassenger(passenger))return;int index=getPassengers().indexOf(passenger);double x=passengerCount()==4?index==1?1.5:index==3?-1.5:0:0,z=passengerCount()==4?index==2?1.5:index==0?-1.5:0:index==0?-.6:.7,yaw=rotationYaw*Math.PI/180;passenger.setPosition(posX+x*Math.cos(yaw)-z*Math.sin(yaw),posY+getMountedYOffset()+passenger.getYOffset(),posZ+z*Math.cos(yaw)+x*Math.sin(yaw));}
    @Override protected void dropFewItems(boolean player,int looting){if(isSaddled()){if(this instanceof EntityHappyGhast){if(Content.available("harness"))entityDropItem(new ItemStack(Content.item("harness"),1,getVariant()),0);}else entityDropItem(new ItemStack(Items.SADDLE),0);}if(!getBodyEquipment().isEmpty()){entityDropItem(getBodyEquipment(),0);bodyEquipment(ItemStack.EMPTY);}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("Saddle",isSaddled());tag.setInteger("Variant",getVariant());tag.setInteger("DashCooldown",dashCooldown);if(!getBodyEquipment().isEmpty())tag.setTag("BodyArmorItem",getBodyEquipment().writeToNBT(new NBTTagCompound()));if(hasFlightHome){tag.setDouble("FlightHomeX",flightHomeX);tag.setDouble("FlightHomeY",flightHomeY);tag.setDouble("FlightHomeZ",flightHomeZ);}}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);flag(1,tag.getBoolean("Saddle"));dataManager.set(VARIANT,MathHelper.clamp(tag.getInteger("Variant"),0,15));dashCooldown=MathHelper.clamp(tag.getInteger("DashCooldown"),0,55);bodyEquipment(new ItemStack(tag.getCompoundTag("BodyArmorItem")));hasFlightHome=false;if(tag.hasKey("FlightHomeX")){flightHomeX=tag.getDouble("FlightHomeX");flightHomeY=tag.getDouble("FlightHomeY");flightHomeZ=tag.getDouble("FlightHomeZ");hasFlightHome=Double.isFinite(flightHomeX)&&Double.isFinite(flightHomeY)&&Double.isFinite(flightHomeZ);}}
}
