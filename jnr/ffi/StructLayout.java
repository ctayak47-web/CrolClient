
package jnr.ffi;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import jnr.ffi.TypeAlias;
import jnr.ffi.util.EnumMapper;

public class StructLayout
extends Type {
    static final Charset ASCII = Charset.forName("ASCII");
    static final Charset UTF8 = Charset.forName("UTF-8");
    private final Runtime runtime;
    private final boolean isUnion = false;
    private boolean resetIndex = false;
    StructLayout enclosing = null;
    int offset = 0;
    int size = 0;
    int alignment = 1;
    int paddedSize = 0;

    protected StructLayout(Runtime runtime) {
        this.runtime = runtime;
    }

    protected StructLayout(Runtime runtime, int structSize) {
        this.runtime = runtime;
        this.size = this.paddedSize = structSize;
    }

    public final Runtime getRuntime() {
        return this.runtime;
    }

    @Override
    public final int size() {
        return this.paddedSize;
    }

    @Override
    public final int alignment() {
        return this.alignment;
    }

    public final int offset() {
        return this.offset;
    }

    @Override
    public NativeType getNativeType() {
        return NativeType.STRUCT;
    }

    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
        sb.append(this.getClass().getSimpleName()).append(" { \n");
        java.lang.String fieldPrefix = "    ";
        for (java.lang.reflect.Field field : fields) {
            try {
                sb.append("    ").append('\n');
            }
            catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static int align(int offset, int alignment) {
        return offset + alignment - 1 & ~(alignment - 1);
    }

    protected final int addField(int size, int align) {
        int off = this.resetIndex ? 0 : StructLayout.align(this.size, align);
        this.size = Math.max(this.size, off + size);
        this.alignment = Math.max(this.alignment, align);
        this.paddedSize = StructLayout.align(this.size, this.alignment);
        return off;
    }

    protected final int addField(int size, int align, Offset offset) {
        this.size = Math.max(this.size, offset.intValue() + size);
        this.alignment = Math.max(this.alignment, align);
        this.paddedSize = StructLayout.align(this.size, this.alignment);
        return offset.intValue();
    }

    protected final int addField(Type t) {
        return this.addField(t.size(), t.alignment());
    }

    protected final int addField(Type t, Offset offset) {
        return this.addField(t.size(), t.alignment(), offset);
    }

    protected final Offset at(int offset) {
        return new Offset(offset);
    }

    protected final void arrayBegin() {
        this.resetIndex = false;
    }

    protected final void arrayEnd() {
        this.resetIndex = false;
    }

    protected <T extends Field> T[] array(T[] array) {
        this.arrayBegin();
        try {
            Class<?> arrayClass = array.getClass().getComponentType();
            Constructor<?> ctor = arrayClass.getDeclaredConstructor(arrayClass.getEnclosingClass());
            Object[] parameters = new Object[]{this};
            for (int i = 0; i < array.length; ++i) {
                array[i] = (Field)ctor.newInstance(parameters);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        this.arrayEnd();
        return array;
    }

    protected final <T extends StructLayout> T inner(T structLayout) {
        structLayout.enclosing = this;
        structLayout.offset = StructLayout.align(this.size, structLayout.alignment);
        this.size = structLayout.offset + structLayout.size;
        this.paddedSize = StructLayout.align(this.size, this.alignment());
        return structLayout;
    }

    protected final <T> Function<T> function(Class<T> closureClass) {
        return new Function<T>(closureClass);
    }

    protected final <T> Function<T> function(Class<T> closureClass, Offset offset) {
        return new Function<T>(closureClass, offset);
    }

    protected static final class Offset
    extends Number {
        private final int offset;

        public Offset(int offset) {
            this.offset = offset;
        }

        @Override
        public int intValue() {
            return this.offset;
        }

        @Override
        public long longValue() {
            return this.offset;
        }

        @Override
        public float floatValue() {
            return this.offset;
        }

        @Override
        public double doubleValue() {
            return this.offset;
        }
    }

    protected abstract class Field {
        private final int offset;

        protected Field(int offset) {
            this.offset = offset;
        }

        public final StructLayout enclosing() {
            return StructLayout.this;
        }

        public final long offset() {
            return this.offset + StructLayout.this.offset;
        }
    }

    protected final class Function<T>
    extends AbstractField {
        private final Class<? extends T> closureClass;
        private T instance;

        public Function(Class<? extends T> closureClass) {
            super(NativeType.ADDRESS);
            this.closureClass = closureClass;
        }

        public Function(Class<? extends T> closureClass, Offset offset) {
            super(NativeType.ADDRESS, offset);
            this.closureClass = closureClass;
        }

        public final void set(jnr.ffi.Pointer ptr, T value) {
            this.instance = value;
            ptr.putPointer(this.offset(), StructLayout.this.getRuntime().getClosureManager().getClosurePointer(this.closureClass, this.instance));
        }
    }

    public final class rlim_t
    extends IntegerAlias {
        public rlim_t() {
            super(TypeAlias.rlim_t);
        }

        public rlim_t(Offset offset) {
            super(TypeAlias.rlim_t, offset);
        }
    }

    public final class socklen_t
    extends IntegerAlias {
        public socklen_t() {
            super(TypeAlias.socklen_t);
        }

        public socklen_t(Offset offset) {
            super(TypeAlias.socklen_t, offset);
        }
    }

    public final class sa_family_t
    extends IntegerAlias {
        public sa_family_t() {
            super(TypeAlias.sa_family_t);
        }

        public sa_family_t(Offset offset) {
            super(TypeAlias.sa_family_t, offset);
        }
    }

    public final class fsfilcnt_t
    extends IntegerAlias {
        public fsfilcnt_t() {
            super(TypeAlias.fsfilcnt_t);
        }

        public fsfilcnt_t(Offset offset) {
            super(TypeAlias.fsfilcnt_t, offset);
        }
    }

    public final class fsblkcnt_t
    extends IntegerAlias {
        public fsblkcnt_t() {
            super(TypeAlias.fsblkcnt_t);
        }

        public fsblkcnt_t(Offset offset) {
            super(TypeAlias.fsblkcnt_t, offset);
        }
    }

    public final class time_t
    extends IntegerAlias {
        public time_t() {
            super(TypeAlias.time_t);
        }

        public time_t(Offset offset) {
            super(TypeAlias.time_t, offset);
        }
    }

    public final class ssize_t
    extends IntegerAlias {
        public ssize_t() {
            super(TypeAlias.ssize_t);
        }

        public ssize_t(Offset offset) {
            super(TypeAlias.ssize_t, offset);
        }
    }

    public final class size_t
    extends IntegerAlias {
        public size_t() {
            super(TypeAlias.size_t);
        }

        public size_t(Offset offset) {
            super(TypeAlias.size_t, offset);
        }
    }

    public final class clock_t
    extends IntegerAlias {
        public clock_t() {
            super(TypeAlias.clock_t);
        }

        public clock_t(Offset offset) {
            super(TypeAlias.clock_t, offset);
        }
    }

    public final class uid_t
    extends IntegerAlias {
        public uid_t() {
            super(TypeAlias.uid_t);
        }

        public uid_t(Offset offset) {
            super(TypeAlias.uid_t, offset);
        }
    }

    public final class swblk_t
    extends IntegerAlias {
        public swblk_t() {
            super(TypeAlias.swblk_t);
        }

        public swblk_t(Offset offset) {
            super(TypeAlias.swblk_t, offset);
        }
    }

    public final class off_t
    extends IntegerAlias {
        public off_t() {
            super(TypeAlias.off_t);
        }

        public off_t(Offset offset) {
            super(TypeAlias.off_t, offset);
        }
    }

    public final class pid_t
    extends IntegerAlias {
        public pid_t() {
            super(TypeAlias.pid_t);
        }

        public pid_t(Offset offset) {
            super(TypeAlias.pid_t, offset);
        }
    }

    public final class id_t
    extends IntegerAlias {
        public id_t() {
            super(TypeAlias.id_t);
        }

        public id_t(Offset offset) {
            super(TypeAlias.id_t, offset);
        }
    }

    public final class nlink_t
    extends IntegerAlias {
        public nlink_t() {
            super(TypeAlias.nlink_t);
        }

        public nlink_t(Offset offset) {
            super(TypeAlias.nlink_t, offset);
        }
    }

    public final class mode_t
    extends IntegerAlias {
        public mode_t() {
            super(TypeAlias.mode_t);
        }

        public mode_t(Offset offset) {
            super(TypeAlias.mode_t, offset);
        }
    }

    public final class key_t
    extends IntegerAlias {
        public key_t() {
            super(TypeAlias.key_t);
        }

        public key_t(Offset offset) {
            super(TypeAlias.key_t, offset);
        }
    }

    public final class ino64_t
    extends IntegerAlias {
        public ino64_t() {
            super(TypeAlias.ino64_t);
        }

        public ino64_t(Offset offset) {
            super(TypeAlias.ino64_t, offset);
        }
    }

    public final class ino_t
    extends IntegerAlias {
        public ino_t() {
            super(TypeAlias.ino_t);
        }

        public ino_t(Offset offset) {
            super(TypeAlias.ino_t, offset);
        }
    }

    public final class in_port_t
    extends IntegerAlias {
        public in_port_t() {
            super(TypeAlias.in_port_t);
        }

        public in_port_t(Offset offset) {
            super(TypeAlias.in_port_t, offset);
        }
    }

    public final class in_addr_t
    extends IntegerAlias {
        public in_addr_t() {
            super(TypeAlias.in_addr_t);
        }

        public in_addr_t(Offset offset) {
            super(TypeAlias.in_addr_t, offset);
        }
    }

    public final class gid_t
    extends IntegerAlias {
        public gid_t() {
            super(TypeAlias.gid_t);
        }

        public gid_t(Offset offset) {
            super(TypeAlias.gid_t, offset);
        }
    }

    public final class blksize_t
    extends IntegerAlias {
        public blksize_t() {
            super(TypeAlias.blksize_t);
        }

        public blksize_t(Offset offset) {
            super(TypeAlias.blksize_t, offset);
        }
    }

    public final class blkcnt_t
    extends IntegerAlias {
        public blkcnt_t() {
            super(TypeAlias.blkcnt_t);
        }

        public blkcnt_t(Offset offset) {
            super(TypeAlias.blkcnt_t, offset);
        }
    }

    public final class dev_t
    extends IntegerAlias {
        public dev_t() {
            super(TypeAlias.dev_t);
        }

        public dev_t(Offset offset) {
            super(TypeAlias.dev_t, offset);
        }
    }

    public final class caddr_t
    extends IntegerAlias {
        public caddr_t() {
            super(TypeAlias.caddr_t);
        }

        public caddr_t(Offset offset) {
            super(TypeAlias.caddr_t, offset);
        }
    }

    public final class uintptr_t
    extends IntegerAlias {
        public uintptr_t() {
            super(TypeAlias.uintptr_t);
        }

        public uintptr_t(Offset offset) {
            super(TypeAlias.uintptr_t, offset);
        }
    }

    public final class intptr_t
    extends IntegerAlias {
        public intptr_t() {
            super(TypeAlias.intptr_t);
        }

        public intptr_t(Offset offset) {
            super(TypeAlias.intptr_t, offset);
        }
    }

    public final class u_int64_t
    extends IntegerAlias {
        public u_int64_t() {
            super(TypeAlias.u_int64_t);
        }

        public u_int64_t(Offset offset) {
            super(TypeAlias.u_int64_t, offset);
        }
    }

    public final class int64_t
    extends IntegerAlias {
        public int64_t() {
            super(TypeAlias.int64_t);
        }

        public int64_t(Offset offset) {
            super(TypeAlias.int64_t, offset);
        }
    }

    public final class u_int32_t
    extends IntegerAlias {
        public u_int32_t() {
            super(TypeAlias.u_int32_t);
        }

        public u_int32_t(Offset offset) {
            super(TypeAlias.u_int32_t, offset);
        }
    }

    public final class int32_t
    extends IntegerAlias {
        public int32_t() {
            super(TypeAlias.int32_t);
        }

        public int32_t(Offset offset) {
            super(TypeAlias.int32_t, offset);
        }
    }

    public final class u_int16_t
    extends IntegerAlias {
        public u_int16_t() {
            super(TypeAlias.u_int16_t);
        }

        public u_int16_t(Offset offset) {
            super(TypeAlias.u_int16_t, offset);
        }
    }

    public final class int16_t
    extends IntegerAlias {
        public int16_t() {
            super(TypeAlias.int16_t);
        }

        public int16_t(Offset offset) {
            super(TypeAlias.int16_t, offset);
        }
    }

    public final class u_int8_t
    extends IntegerAlias {
        public u_int8_t() {
            super(TypeAlias.u_int8_t);
        }

        public u_int8_t(Offset offset) {
            super(TypeAlias.u_int8_t, offset);
        }
    }

    public final class int8_t
    extends IntegerAlias {
        public int8_t() {
            super(TypeAlias.int8_t);
        }

        public int8_t(Offset offset) {
            super(TypeAlias.int8_t, offset);
        }
    }

    protected final class Padding
    extends AbstractField {
        public Padding(Type type, int length) {
            super(type.size() * length, type.alignment());
        }

        public Padding(Type type, int length, Offset offset) {
            super(type.size() * length, type.alignment(), offset);
        }

        public Padding(NativeType type, int length) {
            this(this$0.getRuntime().findType(type), length);
        }

        public Padding(NativeType type, int length, Offset offset) {
            this(this$0.getRuntime().findType(type), length);
        }
    }

    public class AsciiStringRef
    extends UTFStringRef {
        public AsciiStringRef(int size) {
            super(size, ASCII);
        }

        public AsciiStringRef(int size, Offset offset) {
            super(size, ASCII, offset);
        }

        public AsciiStringRef() {
            super(Integer.MAX_VALUE, ASCII);
        }
    }

    public class UTF8StringRef
    extends UTFStringRef {
        public UTF8StringRef(int size) {
            super(size, UTF8);
        }

        public UTF8StringRef(int size, Offset offset) {
            super(size, UTF8, offset);
        }

        public UTF8StringRef() {
            super(Integer.MAX_VALUE, UTF8);
        }
    }

    public class UTFStringRef
    extends String {
        private jnr.ffi.Pointer valueHolder;

        public UTFStringRef(int length, Charset cs) {
            super(StructLayout.this.getRuntime().findType(NativeType.ADDRESS).size(), StructLayout.this.getRuntime().findType(NativeType.ADDRESS).alignment(), length, cs);
        }

        public UTFStringRef(int length, Charset cs, Offset offset) {
            super(StructLayout.this.getRuntime().findType(NativeType.ADDRESS).size(), StructLayout.this.getRuntime().findType(NativeType.ADDRESS).alignment(), offset, length, cs);
        }

        public UTFStringRef(Charset cs) {
            this(Integer.MAX_VALUE, cs);
        }

        @Override
        protected jnr.ffi.Pointer getStringMemory(jnr.ffi.Pointer ptr) {
            return ptr.getPointer(this.offset(), this.length());
        }

        @Override
        public final java.lang.String get(jnr.ffi.Pointer ptr) {
            jnr.ffi.Pointer memory = this.getStringMemory(ptr);
            return memory != null ? memory.getString(0L, this.length, this.charset) : null;
        }

        @Override
        public final void set(jnr.ffi.Pointer ptr, java.lang.String value) {
            if (value != null) {
                this.valueHolder = StructLayout.this.getRuntime().getMemoryManager().allocateDirect(this.length() * 4);
                this.valueHolder.putString(0L, value, this.length() * 4, this.charset);
                ptr.putPointer(this.offset(), this.valueHolder);
            } else {
                this.valueHolder = null;
                ptr.putAddress(this.offset(), 0L);
            }
        }
    }

    public class AsciiString
    extends UTFString {
        public AsciiString(int size) {
            super(size, ASCII);
        }

        public AsciiString(int size, Offset offset) {
            super(size, ASCII, offset);
        }
    }

    public class UTF8String
    extends UTFString {
        public UTF8String(int size) {
            super(size, UTF8);
        }

        public UTF8String(int size, Offset offset) {
            super(size, UTF8, offset);
        }
    }

    public class UTFString
    extends String {
        public UTFString(int length, Charset cs) {
            super(length, 1, length, cs);
        }

        public UTFString(int length, Charset cs, Offset offset) {
            super(length, 1, offset, length, cs);
        }

        @Override
        protected jnr.ffi.Pointer getStringMemory(jnr.ffi.Pointer ptr) {
            return ptr.slice(this.offset(), this.length());
        }

        @Override
        public final java.lang.String get(jnr.ffi.Pointer ptr) {
            return this.getStringMemory(ptr).getString(0L, this.length, this.charset);
        }

        @Override
        public final void set(jnr.ffi.Pointer ptr, java.lang.String value) {
            this.getStringMemory(ptr).putString(0L, value, this.length, this.charset);
        }
    }

    public abstract class String
    extends AbstractField {
        protected final Charset charset;
        protected final int length;

        protected String(int size, int align, int length, Charset cs) {
            super(size, align);
            this.length = length;
            this.charset = cs;
        }

        protected String(int size, int align, Offset offset, int length, Charset cs) {
            super(size, align, offset);
            this.length = length;
            this.charset = cs;
        }

        public final int length() {
            return this.length;
        }

        protected abstract jnr.ffi.Pointer getStringMemory(jnr.ffi.Pointer var1);

        public abstract java.lang.String get(jnr.ffi.Pointer var1);

        public abstract void set(jnr.ffi.Pointer var1, java.lang.String var2);

        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    public class Enum<T extends java.lang.Enum<T>>
    extends Enum32<T> {
        public Enum(Class<T> enumClass) {
            super(enumClass);
        }

        public Enum(Class<T> enumClass, Offset offset) {
            super(enumClass, offset);
        }
    }

    public class EnumLong<E extends java.lang.Enum<E>>
    extends EnumField<E> {
        public EnumLong(Class<E> enumClass) {
            super(NativeType.SLONG, enumClass);
        }

        public EnumLong(Class<E> enumClass, Offset offset) {
            super(NativeType.SLONG, enumClass, offset);
        }

        public final void set(jnr.ffi.Pointer ptr, E value) {
            ptr.putNativeLong(this.offset(), this.enumMapper.intValue((java.lang.Enum)value));
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putNativeLong(this.offset(), value.longValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.longValue(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return ptr.getNativeLong(this.offset());
        }
    }

    public class Enum64<E extends java.lang.Enum<E>>
    extends EnumField<E> {
        public Enum64(Class<E> enumClass) {
            super(NativeType.SLONGLONG, enumClass);
        }

        public Enum64(Class<E> enumClass, Offset offset) {
            super(NativeType.SLONGLONG, enumClass, offset);
        }

        public final void set(jnr.ffi.Pointer ptr, E value) {
            ptr.putLongLong(this.offset(), this.enumMapper.intValue((java.lang.Enum)value));
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putLongLong(this.offset(), value.longValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.longValue(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return ptr.getLongLong(this.offset());
        }
    }

    public class Enum32<E extends java.lang.Enum<E>>
    extends EnumField<E> {
        public Enum32(Class<E> enumClass) {
            super(NativeType.SINT, enumClass);
        }

        public Enum32(Class<E> enumClass, Offset offset) {
            super(NativeType.SINT, enumClass, offset);
        }

        public void set(jnr.ffi.Pointer ptr, E value) {
            ptr.putInt(this.offset(), this.enumMapper.intValue((java.lang.Enum)value));
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putInt(this.offset(), value.intValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return ptr.getInt(this.offset());
        }
    }

    public class Enum16<E extends java.lang.Enum<E>>
    extends EnumField<E> {
        public Enum16(Class<E> enumClass) {
            super(NativeType.SSHORT, enumClass);
        }

        public Enum16(Class<E> enumClass, Offset offset) {
            super(NativeType.SSHORT, enumClass, offset);
        }

        public void set(jnr.ffi.Pointer ptr, E value) {
            ptr.putShort(this.offset(), (short)this.enumMapper.intValue((java.lang.Enum)value));
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putShort(this.offset(), value.shortValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return ptr.getShort(this.offset());
        }
    }

    public class Enum8<E extends java.lang.Enum<E>>
    extends EnumField<E> {
        public Enum8(Class<E> enumClass) {
            super(NativeType.SCHAR, enumClass);
        }

        public Enum8(Class<E> enumClass, Offset offset) {
            super(NativeType.SCHAR, enumClass, offset);
        }

        public final void set(jnr.ffi.Pointer ptr, E value) {
            ptr.putByte(this.offset(), (byte)this.enumMapper.intValue((java.lang.Enum)value));
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putByte(this.offset(), value.byteValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return ptr.getByte(this.offset());
        }
    }

    protected abstract class EnumField<E extends java.lang.Enum<E>>
    extends NumberField {
        protected final Class<E> enumClass;
        protected final EnumMapper enumMapper;

        public EnumField(NativeType type, Class<E> enumClass) {
            super(type);
            this.enumClass = enumClass;
            this.enumMapper = EnumMapper.getInstance(enumClass);
        }

        public EnumField(NativeType type, Class<E> enumClass, Offset offset) {
            super(type, offset);
            this.enumClass = enumClass;
            this.enumMapper = EnumMapper.getInstance(enumClass);
        }

        public E get(jnr.ffi.Pointer ptr) {
            return (E)((java.lang.Enum)this.enumClass.cast(this.enumMapper.valueOf(this.intValue(ptr))));
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return ((java.lang.Enum)this.get(ptr)).toString();
        }
    }

    public class Pointer
    extends NumberField {
        public Pointer() {
            super(NativeType.ADDRESS);
        }

        public Pointer(Offset offset) {
            super(NativeType.ADDRESS, offset);
        }

        public final jnr.ffi.Pointer get(jnr.ffi.Pointer ptr) {
            return ptr.getPointer(this.offset());
        }

        public final int size() {
            return StructLayout.this.getRuntime().findType(NativeType.ADDRESS).size();
        }

        public final void set(jnr.ffi.Pointer ptr, jnr.ffi.Pointer value) {
            ptr.putPointer(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putAddress(this.offset(), value.longValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)ptr.getAddress(this.offset());
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return ptr.getAddress(this.offset());
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return this.get(ptr).toString();
        }
    }

    public final class Double
    extends NumberField {
        public Double() {
            super(NativeType.DOUBLE);
        }

        public Double(Offset offset) {
            super(NativeType.DOUBLE, offset);
        }

        public final double get(jnr.ffi.Pointer ptr) {
            return ptr.getDouble(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, double value) {
            ptr.putDouble(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putDouble(this.offset(), value.doubleValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return (long)this.get(ptr);
        }

        @Override
        public final float floatValue(jnr.ffi.Pointer ptr) {
            return (float)this.get(ptr);
        }

        @Override
        public final double doubleValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.String.valueOf(this.get(ptr));
        }
    }

    public class Float
    extends NumberField {
        public Float() {
            super(NativeType.FLOAT);
        }

        public Float(Offset offset) {
            super(NativeType.FLOAT, offset);
        }

        public final float get(jnr.ffi.Pointer ptr) {
            return ptr.getFloat(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, float value) {
            ptr.putFloat(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putFloat(this.offset(), value.floatValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public final double doubleValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final float floatValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return (long)this.get(ptr);
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.String.valueOf(this.get(ptr));
        }
    }

    public class UnsignedLong
    extends NumberField {
        public UnsignedLong() {
            super(NativeType.ULONG);
        }

        public UnsignedLong(Offset offset) {
            super(NativeType.ULONG, offset);
        }

        public final long get(jnr.ffi.Pointer ptr) {
            long value = ptr.getNativeLong(this.offset());
            long mask = StructLayout.this.getRuntime().findType(NativeType.SLONG).size() == 4 ? 0xFFFFFFFFL : -1L;
            return value < 0L ? (value & mask) + mask + 1L : value;
        }

        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putNativeLong(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putNativeLong(this.offset(), value.longValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return Long.toString(this.get(ptr));
        }
    }

    public class SignedLong
    extends NumberField {
        public SignedLong() {
            super(NativeType.SLONG);
        }

        public SignedLong(Offset offset) {
            super(NativeType.SLONG, offset);
        }

        public final long get(jnr.ffi.Pointer ptr) {
            return ptr.getNativeLong(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putNativeLong(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putNativeLong(this.offset(), value.longValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return Long.toString(this.get(ptr));
        }
    }

    public class Unsigned64
    extends NumberField {
        public Unsigned64() {
            super(NativeType.ULONGLONG);
        }

        public Unsigned64(Offset offset) {
            super(NativeType.ULONGLONG, offset);
        }

        public final long get(jnr.ffi.Pointer ptr) {
            return ptr.getLongLong(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putLongLong(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putLongLong(this.offset(), value.longValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return Long.toString(this.get(ptr));
        }
    }

    public class Signed64
    extends NumberField {
        public Signed64() {
            super(NativeType.SLONGLONG);
        }

        public Signed64(Offset offset) {
            super(NativeType.SLONGLONG, offset);
        }

        public final long get(jnr.ffi.Pointer ptr) {
            return ptr.getLongLong(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putLongLong(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putLongLong(this.offset(), value.longValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final java.lang.String toString(jnr.ffi.Pointer ptr) {
            return Long.toString(this.get(ptr));
        }
    }

    public class Unsigned32
    extends NumberField {
        public Unsigned32() {
            super(NativeType.UINT);
        }

        public Unsigned32(Offset offset) {
            super(NativeType.SINT, offset);
        }

        public final long get(jnr.ffi.Pointer ptr) {
            long value = ptr.getInt(this.offset());
            return value < 0L ? (value & Integer.MAX_VALUE) + 0x80000000L : value;
        }

        public final void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putInt(this.offset(), (int)value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putInt(this.offset(), value.intValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public final long longValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    public class Signed32
    extends NumberField {
        public Signed32() {
            super(NativeType.SINT);
        }

        public Signed32(Offset offset) {
            super(NativeType.SINT, offset);
        }

        public final int get(jnr.ffi.Pointer ptr) {
            return ptr.getInt(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, int value) {
            ptr.putInt(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putInt(this.offset(), value.intValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    public class Unsigned16
    extends NumberField {
        public Unsigned16() {
            super(NativeType.USHORT);
        }

        public Unsigned16(Offset offset) {
            super(NativeType.USHORT, offset);
        }

        public final int get(jnr.ffi.Pointer ptr) {
            int value = ptr.getShort(this.offset());
            return value < 0 ? (value & Short.MAX_VALUE) + 32768 : value;
        }

        public final void set(jnr.ffi.Pointer ptr, int value) {
            ptr.putShort(this.offset(), (short)value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putShort(this.offset(), value.shortValue());
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    public class Signed16
    extends NumberField {
        public Signed16() {
            super(NativeType.SSHORT);
        }

        public Signed16(Offset offset) {
            super(NativeType.SSHORT, offset);
        }

        public final short get(jnr.ffi.Pointer ptr) {
            return ptr.getShort(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, short value) {
            ptr.putShort(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putShort(this.offset(), value.shortValue());
        }

        @Override
        public final short shortValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    public class Unsigned8
    extends NumberField {
        public Unsigned8() {
            super(NativeType.UCHAR);
        }

        public Unsigned8(Offset offset) {
            super(NativeType.UCHAR, offset);
        }

        public final short get(jnr.ffi.Pointer ptr) {
            short value = ptr.getByte(this.offset());
            return value < 0 ? (short)((value & 0x7F) + 128) : value;
        }

        public final void set(jnr.ffi.Pointer ptr, short value) {
            ptr.putByte(this.offset(), (byte)value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putByte(this.offset(), value.byteValue());
        }

        @Override
        public final short shortValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    public class Signed8
    extends NumberField {
        public Signed8() {
            super(NativeType.SCHAR);
        }

        public Signed8(Offset offset) {
            super(NativeType.SCHAR, offset);
        }

        public final byte get(jnr.ffi.Pointer ptr) {
            return ptr.getByte(this.offset());
        }

        public final void set(jnr.ffi.Pointer ptr, byte value) {
            ptr.putByte(this.offset(), value);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putByte(this.offset(), value.byteValue());
        }

        @Override
        public final byte byteValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final short shortValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }

        @Override
        public final int intValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    public abstract class IntegerAlias
    extends NumberField {
        protected IntegerAlias(TypeAlias type) {
            super(StructLayout.this.getRuntime().findType(type));
        }

        protected IntegerAlias(TypeAlias type, Offset offset) {
            super(StructLayout.this.getRuntime().findType(type), offset);
        }

        @Override
        public void set(jnr.ffi.Pointer ptr, Number value) {
            ptr.putInt(this.type, this.offset(), value.longValue());
        }

        public void set(jnr.ffi.Pointer ptr, long value) {
            ptr.putInt(this.type, this.offset(), value);
        }

        public final long get(jnr.ffi.Pointer ptr) {
            return ptr.getInt(this.type, this.offset());
        }

        @Override
        public int intValue(jnr.ffi.Pointer ptr) {
            return (int)this.get(ptr);
        }

        @Override
        public long longValue(jnr.ffi.Pointer ptr) {
            return this.get(ptr);
        }
    }

    protected abstract class NumberField
    extends Field {
        protected final Type type;

        protected NumberField(NativeType nativeType) {
            this(this$0.getRuntime().findType(nativeType));
        }

        protected NumberField(Type type) {
            super(StructLayout.this.addField(type));
            this.type = type;
        }

        protected NumberField(NativeType nativeType, Offset offset) {
            this(this$0.getRuntime().findType(nativeType), offset);
        }

        protected NumberField(Type type, Offset offset) {
            super(StructLayout.this.addField(type, offset));
            this.type = type;
        }

        public abstract void set(jnr.ffi.Pointer var1, Number var2);

        public double doubleValue(jnr.ffi.Pointer ptr) {
            return this.longValue(ptr);
        }

        public float floatValue(jnr.ffi.Pointer ptr) {
            return this.intValue(ptr);
        }

        public byte byteValue(jnr.ffi.Pointer ptr) {
            return (byte)this.intValue(ptr);
        }

        public short shortValue(jnr.ffi.Pointer ptr) {
            return (short)this.intValue(ptr);
        }

        public abstract int intValue(jnr.ffi.Pointer var1);

        public long longValue(jnr.ffi.Pointer ptr) {
            return this.intValue(ptr);
        }

        public java.lang.String toString(jnr.ffi.Pointer ptr) {
            return Integer.toString(this.intValue(ptr), 10);
        }
    }

    public final class BOOL16
    extends AbstractBoolean {
        protected BOOL16() {
            super(NativeType.SSHORT);
        }

        protected BOOL16(Offset offset) {
            super(NativeType.SSHORT, offset);
        }

        @Override
        public final boolean get(jnr.ffi.Pointer ptr) {
            return ptr.getShort(this.offset()) != 0;
        }

        @Override
        public final void set(jnr.ffi.Pointer ptr, boolean value) {
            ptr.putShort(this.offset(), (short)(value ? 1 : 0));
        }
    }

    protected final class WBOOL
    extends AbstractBoolean {
        protected WBOOL() {
            super(NativeType.SINT);
        }

        protected WBOOL(Offset offset) {
            super(NativeType.SINT, offset);
        }

        @Override
        public final boolean get(jnr.ffi.Pointer ptr) {
            return ptr.getInt(this.offset()) != 0;
        }

        @Override
        public final void set(jnr.ffi.Pointer ptr, boolean value) {
            ptr.putInt(this.offset(), value ? 1 : 0);
        }
    }

    protected final class Boolean
    extends AbstractBoolean {
        protected Boolean() {
            super(NativeType.SCHAR);
        }

        protected Boolean(Offset offset) {
            super(NativeType.SCHAR, offset);
        }

        @Override
        public final boolean get(jnr.ffi.Pointer ptr) {
            return ptr.getByte(this.offset()) != 0;
        }

        @Override
        public final void set(jnr.ffi.Pointer ptr, boolean value) {
            ptr.putByte(this.offset(), (byte)(value ? 1 : 0));
        }
    }

    protected abstract class AbstractBoolean
    extends AbstractField {
        protected AbstractBoolean(NativeType type) {
            super(type);
        }

        protected AbstractBoolean(NativeType type, Offset offset) {
            super(type, offset);
        }

        public abstract boolean get(jnr.ffi.Pointer var1);

        public abstract void set(jnr.ffi.Pointer var1, boolean var2);

        public java.lang.String toString(jnr.ffi.Pointer ptr) {
            return java.lang.Boolean.toString(this.get(ptr));
        }
    }

    protected abstract class AbstractField
    extends Field {
        protected AbstractField(int size, int align, Offset offset) {
            super(StructLayout.this.addField(size, align, offset));
        }

        protected AbstractField(int size, int align) {
            super(StructLayout.this.addField(size, align));
        }

        protected AbstractField(NativeType type) {
            super(StructLayout.this.addField(StructLayout.this.getRuntime().findType(type)));
        }

        protected AbstractField(Type type) {
            super(StructLayout.this.addField(type));
        }

        protected AbstractField(NativeType type, Offset offset) {
            super(StructLayout.this.addField(StructLayout.this.getRuntime().findType(type), offset));
        }

        protected AbstractField(Type type, Offset offset) {
            super(StructLayout.this.addField(type, offset));
        }
    }
}

