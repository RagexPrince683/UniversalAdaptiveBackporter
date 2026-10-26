package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;

@Mod.EventBusSubscriber(modid=UAB.ID)
public final class ModernEvents {
    private ModernEvents() { }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void click(PlayerInteractEvent.RightClickBlock event){if(event.getUseBlock()==Event.Result.DENY||!event.getWorld().isBlockModifiable(event.getEntityPlayer(),event.getPos()))return;boolean handled=CopperAging.use(event.getWorld(),event.getPos(),event.getWorld().getBlockState(event.getPos()),event.getEntityPlayer(),event.getHand(),event.getFace());if(!handled)handled=FilledCauldron.fill(event.getWorld(),event.getPos(),event.getEntityPlayer(),event.getHand());if(handled){event.setCancellationResult(EnumActionResult.SUCCESS);event.setCanceled(true);}}
    @SubscribeEvent public static void projectile(ProjectileImpactEvent event){RayTraceResult hit=event.getRayTraceResult();if(hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK)return;net.minecraft.world.World world=event.getEntity().world;Block block=world.getBlockState(hit.getBlockPos()).getBlock();if(block instanceof TargetBlock)((TargetBlock)block).hit(world,hit.getBlockPos(),hit,event.getEntity());if(block instanceof CandleBlock&&event.getEntity().isBurning()&&!world.isRemote)world.setBlockState(hit.getBlockPos(),world.getBlockState(hit.getBlockPos()).withProperty(CandleBlock.LIT,true),3);}
    @SubscribeEvent public static void witherRose(LivingDeathEvent event){if(event.getEntityLiving().world.isRemote||!(event.getSource().getTrueSource() instanceof EntityWither)||!Content.available("wither_rose"))return;net.minecraft.world.World world=event.getEntityLiving().world;BlockPos pos=event.getEntityLiving().getPosition();Block rose=Content.block("wither_rose");if(net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,event.getSource().getTrueSource())&&rose.canPlaceBlockAt(world,pos))world.setBlockState(pos,rose.getDefaultState(),3);else if(world.getGameRules().getBoolean("doMobLoot"))event.getEntityLiving().entityDropItem(Content.stack("wither_rose"),0);}
    private static boolean hoe(Block block){{net.minecraft.block.material.Material material=block.getDefaultState().getMaterial();if(material==net.minecraft.block.material.Material.LEAVES||material==net.minecraft.block.material.Material.SPONGE)return true;}String name=block.getRegistryName().getPath();return name.contains("wart_block")||name.equals("hay_block")||name.equals("dried_kelp_block")||name.equals("target")||name.equals("shroomlight")||name.equals("sculk")||name.equals("moss_block")||name.equals("sponge");}
    @SubscribeEvent public static void hoeSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event){ItemStack held=event.getEntityPlayer().getHeldItemMainhand();if(held.getItem() instanceof ItemHoe&&Features.enabled("functions.enableHoeMining")&&hoe(event.getState().getBlock())){Item.ToolMaterial material=Item.ToolMaterial.valueOf(((ItemHoe)held.getItem()).getMaterialName());event.setNewSpeed(Math.max(event.getNewSpeed(),material.getEfficiency()));}}
    @SubscribeEvent public static void hoeWear(BlockEvent.HarvestDropsEvent event){if(event.getHarvester()==null||!Features.enabled("functions.enableHoeMining")||!hoe(event.getState().getBlock()))return;ItemStack held=event.getHarvester().getHeldItemMainhand();if(held.getItem() instanceof ItemHoe&&!event.getHarvester().capabilities.isCreativeMode)held.damageItem(1,event.getHarvester());}
}
