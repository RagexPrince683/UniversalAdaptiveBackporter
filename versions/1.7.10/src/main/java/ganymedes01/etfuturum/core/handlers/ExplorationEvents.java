package ganymedes01.etfuturum.core.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockDecoratedPot;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.ChunkEvent;
import java.util.ArrayList;

/** Existing dispenser-backed pots are upgraded on the world thread without touching other dispensers. */
public final class ExplorationEvents {
	@SubscribeEvent public void load(ChunkEvent.Load event) {
		if (event.world.isRemote) return;
		for (Object value : new ArrayList<>(event.getChunk().chunkTileEntityMap.values())) {
			TileEntity tile = (TileEntity) value;
			if (event.world.getBlock(tile.xCoord, tile.yCoord, tile.zCoord) == ModBlocks.DECORATED_POT.get()) BlockDecoratedPot.pot(event.world, tile.xCoord, tile.yCoord, tile.zCoord);
		}
	}
}
