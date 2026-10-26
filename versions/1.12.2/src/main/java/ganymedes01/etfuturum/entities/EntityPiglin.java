package ganymedes01.etfuturum.entities;

import java.util.*;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.*;
import net.minecraft.world.*;

public class EntityPiglin extends EntityMob implements IRangedAttackMob {
    private static final net.minecraft.network.datasync.DataParameter<Boolean> BABY=net.minecraft.network.datasync.EntityDataManager.createKey(EntityPiglin.class,net.minecraft.network.datasync.DataSerializers.BOOLEAN);
    private static final net.minecraft.network.datasync.DataParameter<Byte> POSE=net.minecraft.network.datasync.EntityDataManager.createKey(EntityPiglin.class,net.minecraft.network.datasync.DataSerializers.BYTE);
    private int admirationTicks,unsafeTicks,angerTicks;private UUID angerTarget;
    public EntityPiglin(World world){
        super(world);setSize(.6F,1.95F);
        tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIAvoidEntity<>(this,EntityPigZombie.class,6,1,1.2));
        tasks.addTask(1,new EntityAIAvoidEntity<EntityPlayer>(this,EntityPlayer.class,6,1,1.2){@Override public boolean shouldExecute(){return isChild()&&super.shouldExecute();}});
        tasks.addTask(2,new EntityAIAttackMelee(this,1,false){@Override public boolean shouldExecute(){return !hasCrossbow()&&!isChild()&&admirationTicks==0&&super.shouldExecute();}@Override public boolean shouldContinueExecuting(){return !hasCrossbow()&&admirationTicks==0&&super.shouldContinueExecuting();}});
        tasks.addTask(2,new EntityAIAttackRanged(this,1,40,8){@Override public boolean shouldExecute(){return hasCrossbow()&&!isChild()&&admirationTicks==0&&super.shouldExecute();}@Override public boolean shouldContinueExecuting(){return hasCrossbow()&&admirationTicks==0&&super.shouldContinueExecuting();}});
        tasks.addTask(5,new EntityAIWanderAvoidWater(this,.7));tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(7,new EntityAILookIdle(this));
        targetTasks.addTask(1,new EntityAIHurtByTarget(this,true){@Override public boolean shouldExecute(){return !isChild()&&super.shouldExecute();}});
        targetTasks.addTask(2,new EntityAINearestAttackableTarget<>(this,EntityPlayer.class,10,true,false,player->!isChild()&&admirationTicks==0&&(isBrute()||!wearsGold(player))));
        targetTasks.addTask(3,new EntityAINearestAttackableTarget<>(this,EntityHoglin.class,80,true,false,hoglin->!isChild()&&!isBrute()&&!hoglin.isChild()));
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND,new ItemStack(isBrute()?Items.GOLDEN_AXE:Items.GOLDEN_SWORD));
    }
    @Override protected void entityInit(){super.entityInit();dataManager.register(BABY,false);dataManager.register(POSE,(byte)0);}
    public boolean isBrute(){return false;}
    @Override public boolean isChild(){return dataManager.get(BABY);}
    public void setChild(boolean child){dataManager.set(BABY,child);setSize(child?.3F:.6F,child?.975F:1.95F);}
    @Override public void notifyDataManagerChange(net.minecraft.network.datasync.DataParameter<?> key){super.notifyDataManagerChange(key);if(BABY.equals(key))setSize(isChild()?.3F:.6F,isChild()?.975F:1.95F);}
    public boolean isAdmiring(){return (dataManager.get(POSE)&1)!=0;}
    public boolean isAttacking(){return (dataManager.get(POSE)&2)!=0;}
    private boolean hasCrossbow(){return getHeldItemMainhand().getItem()==Content.item("crossbow");}
    @Override public void setSwingingArms(boolean swinging){ }
    @Override public void attackEntityWithRangedAttack(EntityLivingBase target,float distance){if(world.isRemote||!hasCrossbow()||!canEntityBeSeen(target))return;ItemStack weapon=getHeldItemMainhand();if(!weapon.hasTagCompound())weapon.setTagCompound(new NBTTagCompound());weapon.getTagCompound().setBoolean("Charged",true);weapon.getTagCompound().setBoolean("CreativeCharge",true);weapon.getTagCompound().setTag("ChargedProjectile",new ItemStack(Items.ARROW).writeToNBT(new NBTTagCompound()));faceEntity(target,180,180);CrossbowItem.fire(world,this,weapon);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(isBrute()?50:16);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.35);getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(isBrute()?7:5);getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16);}
    @Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,IEntityLivingData data){IEntityLivingData result=super.onInitialSpawn(difficulty,data);if(!isBrute()){if(Content.available("crossbow")&&rand.nextBoolean())setItemStackToSlot(EntityEquipmentSlot.MAINHAND,Content.stack("crossbow"));setChild(rand.nextInt(5)==0);}return result;}
    public static boolean wearsGold(EntityPlayer player){for(ItemStack stack:player.inventory.armorInventory)if(stack.getItem() instanceof ItemArmor&&((ItemArmor)stack.getItem()).getArmorMaterial()==ItemArmor.ArmorMaterial.GOLD)return true;return false;}
    public void angerAt(EntityPlayer player){if(!player.capabilities.disableDamage&&!player.isSpectator()){angerTicks=600;angerTarget=player.getUniqueID();admirationTicks=0;setAttackTarget(isChild()?null:player);}}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(!isBrute()&&!isChild()&&admirationTicks==0&&angerTicks==0&&held.getItem()==Items.GOLD_INGOT){if(!world.isRemote){if(!player.capabilities.isCreativeMode)held.shrink(1);admire();}return true;}return super.processInteract(player,hand);}
    private void admire(){admirationTicks=120;setAttackTarget(null);getNavigator().clearPath();}
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();if(world.isRemote)return;
        dataManager.set(POSE,(byte)((admirationTicks>0?1:0)|(getAttackTarget()!=null?2:0)));
        if(angerTicks>0){angerTicks--;if(!isChild()&&angerTarget!=null&&ticksExisted%20==0&&getAttackTarget()==null){EntityPlayer player=world.getPlayerEntityByUUID(angerTarget);if(player!=null&&!player.capabilities.disableDamage&&!player.isSpectator()&&getDistanceSq(player)<256)setAttackTarget(player);}}else angerTarget=null;
        if(!isBrute()&&angerTicks==0&&getAttackTarget() instanceof EntityPlayer&&wearsGold((EntityPlayer)getAttackTarget()))setAttackTarget(null);
        if(admirationTicks>0){getNavigator().clearPath();setAttackTarget(null);if(--admirationTicks==0)entityDropItem(barter(),.5F);}
        else if(!isAIDisabled()&&!isBrute()&&!isChild()&&angerTicks==0&&ticksExisted%20==0&&net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,this))for(EntityItem item:world.getEntitiesWithinAABB(EntityItem.class,getEntityBoundingBox().grow(1,.5,1))){ItemStack gold=item.getItem();if(!item.isDead&&gold.getItem()==Items.GOLD_INGOT){gold.shrink(1);if(gold.isEmpty())item.setDead();else item.setItem(gold);admire();break;}}
        unsafeTicks=world.provider.isNether()?0:unsafeTicks+1;
        if(unsafeTicks>=300){EntityPigZombie zombie=new EntityPigZombie(world);zombie.copyLocationAndAnglesFrom(this);zombie.setChild(isChild());copyEquipment(this,zombie);zombie.enablePersistence();if(world.spawnEntity(zombie))setDead();}
    }
    static void copyEquipment(EntityLiving from,EntityLiving to){NBTTagCompound data=new NBTTagCompound();from.writeEntityToNBT(data);for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()){to.setItemStackToSlot(slot,from.getItemStackFromSlot(slot).copy());net.minecraft.nbt.NBTTagList chances=data.getTagList(slot.getSlotType()==EntityEquipmentSlot.Type.ARMOR?"ArmorDropChances":"HandDropChances",5);to.setDropChance(slot,chances.getFloatAt(slot.getIndex()));}if(from.hasCustomName())to.setCustomNameTag(from.getCustomNameTag());to.setAlwaysRenderNameTag(from.getAlwaysRenderNameTag());to.setNoAI(from.isAIDisabled());to.setCanPickUpLoot(from.canPickUpLoot());to.setHealth(Math.min(to.getMaxHealth(),from.getHealth()/from.getMaxHealth()*to.getMaxHealth()));}
    public ItemStack barter(){
        List<ItemStack> items=new ArrayList<>();List<Integer> weights=new ArrayList<>();
        ItemStack[] nativeItems={new ItemStack(Items.ENDER_PEARL,2+rand.nextInt(3)),new ItemStack(Items.STRING,3+rand.nextInt(7)),new ItemStack(Items.QUARTZ,5+rand.nextInt(8)),new ItemStack(Blocks.OBSIDIAN),new ItemStack(Items.FIRE_CHARGE),new ItemStack(Items.LEATHER,2+rand.nextInt(3)),new ItemStack(Blocks.SOUL_SAND,2+rand.nextInt(7)),new ItemStack(Items.NETHERBRICK,2+rand.nextInt(7)),new ItemStack(Blocks.GRAVEL,8+rand.nextInt(9)),PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM),PotionTypes.FIRE_RESISTANCE),PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION),PotionTypes.FIRE_RESISTANCE),PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM),PotionTypes.WATER)};
        int[] values={10,20,20,40,40,40,40,40,40,8,8,10};for(int i=0;i<values.length;i++){items.add(nativeItems[i]);weights.add(values[i]);}
        if(Features.enabled("enchantspotions.soulSpeed")){int level=1+rand.nextInt(3);ItemStack book=ItemEnchantedBook.getEnchantedItemStack(new net.minecraft.enchantment.EnchantmentData(ModEnchantments.soulSpeed,level)),boots=new ItemStack(Items.IRON_BOOTS);boots.addEnchantment(ModEnchantments.soulSpeed,level);items.add(book);weights.add(5);items.add(boots);weights.add(8);}
        items.add(new ItemStack(Items.IRON_NUGGET,10+rand.nextInt(27)));weights.add(10);
        if(Content.available("crying_obsidian")){items.add(new ItemStack(Content.block("crying_obsidian"),1+rand.nextInt(3)));weights.add(40);}
        if(Content.available("blackstone")){items.add(new ItemStack(Content.block("blackstone"),8+rand.nextInt(9)));weights.add(40);}
        int total=0;for(int weight:weights)total+=weight;int roll=rand.nextInt(total);for(int i=0;i<weights.size();i++)if((roll-=weights.get(i))<0)return items.get(i);throw new AssertionError();
    }
    @Override public boolean attackEntityFrom(DamageSource source,float amount){boolean hit=super.attackEntityFrom(source,amount);if(hit&&source.getTrueSource() instanceof EntityPlayer)NetherGameplay.anger(world,getPosition(),(EntityPlayer)source.getTrueSource());return hit;}
    @Override public boolean getCanSpawnHere(){return MobRegistry.enabled(getClass())&&world.provider.isNether()&&world.getDifficulty()!=EnumDifficulty.PEACEFUL&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&!world.containsAnyLiquid(getEntityBoundingBox());}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("AdmirationTicks",admirationTicks);tag.setInteger("UnsafeTicks",unsafeTicks);tag.setInteger("GoldAngerTicks",angerTicks);tag.setBoolean("IsBaby",isChild());if(angerTarget!=null)tag.setUniqueId("AngerTarget",angerTarget);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);admirationTicks=Math.max(0,Math.min(120,tag.getInteger("AdmirationTicks")));unsafeTicks=Math.max(0,Math.min(300,tag.getInteger("UnsafeTicks")));angerTicks=Math.max(0,Math.min(600,tag.getInteger("GoldAngerTicks")));setChild(tag.getBoolean("IsBaby"));angerTarget=tag.hasUniqueId("AngerTarget")?tag.getUniqueId("AngerTarget"):null;}
}
