package ganymedes01.etfuturum.potion;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Cooperative 1.7.10 allocation protocol, shared on disk with other participating mods.
 * Construction reserves legacy overrides; pre-init allocates against the live registry.
 * Each native mod carries its own implementation so neither requires the other.
 */
public final class PotionIdRegistry {
    private static final File FILE = new File(Launch.minecraftHome, "config/legacy-potion-ids.properties");
    private static final String SCHEMA = "1";
    private final String owner;
    private final Set<String> requested = new HashSet<String>();
    private final Map<String, Potion> registered = new TreeMap<String, Potion>();
    private volatile Map<String, Integer> active = Collections.emptyMap();

    public PotionIdRegistry(String owner) { this.owner = owner; }

    public void reserve(Map<String, Integer> overrides) { reserve(overrides, overrides.keySet()); }

    public void reserve(Map<String, Integer> overrides, Set<String> enabled) {
        synchronized (lock(FILE)) {
            Properties data = read(FILE);
            for (Map.Entry<String, Integer> entry : overrides.entrySet()) {
                String key = owner + ":" + entry.getKey();
                int override = entry.getValue();
                if (override < -1) throw failure("Invalid override for " + key);
                // Reject unsupported new requests before persisting a reservation that
                // would otherwise survive a failed first startup.
                if (override >= 0 && enabled.contains(entry.getKey())) requireSupported(key, override);
                String stored = data.getProperty(key);
                if (stored == null || stored.equals("-1")) {
                    if (override >= 0 || enabled.contains(entry.getKey())) data.setProperty(key, Integer.toString(override));
                    else data.remove(key);
                } else if (override >= 0 && Integer.parseInt(stored) != override)
                    throw failure(key + " requests " + override + " but its persisted allocation is " + stored
                            + ". Restore the matching configuration/allocation file; changing saved IDs requires migration.");
                if (enabled.contains(entry.getKey())) requested.add(key);
            }
            validate(data);
            write(FILE, data);
        }
    }

    public void allocate() {
        synchronized (lock(FILE)) {
            Properties data = read(FILE);
            validate(data);
            Set<Integer> reserved = new HashSet<Integer>();
            for (String key : data.stringPropertyNames())
                if (!key.equals("schema") && Integer.parseInt(data.getProperty(key)) >= 0)
                    reserved.add(Integer.parseInt(data.getProperty(key)));
            // Native effect packets and NBT read signed bytes; wider allocations require a separate extension.
            int limit = Math.min(127, Potion.potionTypes.length - 1);
            for (String key : new java.util.TreeSet<String>(data.stringPropertyNames())) {
                if (key.equals("schema") || Integer.parseInt(data.getProperty(key)) >= 0) continue;
                int id = 1;
                while (id <= limit && (reserved.contains(id) || Potion.potionTypes[id] != null)) id++;
                if (id > limit) throw failure("No free native potion slot for " + key
                        + ". The runtime registry has " + Potion.potionTypes.length
                        + " slots. An appropriate ID extension or explicit pack allocation is required.");
                data.setProperty(key, Integer.toString(id));
                reserved.add(id);
            }
            TreeMap<String, Integer> result = new TreeMap<String, Integer>();
            for (String key : requested) {
                int id = Integer.parseInt(data.getProperty(key));
                requireSupported(key, id);
                if (Potion.potionTypes[id] != null)
                    throw failure(key + " ID " + id + " is occupied by " + Potion.potionTypes[id].getName()
                            + ". Its persisted ID will not be silently reassigned.");
                result.put(key, id);
            }
            write(FILE, data);
            active = Collections.unmodifiableMap(result);
            LogManager.getLogger(owner).info("Persistent potion allocations: {}", active);
        }
    }

    public int id(String name) {
        Integer id = active.get(owner + ":" + name);
        if (id == null) throw failure("Potion was not allocated: " + name);
        return id;
    }

    public static int maximumSupportedId() {
        // An expanded array alone does not make signed-byte packets/NBT safe.
        // The dummy module ID exists only when EndlessIDs' potion transformations are enabled.
        return Math.min(Potion.potionTypes.length - 1,
                Loader.isModLoaded("endlessids_potion") ? Short.MAX_VALUE : 127);
    }

    private void requireSupported(String key, int id) {
        int maximum = maximumSupportedId();
        if (id < 0 || id > maximum)
            throw failure(key + " ID " + id + " is unsupported by this runtime (0.." + maximum
                    + ", live registry: " + Potion.potionTypes.length + " slots). UAB supports 0..127 natively;"
                    + " higher IDs require the enabled EndlessIDs potion module, including its packet/NBT extensions."
                    + " Restore the matching config/allocation file for existing saves; IDs will not be remapped.");
    }

