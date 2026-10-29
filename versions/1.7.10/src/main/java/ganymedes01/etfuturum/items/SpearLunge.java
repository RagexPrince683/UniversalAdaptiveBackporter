package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.ModEnchantments;
import ganymedes01.etfuturum.api.elytra.IElytraPlayer;
import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import java.util.Map;
import java.util.WeakHashMap;

/** Primary-action movement ability. Native combat still owns every attack and its damage. */
public final class SpearLunge {
    private static final Map<EntityPlayerMP, Stamp> LAST = new WeakHashMap<>();
    private static cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper network;
    private SpearLunge() { }
    public static void initialize() {
        network = cpw.mods.fml.common.network.NetworkRegistry.INSTANCE.newSimpleChannel("UAB_Lunge");
        network.registerMessage(Handler.class, Request.class, 0, cpw.mods.fml.relauncher.Side.SERVER);
    }
    public static void request() { network.sendToServer(new Request()); }
    public static final class Request implements cpw.mods.fml.common.network.simpleimpl.IMessage {
        @Override public void fromBytes(io.netty.buffer.ByteBuf data) { }
        @Override public void toBytes(io.netty.buffer.ByteBuf data) { }
    }
    public static final class Handler implements cpw.mods.fml.common.network.simpleimpl.IMessageHandler<Request, cpw.mods.fml.common.network.simpleimpl.IMessage> {
        @Override public cpw.mods.fml.common.network.simpleimpl.IMessage onMessage(Request message, cpw.mods.fml.common.network.simpleimpl.MessageContext context) {
            ganymedes01.etfuturum.network.ServerPacketExecutor.execute(context, player -> {
                ItemStack stack = player.getHeldItem();
                if (stack != null && stack.getItem() instanceof ItemSpear) swing(player, stack);
            });
            return null;
        }
    }
    public static void swing(EntityPlayerMP player, ItemStack stack) {
        if (player.worldObj.isRemote || !ContentAvailability.enabled("spears") || !player.isEntityAlive()
                || stack != player.getHeldItem() || !ContentAvailability.available(stack.getItem())
                || SpectatorUtils.isSpectator(player) || player.ridingEntity != null || player.isInWater()
                || player instanceof IElytraPlayer && ((IElytraPlayer) player).etfu$isElytraFlying()
                || !player.capabilities.isCreativeMode && player.getFoodStats().getFoodLevel() < 6) return;
        int level = Math.min(3, ModEnchantments.level(ModEnchantments.lunge, stack));
        if (level <= 0) return;
        long now = player.worldObj.getTotalWorldTime();
        Stamp last = LAST.get(player);
        if (last != null && last.world == player.worldObj && now >= last.tick && now - last.tick < 20) return;
        Vec3 eye = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLookVec();
        double reach = player.theItemInWorldManager.getBlockReachDistance();
        Vec3 end = eye.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        MovingObjectPosition block = player.worldObj.func_147447_a(eye, end, false, true, false);
        if (block != null) {
            double distance = eye.distanceTo(block.hitVec);
            boolean attackingEntity = false;
            for (Object object : player.worldObj.getEntitiesWithinAABBExcludingEntity(player,
                    player.boundingBox.addCoord(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach).expand(1, 1, 1))) {
                Entity entity = (Entity) object;
                if (!entity.canBeCollidedWith()) continue;
                MovingObjectPosition hit = entity.boundingBox.expand(entity.getCollisionBorderSize(), entity.getCollisionBorderSize(), entity.getCollisionBorderSize()).calculateIntercept(eye, end);
                if (entity.boundingBox.isVecInside(eye) || hit != null && eye.distanceTo(hit.hitVec) < distance) { attackingEntity = true; break; }
            }
            if (!attackingEntity) return; // Mining keeps its ordinary swing behavior.
        }
        LAST.put(player, new Stamp(player.worldObj, now));
        player.addVelocity(look.xCoord * .458 * level, 0, look.zCoord * .458 * level);
        player.velocityChanged = true;
        player.playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(player));
        player.addExhaustion(4 * level);
        stack.damageItem(1, player);
        player.worldObj.playSoundAtEntity(player, "minecraft_1.21.11:item.spear.lunge_" + level, 1, 1);
    }
    private static final class Stamp {
        final Object world; final long tick;
        Stamp(Object world, long tick) { this.world = world; this.tick = tick; }
    }
}
