
package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;
import com.kenai.jffi.Internals;
import com.kenai.jffi.PageManager;
import com.kenai.jffi.Platform;
import jnr.a64asm.Assembler_A64;
import jnr.a64asm.CPU_A64;
import jnr.ffi.CallingConvention;
import jnr.ffi.Runtime;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.jffi.ARM_64StubCompiler;
import jnr.ffi.provider.jffi.X86_32StubCompiler;
import jnr.ffi.provider.jffi.X86_64StubCompiler;
import jnr.x86asm.Assembler;
import jnr.x86asm.CPU;

abstract class StubCompiler {
    static final long errnoFunctionAddress = StubCompiler.getErrnoSaveFunction();
    static final boolean hasPageManager = StubCompiler.hasPageManager();
    static final boolean hasAssembler = StubCompiler.hasAssembler();

    StubCompiler() {
    }

    public static StubCompiler newCompiler(Runtime runtime) {
        if (errnoFunctionAddress != 0L && hasPageManager && hasAssembler) {
            switch (Platform.getPlatform().getCPU()) {
                case I386: {
                    if (Platform.getPlatform().getOS() == Platform.OS.WINDOWS) break;
                    return new X86_32StubCompiler(runtime);
                }
                case X86_64: {
                    if (Platform.getPlatform().getOS() == Platform.OS.WINDOWS) break;
                    return new X86_64StubCompiler(runtime);
                }
                case AARCH64: {
                    if (Platform.getPlatform().getOS() == Platform.OS.WINDOWS) break;
                    return new ARM_64StubCompiler(runtime);
                }
            }
        }
        return new DummyStubCompiler();
    }

    abstract boolean canCompile(ResultType var1, ParameterType[] var2, CallingConvention var3);

    abstract void compile(Function var1, String var2, ResultType var3, ParameterType[] var4, Class var5, Class[] var6, CallingConvention var7, boolean var8);

    abstract void attach(Class var1);

    private static long getErrnoSaveFunction() {
        try {
            return Internals.getErrnoSaveFunction();
        }
        catch (Throwable t) {
            return 0L;
        }
    }

    private static boolean hasPageManager() {
        try {
            long page = PageManager.getInstance().allocatePages(1, 3);
            PageManager.getInstance().freePages(page, 1);
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }

    private static boolean hasAssembler() {
        try {
            switch (Platform.getPlatform().getCPU()) {
                case I386: {
                    new Assembler(CPU.X86_32);
                    return true;
                }
                case X86_64: {
                    new Assembler(CPU.X86_64);
                    return true;
                }
                case AARCH64: {
                    new Assembler_A64(CPU_A64.A64);
                    return true;
                }
            }
            return false;
        }
        catch (Throwable t) {
            return false;
        }
    }

    static final class DummyStubCompiler
    extends StubCompiler {
        DummyStubCompiler() {
        }

        @Override
        boolean canCompile(ResultType returnType, ParameterType[] parameterTypes, CallingConvention convention) {
            return false;
        }

        @Override
        void compile(Function function, String name, ResultType returnType, ParameterType[] parameterTypes, Class resultClass, Class[] parameterClasses, CallingConvention convention, boolean saveErrno) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        void attach(Class clazz) {
        }
    }
}

