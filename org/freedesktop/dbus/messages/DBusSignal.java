
package org.freedesktop.dbus.messages;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.connections.base.AbstractConnectionBase;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.MessageFormatException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.utils.CommonRegexPattern;
import org.freedesktop.dbus.utils.DBusNamingUtil;
import org.freedesktop.dbus.utils.DBusObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBusSignal
extends Message {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBusSignal.class);
    private static final Map<String, Class<? extends DBusSignal>> CLASS_CACHE = new ConcurrentHashMap<String, Class<? extends DBusSignal>>();
    private static final Map<Class<? extends DBusSignal>, Type[]> TYPE_CACHE = new ConcurrentHashMap<Class<? extends DBusSignal>, Type[]>();
    private static final Map<String, String> SIGNAL_NAMES = new ConcurrentHashMap<String, String>();
    private static final Map<String, String> INT_NAMES = new ConcurrentHashMap<String, String>();
    private static final Map<Class<? extends DBusSignal>, List<CachedConstructor>> CACHED_CONSTRUCTORS = new ConcurrentHashMap<Class<? extends DBusSignal>, List<CachedConstructor>>();
    private Class<? extends DBusSignal> clazz;
    private boolean bodydone = false;
    private byte[] blen;

    DBusSignal() {
    }

    protected DBusSignal(byte _endianess, String _source, String _path, String _iface, String _member, String _sig, Object ... _args) throws DBusException {
        super(_endianess, (byte)4, (byte)0);
        if (null == _path || null == _member || null == _iface) {
            throw new MessageFormatException("Must specify object path, interface and signal name to Signals.");
        }
        ArrayList<Object> hargs = new ArrayList<Object>();
        hargs.add(this.createHeaderArgs((byte)1, "o", _path));
        hargs.add(this.createHeaderArgs((byte)2, "s", _iface));
        hargs.add(this.createHeaderArgs((byte)3, "s", _member));
        if (null != _source) {
            hargs.add(this.createHeaderArgs((byte)7, "s", _source));
        }
        if (null != _sig) {
            hargs.add(this.createHeaderArgs((byte)8, "g", _sig));
            this.setArgs(_args);
        }
        this.padAndMarshall(hargs, this.getSerial(), _sig, _args);
        this.bodydone = true;
    }

    protected DBusSignal(String _objectPath, Object ... _args) throws DBusException {
        this(0, _objectPath, _args);
    }

    protected DBusSignal(byte _endianess, String _objectPath, Object ... _args) throws DBusException {
        super(_endianess, (byte)4, (byte)0);
        DBusObjects.requireObjectPath(_objectPath);
        Class<?> tc = this.getClass();
        String member = DBusNamingUtil.getSignalName(tc);
        Class<?> enc = tc.getEnclosingClass();
        if (null == enc || !DBusInterface.class.isAssignableFrom(enc) || enc.getName().equals(enc.getSimpleName())) {
            throw new DBusException("Signals must be declared as a member of a class implementing DBusInterface which is the member of a package.");
        }
        String iface = DBusNamingUtil.getInterfaceName(enc);
        ArrayList<Object[]> hargs = new ArrayList<Object[]>();
        hargs.add(this.createHeaderArgs((byte)1, "o", _objectPath));
        hargs.add(this.createHeaderArgs((byte)2, "s", iface));
        hargs.add(this.createHeaderArgs((byte)3, "s", member));
        String sig = null;
        if (0 < _args.length) {
            try {
                Type[] types = TYPE_CACHE.get(tc);
                if (null == types) {
                    Constructor<?> con = tc.getDeclaredConstructors()[0];
                    Type[] ts = con.getGenericParameterTypes();
                    types = new Type[ts.length - 1];
                    for (int i = 1; i <= types.length; ++i) {
                        types[i - 1] = ts[i] instanceof TypeVariable ? ((TypeVariable)ts[i]).getBounds()[0] : ts[i];
                    }
                    TYPE_CACHE.put(tc, types);
                }
                sig = Marshalling.getDBusType(types);
                hargs.add(this.createHeaderArgs((byte)8, "g", sig));
                this.setArgs(_args);
            }
            catch (Exception _ex) {
                this.logger.debug("Error adding signal parameters", _ex);
                throw new DBusException("Failed to add signal parameters: " + _ex.getMessage());
            }
        }
        this.blen = new byte[4];
        this.appendBytes(this.blen);
        this.append("ua(yv)", this.getSerial(), hargs.toArray());
        this.pad((byte)8);
    }

    static void addInterfaceMap(String _java, String _dbus) {
        INT_NAMES.put(_dbus, _java);
    }

    static void addSignalMap(String _java, String _dbus) {
        SIGNAL_NAMES.put(_dbus, _java);
    }

    private static Class<? extends DBusSignal> createSignalClass(String _intName, String _sigName) throws DBusException {
        Object name = _intName + "$" + _sigName;
        Class<DBusSignal> c = CLASS_CACHE.get(name);
        if (null == c) {
            c = DBusMatchRule.getCachedSignalType((String)name);
        }
        if (null != c) {
            return c;
        }
        do {
            try {
                c = Class.forName((String)name);
            }
            catch (ClassNotFoundException _exCnf) {
                LOGGER.trace("Class not found for {}", name, (Object)_exCnf);
            }
            name = CommonRegexPattern.EXCEPTION_EXTRACT_PATTERN.matcher((CharSequence)name).replaceAll("\\$$1");
        } while (null == c && CommonRegexPattern.EXCEPTION_PARTIAL_PATTERN.matcher((CharSequence)name).matches());
        if (null == c) {
            throw new DBusException("Could not create class from signal " + _intName + "." + _sigName);
        }
        CLASS_CACHE.put((String)name, c);
        return c;
    }

    public DBusSignal createReal(AbstractConnectionBase _conn) throws DBusException {
        String intname = INT_NAMES.get(this.getInterface());
        String signame = SIGNAL_NAMES.get(this.getName());
        if (null == intname) {
            intname = this.getInterface();
        }
        if (null == signame) {
            signame = this.getName();
        }
        if (null == this.clazz) {
            this.clazz = DBusSignal.createSignalClass(intname, signame);
        }
        this.logger.debug("Converting signal to type: {}", (Object)this.clazz);
        if (!CACHED_CONSTRUCTORS.containsKey(this.clazz)) {
            this.cacheConstructors(this.clazz);
        }
        List<CachedConstructor> list = CACHED_CONSTRUCTORS.get(this.clazz);
        Constructor<? extends DBusSignal> con = null;
        Type[] types = null;
        Object[] parameters = this.getParameters();
        List<Class<?>> wantedArgs = Arrays.stream(parameters).map(Object::getClass).collect(Collectors.toList());
        for (CachedConstructor type : list) {
            if (!type.matchesParameters(wantedArgs)) continue;
            con = type.constructor;
            types = type.types;
            break;
        }
        if (con == null) {
            this.logger.warn("Could not find suitable constructor for class {} with argument-types: {}", (Object)this.clazz.getName(), (Object)wantedArgs);
            return null;
        }
        try {
            DBusSignal s;
            Object[] args = Marshalling.deSerializeParameters(parameters, types, _conn);
            if (null == args) {
                s = con.newInstance(this.getPath());
            } else {
                Object[] params = new Object[args.length + 1];
                params[0] = this.getPath();
                System.arraycopy(args, 0, params, 1, args.length);
                s = con.newInstance(params);
            }
            s.updateEndianess(_conn.getMessageFactory().getEndianess());
            s.setHeader(this.getHeader());
            s.setWireData(this.getWireData());
            s.setByteCounter(this.getWireData().length);
            return s;
        }
        catch (Exception _ex) {
            throw new DBusException(_ex);
        }
    }

    private void cacheConstructors(Class<? extends DBusSignal> _clazz) {
        ArrayList<CachedConstructor> list = new ArrayList<CachedConstructor>();
        Constructor<?>[] constructorArray = _clazz.getDeclaredConstructors();
        int n = constructorArray.length;
        for (int i = 0; i < n; ++i) {
            Constructor<?> constructor;
            Constructor<?> x = constructor = constructorArray[i];
            list.add(new CachedConstructor(x));
        }
        CACHED_CONSTRUCTORS.put(_clazz, list);
    }

    public void appendbody(AbstractConnectionBase _conn) throws DBusException {
        if (this.bodydone) {
            return;
        }
        Type[] types = TYPE_CACHE.get(this.getClass());
        Object[] args = Marshalling.convertParameters(this.getParameters(), types, _conn);
        this.setArgs(args);
        String sig = this.getSig();
        long counter = this.getByteCounter();
        if (null != args && 0 < args.length) {
            this.append(sig, args);
        }
        this.marshallint(this.getByteCounter() - counter, this.blen, 0, 4);
        this.bodydone = true;
    }

    private static class CachedConstructor {
        private final Constructor<? extends DBusSignal> constructor;
        private final List<Class<?>> parameterTypes;
        private final Type[] types;

        CachedConstructor(Constructor<? extends DBusSignal> _constructor) {
            this.constructor = _constructor;
            this.parameterTypes = Arrays.stream(this.constructor.getParameterTypes()).skip(1L).map(c -> {
                if (c.isPrimitive()) {
                    return CachedConstructor.wrap(c);
                }
                return c;
            }).collect(Collectors.toList());
            this.types = CachedConstructor.createTypes(this.constructor);
        }

        public boolean matchesParameters(List<Class<?>> _wantedArgs) {
            if (this.parameterTypes == null || _wantedArgs == null) {
                return false;
            }
            if (this.parameterTypes.size() != _wantedArgs.size()) {
                return false;
            }
            for (int i = 0; i < this.parameterTypes.size(); ++i) {
                Class<?> class1 = this.parameterTypes.get(i);
                if (Enum.class.isAssignableFrom(class1) && String.class.equals(_wantedArgs.get(i)) || DBusInterface.class.isAssignableFrom(class1) && ObjectPath.class.equals(_wantedArgs.get(i)) || Struct.class.isAssignableFrom(class1) && Object[].class.equals(_wantedArgs.get(i)) || class1.isAssignableFrom(_wantedArgs.get(i))) continue;
                return false;
            }
            return true;
        }

        private static Type[] createTypes(Constructor<? extends DBusSignal> _constructor) {
            Type[] ts = _constructor.getGenericParameterTypes();
            Type[] types = new Type[ts.length - 1];
            for (int i = 1; i <= types.length; ++i) {
                types[i - 1] = ts[i] instanceof TypeVariable ? ((TypeVariable)ts[i]).getBounds()[0] : ts[i];
            }
            return types;
        }

        private static <T> Class<T> wrap(Class<T> _clz) {
            return MethodType.methodType(_clz).wrap().returnType();
        }
    }
}

