package ganymedes01.etfuturum.compatibility;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.BooleanSupplier;

/** Explicit equivalence declarations. No optional mod classes or localized names cross this boundary. */
public final class CompatibilityRegistry {
    public enum Use { CRAFTING, SMELTING, FOOD, PLANT, BIOME, WORLDGEN, STRUCTURE, LOOT, ADVANCEMENT }

    public static final class Entry {
        public final String provider, category, identity;
        public final int metadata;
        public final Map<String, String> properties;
        public final Set<Use> uses;
        public Entry(String provider, String category, String identity, int metadata, Map<String, String> properties, Use... uses) {
            if (provider.isEmpty() || category.isEmpty() || identity.isEmpty() || metadata < -1 || uses.length == 0)
                throw new IllegalArgumentException("Invalid compatibility declaration");
            this.provider = provider; this.category = category; this.identity = identity; this.metadata = metadata;
            this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
            this.uses = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(uses)));
        }
        public boolean matches(String id, int meta, Map<String, String> state) {
            return identity.equals(id) && (metadata == -1 || metadata == meta) && state.entrySet().containsAll(properties.entrySet());
        }
    }

    private final Map<String, List<Entry>> categories = new LinkedHashMap<>();
    private final Predicate<String> enabled;
    private final Map<String, BooleanSupplier> providerSettings = new HashMap<>();
    public CompatibilityRegistry(Predicate<String> enabled) { this.enabled = Objects.requireNonNull(enabled); }
    /** Optional addons can supply their own configuration gate before registering declarations. */
    public void registerProvider(String id, BooleanSupplier setting) { providerSettings.put(id, Objects.requireNonNull(setting)); }
    private boolean active(String provider) {
        return providerSettings.containsKey(provider) ? providerSettings.get(provider).getAsBoolean() : enabled.test(provider);
    }
    /** Register during initialization, before recipes/world generation. Exact duplicate declarations are ignored. */
    public void register(Entry entry) {
        List<Entry> entries = categories.computeIfAbsent(entry.category, key -> new ArrayList<>());
        for (Entry old : entries) if (old.provider.equals(entry.provider) && old.identity.equals(entry.identity)
                && old.metadata == entry.metadata && old.properties.equals(entry.properties) && old.uses.equals(entry.uses)) return;
        entries.add(entry);
    }
    public List<Entry> entries(String category, Use use) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : categories.getOrDefault(category, Collections.emptyList()))
            if (entry.uses.contains(use) && active(entry.provider)) result.add(entry);
        return Collections.unmodifiableList(result);
    }
    public boolean matches(String category, Use use, String identity, int metadata, Map<String, String> state) {
        for (Entry entry : categories.getOrDefault(category, Collections.emptyList()))
            if (entry.uses.contains(use) && active(entry.provider) && entry.matches(identity, metadata, state)) return true;
        return false;
    }
}
