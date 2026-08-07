
package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.util.Collection;
import jnr.ffi.Runtime;

public interface ToNativeContext {
    public Collection<Annotation> getAnnotations();

    public Runtime getRuntime();
}

