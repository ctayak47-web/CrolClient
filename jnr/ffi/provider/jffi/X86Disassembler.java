
package jnr.ffi.provider.jffi;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.DefaultTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.jffi.AbstractAsmLibraryInterface;
import jnr.ffi.provider.jffi.NoTrace;
import jnr.ffi.provider.jffi.NoX86;
import jnr.ffi.types.intptr_t;
import jnr.ffi.types.size_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

class X86Disassembler {
    private final UDis86 udis86;
    final Pointer ud;

    static UDis86 loadUDis86() {
        DefaultTypeMapper typeMapper = new DefaultTypeMapper();
        typeMapper.put(X86Disassembler.class, new X86DisassemblerConverter());
        return LibraryLoader.create(UDis86.class).library("udis86").search("/usr/local/lib").search("/opt/local/lib").search("/usr/lib").mapper(typeMapper).load();
    }

    static boolean isAvailable() {
        try {
            return SingletonHolder.INSTANCE != null;
        }
        catch (Throwable ex) {
            return false;
        }
    }

    static X86Disassembler create() {
        return new X86Disassembler(SingletonHolder.INSTANCE);
    }

    private X86Disassembler(UDis86 udis86) {
        this.udis86 = udis86;
        this.ud = Memory.allocateDirect(Runtime.getRuntime(udis86), 1024, true);
        this.udis86.ud_init(this.ud);
    }

    public void setSyntax(Syntax syntax) {
        this.udis86.ud_set_syntax(this, syntax == Syntax.INTEL ? SingletonHolder.intel : SingletonHolder.att);
    }

    public void setMode(Mode mode) {
        this.udis86.ud_set_mode(this, mode == Mode.I386 ? 32 : 64);
    }

    public void setInputBuffer(Pointer buffer, int size) {
        this.udis86.ud_set_input_buffer(this, buffer, size);
    }

    public boolean disassemble() {
        return this.udis86.ud_disassemble(this) != 0;
    }

    public String insn() {
        return this.udis86.ud_insn_asm(this);
    }

    public long offset() {
        return this.udis86.ud_insn_off(this);
    }

    public String hex() {
        return this.udis86.ud_insn_hex(this);
    }

    @ToNativeConverter.NoContext
    public static final class X86DisassemblerConverter
    implements ToNativeConverter<X86Disassembler, Pointer> {
        @Override
        public Pointer toNative(X86Disassembler value, ToNativeContext context) {
            return value.ud;
        }

        @Override
        public Class<Pointer> nativeType() {
            return Pointer.class;
        }
    }

    @NoX86
    @NoTrace
    public static interface UDis86 {
        public void ud_init(Pointer var1);

        public void ud_set_mode(X86Disassembler var1, @u_int8_t int var2);

        public void ud_set_pc(X86Disassembler var1, @u_int64_t int var2);

        public void ud_set_input_buffer(X86Disassembler var1, Pointer var2, @size_t long var3);

        public void ud_set_vendor(X86Disassembler var1, int var2);

        public void ud_set_syntax(X86Disassembler var1, @intptr_t long var2);

        public void ud_input_skip(X86Disassembler var1, @size_t long var2);

        public int ud_input_end(X86Disassembler var1);

        public int ud_decode(X86Disassembler var1);

        public int ud_disassemble(X86Disassembler var1);

        public String ud_insn_asm(X86Disassembler var1);

        @intptr_t
        public long ud_insn_ptr(X86Disassembler var1);

        @u_int64_t
        public long ud_insn_off(X86Disassembler var1);

        public String ud_insn_hex(X86Disassembler var1);

        public int ud_insn_len(X86Disassembler var1);
    }

    static final class SingletonHolder {
        static final UDis86 INSTANCE = X86Disassembler.loadUDis86();
        static final long intel = ((AbstractAsmLibraryInterface)((Object)INSTANCE)).getLibrary().findSymbolAddress("ud_translate_intel");
        static final long att = ((AbstractAsmLibraryInterface)((Object)INSTANCE)).getLibrary().findSymbolAddress("ud_translate_att");

        SingletonHolder() {
        }
    }

    public static enum Syntax {
        INTEL,
        ATT;

    }

    public static enum Mode {
        I386,
        X86_64;

    }
}

