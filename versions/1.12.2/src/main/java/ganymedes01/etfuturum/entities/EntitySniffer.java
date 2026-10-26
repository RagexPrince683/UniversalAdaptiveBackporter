package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import java.util.*;
public class EntitySniffer extends EntityAnimal {
    private static final DataParameter<Boolean> DIGGING=EntityDataManager.createKey(EntitySniffer.class,DataSerializers.BOOLEAN);
    private final Deque<Long> explored=new ArrayDeque<>();
    private int digCooldown=1200,digging;
    public EntitySniffer(World world){super(world);setSize(1.9F,1.75F);tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIPanic(this,1.3));tasks.addTask(2,new EntityAIMate(this,1));tasks.addTask(3,new EntityAITempt(this,1.1,Content.item("torchflower_seeds"),false));tasks.addTask(4,new EntityAIFollowParent(this,1));tasks.addTask(5,new EntityAIWanderAvoidWater(this,.6));}
    @Override protected void entityInit(){super.entityInit();dataManager.register(DIGGING,false);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.1);}
    public boolean isDigging(){return dataManager.get(DIGGING);}
    @Override public boolean isBreedingItem(ItemStack stack){return Content.available("torchflower_seeds")&&stack.getItem()==Content.item("torchflower_seeds");}
    @Override public EntityAgeable createChild(EntityAgeable mate){if(!Content.available("sniffer_egg")||entityDropItem(Content.stack("sniffer_egg"),.1F)==null)return null;setGrowingAge(6000);mate.setGrowingAge(6000);resetInLove();((EntityAnimal)mate).resetInLove();return null;}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||isChild()||isAIDisabled())return;if(digCooldown>0)--digCooldown;BlockPos ground=new BlockPos(this).down();if(digging>0){getNavigator().clearPath();motionX=motionZ=0;if(--digging==0){if(net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,this)){String name=rand.nextBoolean()?"torchflower_seeds":"pitcher_pod";if(Content.available(name)&&entityDropItem(Content.stack(name),.1F)!=null){explored.addLast(ground.toLong());while(explored.size()>20)explored.removeFirst();}}digCooldown=9600;dataManager.set(DIGGING,false);}return;}if(digCooldown==0&&ticksExisted%40==0&&onGround&&!isInWater()){net.minecraft.block.Block block=world.getBlockState(ground).getBlock();if((block==Blocks.GRASS||block==Blocks.DIRT||block==Content.block("moss_block"))&&!explored.contains(ground.toLong())){digging=120;dataManager.set(DIGGING,true);}}}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("DigCooldown",digCooldown);NBTTagList list=new NBTTagList();for(long pos:explored){NBTTagCompound entry=new NBTTagCompound();entry.setLong("Pos",pos);list.appendTag(entry);}tag.setTag("Explored",list);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);digCooldown=tag.hasKey("DigCooldown")?MathHelper.clamp(tag.getInteger("DigCooldown"),0,9600):1200;explored.clear();NBTTagList list=tag.getTagList("Explored",10);for(int i=0;i<Math.min(20,list.tagCount());i++)explored.addLast(list.getCompoundTagAt(i).getLong("Pos"));}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.sniffer.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.sniffer.death");}
}
