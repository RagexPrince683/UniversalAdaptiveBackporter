package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public class EntityCopperGolem extends EntityGolem {
    private static final DataParameter<Byte> OXIDATION=EntityDataManager.createKey(EntityCopperGolem.class,DataSerializers.BYTE);
    private static final DataParameter<Boolean> WAXED=EntityDataManager.createKey(EntityCopperGolem.class,DataSerializers.BOOLEAN);
    private BlockPos destination;
    private int searchCooldown,oxidationTicks=6000;
    public EntityCopperGolem(World world){super(world);setSize(.7F,1.1F);tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(6,new EntityAIWanderAvoidWater(this,.7));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(OXIDATION,(byte)0);dataManager.register(WAXED,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.2);}
    public int getOxidation(){return dataManager.get(OXIDATION);}
    public void setOxidation(int value){dataManager.set(OXIDATION,(byte)MathHelper.clamp(value,0,3));}
    public boolean isWaxed(){return dataManager.get(WAXED);}
    public void wax(boolean value){dataManager.set(WAXED,value);}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(Content.available("honeycomb")&&held.getItem()==Content.item("honeycomb")&&!isWaxed()){if(!world.isRemote){wax(true);if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}if(held.getItem() instanceof ItemAxe&&(isWaxed()||getOxidation()>0)){if(!world.isRemote){if(isWaxed())wax(false);else setOxidation(getOxidation()-1);oxidationTicks=6000;if(!player.capabilities.isCreativeMode)held.damageItem(1,player);}return true;}return super.processInteract(player,hand);}
    private boolean accepts(IInventory inventory,ItemStack held,boolean matching){for(int i=0;i<inventory.getSizeInventory();i++){ItemStack current=inventory.getStackInSlot(i);if(current.isEmpty()&&!matching&&inventory.isItemValidForSlot(i,held)||!current.isEmpty()&&inventory.isItemValidForSlot(i,held)&&current.isItemEqual(held)&&ItemStack.areItemStackTagsEqual(current,held)&&current.getCount()<Math.min(current.getMaxStackSize(),inventory.getInventoryStackLimit()))return true;}return false;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isAIDisabled())return;if(!isWaxed()&&--oxidationTicks<=0){oxidationTicks=6000+rand.nextInt(6000);if(rand.nextInt(3)==0)setOxidation(getOxidation()+1);}if(getOxidation()==3){getNavigator().clearPath();BlockPos pos=new BlockPos(this);if(Content.available("copper_golem_statue")&&world.isAirBlock(pos)&&net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,this)&&world.setBlockState(pos,Content.block("copper_golem_statue").getDefaultState().withProperty(CopperStatue.WAXED,isWaxed()),3)){if(!getHeldItemMainhand().isEmpty()){if(!ItemDelivery.drop(this,getHeldItemMainhand(),0)){world.setBlockToAir(pos);return;}setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);}setDead();}return;}if(searchCooldown>0){--searchCooldown;return;}ItemStack held=getHeldItemMainhand();if(destination==null){double nearest=Double.MAX_VALUE;for(BlockPos pos:MobChestIndex.nearby(world,posX,posY,posZ)){if(!world.isBlockLoaded(pos))continue;TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof IInventory)||!MobChestIndex.chest(tile))continue;IInventory inventory=(IInventory)tile;boolean copper=tile instanceof CopperChestTile;if(held.isEmpty()){if(!copper)continue;boolean stocked=false;for(int i=0;i<inventory.getSizeInventory();i++)if(!inventory.getStackInSlot(i).isEmpty()){stocked=true;break;}if(!stocked)continue;}else if(copper||!accepts(inventory,held,false))continue;double distance=getDistanceSq(pos.getX()+.5,pos.getY(),pos.getZ()+.5);if(!held.isEmpty()&&accepts(inventory,held,true))distance*=.1;if(distance<nearest){nearest=distance;destination=pos;}}if(destination==null){searchCooldown=100;return;}}
        if(!world.isBlockLoaded(destination)){destination=null;searchCooldown=100;return;}if(getDistanceSq(destination.getX()+.5,destination.getY(),destination.getZ()+.5)>6){if(ticksExisted%20==0&&!getNavigator().tryMoveToXYZ(destination.getX()+.5,destination.getY(),destination.getZ()+.5,1)){destination=null;searchCooldown=100;}return;}TileEntity tile=world.getTileEntity(destination);if(tile instanceof IInventory&&MobChestIndex.chest(tile)){IInventory inventory=(IInventory)tile;if(held.isEmpty()&&tile instanceof CopperChestTile){for(int i=0;i<inventory.getSizeInventory();i++)if(!inventory.getStackInSlot(i).isEmpty()){setHeldItem(EnumHand.MAIN_HAND,inventory.decrStackSize(i,Math.min(16,inventory.getStackInSlot(i).getCount())));inventory.markDirty();break;}}else if(!held.isEmpty()&&!(tile instanceof CopperChestTile)){for(int i=0;i<inventory.getSizeInventory()&&!held.isEmpty();i++){if(!inventory.isItemValidForSlot(i,held))continue;ItemStack current=inventory.getStackInSlot(i);int maximum=Math.min(held.getMaxStackSize(),inventory.getInventoryStackLimit());if(current.isEmpty()){ItemStack placed=held.copy();placed.setCount(Math.min(maximum,held.getCount()));inventory.setInventorySlotContents(i,placed);held.shrink(placed.getCount());}else if(current.isItemEqual(held)&&ItemStack.areItemStackTagsEqual(current,held)){int moved=Math.min(held.getCount(),Math.max(0,maximum-current.getCount()));current.grow(moved);held.shrink(moved);}inventory.markDirty();}if(held.isEmpty())setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);}}destination=null;searchCooldown=40;}
    @Override protected void dropFewItems(boolean player,int looting){if(!getHeldItemMainhand().isEmpty()){entityDropItem(getHeldItemMainhand(),0);setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);}if(Content.available("copper_ingot"))entityDropItem(new ItemStack(Content.item("copper_ingot"),1+rand.nextInt(3)),0);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Oxidation",getOxidation());tag.setBoolean("Waxed",isWaxed());tag.setInteger("OxidationTicks",oxidationTicks);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setOxidation(tag.getInteger("Oxidation"));wax(tag.getBoolean("Waxed"));oxidationTicks=tag.hasKey("OxidationTicks")?MathHelper.clamp(tag.getInteger("OxidationTicks"),1,12000):6000;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.copper_golem.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.copper_golem.death");}
}
