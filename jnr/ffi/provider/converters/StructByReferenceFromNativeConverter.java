
package jnr.ffi.provider.converters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;

public class StructByReferenceFromNativeConverter
implements FromNativeConverter<Struct, Pointer> {
    private final Constructor<? extends Struct> constructor;

    public static FromNativeConverter<Struct, Pointer> getInstance(Class structClass, FromNativeContext toNativeContext) {
        try {
            return new StructByReferenceFromNativeConverter(structClass.getConstructor(Runtime.class));
        }
        catch (NoSuchMethodException nsme) {
            throw new RuntimeException(structClass.getName() + " has no constructor that accepts jnr.ffi.Runtime");
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    StructByReferenceFromNativeConverter(Constructor<? extends Struct> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Struct fromNative(Pointer nativeValue, FromNativeContext context) {
        try {
            Struct s = this.constructor.newInstance(context.getRuntime());
            s.useMemory(nativeValue);
            return s;
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }
}

