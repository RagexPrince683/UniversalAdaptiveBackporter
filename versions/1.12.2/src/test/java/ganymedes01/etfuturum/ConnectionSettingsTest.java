package ganymedes01.etfuturum;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import io.netty.buffer.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.fml.relauncher.Side;

public final class ConnectionSettingsTest {
    private static int assertions;
    private static void check(boolean value, String message) { assertions++; if (!value) throw new AssertionError(message); }
    private static void capture(boolean copper, boolean textures) throws Exception {
        File directory = Files.createTempDirectory("uab-connection-").toFile();
        UabConfiguration.open(directory, true, "1.12.2");
        UabConfiguration config = UabConfiguration.get();
        net.minecraftforge.common.config.Configuration nativeConfig = config.nativeConfig();
        nativeConfig.get("update_1_17.copper", "enabled", copper).set(copper);
        nativeConfig.get("update_1_14.new_textures", "enabled", textures).set(textures);
        config.bool("blocksitems", "enableCopper", true); config.bool("functions", "enableNewTextures", true);
        config.finish(); ConnectionSettings.capture();
    }
    private static Map<String, String> encoded() {
        ModMetadata metadata = new ModMetadata(); metadata.modId = UAB.ID; metadata.version = BuildVersion.VERSION;
        FMLHandshakeMessage.ModList outgoing = new FMLHandshakeMessage.ModList(Collections.singletonList(new DummyModContainer(metadata)));
        ConnectionSettings.advertise(outgoing.modList());
        ByteBuf buffer = Unpooled.buffer();
        try {
            outgoing.toBytes(buffer); FMLHandshakeMessage.ModList incoming = new FMLHandshakeMessage.ModList(); incoming.fromBytes(buffer);
            return new HashMap<>(incoming.modList());
        } finally { buffer.release(); }
    }
    public static void main(String[] args) throws Exception {
        java.lang.reflect.Field home = net.minecraftforge.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome"); home.setAccessible(true); home.set(null, new File(System.getProperty("java.io.tmpdir")));
        capture(true, true); Map<String, String> enabled = encoded();
        com.google.gson.JsonObject status = new com.google.gson.JsonObject(), info = new com.google.gson.JsonObject();
        info.add("modList", new com.google.gson.JsonArray()); status.add("modinfo", info);
        ConnectionSettings.advertiseStatus(status);
        com.google.gson.JsonObject advertised = info.getAsJsonArray("modList").get(0).getAsJsonObject();
        check(advertised.get("modid").getAsString().equals("uab:configuration") && advertised.get("version").getAsString().equals(enabled.get("uab:configuration")), "server browser advertises the same frozen settings as login");
        check(ConnectionSettings.matches(enabled, Side.CLIENT), "matching client settings accepted through native codec");
        check(ConnectionSettings.matches(enabled, Side.SERVER), "matching server settings accepted through native codec");
        capture(true, false); check(ConnectionSettings.matches(enabled, Side.CLIENT), "client-only preferences may differ");
        capture(false, false); check(!ConnectionSettings.matches(enabled, Side.CLIENT), "common gameplay mismatch rejected");
        Map<String, String> disabled = encoded(); check(ConnectionSettings.matches(disabled, Side.CLIENT), "matching disabled feature accepted");
        disabled.remove("uab:configuration"); check(!ConnectionSettings.matches(disabled, Side.CLIENT), "missing settings metadata rejected");
        disabled = encoded(); disabled.put(UAB.ID, "different-version"); check(!ConnectionSettings.matches(disabled, Side.CLIENT), "different mod version rejected");
        check(!ConnectionSettings.matches(Collections.emptyMap(), Side.CLIENT), "server requires UAB client");
        check(ConnectionSettings.matches(Collections.emptyMap(), Side.SERVER), "legacy client-only connection policy preserved");
        System.out.println("UAB native connection checks: " + assertions + " assertions");
    }
}
