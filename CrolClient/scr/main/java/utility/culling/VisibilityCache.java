
package crol.client.utility.culling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class VisibilityCache {
    private final Map<Integer, CacheEntry> entries = new HashMap<Integer, CacheEntry>();
    private final int maxEntries;

    public VisibilityCache(int maxEntries) {
        this.maxEntries = Math.max(256, maxEntries);
    }

    public Boolean get(int entityId, long currentTick) {
        CacheEntry entry = this.entries.get(entityId);
        if (entry == null) {
            return null;
        }
        if (currentTick >= entry.expiresAtTick) {
            this.entries.remove(entityId);
            return null;
        }
        return entry.visible;
    }

    public void put(int entityId, boolean visible, long expiresAtTick) {
        this.entries.put(entityId, new CacheEntry(visible, expiresAtTick));
        if (this.entries.size() > this.maxEntries) {
            this.prune(expiresAtTick);
        }
    }

    public void remove(int entityId) {
        this.entries.remove(entityId);
    }

    public void clear() {
        this.entries.clear();
    }

    private void prune(long currentTick) {
        Iterator<Map.Entry<Integer, CacheEntry>> iterator2 = this.entries.entrySet().iterator();
        while (iterator2.hasNext()) {
            if (currentTick < iterator2.next().getValue().expiresAtTick) continue;
            iterator2.remove();
        }
        if (this.entries.size() <= this.maxEntries) {
            return;
        }
        iterator2 = this.entries.entrySet().iterator();
        while (this.entries.size() > this.maxEntries && iterator2.hasNext()) {
            iterator2.next();
            iterator2.remove();
        }
    }

    private static final class CacheEntry {
        private final boolean visible;
        private final long expiresAtTick;

        private CacheEntry(boolean visible, long expiresAtTick) {
            this.visible = visible;
            this.expiresAtTick = expiresAtTick;
        }
    }
}

