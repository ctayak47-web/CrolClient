
package jnr.ffi.provider;

import java.lang.annotation.Annotation;
import java.util.Collection;
import jnr.ffi.NativeType;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ToNativeType;

public class ParameterType
extends ToNativeType {
    public ParameterType(Class javaType, NativeType nativeType, Collection<Annotation> annotations, ToNativeConverter toNativeConverter, ToNativeContext toNativeContext) {
        super(javaType, nativeType, annotations, toNativeConverter, toNativeContext);
    }
}

