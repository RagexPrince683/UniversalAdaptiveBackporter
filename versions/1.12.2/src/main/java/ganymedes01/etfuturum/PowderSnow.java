package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.fml.common.eventhandler.*;

/** Local powder-snow occupancy, equipment and precipitation adaptations. */
public final class PowderSnow {
    private static final UUID SLOW=UUID.fromString("5749d482-7b17-434a-9e36-6eed22cdbe83");
    private static final Map<EntityLivingBase,Long> CONTACT=new WeakHashMap<>();
    private static final DamageSource FREEZE=new DamageSource("freeze").setDamageBypassesArmor();
    private PowderSnow(){}
    public static boolean enabled(){return Features.enabled("content.powder_snow");}
    static void define(){Content.add("powder_snow","content.powder_snow",new Snow()).setCreativeTab(null);Content.add("powder_snow_cauldron","content.powder_snow",new Cauldron()).setCreativeTab(null);Content.add("powder_snow_bucket","content.powder_snow",new ItemBucket(Content.block("powder_snow")).setMaxStackSize(1).setContainerItem(Items.BUCKET));MinecraftForge.EVENT_BUS.register(new PowderSnow());dispensers();}
    public static void precipitation(World world,BlockPos snow){
        BlockPos pos=snow.down();if(world.isRemote||!enabled()||pos.getY()<0||!world.isBlockLoaded(pos)||!world.isRaining()||!world.canSeeSky(snow)||world.getBiome(pos).getTemperature(pos)>=.15F)return;
        IBlockState state=world.getBlockState(pos);boolean empty=state.getBlock()==Blocks.CAULDRON&&state.getValue(BlockCauldron.LEVEL)==0;
        if(!empty&&!(state.getBlock() instanceof Cauldron)||state.getValue(BlockCauldron.LEVEL)>=3||world.rand.nextInt(20)!=1)return;
        world.setBlockState(pos,Content.block("powder_snow_cauldron").getDefaultState().withProperty(BlockCauldron.LEVEL,empty?1:state.getValue(BlockCauldron.LEVEL)+1),3);
    }
    private static boolean boots(EntityLivingBase entity){ItemStack stack=entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);return !stack.isEmpty()&&stack.getItem() instanceof ItemArmor&&((ItemArmor)stack.getItem()).getArmorMaterial()==ItemArmor.ArmorMaterial.LEATHER;}
    private static boolean warm(EntityLivingBase entity){for(ItemStack stack:entity.getArmorInventoryList())if(!stack.isEmpty()&&stack.getItem() instanceof ItemArmor&&((ItemArmor)stack.getItem()).getArmorMaterial()==ItemArmor.ArmorMaterial.LEATHER)return true;return false;}
    private static boolean immune(EntityLivingBase entity){return entity instanceof EntitySnowman||entity instanceof EntityPolarBear||entity instanceof EntityStray||entity instanceof EntityPlayer&&(((EntityPlayer)entity).isSpectator()||((EntityPlayer)entity).capabilities.isCreativeMode);}
    @SubscribeEvent public void freeze(LivingEvent.LivingUpdateEvent event){EntityLivingBase entity=event.getEntityLiving();if(entity.world.isRemote)return;Long touched=CONTACT.get(entity);long now=entity.world.getTotalWorldTime();boolean inside=touched!=null&&now>=touched&&now-touched<=1;int previous=Math.max(0,Math.min(140,entity.getEntityData().getInteger("UabFrozenTicks"))),ticks=inside&&!immune(entity)&&!warm(entity)?Math.min(140,previous+1):Math.max(0,previous-2);if(ticks!=previous)entity.getEntityData().setInteger("UabFrozenTicks",ticks);
        IAttributeInstance speed=entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);if(speed!=null){double amount=-.01*(ticks/28);AttributeModifier old=speed.getModifier(SLOW);if(old!=null&&old.getAmount()!=amount){speed.removeModifier(old);old=null;}if(amount!=0&&old==null)speed.applyModifier(new AttributeModifier(SLOW,"UAB powder snow freezing",amount,0));}
        if(convert(entity,inside&&!immune(entity)&&!warm(entity))){event.setCanceled(true);return;}
        if(inside&&ticks>=140&&entity.ticksExisted%40==0)entity.attackEntityFrom(FREEZE,entity instanceof EntityBlaze||entity instanceof EntityMagmaCube||entity instanceof ganymedes01.etfuturum.entities.EntityStrider?5:1);
    }
    static boolean use(World world,BlockPos pos,EntityPlayer player,EnumHand hand,EnumFacing side){ItemStack held=player.getHeldItem(hand);if(!world.isBlockModifiable(player,pos)||!player.canPlayerEdit(pos,side,held))return false;IBlockState state=world.getBlockState(pos);Block block=state.getBlock();
        if((block==Blocks.CAULDRON||block==Content.block("lava_cauldron"))&&held.getItem()==Content.item("powder_snow_bucket")){if(!world.isRemote&&world.setBlockState(pos,Content.block("powder_snow_cauldron").getDefaultState().withProperty(BlockCauldron.LEVEL,3),3)){if(!player.capabilities.isCreativeMode)FilledCauldron.exchange(player,hand,new ItemStack(Items.BUCKET));sound(world,pos,"empty_powder_snow");}return true;}
        if(!(block instanceof Cauldron))return false;
        if(held.getItem()==Items.WATER_BUCKET){if(!world.isRemote&&world.setBlockState(pos,Blocks.CAULDRON.getDefaultState().withProperty(BlockCauldron.LEVEL,3),3)){if(!player.capabilities.isCreativeMode)FilledCauldron.exchange(player,hand,new ItemStack(Items.BUCKET));sound(world,pos,"empty");}return true;}
        if(held.getItem()==Items.LAVA_BUCKET&&Content.available("lava_cauldron")){if(!world.isRemote&&world.setBlockState(pos,Content.block("lava_cauldron").getDefaultState().withProperty(BlockCauldron.LEVEL,3),3)){if(!player.capabilities.isCreativeMode)FilledCauldron.exchange(player,hand,new ItemStack(Items.BUCKET));sound(world,pos,"empty_lava");}return true;}

        if(held.getItem()==Items.BUCKET&&state.getValue(BlockCauldron.LEVEL)==3){if(!world.isRemote&&world.setBlockState(pos,Blocks.CAULDRON.getDefaultState(),3)){FilledCauldron.exchange(player,hand,Content.stack("powder_snow_bucket"));sound(world,pos,"fill_powder_snow");}return true;}
        if(held.getItem()==Content.item("powder_snow_bucket")&&state.getValue(BlockCauldron.LEVEL)<3){if(!world.isRemote&&world.setBlockState(pos,state.withProperty(BlockCauldron.LEVEL,3),3)){if(!player.capabilities.isCreativeMode)FilledCauldron.exchange(player,hand,new ItemStack(Items.BUCKET));sound(world,pos,"empty_powder_snow");}return true;}return false;
    }
    private static boolean convert(EntityLivingBase entity,boolean inside){
        if(entity.getClass()!=EntitySkeleton.class)return false;
        int elapsed=inside?Math.min(440,entity.getEntityData().getInteger("UabPowderConversion")+1):0;entity.getEntityData().setInteger("UabPowderConversion",elapsed);
        if(elapsed<440||entity.isRiding()||entity.isBeingRidden()||((EntitySkeleton)entity).getLeashed())return false;
        net.minecraft.nbt.NBTTagCompound data=new net.minecraft.nbt.NBTTagCompound();entity.writeToNBT(data);data=(net.minecraft.nbt.NBTTagCompound)data.copy();data.removeTag("UUIDMost");data.removeTag("UUIDLeast");data.getCompoundTag("ForgeData").removeTag("UabPowderConversion");
        EntityStray stray=new EntityStray(entity.world);stray.readFromNBT(data);if(!entity.world.spawnEntity(stray))return false;entity.setDead();return true;
    }
    private static void sound(World world,BlockPos pos,String event){world.playSound(null,pos,ModernSounds.get("item.bucket."+event),SoundCategory.BLOCKS,1,1);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void click(PlayerInteractEvent.RightClickBlock event){if(event.getUseBlock()==Event.Result.DENY||event.getUseItem()==Event.Result.DENY)return;if(use(event.getWorld(),event.getPos(),event.getEntityPlayer(),event.getHand(),event.getFace())){event.setCancellationResult(EnumActionResult.SUCCESS);event.setCanceled(true);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void bucket(FillBucketEvent event){RayTraceResult hit=event.getTarget();if(event.getResult()==Event.Result.DENY||hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK||event.getEmptyBucket().getItem()!=Items.BUCKET||!(event.getWorld().getBlockState(hit.getBlockPos()).getBlock() instanceof Snow))return;EntityPlayer player=event.getEntityPlayer();BlockPos pos=hit.getBlockPos();if(!event.getWorld().isBlockModifiable(player,pos)||!player.canPlayerEdit(pos,hit.sideHit,event.getEmptyBucket()))return;if(!event.getWorld().isRemote&&event.getWorld().setBlockToAir(pos)){event.setFilledBucket(Content.stack("powder_snow_bucket"));event.setResult(Event.Result.ALLOW);sound(event.getWorld(),pos,"fill_powder_snow");}}
    public static final class Snow extends Block {
        Snow(){super(Material.CRAFTED_SNOW);setHardness(.25F);setLightOpacity(0);setSoundType(SoundType.SNOW);}
        @Override public boolean isOpaqueCube(IBlockState state){return false;}
        @Override public boolean isFullCube(IBlockState state){return false;}
        @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
        @Override public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return net.minecraft.block.state.BlockFaceShape.UNDEFINED;}
        @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
        @Override public int quantityDropped(Random random){return 0;}
        @Override public boolean canSilkHarvest(World world,BlockPos pos,IBlockState state,EntityPlayer player){return false;}
        @Override public ItemStack getPickBlock(IBlockState state,RayTraceResult hit,World world,BlockPos pos,EntityPlayer player){return Content.stack("powder_snow_bucket");}
        @Override public void addCollisionBoxToList(IBlockState state,World world,BlockPos pos,AxisAlignedBB box,List<AxisAlignedBB> result,Entity entity,boolean actual){if(entity instanceof EntityFallingBlock||entity instanceof EntityLivingBase&&!entity.isSneaking()&&boots((EntityLivingBase)entity)&&entity.getEntityBoundingBox().minY>=pos.getY()+1-.001)addCollisionBoxToList(pos,box,result,FULL_BLOCK_AABB);else if(entity!=null&&entity.fallDistance>2.5F)addCollisionBoxToList(pos,box,result,new AxisAlignedBB(0,0,0,1,.9,1));}
        @Override public void onFallenUpon(World world,BlockPos pos,Entity entity,float distance){entity.fallDistance=0;}
        @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(!(entity instanceof EntityLivingBase))return;EntityLivingBase living=(EntityLivingBase)entity;entity.fallDistance=0;if(entity.isBurning()){if(!world.isRemote&&(entity instanceof EntityPlayer?world.isBlockModifiable((EntityPlayer)entity,pos):net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,entity)))world.setBlockToAir(pos);entity.extinguish();return;}if(!world.isRemote)CONTACT.put(living,world.getTotalWorldTime());if(entity instanceof EntityPlayer&&((EntityPlayer)entity).capabilities.isFlying)return;entity.motionX*=.9;entity.motionZ*=.9;entity.motionY=Math.max(-.08,entity.motionY*.9);if(boots(living)&&LivingRules.jumping(living)&&!living.isSneaking())entity.motionY=.2;}
    }
    public static final class Cauldron extends BlockCauldron {
        Cauldron(){setHardness(2);setResistance(2);setDefaultState(getDefaultState().withProperty(LEVEL,1));}
        @Override public void fillWithRain(World world,BlockPos pos){}
        @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Items.CAULDRON;}
        @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return new ItemStack(Items.CAULDRON);}
        @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){return use(world,pos,player,hand,side);}
        @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(!world.isRemote&&entity.isBurning()&&entity.getEntityBoundingBox().minY<=pos.getY()+.3+state.getValue(LEVEL)*.2){entity.extinguish();int level=state.getValue(LEVEL)-1;world.setBlockState(pos,level==0?Blocks.CAULDRON.getDefaultState():state.withProperty(LEVEL,level),3);}}
    }
    private static void dispensers(){
        final Item filled=Content.item("powder_snow_bucket");final BehaviorDefaultDispenseItem eject=new BehaviorDefaultDispenseItem();
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(filled,new BehaviorDefaultDispenseItem(){@Override protected ItemStack dispenseStack(IBlockSource source,ItemStack stack){World world=source.getWorld();BlockPos target=source.getBlockPos().offset(source.getBlockState().getValue(BlockDispenser.FACING));if(((ItemBucket)filled).tryPlaceContainedLiquid(null,world,target)){sound(world,target,"empty_powder_snow");return new ItemStack(Items.BUCKET);}return super.dispenseStack(source,stack);}});
        final IBehaviorDispenseItem previous=BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.BUCKET);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BUCKET,(source,stack)->{World world=source.getWorld();BlockPos target=source.getBlockPos().offset(source.getBlockState().getValue(BlockDispenser.FACING));if(!(world.getBlockState(target).getBlock() instanceof Snow)||!world.setBlockToAir(target))return previous.dispense(source,stack);ItemStack result=new ItemStack(filled);stack.shrink(1);sound(world,target,"fill_powder_snow");if(stack.isEmpty())return result;TileEntityDispenser dispenser=source.getBlockTileEntity();if(dispenser.addItemStack(result)<0)eject.dispense(source,result);return stack;});
    }
}
