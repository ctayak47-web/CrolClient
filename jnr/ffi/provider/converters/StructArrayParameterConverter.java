
package jnr.ffi.provider.converters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.DelegatingMemoryIO;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class StructArrayParameterConverter
implements ToNativeConverter<Struct[], Pointer> {
    protected final Runtime runtime;
    protected final int parameterFlags;

    public static ToNativeConverter<Struct[], Pointer> getInstance(ToNativeContext toNativeContext, Class structClass) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return !ParameterFlags.isOut(parameterFlags) ? new StructArrayParameterConverter(toNativeContext.getRuntime(), parameterFlags) : new Out(toNativeContext.getRuntime(), structClass.asSubclass(Struct.class), parameterFlags);
    }

    StructArrayParameterConverter(Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    @Override
    public Pointer toNative(Struct[] structs, ToNativeContext context) {
        if (structs == null) {
            return null;
        }
        Pointer memory = Struct.getMemory(structs[0], this.parameterFlags);
        if (!(memory instanceof DelegatingMemoryIO)) {
            throw new RuntimeException("Struct array must be backed by contiguous array");
        }
        return ((DelegatingMemoryIO)((Object)memory)).getDelegatedMemoryIO();
    }

    private static int align(int offset, int align) {
        return offset + align - 1 & ~(align - 1);
    }

    public static final class Out
    extends StructArrayParameterConverter
    implements ToNativeConverter.PostInvocation<Struct[], Pointer> {
        private final Constructor<? extends Struct> constructor;

        Out(Runtime runtime, Class<? extends Struct> structClass, int parameterFlags) {
            super(runtime, parameterFlags);
            Constructor<? extends Struct> cons;
            try {
                cons = structClass.getConstructor(Runtime.class);
            }
            catch (NoSuchMethodException nsme) {
                throw new RuntimeException(structClass.getName() + " has no constructor that accepts jnr.ffi.Runtime");
            }
            catch (Throwable t) {
                throw new RuntimeException(t);
            }
            this.constructor = cons;
        }

        @Override
        public void postInvoke(Struct[] structs, Pointer primitive, ToNativeContext context) {
            if (structs != null && primitive != null) {
                try {
                    int off = 0;
                    for (int i = 0; i < structs.length; ++i) {
                        structs[i] = this.constructor.newInstance(this.runtime);
                        int structSize = StructArrayParameterConverter.align(Struct.size(structs[i]), Struct.alignment(structs[i]));
                        off = StructArrayParameterConverter.align(off, Struct.alignment(structs[i]));
                        structs[i].useMemory(primitive.slice(off, structSize));
                        off += structSize;
                    }
                }
                catch (InstantiationException ie) {
                    throw new RuntimeException(ie);
                }
                catch (IllegalAccessException iae) {
                    throw new RuntimeException(iae);
                }
                catch (InvocationTargetException ite) {
                    throw new RuntimeException(ite);
                }
            }
        }
    }
}

