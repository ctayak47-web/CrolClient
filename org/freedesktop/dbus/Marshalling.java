
package org.freedesktop.dbus;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.freedesktop.dbus.ArrayFrob;
import org.freedesktop.dbus.Container;
import org.freedesktop.dbus.DBusMap;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.base.AbstractConnectionBase;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.DBusSerializable;
import org.freedesktop.dbus.types.DBusListType;
import org.freedesktop.dbus.types.DBusMapType;
import org.freedesktop.dbus.types.DBusStructType;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
import org.freedesktop.dbus.utils.LoggingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Marshalling {
    private static final String MTH_NAME_DESERIALIZE = "deserialize";
    private static final String ERROR_MULTI_VALUED_ARRAY = "Multi-valued array types not permitted";
    private static final Logger LOGGER = LoggerFactory.getLogger(Marshalling.class);
    private static final Map<Type, String[]> TYPE_CACHE = new ConcurrentHashMap<Type, String[]>();
    private static final Map<Class<?>, Byte> CLASS_TO_ARGUMENTTYPE = new LinkedHashMap();

    private Marshalling() {
    }

    public static String getDBusType(Type[] _javaType) throws DBusException {
        StringBuilder sb = new StringBuilder();
        for (Type t : _javaType) {
            for (String s : Marshalling.getDBusType(t)) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static String[] getDBusType(Type _javaType) throws DBusException {
        String[] cached = TYPE_CACHE.get(_javaType);
        if (null != cached) {
            return cached;
        }
        cached = Marshalling.getDBusType(_javaType, false);
        TYPE_CACHE.put(_javaType, cached);
        return cached;
    }

    public static String[] getDBusType(Type _dataType, boolean _basic) throws DBusException {
        return Marshalling.recursiveGetDBusType(new StringBuffer[10], _dataType, _basic, 0);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String[] recursiveGetDBusType(StringBuffer[] _out, Type _dataType, boolean _basic, int _level) throws DBusException {
        block37: {
            Type[] ts;
            block49: {
                int n;
                Field[] j2;
                block50: {
                    int _ex2;
                    Type[] t2;
                    block45: {
                        Type[] newtypes;
                        block41: {
                            Class dataTypeClazz;
                            block48: {
                                block47: {
                                    block42: {
                                        ParameterizedType p;
                                        block46: {
                                            block44: {
                                                block43: {
                                                    block40: {
                                                        ParameterizedType pt;
                                                        block39: {
                                                            block38: {
                                                                if (_out.length <= _level) {
                                                                    StringBuffer[] newout = new StringBuffer[_out.length];
                                                                    System.arraycopy(_out, 0, newout, 0, _out.length);
                                                                    _out = newout;
                                                                }
                                                                if (null == _out[_level]) {
                                                                    _out[_level] = new StringBuffer();
                                                                } else {
                                                                    _out[_level].delete(0, _out[_level].length());
                                                                }
                                                                if (_basic && !(_dataType instanceof Class)) {
                                                                    throw new DBusException(String.valueOf(_dataType) + " is not a basic type");
                                                                }
                                                                if (!(_dataType instanceof TypeVariable)) break block38;
                                                                _out[_level].append('v');
                                                                break block37;
                                                            }
                                                            if (!(_dataType instanceof GenericArrayType)) break block39;
                                                            GenericArrayType gat = (GenericArrayType)_dataType;
                                                            _out[_level].append('a');
                                                            String[] s = Marshalling.recursiveGetDBusType(_out, gat.getGenericComponentType(), false, _level + 1);
                                                            if (s.length != 1) {
                                                                throw new DBusException(ERROR_MULTI_VALUED_ARRAY);
                                                            }
                                                            _out[_level].append(s[0]);
                                                            break block37;
                                                        }
                                                        if ((!(_dataType instanceof Class) || !DBusSerializable.class.isAssignableFrom((Class)_dataType)) && (!(_dataType instanceof ParameterizedType) || !DBusSerializable.class.isAssignableFrom((Class)(pt = (ParameterizedType)_dataType).getRawType()))) break block40;
                                                        newtypes = null;
                                                        if (_dataType instanceof Class) {
                                                            Class clz = (Class)_dataType;
                                                            for (Method m : clz.getDeclaredMethods()) {
                                                                if (!m.getName().equals(MTH_NAME_DESERIALIZE)) continue;
                                                                newtypes = m.getGenericParameterTypes();
                                                            }
                                                        } else {
                                                            for (Method m : ((Class)((ParameterizedType)_dataType).getRawType()).getDeclaredMethods()) {
                                                                if (!m.getName().equals(MTH_NAME_DESERIALIZE)) continue;
                                                                newtypes = m.getGenericParameterTypes();
                                                            }
                                                        }
                                                        if (null == newtypes) {
                                                            throw new DBusException("Serializable classes must implement a deserialize method");
                                                        }
                                                        break block41;
                                                    }
                                                    if (!(_dataType instanceof ParameterizedType)) break block42;
                                                    p = (ParameterizedType)_dataType;
                                                    if (!p.getRawType().equals(Map.class)) break block43;
                                                    _out[_level].append("a{");
                                                    Type[] t2 = p.getActualTypeArguments();
                                                    try {
                                                        String[] s = Marshalling.recursiveGetDBusType(_out, t2[0], true, _level + 1);
                                                        if (s.length != 1) {
                                                            throw new DBusException(ERROR_MULTI_VALUED_ARRAY);
                                                        }
                                                        _out[_level].append(s[0]);
                                                        s = Marshalling.recursiveGetDBusType(_out, t2[1], false, _level + 1);
                                                        if (s.length != 1) {
                                                            throw new DBusException(ERROR_MULTI_VALUED_ARRAY);
                                                        }
                                                        _out[_level].append(s[0]);
                                                    }
                                                    catch (ArrayIndexOutOfBoundsException _ex2) {
                                                        LOGGER.debug("", _ex2);
                                                        throw new DBusException("Map must have 2 parameters");
                                                    }
                                                    _out[_level].append('}');
                                                    break block37;
                                                }
                                                if (!List.class.isAssignableFrom((Class)p.getRawType())) break block44;
                                                t2 = p.getActualTypeArguments();
                                                _ex2 = t2.length;
                                                break block45;
                                            }
                                            if (!p.getRawType().equals(Variant.class)) break block46;
                                            _out[_level].append('v');
                                            break block37;
                                        }
                                        if (DBusInterface.class.isAssignableFrom((Class)p.getRawType())) {
                                            _out[_level].append('o');
                                            break block37;
                                        } else if (Struct.class.isAssignableFrom((Class)p.getRawType())) {
                                            _out[_level].append('(');
                                            break block37;
                                        } else {
                                            if (!Tuple.class.isAssignableFrom((Class)p.getRawType())) {
                                                throw new DBusException("Exporting non-exportable parameterized type " + String.valueOf(_dataType));
                                            }
                                            Type[] ts2 = p.getActualTypeArguments();
                                            ArrayList vs = new ArrayList();
                                            Type[] j2 = ts2;
                                            int n2 = j2.length;
                                            int s = 0;
                                            while (true) {
                                                if (s >= n2) {
                                                    return vs.toArray(new String[0]);
                                                }
                                                Type t3 = j2[s];
                                                Collections.addAll(vs, Marshalling.recursiveGetDBusType(_out, t3, false, _level + 1));
                                                ++s;
                                            }
                                        }
                                    }
                                    if (!(_dataType instanceof Class)) break block37;
                                    dataTypeClazz = (Class)_dataType;
                                    if (!dataTypeClazz.isArray()) break block47;
                                    if (Type.class.equals(((Class)_dataType).getComponentType())) {
                                        _out[_level].append('g');
                                        break block37;
                                    } else {
                                        _out[_level].append('a');
                                        String[] s = Marshalling.recursiveGetDBusType(_out, ((Class)_dataType).getComponentType(), false, _level + 1);
                                        if (s.length != 1) {
                                            throw new DBusException(ERROR_MULTI_VALUED_ARRAY);
                                        }
                                        _out[_level].append(s[0]);
                                    }
                                    break block37;
                                }
                                if (!Struct.class.isAssignableFrom((Class)_dataType)) break block48;
                                _out[_level].append('(');
                                ts = Container.getTypeCache(_dataType);
                                if (null != ts) break block49;
                                Field[] fs = ((Class)_dataType).getDeclaredFields();
                                ts = new Type[fs.length];
                                j2 = fs;
                                n = j2.length;
                                break block50;
                            }
                            if (Enum.class.isAssignableFrom(dataTypeClazz)) {
                                _out[_level].append('s');
                                break block37;
                            } else {
                                boolean found = false;
                                for (Map.Entry<Class<?>, Byte> entry : CLASS_TO_ARGUMENTTYPE.entrySet()) {
                                    if (!entry.getKey().isAssignableFrom(dataTypeClazz)) continue;
                                    _out[_level].append((char)entry.getValue().byteValue());
                                    found = true;
                                    break;
                                }
                                if (!found) {
                                    throw new DBusException("Exporting non-exportable type: " + String.valueOf(_dataType));
                                }
                            }
                            break block37;
                        }
                        String[] sigs = new String[newtypes.length];
                        int j2 = 0;
                        while (true) {
                            if (j2 >= sigs.length) {
                                return sigs;
                            }
                            String[] ss = Marshalling.recursiveGetDBusType(_out, newtypes[j2], false, _level + 1);
                            if (1 != ss.length) {
                                throw new DBusException("Serializable classes must serialize to native DBus types");
                            }
                            sigs[j2] = ss[0];
                            ++j2;
                        }
                    }
                    for (int j2 = 0; j2 < _ex2; ++j2) {
                        Type t4 = t2[j2];
                        if (Type.class.equals((Object)t4)) {
                            _out[_level].append('g');
                            continue;
                        }
                        String[] s = Marshalling.recursiveGetDBusType(_out, t4, false, _level + 1);
                        if (s.length != 1) {
                            throw new DBusException(ERROR_MULTI_VALUED_ARRAY);
                        }
                        _out[_level].append('a');
                        _out[_level].append(s[0]);
                    }
                    break block37;
                }
                for (int s = 0; s < n; ++s) {
                    Field f = j2[s];
                    Position p = f.getAnnotation(Position.class);
                    if (null == p) continue;
                    ts[p.value()] = f.getGenericType();
                }
                Container.putTypeCache(_dataType, ts);
            }
            for (Type t : ts) {
                if (t == null) continue;
                for (String s : Marshalling.recursiveGetDBusType(_out, t, false, _level + 1)) {
                    _out[_level].append(s);
                }
            }
            _out[_level].append(')');
        }
        LOGGER.trace("Converted Java type: {} to D-Bus Type: {}", (Object)_dataType, (Object)_out[_level]);
        return new String[]{_out[_level].toString()};
    }

    public static int getJavaType(String _dbusType, List<Type> _resultValue, int _limit) throws DBusException {
        if (null == _dbusType || _dbusType.isEmpty() || 0 == _limit) {
            return 0;
        }
        try {
            int idx;
            block22: for (idx = 0; idx < _dbusType.length() && (-1 == _limit || _limit > _resultValue.size()); ++idx) {
                switch (_dbusType.charAt(idx)) {
                    case '(': {
                        int structIdx = idx + 1;
                        int structLen = 1;
                        while (structLen > 0) {
                            if (')' == _dbusType.charAt(structIdx)) {
                                --structLen;
                            } else if ('(' == _dbusType.charAt(structIdx)) {
                                ++structLen;
                            }
                            ++structIdx;
                        }
                        ArrayList<Type> contained = new ArrayList();
                        int javaType = Marshalling.getJavaType(_dbusType.substring(idx + 1, structIdx - 1), contained, -1);
                        _resultValue.add(new DBusStructType(contained.toArray(new Type[0])));
                        idx = structIdx - 1;
                        continue block22;
                    }
                    case 'a': {
                        int javaType;
                        ArrayList<Type> contained;
                        if ('{' == _dbusType.charAt(idx + 1)) {
                            contained = new ArrayList();
                            javaType = Marshalling.getJavaType(_dbusType.substring(idx + 2), contained, 2);
                            _resultValue.add(new DBusMapType((Type)contained.get(0), (Type)contained.get(1)));
                            idx += javaType + 2;
                            continue block22;
                        }
                        contained = new ArrayList();
                        javaType = Marshalling.getJavaType(_dbusType.substring(idx + 1), contained, 1);
                        _resultValue.add(new DBusListType((Type)contained.get(0)));
                        idx += javaType;
                        continue block22;
                    }
                    case 'v': {
                        _resultValue.add((Type)((Object)Variant.class));
                        continue block22;
                    }
                    case 'b': {
                        _resultValue.add((Type)((Object)Boolean.class));
                        continue block22;
                    }
                    case 'n': {
                        _resultValue.add((Type)((Object)Short.class));
                        continue block22;
                    }
                    case 'y': {
                        _resultValue.add((Type)((Object)Byte.class));
                        continue block22;
                    }
                    case 'o': {
                        _resultValue.add((Type)((Object)DBusPath.class));
                        continue block22;
                    }
                    case 'q': {
                        _resultValue.add((Type)((Object)UInt16.class));
                        continue block22;
                    }
                    case 'i': {
                        _resultValue.add((Type)((Object)Integer.class));
                        continue block22;
                    }
                    case 'u': {
                        _resultValue.add((Type)((Object)UInt32.class));
                        continue block22;
                    }
                    case 'x': {
                        _resultValue.add((Type)((Object)Long.class));
                        continue block22;
                    }
                    case 't': {
                        _resultValue.add((Type)((Object)UInt64.class));
                        continue block22;
                    }
                    case 'd': {
                        _resultValue.add((Type)((Object)Double.class));
                        continue block22;
                    }
                    case 'f': {
                        _resultValue.add((Type)((Object)Float.class));
                        continue block22;
                    }
                    case 's': {
                        _resultValue.add((Type)((Object)CharSequence.class));
                        continue block22;
                    }
                    case 'h': {
                        _resultValue.add((Type)((Object)FileDescriptor.class));
                        continue block22;
                    }
                    case 'g': {
                        _resultValue.add((Type)((Object)Type[].class));
                        continue block22;
                    }
                    case '{': {
                        _resultValue.add((Type)((Object)Map.Entry.class));
                        ArrayList<Type> contained = new ArrayList<Type>();
                        int javaType = Marshalling.getJavaType(_dbusType.substring(idx + 1), contained, 2);
                        idx += javaType + 1;
                        continue block22;
                    }
                    default: {
                        throw new DBusException(String.format("Failed to parse DBus type signature: %s (%s).", _dbusType, Character.valueOf(_dbusType.charAt(idx))));
                    }
                }
            }
            return idx;
        }
        catch (IndexOutOfBoundsException _ex) {
            LOGGER.debug("Failed to parse DBus type signature.", _ex);
            throw new DBusException("Failed to parse DBus type signature: " + _dbusType);
        }
    }

    public static Object[] convertParameters(Object[] _parameters, Type[] _types, String[] _customSignatures, AbstractConnectionBase _conn) throws DBusException {
        if (_parameters == null) {
            return null;
        }
        Object[] parameters = _parameters;
        Type[] types = _types;
        int lastCustomSig = 0;
        for (int i = 0; i < parameters.length; ++i) {
            if (null == parameters[i]) continue;
            LOGGER.trace("Converting {} from '{}' to {}", i, parameters[i], types[i]);
            Object object = parameters[i];
            if (object instanceof DBusSerializable) {
                DBusSerializable ds = (DBusSerializable)object;
                for (Method m : parameters[i].getClass().getDeclaredMethods()) {
                    if (!m.getName().equals(MTH_NAME_DESERIALIZE)) continue;
                    Class<?>[] newtypes = m.getParameterTypes();
                    Type[] expand = new Type[types.length + newtypes.length - 1];
                    System.arraycopy(types, 0, expand, 0, i);
                    System.arraycopy(newtypes, 0, expand, i, newtypes.length);
                    System.arraycopy(types, i + 1, expand, i + newtypes.length, types.length - i - 1);
                    types = expand;
                    Object[] newparams = ds.serialize();
                    Object[] exparams = new Object[parameters.length + newparams.length - 1];
                    System.arraycopy(parameters, 0, exparams, 0, i);
                    System.arraycopy(newparams, 0, exparams, i, newparams.length);
                    System.arraycopy(parameters, i + 1, exparams, i + newparams.length, parameters.length - i - 1);
                    parameters = exparams;
                }
                --i;
                continue;
            }
            Object object2 = parameters[i];
            if (object2 instanceof Tuple) {
                Tuple tup = (Tuple)object2;
                Type[] typeArray = ((ParameterizedType)types[i]).getActualTypeArguments();
                Type[] expand = new Type[types.length + typeArray.length - 1];
                System.arraycopy(types, 0, expand, 0, i);
                System.arraycopy(typeArray, 0, expand, i, typeArray.length);
                System.arraycopy(types, i + 1, expand, i + typeArray.length, types.length - i - 1);
                types = expand;
                Object[] newparams = tup.getParameters();
                Object[] exparams = new Object[parameters.length + newparams.length - 1];
                System.arraycopy(parameters, 0, exparams, 0, i);
                System.arraycopy(newparams, 0, exparams, i, newparams.length);
                System.arraycopy(parameters, i + 1, exparams, i + newparams.length, parameters.length - i - 1);
                parameters = exparams;
                LoggingHelper.logIf(LOGGER.isTraceEnabled(), () -> LOGGER.trace("New params: {}, new types: {}", (Object)Arrays.deepToString(exparams), (Object)Arrays.deepToString(expand)));
                --i;
                continue;
            }
            if (types[i] instanceof TypeVariable && !(parameters[i] instanceof Variant)) {
                if (_customSignatures != null && _customSignatures.length > 0 && _customSignatures.length > lastCustomSig) {
                    parameters[i] = new Variant<Object>(parameters[i], _customSignatures[lastCustomSig]);
                    ++lastCustomSig;
                    continue;
                }
                parameters[i] = new Variant<Object>(parameters[i]);
                continue;
            }
            Object object3 = parameters[i];
            if (!(object3 instanceof DBusInterface)) continue;
            DBusInterface di = (DBusInterface)object3;
            parameters[i] = _conn.getExportedObject(di);
        }
        return parameters;
    }

    public static Object[] convertParameters(Object[] _parameters, Type[] _types, AbstractConnectionBase _conn) throws DBusException {
        return Marshalling.convertParameters(_parameters, _types, null, _conn);
    }

    /*
     * Could not resolve type clashes
     * Unable to fully structure code
     */
    static Object deSerializeParameter(Object _parameter, Type _type, AbstractConnectionBase _conn) throws Exception {
        block32: {
            block34: {
                block35: {
                    block33: {
                        Marshalling.LOGGER.trace("Deserializing from {} to {}", (Object)_parameter.getClass(), (Object)_type);
                        parameter /* !! */  = _parameter;
                        if (_type instanceof TypeVariable && parameter /* !! */  instanceof Variant) {
                            variant = (Variant)parameter /* !! */ ;
                            parameter /* !! */  = variant.getValue();
                            Marshalling.LOGGER.trace("Type is variant, unwrapping to {}", (Object)parameter /* !! */ );
                        }
                        if (_type instanceof Class && ((Class)_type).isArray() && ((Class)_type).getComponentType().equals(Type.class) && parameter /* !! */  instanceof String) {
                            rv = new ArrayList<Type>();
                            Marshalling.getJavaType((String)parameter /* !! */ , rv, -1);
                            parameter /* !! */  = rv.toArray(new Type[0]);
                        }
                        if (parameter /* !! */  instanceof ObjectPath) {
                            op = (ObjectPath)parameter /* !! */ ;
                            Marshalling.LOGGER.trace("Parameter is ObjectPath");
                            parameter /* !! */  = _type instanceof Class != false && DBusInterface.class.isAssignableFrom((Class)_type) != false ? _conn.getExportedObject(op.getSource(), op.getPath(), (Class)_type) : new DBusPath(op.getPath());
                        }
                        if (parameter /* !! */  instanceof String) {
                            str = (String)parameter /* !! */ ;
                            if (_type instanceof Class && Enum.class.isAssignableFrom((Class)_type)) {
                                Marshalling.LOGGER.trace("Type seems to be an enum");
                                parameter /* !! */  = Enum.valueOf((Class)_type, str);
                            }
                        }
                        if (parameter /* !! */  instanceof Object[]) {
                            objArr = parameter /* !! */ ;
                            if (_type instanceof Class && Struct.class.isAssignableFrom((Class)_type)) {
                                Marshalling.LOGGER.trace("Creating Struct {} from {}", (Object)_type, (Object)parameter /* !! */ );
                                ts = Container.getTypeCache(_type);
                                if (ts == null) {
                                    fs = ((Class)_type).getDeclaredFields();
                                    ts = new Type[fs.length];
                                    var7_8 = fs;
                                    var8_13 = var7_8.length;
                                    for (var9_17 = 0; var9_17 < var8_13; ++var9_17) {
                                        f = var7_8[var9_17];
                                        p = f.getAnnotation(Position.class);
                                        if (null == p) continue;
                                        ts[p.value()] = f.getGenericType();
                                    }
                                    Container.putTypeCache(_type, (Type[])ts);
                                }
                                parameter /* !! */  = Marshalling.deSerializeParameters(objArr, (Type[])ts, _conn);
                                for (AccessibleObject con : ((Class)_type).getDeclaredConstructors()) {
                                    try {
                                        parameter /* !! */  = con.newInstance(objArr);
                                        break;
                                    }
                                    catch (IllegalArgumentException _exIa) {
                                        Marshalling.LOGGER.trace("Could not create new instance", _exIa);
                                    }
                                }
                            }
                        }
                        if (parameter /* !! */  instanceof Object[]) {
                            oa = parameter /* !! */ ;
                            Marshalling.LOGGER.trace("Parameter is object array");
                            ts = new Type[oa.length];
                            Arrays.fill(ts, parameter /* !! */ .getClass().getComponentType());
                            parameter /* !! */  = Marshalling.deSerializeParameters(oa, (Type[])ts, _conn);
                        }
                        if (!(parameter /* !! */  instanceof List)) break block32;
                        Marshalling.LOGGER.trace("Parameter is List");
                        if (!(_type instanceof ParameterizedType)) break block33;
                        pt = (ParameterizedType)_type;
                        type2 = pt.getActualTypeArguments()[0];
                        break block34;
                    }
                    if (!(_type instanceof GenericArrayType)) break block35;
                    gat = (GenericArrayType)_type;
                    type2 = gat.getGenericComponentType();
                    break block34;
                }
                if (!(_type instanceof Class)) ** GOTO lbl-1000
                clz = (Class)_type;
                if (((Class)_type).isArray()) {
                    type2 = clz.getComponentType();
                } else lbl-1000:
                

                {
                    type2 = null;
                }
            }
            if (null != type2) {
                parameter /* !! */  = Marshalling.deSerializeParameters((List)parameter /* !! */ , type2, _conn);
            }
        }
        if ((_type.equals(Float.class) || _type.equals(Float.TYPE)) && !(parameter /* !! */  instanceof Float)) {
            parameter /* !! */  = Float.valueOf(((Number)parameter /* !! */ ).floatValue());
            Marshalling.LOGGER.trace("Parameter is float of value: {}", (Object)parameter /* !! */ );
        }
        if (parameter /* !! */  instanceof Object[] || parameter /* !! */  instanceof List || parameter /* !! */ .getClass().isArray()) {
            if (_type instanceof ParameterizedType) {
                pt = (ParameterizedType)_type;
                parameter /* !! */  = ArrayFrob.convert(parameter /* !! */ , (Class)pt.getRawType());
            } else if (_type instanceof GenericArrayType) {
                gat = (GenericArrayType)_type;
                ct = gat.getGenericComponentType();
                cc = null;
                if (ct instanceof Class) {
                    cc = clz = (Class)ct;
                }
                if (ct instanceof ParameterizedType) {
                    pt = (ParameterizedType)ct;
                    cc = (Class)pt.getRawType();
                }
                o = Array.newInstance(cc, 0);
                parameter /* !! */  = ArrayFrob.convert(parameter /* !! */ , o.getClass());
            } else if (_type instanceof Class) {
                clz = (Class)_type;
                if (((Class)_type).isArray()) {
                    cc = clz.getComponentType();
                    if ((cc.equals(Float.class) || cc.equals(Float.TYPE)) && parameter /* !! */  instanceof double[]) {
                        dbArr = (double[])parameter /* !! */ ;
                        tmp2 = new float[dbArr.length];
                        for (i = 0; i < dbArr.length; ++i) {
                            tmp2[i] = (float)dbArr[i];
                        }
                        parameter /* !! */  = tmp2;
                    }
                    o = Array.newInstance(cc, 0);
                    parameter /* !! */  = ArrayFrob.convert(parameter /* !! */ , o.getClass());
                }
            }
        }
        if (parameter /* !! */  instanceof DBusMap) {
            dmap = (DBusMap)parameter /* !! */ ;
            Marshalling.LOGGER.trace("Deserializing a Map");
            if (_type instanceof ParameterizedType) {
                pt = (ParameterizedType)_type;
                maptypes = pt.getActualTypeArguments();
            } else {
                maptypes = parameter /* !! */ .getClass().getTypeParameters();
            }
            for (i = 0; i < dmap.entries.length; ++i) {
                dmap.entries[i][0] = Marshalling.deSerializeParameter(dmap.entries[i][0], maptypes[0], _conn);
                dmap.entries[i][1] = Marshalling.deSerializeParameter(dmap.entries[i][1], maptypes[1], _conn);
            }
        }
        return parameter /* !! */ ;
    }

    static List<Object> deSerializeParameters(List<Object> _parameters, Type _type, AbstractConnectionBase _conn) throws Exception {
        LOGGER.trace("Deserializing from {} to {}", (Object)_parameters, (Object)_type);
        if (_parameters == null) {
            return null;
        }
        for (int i = 0; i < _parameters.size(); ++i) {
            if (_parameters.get(i) == null) continue;
            _parameters.set(i, Marshalling.deSerializeParameter(_parameters.get(i), _type, _conn));
        }
        return _parameters;
    }

    public static Object[] deSerializeParameters(Object[] _parameters, Type[] _types, AbstractConnectionBase _conn) throws Exception {
        Class clz;
        ParameterizedType pt;
        Type type;
        LoggingHelper.logIf(LOGGER.isTraceEnabled(), () -> LOGGER.trace("Deserializing from {} to {} ", (Object)Arrays.deepToString(_parameters), (Object)Arrays.deepToString(_types)));
        if (null == _parameters) {
            return null;
        }
        Object[] parameters = _parameters;
        Type[] types = _types;
        if (types.length == 1 && (type = types[0]) instanceof ParameterizedType && Tuple.class.isAssignableFrom((Class)(pt = (ParameterizedType)type).getRawType())) {
            types = pt.getActualTypeArguments();
        }
        if (types.length == 1 && (type = types[0]) instanceof Class && Tuple.class.isAssignableFrom(clz = (Class)type)) {
            String typeName = types[0].getTypeName();
            Constructor<?>[] constructors = Class.forName(typeName).getDeclaredConstructors();
            if (constructors.length != 1) {
                throw new DBusException("Error deserializing message: We had a Tuple type but wrong number of constructors for this Tuple. There should be exactly one.");
            }
            if (constructors[0].getParameterCount() != parameters.length) {
                throw new DBusException("Error deserializing message: We had a Tuple type but it had wrong number of constructor arguments. The number of constructor arguments should match the number of parameters to deserialize.");
            }
            Object o = constructors[0].newInstance(parameters);
            return new Object[]{o};
        }
        for (int i = 0; i < parameters.length; ++i) {
            ParameterizedType pt2;
            Type constructors;
            if (i >= types.length) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Parameter length differs, expected {} but got {}", (Object)parameters.length, (Object)types.length);
                    for (int j = 0; j < parameters.length; ++j) {
                        LOGGER.error("Error, Parameters differ: {}, '{}'", (Object)j, parameters[j]);
                    }
                }
                throw new DBusException("Error deserializing message: number of parameters didn't match receiving signature");
            }
            if (null == parameters[i]) continue;
            if (types[i] instanceof Class && DBusSerializable.class.isAssignableFrom((Class)types[i]) || (constructors = types[i]) instanceof ParameterizedType && DBusSerializable.class.isAssignableFrom((Class)(pt2 = (ParameterizedType)constructors).getRawType())) {
                Class dsc = types[i] instanceof Class ? (Class)types[i] : (Class)((ParameterizedType)types[i]).getRawType();
                for (Method m : dsc.getDeclaredMethods()) {
                    if (!m.getName().equals(MTH_NAME_DESERIALIZE)) continue;
                    Type[] newtypes = m.getGenericParameterTypes();
                    try {
                        Object[] sub = new Object[newtypes.length];
                        System.arraycopy(parameters, i, sub, 0, newtypes.length);
                        sub = Marshalling.deSerializeParameters(sub, newtypes, _conn);
                        DBusSerializable sz = (DBusSerializable)dsc.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                        m.invoke((Object)sz, sub);
                        Object[] compress = new Object[parameters.length - newtypes.length + 1];
                        System.arraycopy(parameters, 0, compress, 0, i);
                        compress[i] = sz;
                        System.arraycopy(parameters, i + newtypes.length, compress, i + 1, parameters.length - i - newtypes.length);
                        parameters = compress;
                    }
                    catch (ArrayIndexOutOfBoundsException _ex) {
                        LOGGER.debug("", _ex);
                        throw new DBusException(String.format("Not enough elements to create custom object from serialized data (%s < %s).", parameters.length - i, newtypes.length));
                    }
                }
                continue;
            }
            parameters[i] = Marshalling.deSerializeParameter(parameters[i], types[i], _conn);
        }
        return parameters;
    }

    static {
        CLASS_TO_ARGUMENTTYPE.put(Boolean.class, (byte)98);
        CLASS_TO_ARGUMENTTYPE.put(Boolean.TYPE, (byte)98);
        CLASS_TO_ARGUMENTTYPE.put(Byte.class, (byte)121);
        CLASS_TO_ARGUMENTTYPE.put(Byte.TYPE, (byte)121);
        CLASS_TO_ARGUMENTTYPE.put(Short.class, (byte)110);
        CLASS_TO_ARGUMENTTYPE.put(Short.TYPE, (byte)110);
        CLASS_TO_ARGUMENTTYPE.put(Integer.class, (byte)105);
        CLASS_TO_ARGUMENTTYPE.put(Integer.TYPE, (byte)105);
        CLASS_TO_ARGUMENTTYPE.put(Long.class, (byte)120);
        CLASS_TO_ARGUMENTTYPE.put(Long.TYPE, (byte)120);
        CLASS_TO_ARGUMENTTYPE.put(Double.class, (byte)100);
        CLASS_TO_ARGUMENTTYPE.put(Double.TYPE, (byte)100);
        if (AbstractConnection.FLOAT_SUPPORT) {
            CLASS_TO_ARGUMENTTYPE.put(Float.class, (byte)102);
            CLASS_TO_ARGUMENTTYPE.put(Float.TYPE, (byte)102);
        } else {
            CLASS_TO_ARGUMENTTYPE.put(Float.class, (byte)100);
            CLASS_TO_ARGUMENTTYPE.put(Float.TYPE, (byte)100);
        }
        CLASS_TO_ARGUMENTTYPE.put(UInt16.class, (byte)113);
        CLASS_TO_ARGUMENTTYPE.put(UInt32.class, (byte)117);
        CLASS_TO_ARGUMENTTYPE.put(UInt64.class, (byte)116);
        CLASS_TO_ARGUMENTTYPE.put(CharSequence.class, (byte)115);
        CLASS_TO_ARGUMENTTYPE.put(Variant.class, (byte)118);
        CLASS_TO_ARGUMENTTYPE.put(FileDescriptor.class, (byte)104);
        CLASS_TO_ARGUMENTTYPE.put(DBusInterface.class, (byte)111);
        CLASS_TO_ARGUMENTTYPE.put(DBusPath.class, (byte)111);
        CLASS_TO_ARGUMENTTYPE.put(ObjectPath.class, (byte)111);
    }
}

