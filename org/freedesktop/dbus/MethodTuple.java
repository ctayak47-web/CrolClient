
package org.freedesktop.dbus;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodTuple {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String name;
    private final String sig;

    public MethodTuple(String _name, String _sig) {
        this.name = _name;
        this.sig = null != _sig ? _sig : "";
        this.logger.trace("new MethodTuple({}, {})", (Object)this.name, (Object)this.sig);
    }

    public int hashCode() {
        return Objects.hash(this.name, this.sig);
    }

    public boolean equals(Object _obj) {
        if (this == _obj) {
            return true;
        }
        if (!(_obj instanceof MethodTuple)) {
            return false;
        }
        MethodTuple other = (MethodTuple)_obj;
        return Objects.equals(this.name, other.name) && Objects.equals(this.sig, other.sig);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public String getName() {
        return this.name;
    }

    public String getSig() {
        return this.sig;
    }
}

