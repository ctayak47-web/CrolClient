
package jnr.ffi.provider;

import jnr.ffi.mapper.FunctionMapper;

public class IdentityFunctionMapper
implements FunctionMapper {
    public static FunctionMapper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public String mapFunctionName(String functionName, FunctionMapper.Context context) {
        return functionName;
    }

    private static final class SingletonHolder {
        public static final FunctionMapper INSTANCE = new IdentityFunctionMapper();

        private SingletonHolder() {
        }
    }
}

