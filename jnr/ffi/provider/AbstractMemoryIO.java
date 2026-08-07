
package jnr.ffi.provider;

import java.nio.ByteBuffer;
import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import jnr.ffi.provider.AbstractArrayMemoryIO;
import jnr.ffi.provider.AbstractBufferMemoryIO;
import jnr.ffi.provider.BoundedMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;
import jnr.ffi.provider.ShareMemoryIO;

public abstract class AbstractMemoryIO
extends Pointer {
    protected static void checkBounds(long size, long off, long len) {
        if ((off | len | off + len | size - (off + len)) < 0L) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected AbstractMemoryIO(Runtime runtime, long address, boolean isDirect) {
        super(runtime, address, isDirect);
    }

    @Override
    public int indexOf(long offset, byte value) {
        return this.indexOf(offset, value, Integer.MAX_VALUE);
    }

    @Override
    public long getAddress(long offset) {
        return this.getRuntime().addressSize() == 4 ? (long)this.getInt(offset) : this.getLongLong(offset);
    }

    @Override
    public void putAddress(long offset, long value) {
        if (this.getRuntime().addressSize() == 4) {
            this.putInt(offset, (int)value);
        } else {
            this.putLongLong(offset, value);
        }
    }

    @Override
    public void checkBounds(long offset, long size) {
    }

    @Override
    public void putAddress(long offset, Address value) {
        if (this.getRuntime().addressSize() == 4) {
            this.putInt(offset, value.intValue());
        } else {
            this.putLongLong(offset, value.longValue());
        }
    }

    @Override
    public final long getNativeLong(long offset) {
        return this.getRuntime().longSize() == 4 ? (long)this.getInt(offset) : this.getLongLong(offset);
    }

    @Override
    public void putNativeLong(long offset, long value) {
        if (this.getRuntime().longSize() == 4) {
            this.putInt(offset, (int)value);
        } else {
            this.putLongLong(offset, value);
        }
    }

    @Override
    public long getLong(long offset) {
        return this.getRuntime().longSize() == 4 ? (long)this.getInt(offset) : this.getLongLong(offset);
    }

    @Override
    public void putLong(long offset, long value) {
        if (this.getRuntime().longSize() == 4) {
            this.putInt(offset, (int)value);
        } else {
            this.putLongLong(offset, value);
        }
    }

    @Override
    public void putInt(Type type, long offset, long value) {
        switch (type.getNativeType()) {
            case SCHAR: 
            case UCHAR: {
                this.putByte(offset, (byte)value);
                break;
            }
            case SSHORT: 
            case USHORT: {
                this.putShort(offset, (short)value);
                break;
            }
            case SINT: 
            case UINT: {
                this.putInt(offset, (int)value);
                break;
            }
            case SLONG: 
            case ULONG: {
                this.putNativeLong(offset, value);
                break;
            }
            case SLONGLONG: 
            case ULONGLONG: {
                this.putLongLong(offset, value);
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported integer type: " + (Object)((Object)type.getNativeType()));
            }
        }
    }

    @Override
    public long getInt(Type type, long offset) {
        switch (type.getNativeType()) {
            case SCHAR: 
            case UCHAR: {
                return this.getByte(offset);
            }
            case SSHORT: 
            case USHORT: {
                return this.getShort(offset);
            }
            case SINT: 
            case UINT: {
                return this.getInt(offset);
            }
            case SLONG: 
            case ULONG: {
                return this.getNativeLong(offset);
            }
            case SLONGLONG: 
            case ULONGLONG: {
                return this.getLongLong(offset);
            }
        }
        throw new IllegalArgumentException("unsupported integer type: " + (Object)((Object)type.getNativeType()));
    }

    @Override
    public AbstractMemoryIO slice(long offset) {
        return new ShareMemoryIO(this, offset);
    }

    @Override
    public AbstractMemoryIO slice(long offset, long size) {
        return new BoundedMemoryIO(this, offset, size);
    }

    @Override
    public void transferTo(long offset, Pointer other, long otherOffset, long count) {
        Pointer dst = other instanceof DelegatingMemoryIO ? ((DelegatingMemoryIO)((Object)other)).getDelegatedMemoryIO() : other;
        dst.checkBounds(otherOffset, count);
        if (dst instanceof AbstractArrayMemoryIO) {
            AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO)dst;
            this.get(offset, aio.array(), aio.offset() + (int)otherOffset, (int)count);
        } else if (dst instanceof AbstractBufferMemoryIO && ((AbstractBufferMemoryIO)dst).getByteBuffer().hasArray()) {
            ByteBuffer buf = ((AbstractBufferMemoryIO)dst).getByteBuffer();
            this.get(offset, buf.array(), buf.arrayOffset() + buf.position() + (int)otherOffset, (int)count);
        } else {
            for (long i = 0L; i < count; ++i) {
                other.putByte(otherOffset + i, this.getByte(offset + i));
            }
        }
    }

    @Override
    public void transferFrom(long offset, Pointer other, long otherOffset, long count) {
        Pointer src = other instanceof DelegatingMemoryIO ? ((DelegatingMemoryIO)((Object)other)).getDelegatedMemoryIO() : other;
        src.checkBounds(otherOffset, count);
        if (src instanceof AbstractArrayMemoryIO) {
            AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO)src;
            this.put(offset, aio.array(), aio.offset() + (int)otherOffset, (int)count);
        } else if (src instanceof AbstractBufferMemoryIO && ((AbstractBufferMemoryIO)src).getByteBuffer().hasArray()) {
            ByteBuffer buf = ((AbstractBufferMemoryIO)src).getByteBuffer();
            this.put(offset, buf.array(), buf.arrayOffset() + buf.position() + (int)otherOffset, (int)count);
        } else {
            for (long i = 0L; i < count; ++i) {
                this.putByte(offset + i, other.getByte(otherOffset + i));
            }
        }
    }
}

