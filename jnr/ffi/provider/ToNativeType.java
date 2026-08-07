
package jnr.ffi.provider;

import java.lang.annotation.Annotation;
import java.util.Collection;
import jnr.ffi.NativeType;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.SigType;

public class ToNativeType
extends SigType
implements jnr.ffi.mapper.ToNativeType {
    private final ToNativeConverter toNativeConverter;
    private final ToNativeContext toNativeContext;

    public ToNativeType(Class javaType, NativeType nativeType, Collection<Annotation> annotations, ToNativeConverter toNativeConverter, ToNativeContext toNativeContext) {
        super(javaType, nativeType, annotations, toNativeConverter != null ? toNativeConverter.nativeType() : javaType);
        this.toNativeConverter = toNativeConverter;
        this.toNativeContext = toNativeContext;
    }

    @Override
    public final ToNativeConverter getToNativeConverter() {
        return this.toNativeConverter;
    }

    public ToNativeContext getToNativeContext() {
        return this.toNativeContext;
    }
}

