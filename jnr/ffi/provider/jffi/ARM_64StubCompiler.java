
package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;
import jnr.a64asm.Assembler_A64;
import jnr.a64asm.CPU_A64;
import jnr.a64asm.Immediate;
import jnr.a64asm.Offset;
import jnr.a64asm.Post_index;
import jnr.a64asm.Pre_index;
import jnr.a64asm.Register;
import jnr.a64asm.Shift;
import jnr.ffi.CallingConvention;
import jnr.ffi.Runtime;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.jffi.AbstractA64StubCompiler;
import jnr.ffi.provider.jffi.CodegenUtils;

final class ARM_64StubCompiler
extends AbstractA64StubCompiler {
    static final Register[] srcRegisters32 = new Register[]{Register.gpw(2), Register.gpw(3), Register.gpw(4), Register.gpw(5), Register.gpw(6), Register.gpw(7)};
    static final Register[] srcRegisters64 = new Register[]{Register.gpb(2), Register.gpb(3), Register.gpb(4), Register.gpb(5), Register.gpb(6), Register.gpb(7)};
    static final Register[] dstRegisters32 = new Register[]{Register.gpw(0), Register.gpw(1), Register.gpw(2), Register.gpw(3), Register.gpw(4), Register.gpw(5), Register.gpw(6), Register.gpw(7)};
    static final Register[] dstRegisters64 = new Register[]{Register.gpb(0), Register.gpb(1), Register.gpb(2), Register.gpb(3), Register.gpb(4), Register.gpb(5), Register.gpb(6), Register.gpb(7)};

    ARM_64StubCompiler(Runtime runtime) {
        super(runtime);
    }

    @Override
    boolean canCompile(ResultType returnType, ParameterType[] parameterTypes, CallingConvention convention) {
        if (convention != CallingConvention.DEFAULT) {
            return false;
        }
        switch (returnType.getNativeType()) {
            case VOID: 
            case SCHAR: 
            case UCHAR: 
            case SSHORT: 
            case USHORT: 
            case SINT: 
            case UINT: 
            case SLONG: 
            case ULONG: 
            case SLONGLONG: 
            case ULONGLONG: 
            case FLOAT: 
            case DOUBLE: 
            case ADDRESS: {
                break;
            }
            default: {
                return false;
            }
        }
        int fCount = 0;
        int iCount = 0;
        block7: for (ParameterType t : parameterTypes) {
            switch (t.getNativeType()) {
                case SCHAR: 
                case UCHAR: 
                case SSHORT: 
                case USHORT: 
                case SINT: 
                case UINT: 
                case SLONG: 
                case ULONG: 
                case SLONGLONG: 
                case ULONGLONG: 
                case ADDRESS: {
                    ++iCount;
                    continue block7;
                }
                case FLOAT: 
                case DOUBLE: {
                    ++fCount;
                    continue block7;
                }
                default: {
                    return false;
                }
            }
        }
        return iCount <= 6 && fCount <= 8;
    }

    @Override
    final void compile(Function function, String name, ResultType resultType, ParameterType[] parameterTypes, Class resultClass, Class[] parameterClasses, CallingConvention convention, boolean saveErrno) {
        Shift sh;
        int count;
        Assembler_A64 a = new Assembler_A64(CPU_A64.A64);
        int iCount = ARM_64StubCompiler.iCount(parameterTypes);
        int fCount = ARM_64StubCompiler.fCount(parameterTypes);
        Pre_index pindex = new Pre_index(Register.gpb(31), Immediate.imm(-32L));
        a.stp(Register.gpb(29), Register.gpb(30), pindex);
        a.mov(Register.gpb(29), Register.gpb(31));
        boolean canJumpToTarget = !saveErrno & iCount <= 6 & fCount <= 8;
        switch (resultType.getNativeType()) {
            case SINT: 
            case UINT: {
                canJumpToTarget &= Integer.TYPE == resultClass;
                break;
            }
            case SLONGLONG: 
            case ULONGLONG: {
                canJumpToTarget &= Long.TYPE == resultClass;
                break;
            }
            case FLOAT: {
                canJumpToTarget &= Float.TYPE == resultClass;
                break;
            }
            case DOUBLE: {
                canJumpToTarget &= Double.TYPE == resultClass;
                break;
            }
            case VOID: {
                break;
            }
            default: {
                canJumpToTarget = false;
            }
        }
        block35: for (int i = 0; i < Math.min(iCount, 6); ++i) {
            switch (parameterTypes[i].getNativeType()) {
                case SCHAR: {
                    a.sxtb(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    continue block35;
                }
                case UCHAR: {
                    a.uxtb(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    continue block35;
                }
                case SSHORT: {
                    a.sxth(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    continue block35;
                }
                case USHORT: {
                    a.uxth(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    continue block35;
                }
                case SINT: {
                    a.sxtw(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    continue block35;
                }
                case UINT: {
                    a.uxtw(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    continue block35;
                }
                default: {
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                }
            }
        }
        if (iCount > 6) {
            throw new IllegalArgumentException("integer argument count > 6");
        }
        if (fCount > 8) {
            throw new IllegalArgumentException("float argument count > 8");
        }
        Offset offset = new Offset(Register.gpb(29), Immediate.imm(16L));
        long function_addr = function.getFunctionAddress();
        short funn_addr_chunks = (short)(function_addr & 0xFFFFL);
        a.mov(Register.gpb(9), Immediate.imm(funn_addr_chunks));
        for (count = 1; count < 4; ++count) {
            sh = new Shift(1, 16 * count);
            funn_addr_chunks = (short)(function_addr >> 16 * count & 0xFFFFL);
            a.movk(Register.gpb(9), Immediate.imm(funn_addr_chunks), sh);
        }
        a.blr(Register.gpb(9));
        if (saveErrno) {
            switch (resultType.getNativeType()) {
                case VOID: {
                    break;
                }
                default: {
                    a.str(dstRegisters64[0], offset);
                }
            }
            function_addr = errnoFunctionAddress;
            funn_addr_chunks = (short)(function_addr & 0xFFFFL);
            a.mov(Register.gpb(9), Immediate.imm(funn_addr_chunks));
            for (count = 1; count < 4; ++count) {
                sh = new Shift(1, 16 * count);
                funn_addr_chunks = (short)(function_addr >> 16 * count & 0xFFFFL);
                a.movk(Register.gpb(9), Immediate.imm(funn_addr_chunks), sh);
            }
            a.blr(Register.gpb(9));
            switch (resultType.getNativeType()) {
                case VOID: {
                    break;
                }
                case SCHAR: {
                    a.ldrsb(dstRegisters64[0], offset);
                    break;
                }
                case UCHAR: {
                    a.ldrb(dstRegisters64[0], offset);
                    break;
                }
                case SSHORT: {
                    a.ldrsh(dstRegisters64[0], offset);
                    break;
                }
                case USHORT: {
                    a.ldrh(dstRegisters64[0], offset);
                    break;
                }
                case SINT: {
                    a.ldrsw(dstRegisters64[0], offset);
                    break;
                }
                case UINT: {
                    a.ldr(dstRegisters64[0], offset);
                    break;
                }
                default: {
                    a.ldr(dstRegisters64[0], offset);
                    break;
                }
            }
        } else {
            switch (resultType.getNativeType()) {
                case SCHAR: {
                    a.sxtb(dstRegisters64[0], dstRegisters32[0]);
                    break;
                }
                case UCHAR: {
                    a.uxtb(dstRegisters64[0], dstRegisters32[0]);
                    break;
                }
                case SSHORT: {
                    a.sxth(dstRegisters64[0], dstRegisters32[0]);
                    break;
                }
                case USHORT: {
                    a.uxth(dstRegisters64[0], dstRegisters32[0]);
                    break;
                }
                case SINT: {
                    a.sxtw(dstRegisters64[0], dstRegisters32[0]);
                    break;
                }
                case UINT: {
                    a.uxtw(dstRegisters64[0], dstRegisters32[0]);
                }
            }
        }
        Post_index posindex = new Post_index(Register.gpb(31), Immediate.imm(32L));
        a.ldp(Register.gpb(29), Register.gpb(30), posindex);
        a.ret(null);
        this.stubs_A64.add(new AbstractA64StubCompiler.Stub(name, CodegenUtils.sig(resultClass, parameterClasses), a));
    }

    static int fCount(ParameterType[] parameterTypes) {
        int fCount = 0;
        for (ParameterType t : parameterTypes) {
            switch (t.getNativeType()) {
                case FLOAT: 
                case DOUBLE: {
                    ++fCount;
                }
            }
        }
        return fCount;
    }

    static int iCount(ParameterType[] parameterTypes) {
        int iCount = 0;
        for (ParameterType t : parameterTypes) {
            switch (t.getNativeType()) {
                case SCHAR: 
                case UCHAR: 
                case SSHORT: 
                case USHORT: 
                case SINT: 
                case UINT: 
                case SLONG: 
                case ULONG: 
                case SLONGLONG: 
                case ULONGLONG: 
                case ADDRESS: {
                    ++iCount;
                }
            }
        }
        return iCount;
    }
}

