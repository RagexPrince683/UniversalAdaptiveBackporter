package ganymedes01.etfuturum.api.spectator;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.core.utils.helpers.SafeEnumHelperClient;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;

@ApiStatus.AvailableSince("3.0.0")
public class SpectatorUtils {
    @ApiStatus.AvailableSince("3.0.0")
    public static final IEntitySelector EXCEPT_SPECTATING;
    @ApiStatus.AvailableSince("3.0.0")
    public static final WorldSettings.GameType SPECTATOR_GAMETYPE;

    static {
        EXCEPT_SPECTATING = p_82704_1_ -> !isSpectator(p_82704_1_);
        SPECTATOR_GAMETYPE = SafeEnumHelperClient.addGameType("spectator", 3, "Spectator");
    }

    @ApiStatus.AvailableSince("3.0.0")
    public static boolean isSpectator(Entity entity) {
        if (entity == null || entity.worldObj == null) return false;
        if (entity instanceof EntityPlayerMP player) {
            if(!(player instanceof FakePlayer) && player.theItemInWorldManager != null) {
                return player.theItemInWorldManager.getGameType() == SpectatorUtils.SPECTATOR_GAMETYPE;
            }
        } else if (entity.worldObj.isRemote && FMLCommonHandler.instance().getSide() == Side.CLIENT
                && isLocalSpectator(entity)) {
            return true;
        } else if (entity instanceof ISpectatorInfo info) {
            // Remote clients do not receive another player's GameType. The player mixin supplies the synchronized value.
            return info.etfu$isSpectator();
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private static boolean isLocalSpectator(Entity entity) {
        net.minecraft.client.Minecraft client = FMLClientHandler.instance().getClient();
        return client != null && client.theWorld == entity.worldObj && client.thePlayer == entity
                && client.playerController != null && client.playerController.currentGameType == SPECTATOR_GAMETYPE;
    }

    @ApiStatus.AvailableSince("3.0.0")
    public static boolean wasSpectator(Entity entity) {
        return entity instanceof ISpectatorInfo info && info.etfu$wasSpectator();
    }

    @ApiStatus.AvailableSince("3.0.0")
    @Nullable
    public static Entity getSpectatingEntity(Entity entity) {
        return entity instanceof ISpectatorInfo info ? info.etfu$spectatingEntity() : null;
    }

    @ApiStatus.AvailableSince("3.0.0")
    public static boolean canSpectatorSelectTileEntity(TileEntity te) {
        return (te instanceof IInventory || te instanceof TileEntityEnderChest)
                && !BlockTags.hasTag(te.getBlockType(), te.getBlockMetadata(), Tags.MOD_ID + ":spectators_cannot_interact");
    }
}
