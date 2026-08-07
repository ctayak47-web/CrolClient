
package jnr.ffi.provider.converters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Set;
import jnr.ffi.mapper.DataConverter;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.util.EnumMapper;

@FromNativeConverter.Cacheable
@ToNativeConverter.Cacheable
public final class EnumSetConverter
implements DataConverter<Set<? extends Enum>, Integer> {
    private final Class<? extends Enum> enumClass;
    private final EnumMapper enumMapper;
    private final EnumSet<? extends Enum> allValues;

    private EnumSetConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        this.enumMapper = EnumMapper.getInstance(enumClass);
        this.allValues = EnumSet.allOf(enumClass);
    }

    public static ToNativeConverter<Set<? extends Enum>, Integer> getToNativeConverter(SignatureType type, ToNativeContext toNativeContext) {
        return EnumSetConverter.getInstance(type.getGenericType());
    }

    public static FromNativeConverter<Set<? extends Enum>, Integer> getFromNativeConverter(SignatureType type, FromNativeContext fromNativeContext) {
        return EnumSetConverter.getInstance(type.getGenericType());
    }

    private static EnumSetConverter getInstance(Type parameterizedType) {
        if (!(parameterizedType instanceof ParameterizedType)) {
            return null;
        }
        if (((ParameterizedType)parameterizedType).getActualTypeArguments().length < 1) {
            return null;
        }
        Type enumType = ((ParameterizedType)parameterizedType).getActualTypeArguments()[0];
        if (!(enumType instanceof Class) || !Enum.class.isAssignableFrom((Class)enumType)) {
            return null;
        }
        return new EnumSetConverter(((Class)enumType).asSubclass(Enum.class));
    }

    @Override
    public Set fromNative(Integer nativeValue, FromNativeContext context) {
        EnumSet<? extends Enum> enums = EnumSet.noneOf(this.enumClass);
        for (Enum enum_ : this.allValues) {
            int enumValue = this.enumMapper.intValue(enum_);
            if ((nativeValue & enumValue) != enumValue) continue;
            enums.add(enum_);
        }
        return enums;
    }

    @Override
    public Integer toNative(Set<? extends Enum> value, ToNativeContext context) {
        int intValue = 0;
        for (Enum enum_ : value) {
            intValue |= this.enumMapper.intValue(enum_);
        }
        return intValue;
    }

    @Override
    public Class<Integer> nativeType() {
        return Integer.class;
    }
}

