
package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.util.Collection;
import jnr.ffi.Library;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.provider.jffi.NativeLibrary;

public final class NativeFunctionMapperContext
implements FunctionMapper.Context {
    private final NativeLibrary library;
    private final Collection<Annotation> annotations;

    public NativeFunctionMapperContext(NativeLibrary library, Collection<Annotation> annotations) {
        this.library = library;
        this.annotations = annotations;
    }

    @Override
    public Library getLibrary() {
        return null;
    }

    @Override
    public boolean isSymbolPresent(String name) {
        return this.library.getSymbolAddress(name) != 0L;
    }

    @Override
    public Collection<Annotation> getAnnotations() {
        return this.annotations;
    }
}

