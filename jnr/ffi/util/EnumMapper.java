
package jnr.ffi.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;

@ToNativeConverter.NoContext
@FromNativeConverter.NoContext
public final class EnumMapper {
    private final Class<? extends Enum> enumClass;
    private final int[] intValues;
    private final Map<Number, Enum> reverseLookupMap = new HashMap<Number, Enum>();

    private EnumMapper(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        EnumSet<? extends Enum> enums = EnumSet.allOf(enumClass);
        this.intValues = new int[enums.size()];
        Method intValueMethod = EnumMapper.getNumberValueMethod(enumClass, Integer.TYPE);
        for (Enum enum_ : enums) {
            Number value = intValueMethod != null ? (Number)EnumMapper.reflectedNumberValue(enum_, intValueMethod) : (Number)enum_.ordinal();
            this.intValues[enum_.ordinal()] = value.intValue();
            this.reverseLookupMap.put(value, enum_);
        }
    }

    public static EnumMapper getInstance(Class<? extends Enum> enumClass) {
        EnumMapper mapper = (EnumMapper)StaticDataHolder.MAPPERS.get(enumClass);
        if (mapper != null) {
            return mapper;
        }
        return EnumMapper.addMapper(enumClass);
    }

    private static synchronized EnumMapper addMapper(Class<? extends Enum> enumClass) {
        EnumMapper mapper = new EnumMapper(enumClass);
        IdentityHashMap<Class<? extends Enum>, EnumMapper> tmp = new IdentityHashMap<Class<? extends Enum>, EnumMapper>(StaticDataHolder.MAPPERS);
        tmp.put(enumClass, mapper);
        StaticDataHolder.MAPPERS = tmp;
        return mapper;
    }

    private static Method getNumberValueMethod(Class c, Class numberClass) {
        try {
            Method m = c.getDeclaredMethod(numberClass.getSimpleName() + "Value", new Class[0]);
            return m != null && numberClass == m.getReturnType() ? m : null;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static Number reflectedNumberValue(Enum e, Method m) {
        try {
            return (Number)m.invoke((Object)e, new Object[0]);
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public final Integer integerValue(Enum value) {
        if (value.getClass() != this.enumClass) {
            throw new IllegalArgumentException("enum class mismatch, " + value.getClass());
        }
        return this.intValues[value.ordinal()];
    }

    public final int intValue(Enum value) {
        return this.integerValue(value);
    }

    public Enum valueOf(int value) {
        return this.reverseLookup(value);
    }

    private Enum reverseLookup(int value) {
        Enum e = this.reverseLookupMap.get(value);
        return e != null ? e : this.badValue(value);
    }

    private Enum badValue(int value) {
        try {
            return Enum.valueOf(this.enumClass, "__UNKNOWN_NATIVE_VALUE");
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("No known Enum mapping for value " + value + " of type " + this.enumClass.getName());
        }
    }

    private static final class StaticDataHolder {
        private static volatile Map<Class<? extends Enum>, EnumMapper> MAPPERS = Collections.emptyMap();

        private StaticDataHolder() {
        }
    }

    public static interface IntegerEnum {
        public int intValue();
    }
}