    public void track(String name, Potion potion) {
        if (potion.id != id(name) || Potion.potionTypes[potion.id] != potion)
            throw failure("Incorrect registration for " + name);
        registered.put(owner + ":" + name, potion);
    }

    public void verify() {
        if (registered.size() != active.size()) throw failure("Incomplete potion registration.");
        for (Map.Entry<String, Potion> entry : registered.entrySet()) {
            Potion potion = entry.getValue();
            if (potion.id >= Potion.potionTypes.length || Potion.potionTypes[potion.id] != potion)
                throw failure(entry.getKey() + " ID " + potion.id + " was overwritten by another mod.");
        }
    }

    /** Pure immutable handshake data: no registry or world mutations on network threads. */
    public String handshake() {
        StringBuilder value = new StringBuilder(SCHEMA);
        for (Map.Entry<String, Integer> entry : active.entrySet())
            value.append(';').append(entry.getKey()).append('=').append(entry.getValue());
        return value.toString();
    }

    public String handshakeKey() { return owner + ":potion_ids"; }

    public boolean matches(Map<String, String> remote) {
        String other = remote.get(handshakeKey());
        boolean matches = handshake().equals(other);
        if (!matches) LogManager.getLogger(owner).error(
                "Potion allocation mismatch: local {}, remote {}. Install matching mods and config/legacy-potion-ids.properties on both sides.",
                handshake(), other);
        return matches;
    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        if (event.world.isRemote || event.world.provider.dimensionId != 0) return;
        verify();
        File base = event.world.getSaveHandler().getMapFileFromName("legacy-potion-ids");
        if (base == null) throw failure("Cannot locate the world's potion allocation snapshot.");
        File snapshot = new File(base.getParentFile(), "legacy-potion-ids.properties");
        synchronized (lock(FILE)) {
            Properties current = read(FILE);
            Properties saved = read(snapshot);
            // Validate every recorded owner, including temporarily absent/disabled effects.
            for (String key : saved.stringPropertyNames()) {
                if (!key.equals("schema") && !saved.getProperty(key).equals(current.getProperty(key)))
                    throw failure("World allocation for " + key + " is " + saved.getProperty(key)
                            + " but the installation has " + current.getProperty(key)
                            + ". Restore the world's matching allocation file before loading it.");
            }
            for (String key : current.stringPropertyNames())
                if (!key.equals("schema") && Integer.parseInt(current.getProperty(key)) >= 0)
                    saved.setProperty(key, current.getProperty(key));
            write(snapshot, saved);
        }
    }

    private static Object lock(File file) {
        try { return file.getCanonicalPath().intern(); }
        catch (IOException exception) { throw new IllegalStateException("Cannot resolve potion allocation file", exception); }
    }

    private static Properties read(File file) {
        Properties data = new Properties();
        if (file.exists()) {
            try (FileInputStream input = new FileInputStream(file)) { data.load(input); }
            catch (IOException exception) { throw new IllegalStateException("Cannot read " + file, exception); }
            if (!SCHEMA.equals(data.getProperty("schema")))
                throw new IllegalStateException("Unsupported potion allocation schema in " + file);
        } else data.setProperty("schema", SCHEMA);
        validate(data);
        return data;
    }

    private static void validate(Properties data) {
        Set<Integer> occupied = new HashSet<Integer>();
        for (String key : data.stringPropertyNames()) {
            if (key.equals("schema")) continue;
            if (!key.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                throw new IllegalStateException("Invalid potion allocation name: " + key);
            int id;
            try { id = Integer.parseInt(data.getProperty(key)); }
            catch (NumberFormatException exception) { throw new IllegalStateException("Invalid potion allocation for " + key, exception); }
            if (id < -1 || id > Short.MAX_VALUE || id >= 0 && !occupied.add(id))
                throw new IllegalStateException("Invalid or duplicate potion allocation: " + key + "=" + id
                        + ". Preserve saved IDs and correct conflicting legacy overrides explicitly.");
        }
    }

    private static void write(File file, Properties data) {
        Path temporary = null;
        try {
            Files.createDirectories(file.toPath().getParent());
            temporary = Files.createTempFile(file.toPath().getParent(), "potion-ids-", ".tmp");
            try (FileOutputStream output = new FileOutputStream(temporary.toFile())) {
                data.store(output, "Persistent native potion identities. Keep with the modpack and saved worlds.");
                output.getFD().sync();
            }
            Files.move(temporary, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot persist potion allocations to " + file, exception);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException exception) { LogManager.getLogger("PotionIds").warn("Cannot remove allocation temporary file {}", temporary); }
            }
        }
    }

    private IllegalStateException failure(String message) { return new IllegalStateException(owner + ": " + message); }
}
