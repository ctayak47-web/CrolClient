
package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jnr.ffi.Library;
import jnr.ffi.mapper.SimpleFunctionMapper;

public interface FunctionMapper {
    public static final FunctionMapper IDENTITY = new FunctionMapper(){

        @Override
        public String mapFunctionName(String functionName, Context context) {
            return functionName;
        }
    };

    public String mapFunctionName(String var1, Context var2);

    public static final class Builder {
        private final Map<String, String> functionNameMap = Collections.synchronizedMap(new HashMap());

        public Builder map(String javaName, String nativeFunction) {
            this.functionNameMap.put(javaName, nativeFunction);
            return this;
        }

        public FunctionMapper build() {
            return new SimpleFunctionMapper(this.functionNameMap);
        }
    }

    public static interface Context {
        @Deprecated
        public Library getLibrary();

        public boolean isSymbolPresent(String var1);

        public Collection<Annotation> getAnnotations();
    }
}

