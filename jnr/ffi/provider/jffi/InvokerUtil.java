
package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallContextCache;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.MethodParameterContext;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.ToNativeType;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.SigType;
import jnr.ffi.provider.jffi.ConverterMetaData;
import jnr.ffi.provider.jffi.Types;
import jnr.ffi.util.Annotations;

final class InvokerUtil {
    static final Map<NativeType, com.kenai.jffi.Type> jffiTypes;

    InvokerUtil() {
    }

    public static CallingConvention getCallingConvention(Map<LibraryOption, ?> libraryOptions) {
        Object convention = libraryOptions.get((Object)LibraryOption.CallingConvention);
        if (convention instanceof com.kenai.jffi.CallingConvention) {
            return com.kenai.jffi.CallingConvention.DEFAULT.equals(convention) ? CallingConvention.DEFAULT : CallingConvention.STDCALL;
        }
        if (convention instanceof CallingConvention) {
            switch ((CallingConvention)((Object)convention)) {
                case DEFAULT: {
                    return CallingConvention.DEFAULT;
                }
                case STDCALL: {
                    return CallingConvention.STDCALL;
                }
            }
        } else if (convention != null) {
            throw new IllegalArgumentException("unknown calling convention: " + convention);
        }
        return CallingConvention.DEFAULT;
    }

    public static CallingConvention getCallingConvention(Class interfaceClass, Map<LibraryOption, ?> options) {
        if (interfaceClass.isAnnotationPresent(StdCall.class)) {
            return CallingConvention.STDCALL;
        }
        return InvokerUtil.getCallingConvention(options);
    }

    public static boolean hasAnnotation(Collection<Annotation> annotations, Class<? extends Annotation> annotationClass) {
        for (Annotation a : annotations) {
            if (!annotationClass.isInstance(a)) continue;
            return true;
        }
        return false;
    }

    static com.kenai.jffi.Type jffiType(NativeType jnrType) {
        com.kenai.jffi.Type jffiType = jffiTypes.get((Object)jnrType);
        if (jffiType != null) {
            return jffiType;
        }
        throw new IllegalArgumentException("unsupported parameter type: " + (Object)((Object)jnrType));
    }

    static NativeType nativeType(Type jnrType) {
        return jnrType.getNativeType();
    }

    static Collection<Annotation> getAnnotations(FromNativeType fromNativeType) {
        return fromNativeType != null ? ConverterMetaData.getAnnotations(fromNativeType.getFromNativeConverter()) : Annotations.EMPTY_ANNOTATIONS;
    }

    static Collection<Annotation> getAnnotations(ToNativeType toNativeType) {
        return toNativeType != null ? ConverterMetaData.getAnnotations(toNativeType.getToNativeConverter()) : Annotations.EMPTY_ANNOTATIONS;
    }

    static ResultType getResultType(Runtime runtime, Class type, Collection<Annotation> annotations, FromNativeConverter fromNativeConverter, FromNativeContext fromNativeContext) {
        Collection<Annotation> converterAnnotations = ConverterMetaData.getAnnotations(fromNativeConverter);
        Collection<Annotation> allAnnotations = Annotations.mergeAnnotations(annotations, converterAnnotations);
        NativeType nativeType = InvokerUtil.getMethodResultNativeType(runtime, fromNativeConverter != null ? fromNativeConverter.nativeType() : type, allAnnotations);
        boolean useContext = fromNativeConverter != null && !InvokerUtil.hasAnnotation(converterAnnotations, FromNativeConverter.NoContext.class);
        return new ResultType(type, nativeType, allAnnotations, fromNativeConverter, useContext ? fromNativeContext : null);
    }

    static ResultType getResultType(Runtime runtime, Class type, Collection<Annotation> annotations, FromNativeType fromNativeType, FromNativeContext fromNativeContext) {
        Collection<Annotation> converterAnnotations = InvokerUtil.getAnnotations(fromNativeType);
        Collection<Annotation> allAnnotations = Annotations.mergeAnnotations(annotations, converterAnnotations);
        FromNativeConverter fromNativeConverter = fromNativeType != null ? fromNativeType.getFromNativeConverter() : null;
        NativeType nativeType = InvokerUtil.getMethodResultNativeType(runtime, fromNativeConverter != null ? fromNativeConverter.nativeType() : type, allAnnotations);
        boolean useContext = fromNativeConverter != null && !InvokerUtil.hasAnnotation(converterAnnotations, FromNativeConverter.NoContext.class);
        return new ResultType(type, nativeType, allAnnotations, fromNativeConverter, useContext ? fromNativeContext : null);
    }

    private static ParameterType getParameterType(Runtime runtime, Class type, Collection<Annotation> annotations, ToNativeConverter toNativeConverter, ToNativeContext toNativeContext) {
        NativeType nativeType = InvokerUtil.getMethodParameterNativeType(runtime, toNativeConverter != null ? toNativeConverter.nativeType() : type, annotations);
        return new ParameterType(type, nativeType, annotations, toNativeConverter, toNativeContext);
    }

    private static ParameterType getParameterType(Runtime runtime, Class type, Collection<Annotation> annotations, ToNativeType toNativeType, ToNativeContext toNativeContext) {
        ToNativeConverter toNativeConverter = toNativeType != null ? toNativeType.getToNativeConverter() : null;
        NativeType nativeType = InvokerUtil.getMethodParameterNativeType(runtime, toNativeConverter != null ? toNativeConverter.nativeType() : type, annotations);
        return new ParameterType(type, nativeType, annotations, toNativeConverter, toNativeContext);
    }

