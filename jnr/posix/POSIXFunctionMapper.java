
package jnr.posix;

import jnr.ffi.mapper.FunctionMapper;

@Deprecated
final class POSIXFunctionMapper
implements FunctionMapper {
    public static final FunctionMapper INSTANCE = new POSIXFunctionMapper();

    private POSIXFunctionMapper() {
    }

    @Override
    public String mapFunctionName(String name, FunctionMapper.Context ctx) {
        if (ctx.getLibrary().getName().equals("msvcrt") && (name.equals("getpid") || name.equals("chmod"))) {
            name = "_" + name;
        }
        return name;
    }
}

