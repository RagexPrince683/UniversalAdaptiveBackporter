package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class Gameplay {
    @SubscribeEvent public void seagrass(net.minecraftforge.event.entity.player.BonemealEvent event){
        if(event.getResult()!=net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT)return;
        if(!Content.available("seagrass")||event.getBlock().getBlock() instanceof Seagrass)return;
        BlockPos pos=event.getPos();if(!AquaticPlant.sourceWater(event.getWorld(),pos)||!Content.block("seagrass").canPlaceBlockAt(event.getWorld(),pos))return;
        if(!event.getWorld().isRemote)event.getWorld().setBlockState(pos,Content.block("seagrass").getDefaultState(),2);
        event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW);
    }
    @SubscribeEvent public void nylium(net.minecraftforge.event.entity.player.BonemealEvent event){
        if(event.getResult()!=net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT||event.getBlock().getBlock()!=Blocks.NETHERRACK||event.getWorld().getBlockState(event.getPos().up()).isOpaqueCube())return;
        boolean crimson=false,warped=false;
        for(BlockPos pos:BlockPos.getAllInBox(event.getPos().add(-1,-1,-1),event.getPos().add(1,1,1))){if(!event.getWorld().isBlockLoaded(pos))continue;Block block=event.getWorld().getBlockState(pos).getBlock();crimson|=block==Content.block("crimson_nylium")&&Content.available("crimson_nylium");warped|=block==Content.block("warped_nylium")&&Content.available("warped_nylium");}
        if(!crimson&&!warped)return;
        if(!event.getWorld().isRemote)event.getWorld().setBlockState(event.getPos(),Content.block(crimson&&(!warped||event.getWorld().rand.nextBoolean())?"crimson_nylium":"warped_nylium").getDefaultState(),3);
        event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW);
    }
    @SubscribeEvent public void bubbles(BlockEvent.NeighborNotifyEvent event) {
        if(event.getWorld().isRemote)return;
        if(event.getState().getMaterial()!=net.minecraft.block.material.Material.WATER&&event.getState().getBlock()!=Blocks.SOUL_SAND&&event.getState().getBlock()!=Blocks.MAGMA)return;
        BubbleColumn.seed(event.getWorld(),event.getPos());BubbleColumn.seed(event.getWorld(),event.getPos().up());
    }
    @SubscribeEvent public void rawOres(BlockEvent.HarvestDropsEvent event) {
        if (event.isSilkTouching()) return;
        Block block = event.getState().getBlock();
        Block base = block instanceof DeepslateOre ? ((DeepslateOre) block).base : block;
        String raw = base == Blocks.IRON_ORE ? "raw_iron" : base == Blocks.GOLD_ORE ? "raw_gold" : null;
        if (raw == null || !Content.available(raw)) return;
        // Replace only this ore's own drop; leave drops contributed by other mods intact.
        for (int i = 0; i < event.getDrops().size(); i++) {
            ItemStack drop = event.getDrops().get(i);
            if (drop.getItem() != Item.getItemFromBlock(block)) continue;
            int quantity = RawOreDrops.quantity(base == Blocks.IRON_ORE ? "oreIron" : "oreGold", 1, 1, event.getFortuneLevel(), event.getWorld().rand);
            ItemStack replacement = Content.stack(raw); replacement.setCount(drop.getCount() * quantity);
            event.getDrops().set(i, replacement);
        }
    }
    @SubscribeEvent public void fuel(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == Content.item("dried_kelp_block")) event.setBurnTime(4000);
    }
    @SubscribeEvent public void strip(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!(held.getItem() instanceof ItemAxe)) return;
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        String target = null;
        if (state.getBlock() == Blocks.LOG) target = "stripped_" + state.getValue(BlockOldLog.VARIANT).getName() + "_log";
        else if (state.getBlock() == Blocks.LOG2) target = "stripped_" + state.getValue(BlockNewLog.VARIANT).getName() + "_log";
        else if (state.getBlock() instanceof ModernLog && !state.getBlock().getRegistryName().getPath().startsWith("stripped_")) target = "stripped_" + state.getBlock().getRegistryName().getPath();
        if (target == null || !Content.available(target) || !event.getEntityPlayer().canPlayerEdit(event.getPos(), event.getFace(), held)) return;
        event.setCanceled(true); event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!event.getWorld().isRemote) {
            IBlockState stripped = Content.block(target).getDefaultState().withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS));
            event.getWorld().setBlockState(event.getPos(), stripped, 3);
            if (!event.getEntityPlayer().capabilities.isCreativeMode) held.damageItem(1, event.getEntityPlayer());
            event.getWorld().playSound(null, event.getPos(), SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 1, 1);
        }
    }
}
