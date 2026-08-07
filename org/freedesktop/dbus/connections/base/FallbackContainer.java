
package org.freedesktop.dbus.connections.base;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.freedesktop.dbus.messages.ExportedObject;
import org.freedesktop.dbus.utils.LoggingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FallbackContainer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String[], ExportedObject> fallbacks = new ConcurrentHashMap<String[], ExportedObject>();

    FallbackContainer() {
    }

    public synchronized void add(String _path, ExportedObject _eo) {
        this.logger.debug("Adding fallback on {} of {}", (Object)_path, (Object)_eo);
        this.fallbacks.put(_path.split("/"), _eo);
    }

    public synchronized void remove(String _path) {
        this.logger.debug("Removing fallback on {}", (Object)_path);
        this.fallbacks.remove(_path.split("/"));
    }

    public synchronized ExportedObject get(String _path) {
        int best = 0;
        ExportedObject bestobject = null;
        String[] pathel = _path.split("/");
        for (Map.Entry<String[], ExportedObject> entry : this.fallbacks.entrySet()) {
            int i;
            String[] fbpath = entry.getKey();
            LoggingHelper.logIf(this.logger.isTraceEnabled(), () -> this.logger.trace("Trying fallback path {} to match {}", (Object)Arrays.deepToString(fbpath), (Object)Arrays.deepToString(pathel)));
            for (i = 0; i < pathel.length && i < fbpath.length && pathel[i].equals(fbpath[i]); ++i) {
            }
            if (i > 0 && i == fbpath.length && i > best) {
                bestobject = entry.getValue();
            }
            this.logger.trace("Matches {} bestobject now {}", (Object)i, (Object)bestobject);
        }
        this.logger.debug("Found fallback for {} of {}", (Object)_path, (Object)bestobject);
        return bestobject;
    }
}

