
package jnr.constants;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jnr.constants.Constant;
import jnr.constants.PlatformConstants;
import jnr.constants.platform.Errno;

public class ConstantSet
extends AbstractSet<Constant> {
    private final Map<String, Constant> nameToConstant;
    private final Map<Long, Constant> valueToConstant;
    private final Set<Enum> constants;
    private final Class<Enum> enumClass;
    private volatile Long minValue;
    private volatile Long maxValue;
    private static final ConcurrentMap<String, ConstantSet> constantSets;
    private static final Object lock;
    private static final ClassLoader LOADER;
    private static final boolean CAN_LOAD_RESOURCES;
    private static volatile Throwable RESOURCE_READ_ERROR;

    public static ConstantSet getConstantSet(String name) {
        ConstantSet constants = (ConstantSet)constantSets.get(name);
        return constants != null ? constants : ConstantSet.loadConstantSet(name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ConstantSet loadConstantSet(String name) {
        Object object = lock;
        synchronized (object) {
            ConstantSet constants = (ConstantSet)constantSets.get(name);
            if (constants == null) {
                Class<Enum> enumClass = ConstantSet.getEnumClass(name);
                if (enumClass == null) {
                    return null;
                }
                if (!Constant.class.isAssignableFrom(enumClass)) {
                    throw new ClassCastException("class for " + name + " does not implement Constant interface");
                }
                constants = new ConstantSet(enumClass);
                constantSets.put(name, constants);
            }
            return constants;
        }
    }

    private static final Class<Enum> getEnumClass(String name) {
        String[] prefixes;
        for (String prefix : prefixes = PlatformConstants.getPlatform().getPackagePrefixes()) {
            String path;
            URL resource;
            String fullName = prefix + "." + name;
            boolean doClass = true;
            if (CAN_LOAD_RESOURCES && (resource = LOADER.getResource(path = fullName.replace('.', '/') + ".class")) == null) {
                doClass = false;
            }
            if (!doClass) continue;
            try {
                return Class.forName(fullName, true, LOADER).asSubclass(Enum.class);
            }
            catch (ClassNotFoundException classNotFoundException) {
                
            }
        }
        return null;
    }

    private ConstantSet(Class<Enum> enumClass) {
        this.enumClass = enumClass;
        this.constants = EnumSet.allOf(enumClass);
        HashMap<String, Constant> names = new HashMap<String, Constant>();
        HashMap<Long, Constant> values2 = new HashMap<Long, Constant>();
        for (Enum e : this.constants) {
            if (!(e instanceof Constant)) continue;
            Constant c = (Constant)((Object)e);
            names.put(e.name(), c);
            values2.put(c.longValue(), c);
        }
        this.nameToConstant = Collections.unmodifiableMap(names);
        this.valueToConstant = Collections.unmodifiableMap(values2);
    }

    public final Constant getConstant(String name) {
        return this.nameToConstant.get(name);
    }

    public Constant getConstant(long value) {
        return this.valueToConstant.get(value);
    }

    public long getValue(String name) {
        Constant c = this.getConstant(name);
        return c != null ? c.longValue() : 0L;
    }

    public String getName(int value) {
        Constant c = this.getConstant(value);
        return c != null ? c.name() : "unknown";
    }

    private Long getLongField(String name, long defaultValue) {
        try {
            Field f = this.enumClass.getField(name);
            return (Long)f.get(this.enumClass);
        }
        catch (NoSuchFieldException ex) {
            return defaultValue;
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public long minValue() {
        if (this.minValue == null) {
            this.minValue = this.getLongField("MIN_VALUE", Integer.MIN_VALUE);
        }
        return this.minValue.intValue();
    }

    public long maxValue() {
        if (this.maxValue == null) {
            this.maxValue = this.getLongField("MAX_VALUE", Integer.MAX_VALUE);
        }
        return this.maxValue.intValue();
    }

    @Override
    public Iterator<Constant> iterator() {
        return new ConstantIterator(this.constants);
    }

    @Override
    public int size() {
        return this.constants.size();
    }

    @Override
    public boolean contains(Object o) {
        return o != null && o.getClass().equals(this.enumClass);
    }

    public static void main(String[] args) {
        System.out.println(Errno.values().length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static {
        boolean canLoadResources;
        block14: {
            constantSets = new ConcurrentHashMap<String, ConstantSet>();
            lock = new Object();
            ClassLoader _loader = ConstantSet.class.getClassLoader();
            LOADER = _loader != null ? _loader : ClassLoader.getSystemClassLoader();
            canLoadResources = false;
            try {
                URL thisClass = AccessController.doPrivileged(new PrivilegedAction<URL>(){

                    @Override
                    public URL run() {
                        return LOADER.getResource("jnr/constants/ConstantSet.class");
                    }
                });
                InputStream stream = thisClass.openStream();
                try {
                    stream.read();
                }
                catch (Throwable t) {
                    RESOURCE_READ_ERROR = t;
                }
                finally {
                    try {
                        stream.close();
                    }
                    catch (Exception exception) {}
                }
                canLoadResources = true;
            }
            catch (Throwable t) {
                if (RESOURCE_READ_ERROR != null) break block14;
                RESOURCE_READ_ERROR = t;
            }
        }
        CAN_LOAD_RESOURCES = canLoadResources;
    }

    private final class ConstantIterator
    implements Iterator<Constant> {
        private final Iterator<Enum> it;
        private Constant next = null;

        ConstantIterator(Collection<Enum> constants) {
            this.it = constants.iterator();
            this.next = this.it.hasNext() ? (Constant)((Object)this.it.next()) : null;
        }

        @Override
        public boolean hasNext() {
            return this.next != null && !this.next.name().equals("__UNKNOWN_CONSTANT__");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Constant next() {
            Constant prev = this.next;
            this.next = this.it.hasNext() ? (Constant)((Object)this.it.next()) : null;
            return prev;
        }
    }
}

