package ganymedes01.etfuturum;

import java.util.Map;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import net.minecraftforge.fml.relauncher.Side;

/** Immutable startup metadata read by Forge's networking threads before world entry. */
public final class ConnectionSettings {
    private static final String KEY = "uab:configuration";
    private static volatile String fingerprint;
    private ConnectionSettings() { }

    public static void capture() { fingerprint = UabConfiguration.get().handshake(); }
    public static void advertise(Map<String, String> mods) {
        String active = fingerprint;
        if (active == null) throw new IllegalStateException("UAB connection attempted before startup settings were frozen");
        mods.put(KEY, active);
    }
    public static void advertiseStatus(com.google.gson.JsonObject response) {
        String active = fingerprint;
        if (active == null || !response.has("modinfo")) return;
        com.google.gson.JsonObject entry = new com.google.gson.JsonObject();
        entry.addProperty("modid", KEY); entry.addProperty("version", active);
        response.getAsJsonObject("modinfo").getAsJsonArray("modList").add(entry);
    }
    public static boolean matches(Map<String, String> remote, Side side) {
        // Retain the legacy policy: a UAB client may join a server without UAB.
        if (!remote.containsKey(UAB.ID)) return side == Side.SERVER;
        String active = fingerprint;
        return active != null && BuildVersion.VERSION.equals(remote.get(UAB.ID)) && active.equals(remote.get(KEY));
    }
}
