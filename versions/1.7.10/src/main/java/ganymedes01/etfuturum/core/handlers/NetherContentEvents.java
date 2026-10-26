package ganymedes01.etfuturum.core.handlers;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import ganymedes01.etfuturum.entities.EntityPiglin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import java.util.List;

public final class NetherContentEvents {
	public static final NetherContentEvents INSTANCE = new NetherContentEvents();
	private NetherContentEvents() { }
	public static void anger(World world, double x, double y, double z, EntityPlayer player) {
		if (!ConfigEntities.enableNetherMobs || world.isRemote) return;
		List<EntityPiglin> piglins = world.getEntitiesWithinAABB(EntityPiglin.class, AxisAlignedBB.getBoundingBox(x - 16, y - 16, z - 16, x + 17, y + 17, z + 17));
		for (EntityPiglin piglin : piglins) piglin.angerAt(player);
	}
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void breakGold(BlockEvent.BreakEvent event) {
		if (event.block == Blocks.gold_block || event.block == Blocks.gold_ore || event.block == ModBlocks.NETHER_GOLD_ORE.get() || event.block == ModBlocks.GILDED_BLACKSTONE.get()) anger(event.world, event.x, event.y, event.z, event.getPlayer());
	}
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void openContainer(PlayerInteractEvent event) {
		if (!event.world.isRemote && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.useBlock != Event.Result.DENY && !event.entityPlayer.isSneaking()
				&& (event.world.getBlock(event.x, event.y, event.z) instanceof net.minecraft.block.BlockChest
					|| event.world.getBlock(event.x, event.y, event.z) == Blocks.ender_chest
					|| event.world.getTileEntity(event.x, event.y, event.z) instanceof ganymedes01.etfuturum.tileentities.TileEntityBarrel
					|| event.world.getTileEntity(event.x, event.y, event.z) instanceof ganymedes01.etfuturum.tileentities.TileEntityShulkerBox)) anger(event.world, event.x, event.y, event.z, event.entityPlayer);
	}
	@SubscribeEvent
	public void nyliumSpread(BonemealEvent event) {
		if (event.block != Blocks.netherrack || !ModBlocks.NYLIUM.isEnabled()) return;
		boolean crimson = false, warped = false;
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			int x = event.x + dx, z = event.z + dz;
			if (!event.world.blockExists(x, event.y, z) || event.world.getBlock(x, event.y, z) != ModBlocks.NYLIUM.get()) continue;
			if (event.world.getBlockMetadata(x, event.y, z) == 0) crimson = true; else warped = true;
		}
		if (!crimson && !warped || event.world.getBlock(event.x, event.y + 1, event.z).isOpaqueCube()) return;
		if (!event.world.isRemote) event.world.setBlock(event.x, event.y, event.z, ModBlocks.NYLIUM.get(), warped && (!crimson || event.world.rand.nextBoolean()) ? 1 : 0, 2);
		event.setResult(Event.Result.ALLOW);
	}
}
