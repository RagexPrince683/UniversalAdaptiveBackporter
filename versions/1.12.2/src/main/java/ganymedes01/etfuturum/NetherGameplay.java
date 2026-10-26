package ganymedes01.etfuturum;

import java.util.UUID;
import ganymedes01.etfuturum.entities.EntityPiglin;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;

@Mod.EventBusSubscriber(modid=UAB.ID)
public final class NetherGameplay {
    private static final UUID SPEED=UUID.fromString("b10c9628-e8b7-4a56-b928-891b21aa491d");
    private NetherGameplay() { }
    public static boolean soulSpeed(Entity entity){return entity instanceof EntityLivingBase&&Features.enabled("enchantspotions.soulSpeed")&&ModEnchantments.level(ModEnchantments.soulSpeed,((EntityLivingBase)entity).getItemStackFromSlot(EntityEquipmentSlot.FEET))>0;}
    @SubscribeEvent public static void update(LivingEvent.LivingUpdateEvent event){
        EntityLivingBase entity=event.getEntityLiving();if(entity.world.isRemote)return;IAttributeInstance speed=entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);if(speed==null)return;
        AttributeModifier previous=speed.getModifier(SPEED);ItemStack boots=entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);int level=Features.enabled("enchantspotions.soulSpeed")?ModEnchantments.level(ModEnchantments.soulSpeed,boots):0;boolean active=false;
        if(level>0&&entity.onGround){BlockPos pos=new BlockPos(entity.posX,entity.getEntityBoundingBox().minY-.1,entity.posZ);if(entity.world.isBlockLoaded(pos)){Block block=entity.world.getBlockState(pos).getBlock();active=block==Blocks.SOUL_SAND||Content.available("soul_soil")&&block==Content.block("soul_soil");}}
        double amount=.03*(1+level*.35);if(previous!=null&&(!active||previous.getAmount()!=amount)){speed.removeModifier(previous);previous=null;}
        if(active&&previous==null)speed.applyModifier(new AttributeModifier(SPEED,"Soul speed",amount,0).setSaved(false));
        if(active&&entity.ticksExisted%5==0&&entity.getRNG().nextInt(5)==0&&(entity.posX!=entity.prevPosX||entity.posZ!=entity.prevPosZ))boots.damageItem(1,entity);
    }
    public static void anger(World world,BlockPos pos,EntityPlayer player){if(world.isRemote||!Features.enabled("entities.enableNetherMobs"))return;for(EntityPiglin piglin:world.getEntitiesWithinAABB(EntityPiglin.class,new AxisAlignedBB(pos).grow(16)))piglin.angerAt(player);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void gold(BlockEvent.BreakEvent event){Block block=event.getState().getBlock();if(block==Blocks.GOLD_BLOCK||block==Blocks.GOLD_ORE||block==Content.block("nether_gold_ore"))anger(event.getWorld(),event.getPos(),event.getPlayer());}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void containers(PlayerInteractEvent.RightClickBlock event){if(event.getUseBlock()==Event.Result.DENY||event.getEntityPlayer().isSneaking())return;Block block=event.getWorld().getBlockState(event.getPos()).getBlock();if(block instanceof BlockChest||block instanceof BlockShulkerBox||block==Blocks.ENDER_CHEST||block instanceof BarrelBlock)anger(event.getWorld(),event.getPos(),event.getEntityPlayer());}
}