    static ParameterType[] getParameterTypes(Runtime runtime, SignatureTypeMapper typeMapper, Method m) {
        Class<?>[] javaParameterTypes = m.getParameterTypes();
        Annotation[][] parameterAnnotations = m.getParameterAnnotations();
        ParameterType[] parameterTypes = new ParameterType[javaParameterTypes.length];
        for (int pidx = 0; pidx < javaParameterTypes.length; ++pidx) {
            Collection<Annotation> annotations = Annotations.sortedAnnotationCollection(parameterAnnotations[pidx]);
            MethodParameterContext toNativeContext = new MethodParameterContext(runtime, m, pidx, annotations);
            DefaultSignatureType signatureType = DefaultSignatureType.create(javaParameterTypes[pidx], toNativeContext);
            ToNativeType toNativeType = typeMapper.getToNativeType(signatureType, toNativeContext);
            ToNativeConverter toNativeConverter = toNativeType != null ? toNativeType.getToNativeConverter() : null;
            Collection<Annotation> converterAnnotations = ConverterMetaData.getAnnotations(toNativeConverter);
            Collection<Annotation> allAnnotations = Annotations.mergeAnnotations(annotations, converterAnnotations);
            boolean contextRequired = toNativeConverter != null && !InvokerUtil.hasAnnotation(converterAnnotations, ToNativeConverter.NoContext.class);
            parameterTypes[pidx] = InvokerUtil.getParameterType(runtime, javaParameterTypes[pidx], allAnnotations, toNativeConverter, (ToNativeContext)(contextRequired ? toNativeContext : null));
        }
        return parameterTypes;
    }

    static CallContext getCallContext(SigType resultType, SigType[] parameterTypes, CallingConvention convention, boolean requiresErrno) {
        return InvokerUtil.getCallContext(resultType, parameterTypes.length, parameterTypes, parameterTypes.length, convention, requiresErrno);
    }

    static CallContext getCallContext(SigType resultType, int fixedParamCount, SigType[] parameterTypes, CallingConvention convention, boolean requiresErrno) {
        return InvokerUtil.getCallContext(resultType, fixedParamCount, parameterTypes, parameterTypes.length, convention, requiresErrno);
    }

    static CallContext getCallContext(SigType resultType, int fixedParamCount, SigType[] parameterTypes, int paramTypesLength, CallingConvention convention, boolean requiresErrno) {
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[paramTypesLength];
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = InvokerUtil.jffiType(parameterTypes[i].getNativeType());
        }
        return CallContextCache.getInstance().getCallContext(InvokerUtil.jffiType(resultType.getNativeType()), fixedParamCount, nativeParamTypes, InvokerUtil.jffiConvention(convention), requiresErrno);
    }

    public static CallingConvention getNativeCallingConvention(Method m) {
        if (m.isAnnotationPresent(StdCall.class) || m.getDeclaringClass().isAnnotationPresent(StdCall.class)) {
            return CallingConvention.STDCALL;
        }
        return CallingConvention.DEFAULT;
    }

    static NativeType getMethodParameterNativeType(Runtime runtime, Class parameterClass, Collection<Annotation> annotations) {
        return Types.getType(runtime, parameterClass, annotations).getNativeType();
    }

    static NativeType getMethodResultNativeType(Runtime runtime, Class resultClass, Collection<Annotation> annotations) {
        return Types.getType(runtime, resultClass, annotations).getNativeType();
    }

    public static final com.kenai.jffi.CallingConvention jffiConvention(CallingConvention callingConvention) {
        return callingConvention == CallingConvention.DEFAULT ? com.kenai.jffi.CallingConvention.DEFAULT : com.kenai.jffi.CallingConvention.STDCALL;
    }

    static {
        EnumMap<NativeType, com.kenai.jffi.Type> m = new EnumMap<NativeType, com.kenai.jffi.Type>(NativeType.class);
        m.put(NativeType.VOID, com.kenai.jffi.Type.VOID);
        m.put(NativeType.SCHAR, com.kenai.jffi.Type.SCHAR);
        m.put(NativeType.UCHAR, com.kenai.jffi.Type.UCHAR);
        m.put(NativeType.SSHORT, com.kenai.jffi.Type.SSHORT);
        m.put(NativeType.USHORT, com.kenai.jffi.Type.USHORT);
        m.put(NativeType.SINT, com.kenai.jffi.Type.SINT);
        m.put(NativeType.UINT, com.kenai.jffi.Type.UINT);
        m.put(NativeType.SLONG, com.kenai.jffi.Type.SLONG);
        m.put(NativeType.ULONG, com.kenai.jffi.Type.ULONG);
        m.put(NativeType.SLONGLONG, com.kenai.jffi.Type.SLONG_LONG);
        m.put(NativeType.ULONGLONG, com.kenai.jffi.Type.ULONG_LONG);
        m.put(NativeType.FLOAT, com.kenai.jffi.Type.FLOAT);
        m.put(NativeType.DOUBLE, com.kenai.jffi.Type.DOUBLE);
        m.put(NativeType.ADDRESS, com.kenai.jffi.Type.POINTER);
        jffiTypes = Collections.unmodifiableMap(m);
    }
}

