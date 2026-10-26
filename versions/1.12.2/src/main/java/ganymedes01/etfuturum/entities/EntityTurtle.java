package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class EntityTurtle extends EntityAquaticAnimal {
    private static final DataParameter<Boolean> EGG=EntityDataManager.createKey(EntityTurtle.class,DataSerializers.BOOLEAN);
    private BlockPos home;private int diggingTicks;private boolean hadChildAge;
    public EntityTurtle(World world){super(world);setSize(1.2F,.4F);if(navigator instanceof PathNavigateGround)((PathNavigateGround)navigator).setCanSwim(true);}
    @Override protected void initEntityAI(){tasks.addTask(1,new EntityAIPanic(this,1.2));tasks.addTask(2,new EntityAIMate(this,1));tasks.addTask(3,new EntityAITempt(this,1.1,Content.item("seagrass"),false));tasks.addTask(5,new EntityAIWander(this,.8));tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,6));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(EGG,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.25);}
    @Override public boolean canBreatheUnderwater(){return true;}
    public boolean hasEgg(){return dataManager.get(EGG);}
    public void setHome(BlockPos pos){home=pos.toImmutable();}
    @Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,IEntityLivingData data){setHome(new BlockPos(this));return super.onInitialSpawn(difficulty,data);}
    @Override public boolean isBreedingItem(ItemStack stack){return !hasEgg()&&!stack.isEmpty()&&stack.getItem()==Content.item("seagrass");}
    @Override public EntityAgeable createChild(EntityAgeable other){dataManager.set(EGG,true);setGrowingAge(6000);other.setGrowingAge(6000);resetInLove();((EntityAnimal)other).resetInLove();return null;}
    @Override public boolean getCanSpawnHere(){BlockPos pos=new BlockPos(this);return MobRegistry.enabled(getClass())&&world.provider.getDimension()==0&&pos.getY()>=60&&pos.getY()<=67&&world.getBlockState(pos.down()).getBlock()==Blocks.SAND&&world.getLightFromNeighbors(pos)>8&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&!world.containsAnyLiquid(getEntityBoundingBox());}
    @Override protected double swimAcceleration(){return isChild()?.006:.015;}
    @Override protected boolean swimDestination(){if(hasEgg()&&home!=null){swimX=home.getX()+.5;swimY=home.getY();swimZ=home.getZ()+.5;return true;}if(isChild())for(int i=0;i<4;i++){int x=MathHelper.floor(posX)+rand.nextInt(9)-4,y=MathHelper.floor(posY),z=MathHelper.floor(posZ)+rand.nextInt(9)-4;if(waterAt(x,y,z)&&waterAt(x,y-1,z)){swimX=x+.5;swimY=y-.5;swimZ=z+.5;return true;}}return false;}
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();if(world.isRemote)return;BlockPos pos=new BlockPos(this);if(home==null)home=pos;
        if(hadChildAge&&!isChild()&&Content.available("turtle_scute")&&world.getGameRules().getBoolean("doMobLoot"))entityDropItem(Content.stack("turtle_scute"),.1F);hadChildAge=isChild();
        if(hasEgg()&&!isInWater()){
            if(Math.abs(pos.getX()-home.getX())<9&&Math.abs(pos.getZ()-home.getZ())<9&&world.getBlockState(pos.down()).getBlock()==Blocks.SAND&&world.isAirBlock(pos)){
                navigator.clearPath();if(++diggingTicks>=200&&Content.available("turtle_egg")&&net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,this)&&world.setBlockState(pos,Content.block("turtle_egg").getStateFromMeta(rand.nextInt(4)<<2),3)){dataManager.set(EGG,false);diggingTicks=0;setGrowingAge(600);}
            }else if(ticksExisted%40==0&&world.isBlockLoaded(home))navigator.tryMoveToXYZ(home.getX()+.5,home.getY(),home.getZ()+.5,1);
        }else{
            diggingTicks=0;if(!isInWater()&&ticksExisted%100==0&&!hasEgg())for(int i=0;i<16;i++){int x=pos.getX()+rand.nextInt(17)-8,z=pos.getZ()+rand.nextInt(17)-8;if(waterAt(x,pos.getY(),z)){navigator.tryMoveToXYZ(x+.5,pos.getY(),z+.5,1);break;}}
        }
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("HasEgg",hasEgg());tag.setBoolean("HasHome",home!=null);if(home!=null){tag.setInteger("HomePosX",home.getX());tag.setInteger("HomePosY",home.getY());tag.setInteger("HomePosZ",home.getZ());}}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);dataManager.set(EGG,tag.getBoolean("HasEgg"));home=tag.getBoolean("HasHome")?new BlockPos(tag.getInteger("HomePosX"),MathHelper.clamp(tag.getInteger("HomePosY"),1,255),tag.getInteger("HomePosZ")):null;hadChildAge=isChild();}
}
