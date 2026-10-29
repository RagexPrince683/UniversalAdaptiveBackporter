package ganymedes01.etfuturum;

import java.util.*;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.relauncher.*;
import ganymedes01.etfuturum.blocks.BaseBlock;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.*;

/** Native legacy bucket, collision and entity rules with local snow occupancy. */
public final class PowderSnow {
    private static final UUID SLOW=UUID.fromString("5749d482-7b17-434a-9e36-6eed22cdbe83");
    private static final Map<EntityLivingBase,Long> CONTACT=new WeakHashMap<>();
    private static final DamageSource FREEZE=new DamageSource("freeze").setDamageBypassesArmor();
    private PowderSnow(){}
    public static boolean enabled(){return ganymedes01.etfuturum.configuration.ContentAvailability.enabled("powder_snow");}
    public static void initialize(){
        register("powder_snow",new Snow());register("powder_snow_cauldron",new Cauldron());
        ModernAdditions.add("powder_snow_bucket","powder_snow",new ItemBucket(ModernAdditions.block("powder_snow")).setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("etfuturum.powder_snow_bucket").setTextureName("powder_snow_bucket"));
        MinecraftForge.EVENT_BUS.register(new PowderSnow());dispensers();
    }
    private static void register(String name,Block block){cpw.mods.fml.common.registry.GameRegistry.registerBlock(block,(Class<? extends ItemBlock>)null,name);ModernAdditions.BLOCKS.put(name,block);block.setCreativeTab(null);ganymedes01.etfuturum.configuration.ContentAvailability.register(block,enabled());}
    public static void precipitation(World world,int x,int y,int z){
        int cy=y-1;if(world.isRemote||!enabled()||cy<0||!world.blockExists(x,cy,z)||!world.isRaining()||!world.canBlockSeeTheSky(x,y,z)||world.getBiomeGenForCoords(x,z).getFloatTemperature(x,cy,z)>=.15F)return;
        Block block=world.getBlock(x,cy,z);int level=world.getBlockMetadata(x,cy,z);boolean empty=block==Blocks.cauldron&&level==0;
        if(!empty&&!(block instanceof Cauldron)||level>=3||world.rand.nextInt(20)!=1)return;
        world.setBlock(x,cy,z,ModernAdditions.block("powder_snow_cauldron"),empty?1:level+1,3);
    }
    private static boolean boots(EntityLivingBase entity){ItemStack stack=entity.getEquipmentInSlot(1);return stack!=null&&stack.getItem() instanceof ItemArmor&&((ItemArmor)stack.getItem()).getArmorMaterial()==ItemArmor.ArmorMaterial.CLOTH;}
    private static boolean warm(EntityLivingBase entity){for(int slot=1;slot<=4;slot++){ItemStack stack=entity.getEquipmentInSlot(slot);if(stack!=null&&stack.getItem() instanceof ItemArmor&&((ItemArmor)stack.getItem()).getArmorMaterial()==ItemArmor.ArmorMaterial.CLOTH)return true;}return false;}
    private static boolean immune(EntityLivingBase entity){return entity instanceof EntitySnowman||entity instanceof ganymedes01.etfuturum.entities.EntityPolarBear||entity instanceof ganymedes01.etfuturum.entities.EntityStray||entity instanceof EntityPlayer&&(((EntityPlayer)entity).capabilities.isCreativeMode||ganymedes01.etfuturum.api.spectator.SpectatorUtils.isSpectator((EntityPlayer)entity));}
    @SubscribeEvent public void freeze(LivingEvent.LivingUpdateEvent event){EntityLivingBase entity=event.entityLiving;if(entity.worldObj.isRemote)return;Long touched=CONTACT.get(entity);long now=entity.worldObj.getTotalWorldTime();boolean inside=touched!=null&&now>=touched&&now-touched<=1;int previous=Math.max(0,Math.min(140,entity.getEntityData().getInteger("UabFrozenTicks"))),ticks=inside&&!immune(entity)&&!warm(entity)?Math.min(140,previous+1):Math.max(0,previous-2);if(ticks!=previous)entity.getEntityData().setInteger("UabFrozenTicks",ticks);
        IAttributeInstance speed=entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);if(speed!=null){double amount=-.01*(ticks/28);AttributeModifier old=speed.getModifier(SLOW);if(old!=null&&old.getAmount()!=amount){speed.removeModifier(old);old=null;}if(amount!=0&&old==null)speed.applyModifier(new AttributeModifier(SLOW,"UAB powder snow freezing",amount,0));}
        if(convert(entity,inside&&!immune(entity)&&!warm(entity))){event.setCanceled(true);return;}
        if(inside&&ticks>=140&&entity.ticksExisted%40==0)entity.attackEntityFrom(FREEZE,entity instanceof EntityBlaze||entity instanceof EntityMagmaCube||entity instanceof ganymedes01.etfuturum.entities.EntityStrider?5:1);
    }
    private static boolean convert(EntityLivingBase entity,boolean inside){
        if(entity.getClass()!=EntitySkeleton.class||((EntitySkeleton)entity).getSkeletonType()!=0||!ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableStray)return false;
        int elapsed=inside?Math.min(440,entity.getEntityData().getInteger("UabPowderConversion")+1):0;entity.getEntityData().setInteger("UabPowderConversion",elapsed);
        if(elapsed<440||entity.ridingEntity!=null||entity.riddenByEntity!=null||((EntitySkeleton)entity).getLeashed())return false;
        net.minecraft.nbt.NBTTagCompound data=new net.minecraft.nbt.NBTTagCompound();entity.writeToNBT(data);data=(net.minecraft.nbt.NBTTagCompound)data.copy();data.removeTag("UUIDMost");data.removeTag("UUIDLeast");data.getCompoundTag("ForgeData").removeTag("UabPowderConversion");
        ganymedes01.etfuturum.entities.EntityStray stray=new ganymedes01.etfuturum.entities.EntityStray(entity.worldObj);stray.readFromNBT(data);if(!entity.worldObj.spawnEntityInWorld(stray))return false;entity.setDead();return true;
    }
    private static void exchange(EntityPlayer player,ItemStack result){if(player.capabilities.isCreativeMode){if(!player.inventory.hasItemStack(result)&&!player.inventory.addItemStackToInventory(result))player.dropPlayerItemWithRandomChoice(result,false);return;}ItemStack held=player.getHeldItem();if(--held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,result);else if(!player.inventory.addItemStackToInventory(result))player.dropPlayerItemWithRandomChoice(result,false);player.inventory.markDirty();}
    public static boolean use(World world,int x,int y,int z,EntityPlayer player,int side){ItemStack held=player.getHeldItem();if(held==null||!world.canMineBlock(player,x,y,z)||!player.canPlayerEdit(x,y,z,side,held))return false;Block block=world.getBlock(x,y,z);int level=world.getBlockMetadata(x,y,z);Item filled=ModernAdditions.item("powder_snow_bucket");
        if((block==Blocks.cauldron||block==ModBlocks.LAVA_CAULDRON.get())&&held.getItem()==filled){if(!world.isRemote&&world.setBlock(x,y,z,ModernAdditions.block("powder_snow_cauldron"),3,3)){if(!player.capabilities.isCreativeMode)exchange(player,new ItemStack(Items.bucket));sound(world,x,y,z,"empty_powder_snow");}return true;}
        if(!(block instanceof Cauldron))return false;
        if(held.getItem()==Items.water_bucket){if(!world.isRemote&&world.setBlock(x,y,z,Blocks.cauldron,3,3)){if(!player.capabilities.isCreativeMode)exchange(player,new ItemStack(Items.bucket));sound(world,x,y,z,"empty");}return true;}
        if(held.getItem()==Items.lava_bucket&&ModBlocks.LAVA_CAULDRON.isEnabled()){if(!world.isRemote&&world.setBlock(x,y,z,ModBlocks.LAVA_CAULDRON.get(),0,3)){if(!player.capabilities.isCreativeMode)exchange(player,new ItemStack(Items.bucket));sound(world,x,y,z,"empty_lava");}return true;}

        if(held.getItem()==Items.bucket&&level==3){if(!world.isRemote&&world.setBlock(x,y,z,Blocks.cauldron,0,3)){exchange(player,new ItemStack(filled));sound(world,x,y,z,"fill_powder_snow");}return true;}
        if(held.getItem()==filled&&level<3){if(!world.isRemote&&world.setBlockMetadataWithNotify(x,y,z,3,3)){if(!player.capabilities.isCreativeMode)exchange(player,new ItemStack(Items.bucket));sound(world,x,y,z,"empty_powder_snow");}return true;}return false;
    }
    private static void sound(World world,int x,int y,int z,String event){world.playSoundEffect(x+.5,y+.5,z+.5,Tags.MC_ASSET_VER+":item.bucket."+event,1,1);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void click(PlayerInteractEvent event){if(event.action!=PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK||event.useBlock==Event.Result.DENY||event.useItem==Event.Result.DENY)return;if(use(event.world,event.x,event.y,event.z,event.entityPlayer,event.face))event.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void bucket(FillBucketEvent event){MovingObjectPosition hit=event.target;if(event.getResult()==Event.Result.DENY||hit==null||hit.typeOfHit!=MovingObjectPosition.MovingObjectType.BLOCK||event.current.getItem()!=Items.bucket||!(event.world.getBlock(hit.blockX,hit.blockY,hit.blockZ) instanceof Snow))return;EntityPlayer player=event.entityPlayer;int x=hit.blockX,y=hit.blockY,z=hit.blockZ;if(!event.world.canMineBlock(player,x,y,z)||!player.canPlayerEdit(x,y,z,hit.sideHit,event.current))return;if(!event.world.isRemote&&event.world.setBlockToAir(x,y,z)){event.result=ModernAdditions.stack("powder_snow_bucket");event.setResult(Event.Result.ALLOW);sound(event.world,x,y,z,"fill_powder_snow");}}
    public static final class Snow extends BaseBlock {
        Snow(){super(Material.craftedSnow);setNames("powder_snow");setHardness(.25F);setLightOpacity(0);setStepSound(soundTypeSnow);}
        @Override public boolean isOpaqueCube(){return false;}
        @Override public boolean renderAsNormalBlock(){return false;}
        @Override public boolean isSideSolid(IBlockAccess world,int x,int y,int z,ForgeDirection side){return false;}
        @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return null;}
        @Override public int quantityDropped(Random random){return 0;}
        @Override protected boolean canSilkHarvest(){return false;}
        @Override @SideOnly(Side.CLIENT) public Item getItem(World world,int x,int y,int z){return ModernAdditions.item("powder_snow_bucket");}
        @Override public void addCollisionBoxesToList(World world,int x,int y,int z,AxisAlignedBB box,List result,Entity entity){AxisAlignedBB shape=null;if(entity instanceof EntityFallingBlock||entity instanceof EntityLivingBase&&!entity.isSneaking()&&boots((EntityLivingBase)entity)&&entity.boundingBox.minY>=y+1-.001)shape=AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1);else if(entity!=null&&entity.fallDistance>2.5F)shape=AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+.9,z+1);if(shape!=null&&shape.intersectsWith(box))result.add(shape);}
        @Override public void onFallenUpon(World world,int x,int y,int z,Entity entity,float distance){entity.fallDistance=0;}
        @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity){if(!(entity instanceof EntityLivingBase))return;EntityLivingBase living=(EntityLivingBase)entity;entity.fallDistance=0;if(entity.isBurning()){if(!world.isRemote&&(entity instanceof EntityPlayer?world.canMineBlock((EntityPlayer)entity,x,y,z):world.getGameRules().getGameRuleBooleanValue("mobGriefing")))world.setBlockToAir(x,y,z);entity.extinguish();return;}if(!world.isRemote)CONTACT.put(living,world.getTotalWorldTime());if(entity instanceof EntityPlayer&&((EntityPlayer)entity).capabilities.isFlying)return;entity.motionX*=.9;entity.motionZ*=.9;entity.motionY=Math.max(-.08,entity.motionY*.9);if(boots(living)&&living instanceof ganymedes01.etfuturum.entities.RiderInput&&((ganymedes01.etfuturum.entities.RiderInput)living).etfuturum$isJumping()&&!living.isSneaking())entity.motionY=.2;}
    }
    public static final class Cauldron extends BlockCauldron {
        @SideOnly(Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
        Cauldron(){setBlockName("etfuturum.powder_snow_cauldron");setHardness(2);setResistance(2);}
        @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas){models=new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[3];for(int i=0;i<3;i++)models[i]=new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas,"powder_snow_cauldron_"+(i==2?"full":"level"+(i+1)));blockIcon=models[0].particle;}
        @SideOnly(Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta){return models[Math.max(0,Math.min(2,meta-1))];}
        @Override public int getRenderType(){return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK;}
        @Override public void fillWithRain(World world,int x,int y,int z){}
        @Override public Item getItemDropped(int meta,Random random,int fortune){return Items.cauldron;}
        @Override @SideOnly(Side.CLIENT) public Item getItem(World world,int x,int y,int z){return Items.cauldron;}
        @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float a,float b,float c){return use(world,x,y,z,player,side);}
        @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity){int level=world.getBlockMetadata(x,y,z);if(!world.isRemote&&entity.isBurning()&&entity.boundingBox.minY<=y+.3+level*.2){entity.extinguish();if(level<=1)world.setBlock(x,y,z,Blocks.cauldron,0,3);else world.setBlockMetadataWithNotify(x,y,z,level-1,3);}}
    }
    private static void dispensers(){
        final Item filled=ModernAdditions.item("powder_snow_bucket");final BehaviorDefaultDispenseItem eject=new BehaviorDefaultDispenseItem();
        BlockDispenser.dispenseBehaviorRegistry.putObject(filled,new BehaviorDefaultDispenseItem(){@Override protected ItemStack dispenseStack(IBlockSource source,ItemStack stack){World world=source.getWorld();ForgeDirection side=ForgeDirection.getOrientation(source.getBlockMetadata()&7);int x=source.getXInt()+side.offsetX,y=source.getYInt()+side.offsetY,z=source.getZInt()+side.offsetZ;if(((ItemBucket)filled).tryPlaceContainedLiquid(world,x,y,z)){sound(world,x,y,z,"empty_powder_snow");return new ItemStack(Items.bucket);}return super.dispenseStack(source,stack);}});
        final IBehaviorDispenseItem previous=(IBehaviorDispenseItem)BlockDispenser.dispenseBehaviorRegistry.getObject(Items.bucket);
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.bucket,(IBehaviorDispenseItem)(source,stack)->{World world=source.getWorld();ForgeDirection side=ForgeDirection.getOrientation(source.getBlockMetadata()&7);int x=source.getXInt()+side.offsetX,y=source.getYInt()+side.offsetY,z=source.getZInt()+side.offsetZ;if(!(world.getBlock(x,y,z) instanceof Snow)||!world.setBlockToAir(x,y,z))return previous.dispense(source,stack);ItemStack result=new ItemStack(filled);stack.stackSize--;sound(world,x,y,z,"fill_powder_snow");if(stack.stackSize<=0)return result;TileEntityDispenser dispenser=(TileEntityDispenser)source.getBlockTileEntity();if(dispenser.func_146019_a(result)<0)eject.dispense(source,result);return stack;});
    }
}
