package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.block.BlockJukebox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import java.util.UUID;

public class EntityAllay extends EntityCreature {
    private static final DataParameter<Boolean> DANCING=EntityDataManager.createKey(EntityAllay.class,DataSerializers.BOOLEAN);
    private UUID owner;
    private ItemStack collected=ItemStack.EMPTY;
    private EntityItem wanted;
    private int duplicationCooldown,deliveryTicks,noteTicks;
    private BlockPos note=BlockPos.ORIGIN;
    public EntityAllay(World world){super(world);setSize(.35F,.6F);enablePersistence();}
    @Override protected void entityInit(){super.entityInit();dataManager.register(DANCING,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.1);}
    public boolean isDancing(){return dataManager.get(DANCING);}
    public void hearNote(BlockPos pos){note=pos.toImmutable();noteTicks=600;}
    @Override public void fall(float distance,float multiplier){}
    @Override public boolean attackEntityFrom(DamageSource source,float damage){return !(source.getTrueSource() instanceof EntityPlayer&&owner!=null&&owner.equals(source.getTrueSource().getUniqueID()))&&super.attackEntityFrom(source,damage);}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(Content.available("amethyst_shard")&&held.getItem()==Content.item("amethyst_shard")&&isDancing()&&duplicationCooldown==0){if(!world.isRemote){EntityAllay copy=new EntityAllay(world);copy.copyLocationAndAnglesFrom(this);if(world.spawnEntity(copy)){duplicationCooldown=copy.duplicationCooldown=6000;if(!player.capabilities.isCreativeMode)held.shrink(1);world.setEntityState(this,(byte)7);}}return true;}if(getHeldItemMainhand().isEmpty()&&!held.isEmpty()){if(!world.isRemote){ItemStack sample=held.copy();sample.setCount(1);setHeldItem(EnumHand.MAIN_HAND,sample);owner=player.getUniqueID();if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}if(held.isEmpty()&&!getHeldItemMainhand().isEmpty()){if(!world.isRemote){ItemStack sample=getHeldItemMainhand();setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);if(!player.inventory.addItemStackToInventory(sample))player.dropItem(sample,false);if(!collected.isEmpty()){EntityItem drop=entityDropItem(collected,0);if(drop!=null)collected=ItemStack.EMPTY;}owner=null;}return true;}return super.processInteract(player,hand);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isAIDisabled())return;if(duplicationCooldown>0)--duplicationCooldown;if(noteTicks>0)--noteTicks;if(deliveryTicks>0)--deliveryTicks;
        if(ticksExisted%20==0){heal(1);wanted=null;ItemStack sample=getHeldItemMainhand();if(!sample.isEmpty()&&(collected.isEmpty()||collected.getCount()<collected.getMaxStackSize())&&net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,this))for(EntityItem item:world.getEntitiesWithinAABB(EntityItem.class,getEntityBoundingBox().grow(16,8,16))){ItemStack stack=item.getItem();if(!item.cannotPickup()&&stack.isItemEqual(sample)&&(collected.isEmpty()||ItemStack.areItemStackTagsEqual(collected,stack))&&(wanted==null||getDistanceSq(item)<getDistanceSq(wanted)))wanted=item;}}
        if(ticksExisted%40==0){boolean dancing=false;BlockPos origin=new BlockPos(this);for(BlockPos pos:BlockPos.getAllInBox(origin.add(-5,-2,-5),origin.add(5,2,5)))if(world.isBlockLoaded(pos)&&world.getBlockState(pos).getBlock()==Blocks.JUKEBOX&&world.getBlockState(pos).getValue(BlockJukebox.HAS_RECORD)){dancing=true;break;}dataManager.set(DANCING,dancing);}
    }
    @Override public void travel(float strafe,float vertical,float forward){if(world.isRemote||isAIDisabled())return;EntityPlayer player=owner==null?null:world.getPlayerEntityByUUID(owner);double x=player==null?posX+Math.sin(ticksExisted*.03):player.posX,y=player==null?posY+Math.sin(ticksExisted*.02)*.2:player.posY+1.5,z=player==null?posZ+Math.cos(ticksExisted*.03):player.posZ;if(noteTicks>0){x=note.getX()+.5;y=note.getY()+1.2;z=note.getZ()+.5;}
        if(wanted!=null&&wanted.isEntityAlive()){x=wanted.posX;y=wanted.posY+.2;z=wanted.posZ;if(getDistanceSq(wanted)<1.5){ItemStack stack=wanted.getItem();int count=Math.min(stack.getCount(),collected.isEmpty()?stack.getMaxStackSize():collected.getMaxStackSize()-collected.getCount());if(count>0){if(collected.isEmpty()){collected=stack.copy();collected.setCount(count);}else collected.grow(count);ItemStack rest=stack.copy();rest.shrink(count);if(rest.isEmpty())wanted.setDead();else wanted.setItem(rest);}wanted=null;}}
        else if(!collected.isEmpty()&&deliveryTicks==0&&(player!=null||noteTicks>0)&&getDistanceSq(x,y,z)<9){EntityItem drop=entityDropItem(collected,.2F);if(drop!=null){drop.setPickupDelay(60);collected=ItemStack.EMPTY;deliveryTicks=60;}}
        if(!isDancing()&&world.isBlockLoaded(new BlockPos(x,y,z))){double dx=x-posX,dy=y-posY,dz=z-posZ,length=Math.sqrt(dx*dx+dy*dy+dz*dz);if(length>1){motionX+=(dx/length*.25-motionX)*.15;motionY+=(dy/length*.25-motionY)*.15;motionZ+=(dz/length*.25-motionZ)*.15;rotationYaw=(float)(Math.atan2(dz,dx)*180/Math.PI)-90;renderYawOffset=rotationYaw;}}if(collidedHorizontally)motionY+=.05;move(MoverType.SELF,motionX,motionY,motionZ);motionX*=.9;motionY*=.9;motionZ*=.9;}
    @Override protected void dropFewItems(boolean player,int looting){if(!collected.isEmpty()){entityDropItem(collected,0);collected=ItemStack.EMPTY;}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);if(owner!=null)tag.setString("LikedPlayer",owner.toString());if(!collected.isEmpty())tag.setTag("Inventory",collected.writeToNBT(new NBTTagCompound()));tag.setInteger("DuplicationCooldown",duplicationCooldown);tag.setInteger("NoteTicks",noteTicks);tag.setInteger("NoteX",note.getX());tag.setInteger("NoteY",note.getY());tag.setInteger("NoteZ",note.getZ());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);try{owner=UUID.fromString(tag.getString("LikedPlayer"));}catch(IllegalArgumentException ignored){owner=null;}collected=tag.hasKey("Inventory",10)?new ItemStack(tag.getCompoundTag("Inventory")):ItemStack.EMPTY;duplicationCooldown=MathHelper.clamp(tag.getInteger("DuplicationCooldown"),0,6000);noteTicks=MathHelper.clamp(tag.getInteger("NoteTicks"),0,600);note=new BlockPos(tag.getInteger("NoteX"),tag.getInteger("NoteY"),tag.getInteger("NoteZ"));}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get(getHeldItemMainhand().isEmpty()?"entity.allay.ambient_without_item":"entity.allay.ambient_with_item");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.allay.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.allay.death");}
}
