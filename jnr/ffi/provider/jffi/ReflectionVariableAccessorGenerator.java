
package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Variable;
import jnr.ffi.mapper.DataConverter;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.FromNativeType;
import jnr.ffi.provider.ToNativeType;
import jnr.ffi.provider.jffi.DefaultInvokerFactory;
import jnr.ffi.provider.jffi.MemoryUtil;
import jnr.ffi.provider.jffi.NumberUtil;
import jnr.ffi.provider.jffi.SimpleNativeContext;
import jnr.ffi.provider.jffi.Types;

class ReflectionVariableAccessorGenerator {
    ReflectionVariableAccessorGenerator() {
    }

    static Variable createVariableAccessor(Runtime runtime, Method method, long symbolAddress, SignatureTypeMapper typeMapper, Collection<Annotation> annotations) {
        Type variableType = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0];
        if (!(variableType instanceof Class)) {
            throw new IllegalArgumentException("unsupported variable class: " + variableType);
        }
        Class javaType = (Class)variableType;
        SimpleNativeContext context = new SimpleNativeContext(runtime, annotations);
        DefaultSignatureType signatureType = DefaultSignatureType.create(javaType, context);
        jnr.ffi.mapper.FromNativeType mappedFromNativeType = typeMapper.getFromNativeType(signatureType, context);
        FromNativeConverter fromNativeConverter = mappedFromNativeType != null ? mappedFromNativeType.getFromNativeConverter() : null;
        jnr.ffi.mapper.ToNativeType mappedToNativeType = typeMapper.getToNativeType(signatureType, context);
        ToNativeConverter toNativeConverter = mappedToNativeType != null ? mappedToNativeType.getToNativeConverter() : null;
        Class boxedType = toNativeConverter != null ? toNativeConverter.nativeType() : javaType;
        NativeType nativeType = Types.getType(runtime, boxedType, annotations).getNativeType();
        ToNativeType toNativeType = new ToNativeType(javaType, nativeType, annotations, toNativeConverter, null);
        FromNativeType fromNativeType = new FromNativeType(javaType, nativeType, annotations, fromNativeConverter, null);
        Pointer memory = MemoryUtil.newPointer(runtime, symbolAddress);
        Variable variable = ReflectionVariableAccessorGenerator.getNativeVariableAccessor(memory, toNativeType, fromNativeType);
        return toNativeType.getToNativeConverter() != null ? ReflectionVariableAccessorGenerator.getConvertingVariable(variable, toNativeType.getToNativeConverter(), fromNativeType.getFromNativeConverter()) : variable;
    }

    static Variable getConvertingVariable(Variable nativeVariable, ToNativeConverter toNativeConverter, FromNativeConverter fromNativeConverter) {
        if (toNativeConverter != null && fromNativeConverter == null || toNativeConverter == null && fromNativeConverter != null) {
            throw new UnsupportedOperationException("convertible types must have both a ToNativeConverter and a FromNativeConverter");
        }
        return new ConvertingVariable(nativeVariable, toNativeConverter, fromNativeConverter);
    }

    static Variable getNativeVariableAccessor(Pointer memory, ToNativeType toNativeType, FromNativeType fromNativeType) {
        if (Pointer.class == toNativeType.effectiveJavaType()) {
            return new PointerVariable(memory);
        }
        if (Number.class.isAssignableFrom(toNativeType.effectiveJavaType())) {
            return new NumberVariable(memory, ReflectionVariableAccessorGenerator.getPointerOp(toNativeType.getNativeType()), DefaultInvokerFactory.getNumberDataConverter(toNativeType.getNativeType()), DefaultInvokerFactory.getNumberResultConverter(fromNativeType));
        }
        throw new UnsupportedOperationException("unsupported variable type: " + toNativeType.effectiveJavaType());
    }

    private static PointerOp<Number> getPointerOp(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR: 
            case UCHAR: {
                return Int8PointerOp.INSTANCE;
            }
            case SSHORT: 
            case USHORT: {
                return Int16PointerOp.INSTANCE;
            }
            case SINT: 
            case UINT: {
                return Int32PointerOp.INSTANCE;
            }
            case SLONGLONG: 
            case ULONGLONG: {
                return Int64PointerOp.INSTANCE;
            }
            case SLONG: 
            case ULONG: 
            case ADDRESS: {
                return NumberUtil.sizeof(nativeType) == 4 ? Int32PointerOp.INSTANCE : Int64PointerOp.INSTANCE;
            }
            case FLOAT: {
                return FloatPointerOp.INSTANCE;
            }
            case DOUBLE: {
                return DoublePointerOp.INSTANCE;
            }
        }
        throw new UnsupportedOperationException("cannot convert " + (Object)((Object)nativeType));
    }

    private static final class ConvertingVariable
    implements Variable {
        private final Variable variable;
        private final ToNativeConverter toNativeConverter;
        private final FromNativeConverter fromNativeConverter;

        private ConvertingVariable(Variable variable, ToNativeConverter toNativeConverter, FromNativeConverter fromNativeConverter) {
            this.variable = variable;
            this.toNativeConverter = toNativeConverter;
            this.fromNativeConverter = fromNativeConverter;
        }

        public Object get() {
            return this.fromNativeConverter.fromNative(this.variable.get(), null);
        }

        public void set(Object value) {
            this.variable.set(this.toNativeConverter.toNative(value, null));
        }
    }

    private static final class PointerVariable
    extends AbstractVariable<Pointer> {
        private PointerVariable(Pointer memory) {
            super(memory);
        }

        @Override
        public Pointer get() {
            return this.memory.getPointer(0L);
        }

        @Override
        public void set(Pointer value) {
            if (value != null) {
                this.memory.putPointer(0L, value);
            } else {
                this.memory.putAddress(0L, 0L);
            }
        }
    }

    private static final class NumberVariable
    extends AbstractVariable<Number> {
        private final DataConverter<Number, Number> dataConverter;
        private final DefaultInvokerFactory.ResultConverter<? extends Number, Number> resultConverter;
        private final PointerOp<Number> pointerOp;

        private NumberVariable(Pointer memory, PointerOp<Number> pointerOp, DataConverter<Number, Number> dataConverter, DefaultInvokerFactory.ResultConverter<? extends Number, Number> resultConverter) {
            super(memory);
            this.pointerOp = pointerOp;
            this.dataConverter = dataConverter;
            this.resultConverter = resultConverter;
        }

        @Override
        public Number get() {
            return this.resultConverter.fromNative((Number)this.dataConverter.fromNative(this.pointerOp.get(this.memory), null), null);
        }

        @Override
        public void set(Number value) {
            this.pointerOp.put(this.memory, (Number)this.dataConverter.toNative(value, null));
        }
    }

    private static interface PointerOp<T> {
        public T get(Pointer var1);

        public void put(Pointer var1, T var2);
    }

    private static final class Int8PointerOp
    implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int8PointerOp();

        private Int8PointerOp() {
        }

        @Override
        public Number get(Pointer memory) {
            return memory.getByte(0L);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putByte(0L, value.byteValue());
        }
    }

    private static final class Int16PointerOp
    implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int16PointerOp();

        private Int16PointerOp() {
        }

        @Override
        public Number get(Pointer memory) {
            return memory.getShort(0L);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putShort(0L, value.shortValue());
        }
    }

    private static final class Int32PointerOp
    implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int32PointerOp();

        private Int32PointerOp() {
        }

        @Override
        public Number get(Pointer memory) {
            return memory.getInt(0L);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putInt(0L, value.intValue());
        }
    }

    private static final class Int64PointerOp
    implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new Int64PointerOp();

        private Int64PointerOp() {
        }

        @Override
        public Number get(Pointer memory) {
            return memory.getLongLong(0L);
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putLongLong(0L, value.longValue());
        }
    }

    private static final class FloatPointerOp
    implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new FloatPointerOp();

        private FloatPointerOp() {
        }

        @Override
        public Number get(Pointer memory) {
            return Float.valueOf(memory.getFloat(0L));
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putFloat(0L, value.floatValue());
        }
    }

    private static final class DoublePointerOp
    implements PointerOp<Number> {
        static final PointerOp<Number> INSTANCE = new DoublePointerOp();

        private DoublePointerOp() {
        }

        @Override
        public Number get(Pointer memory) {
            return Float.valueOf(memory.getFloat(0L));
        }

        @Override
        public void put(Pointer memory, Number value) {
            memory.putFloat(0L, value.floatValue());
        }
    }

    private static abstract class AbstractVariable<T>
    implements Variable<T> {
        protected final Pointer memory;

        protected AbstractVariable(Pointer memory) {
            this.memory = memory;
        }
    }
}

