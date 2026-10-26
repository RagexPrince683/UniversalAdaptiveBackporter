package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.EntityCrossbowBolt;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** The actual ammunition is consumed on charging and saved with the charged weapon. */
public final class CrossbowItem extends Item {
    CrossbowItem(){setMaxStackSize(1);setMaxDamage(465);addPropertyOverride(new ResourceLocation("pull"),(stack,world,entity)->entity!=null&&entity.getActiveItemStack()==stack&&!charged(stack)?Math.min(1,(float)entity.getItemInUseMaxCount()/chargeTime(stack)):0);addPropertyOverride(new ResourceLocation("pulling"),(stack,world,entity)->entity!=null&&entity.isHandActive()&&entity.getActiveItemStack()==stack&&!charged(stack)?1:0);addPropertyOverride(new ResourceLocation("charged"),(stack,world,entity)->charged(stack)?1:0);}
    public static boolean charged(ItemStack stack){return stack.hasTagCompound()&&stack.getTagCompound().getBoolean("Charged");}
    public static int chargeTime(ItemStack stack){return 25-5*Math.min(3,ModEnchantments.level(ModEnchantments.quickCharge,stack));}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.BOW;}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 72000;}
    @Override public int getItemEnchantability(){return 1;}
    private static boolean ammunition(ItemStack stack){return stack.getItem()==Items.ARROW||stack.getItem()==Items.TIPPED_ARROW||stack.getItem()==Items.SPECTRAL_ARROW;}
    private static ItemStack ammunition(EntityPlayer player){if(ammunition(player.getHeldItemOffhand()))return player.getHeldItemOffhand();if(ammunition(player.getHeldItemMainhand()))return player.getHeldItemMainhand();for(int i=0;i<player.inventory.getSizeInventory();i++){ItemStack stack=player.inventory.getStackInSlot(i);if(ammunition(stack))return stack;}return ItemStack.EMPTY;}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){ItemStack stack=player.getHeldItem(hand);if(charged(stack)){if(!world.isRemote)fire(world,player,stack);return new ActionResult<>(EnumActionResult.SUCCESS,stack);}if(!ammunition(player).isEmpty()||player.capabilities.isCreativeMode){player.setActiveHand(hand);return new ActionResult<>(EnumActionResult.SUCCESS,stack);}return new ActionResult<>(EnumActionResult.FAIL,stack);}
    @Override public void onUsingTick(ItemStack stack,EntityLivingBase living,int remaining){if(living instanceof EntityPlayer&&getMaxItemUseDuration(stack)-remaining>=chargeTime(stack)){charge(stack,living.world,(EntityPlayer)living);living.stopActiveHand();}}
    @Override public void onPlayerStoppedUsing(ItemStack stack,World world,EntityLivingBase living,int remaining){if(living instanceof EntityPlayer&&getMaxItemUseDuration(stack)-remaining>=chargeTime(stack))charge(stack,world,(EntityPlayer)living);}
    private static void charge(ItemStack stack,World world,EntityPlayer player){if(world.isRemote||charged(stack))return;ItemStack ammo=ammunition(player);if(ammo.isEmpty()&&!player.capabilities.isCreativeMode)return;ItemStack saved=ammo.isEmpty()?new ItemStack(Items.ARROW):ammo.copy();saved.setCount(1);if(!player.capabilities.isCreativeMode)ammo.shrink(1);if(!stack.hasTagCompound())stack.setTagCompound(new NBTTagCompound());stack.getTagCompound().setBoolean("Charged",true);stack.getTagCompound().setBoolean("CreativeCharge",player.capabilities.isCreativeMode);stack.getTagCompound().setTag("ChargedProjectile",saved.writeToNBT(new NBTTagCompound()));world.playSound(null,player.posX,player.posY,player.posZ,SoundEvents.ITEM_ARMOR_EQUIP_IRON,SoundCategory.PLAYERS,.6F,1.4F);}
    public static boolean fire(World world,EntityLivingBase shooter,ItemStack stack){return fire(world,shooter,stack,shooter.getLookVec());}
    public static boolean fireAt(World world,EntityLivingBase shooter,ItemStack stack,EntityLivingBase target){double dx=target.posX-shooter.posX,dz=target.posZ-shooter.posZ,dy=target.getEntityBoundingBox().minY+target.height/3D-(shooter.posY+shooter.getEyeHeight()-.1);return fire(world,shooter,stack,new Vec3d(dx,dy+Math.sqrt(dx*dx+dz*dz)*.1,dz).normalize());}
    private static boolean fire(World world,EntityLivingBase shooter,ItemStack stack,Vec3d aim){
        if(world.isRemote||!charged(stack))return false;NBTTagCompound tag=stack.getTagCompound();ItemStack ammo=tag.hasKey("ChargedProjectile",10)?new ItemStack(tag.getCompoundTag("ChargedProjectile")):new ItemStack(Items.ARROW);if(!ammunition(ammo))return false;
        int count=ModEnchantments.level(ModEnchantments.multishot,stack)>0?3:1;boolean fired=false;
        for(int i=0;i<count;i++){EntityCrossbowBolt bolt=new EntityCrossbowBolt(world,shooter,ammo,ModEnchantments.level(ModEnchantments.piercing,stack));Vec3d direction=aim.rotateYaw(i==0?0:(i==1?-10:10)*(float)Math.PI/180);bolt.shoot(direction.x,direction.y,direction.z,3.15F,1);bolt.pickupStatus=i==0&&!tag.getBoolean("CreativeCharge")?net.minecraft.entity.projectile.EntityArrow.PickupStatus.ALLOWED:net.minecraft.entity.projectile.EntityArrow.PickupStatus.CREATIVE_ONLY;fired|=world.spawnEntity(bolt);}
        if(fired){tag.setBoolean("Charged",false);tag.removeTag("ChargedProjectile");stack.damageItem(count,shooter);world.playSound(null,shooter.posX,shooter.posY,shooter.posZ,SoundEvents.ENTITY_ARROW_SHOOT,SoundCategory.PLAYERS,1,.8F);}return fired;
    }
}
