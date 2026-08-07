
package jnr.ffi.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import jnr.ffi.mapper.FunctionMapper;

public final class CompositeFunctionMapper
implements FunctionMapper {
    private final Collection<FunctionMapper> functionMappers;

    public CompositeFunctionMapper(Collection<FunctionMapper> functionMappers) {
        this.functionMappers = Collections.unmodifiableList(new ArrayList<FunctionMapper>(functionMappers));
    }

    @Override
    public String mapFunctionName(String functionName, FunctionMapper.Context context) {
        for (FunctionMapper functionMapper : this.functionMappers) {
            String mappedName = functionMapper.mapFunctionName(functionName, context);
            if (mappedName == functionName) continue;
            return mappedName;
        }
        return functionName;
    }
}

