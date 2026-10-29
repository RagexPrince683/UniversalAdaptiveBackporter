package ganymedes01.etfuturum;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.WeakHashMap;

/** Primary-action movement ability. Native combat still owns every attack and its damage. */
public final class SpearLunge {
    private static final Map<EntityPlayerMP, Stamp> LAST = new WeakHashMap<>();
    private static net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper network;
    private static final java.util.Set<net.minecraft.network.NetHandlerPlayServer> PENDING = java.util.Collections.newSetFromMap(new WeakHashMap<>());
    private SpearLunge() { }
    public static void initialize() {
        network = net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.newSimpleChannel("UAB_Lunge");
        network.registerMessage(Handler.class, Request.class, 0, net.minecraftforge.fml.relauncher.Side.SERVER);
    }
    public static void request() { network.sendToServer(new Request()); }
    public static final class Request implements net.minecraftforge.fml.common.network.simpleimpl.IMessage {
        @Override public void fromBytes(io.netty.buffer.ByteBuf data) { }
        @Override public void toBytes(io.netty.buffer.ByteBuf data) { }
    }
    public static final class Handler implements net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler<Request, net.minecraftforge.fml.common.network.simpleimpl.IMessage> {
        @Override public net.minecraftforge.fml.common.network.simpleimpl.IMessage onMessage(Request message, net.minecraftforge.fml.common.network.simpleimpl.MessageContext context) {
            net.minecraft.network.NetHandlerPlayServer handler = context.getServerHandler();
            EntityPlayerMP player = handler.player;
            net.minecraft.world.WorldServer world = player.getServerWorld();
            synchronized (PENDING) { if (!PENDING.add(handler)) return null; }
            world.addScheduledTask(() -> {
                try {
                    if (handler.player != player || player.connection != handler || !handler.netManager.isChannelOpen()
                            || player.world != world || net.minecraftforge.common.DimensionManager.getWorld(player.dimension) != world) return;
                    ItemStack stack = player.getHeldItemMainhand();
                    if (stack.getItem() instanceof SpearItem) swing(player, stack);
                } finally { synchronized (PENDING) { PENDING.remove(handler); } }
            });
            return null;
        }
    }
    static void swing(EntityPlayerMP player, ItemStack stack) {
        if (player.world.isRemote || !Features.enabled("content.spears") || !player.isEntityAlive()
                || stack != player.getHeldItemMainhand() || !Content.available(stack.getItem().getRegistryName().getPath())
                || player.isSpectator() || player.isRiding() || player.isInWater() || player.isElytraFlying()
                || !player.capabilities.isCreativeMode && player.getFoodStats().getFoodLevel() < 6) return;
        int level = Math.min(3, ModEnchantments.level(ModEnchantments.lunge, stack));
        if (level <= 0) return;
        long now = player.world.getTotalWorldTime();
        Stamp last = LAST.get(player);
        if (last != null && last.world == player.world && now >= last.tick && now - last.tick < 20) return;
        Vec3d eye = player.getPositionEyes(1), look = player.getLookVec();
        double reach = player.getEntityAttribute(EntityPlayerMP.REACH_DISTANCE).getAttributeValue();
        Vec3d end = eye.add(look.scale(reach));
        RayTraceResult block = player.world.rayTraceBlocks(eye, end, false, true, false);
        if (block != null) {
            double distance = eye.distanceTo(block.hitVec);
            boolean attackingEntity = false;
            for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player,
                    player.getEntityBoundingBox().expand(look.x * reach, look.y * reach, look.z * reach).grow(1))) {
                if (!entity.canBeCollidedWith()) continue;
                RayTraceResult hit = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize()).calculateIntercept(eye, end);
                if (entity.getEntityBoundingBox().contains(eye) || hit != null && eye.distanceTo(hit.hitVec) < distance) { attackingEntity = true; break; }
            }
            if (!attackingEntity) return;
        }
        LAST.put(player, new Stamp(player.world, now));
        player.addVelocity(look.x * .458 * level, 0, look.z * .458 * level);
        player.velocityChanged = true;
        player.connection.sendPacket(new SPacketEntityVelocity(player));
        player.addExhaustion(4 * level);
        stack.damageItem(1, player);
        player.world.playSound(null, player.posX, player.posY, player.posZ, ModernSounds.get("item.spear.lunge_" + level), SoundCategory.PLAYERS, 1, 1);
    }
    private static final class Stamp {
        final Object world; final long tick;
        Stamp(Object world, long tick) { this.world = world; this.tick = tick; }
    }
}
