
package jnr.ffi.provider.jffi;

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.EnumSet;
import jnr.ffi.provider.jffi.ParameterStrategy;

public final class BufferParameterStrategy
extends ParameterStrategy {
    private static final int BYTE_POSITION_SHIFT = 0;
    private static final int SHORT_POSITION_SHIFT = 1;
    private static final int CHAR_POSITION_SHIFT = 1;
    private static final int BOOLEAN_POSITION_SHIFT = 2;
    private static final int INT_POSITION_SHIFT = 2;
    private static final int FLOAT_POSITION_SHIFT = 2;
    private static final int LONG_POSITION_SHIFT = 3;
    private static final int DOUBLE_POSITION_SHIFT = 3;
    private final int shift;
    private static final BufferParameterStrategy[] DIRECT_BUFFER_PARAMETER_STRATEGIES;
    private static final BufferParameterStrategy[] HEAP_BUFFER_PARAMETER_STRATEGIES;

    private BufferParameterStrategy(ObjectParameterStrategy.StrategyType type, ObjectParameterType.ComponentType componentType) {
        super(type, ObjectParameterType.create(ObjectParameterType.ObjectType.ARRAY, componentType));
        this.shift = BufferParameterStrategy.calculateShift(componentType);
    }

    public static long address(ByteBuffer ptr) {
        return BufferParameterStrategy.address(ptr, 0);
    }

    public static long address(ShortBuffer ptr) {
        return BufferParameterStrategy.address(ptr, 1);
    }

    public static long address(CharBuffer ptr) {
        return BufferParameterStrategy.address(ptr, 1);
    }

    public static long address(IntBuffer ptr) {
        return BufferParameterStrategy.address(ptr, 2);
    }

    public static long address(FloatBuffer ptr) {
        return BufferParameterStrategy.address(ptr, 2);
    }

    public static long address(LongBuffer ptr) {
        return BufferParameterStrategy.address(ptr, 3);
    }

    public static long address(DoubleBuffer ptr) {
        return BufferParameterStrategy.address(ptr, 3);
    }

    public static long address(Buffer buffer) {
        if (buffer instanceof ByteBuffer) {
            return BufferParameterStrategy.address(buffer, 0);
        }
        if (buffer instanceof ShortBuffer) {
            return BufferParameterStrategy.address(buffer, 1);
        }
        if (buffer instanceof CharBuffer) {
            return BufferParameterStrategy.address(buffer, 1);
        }
        if (buffer instanceof IntBuffer) {
            return BufferParameterStrategy.address(buffer, 2);
        }
        if (buffer instanceof LongBuffer) {
            return BufferParameterStrategy.address(buffer, 3);
        }
        if (buffer instanceof FloatBuffer) {
            return BufferParameterStrategy.address(buffer, 2);
        }
        if (buffer instanceof DoubleBuffer) {
            return BufferParameterStrategy.address(buffer, 3);
        }
        if (buffer == null) {
            return BufferParameterStrategy.address(buffer, 0);
        }
        throw new IllegalArgumentException("unsupported java.nio.Buffer subclass: " + buffer.getClass());
    }

    private static long address(Buffer ptr, int shift) {
        return ptr != null && ptr.isDirect() ? MemoryIO.getInstance().getDirectBufferAddress(ptr) + (long)(ptr.position() << shift) : 0L;
    }

    public long address(Object o) {
        return BufferParameterStrategy.address((Buffer)o, this.shift);
    }

    public Object object(Object o) {
        return ((Buffer)o).array();
    }

    public int offset(Object o) {
        Buffer buffer = (Buffer)o;
        return buffer.arrayOffset() + buffer.position();
    }

    public int length(Object o) {
        return ((Buffer)o).remaining();
    }

    static int calculateShift(ObjectParameterType.ComponentType componentType) {
        switch (componentType) {
            case BYTE: {
                return 0;
            }
            case SHORT: {
                return 1;
            }
            case CHAR: {
                return 1;
            }
            case INT: {
                return 2;
            }
            case BOOLEAN: {
                return 2;
            }
            case FLOAT: {
                return 2;
            }
            case LONG: {
                return 3;
            }
            case DOUBLE: {
                return 3;
            }
        }
        throw new IllegalArgumentException("unsupported component type: " + (Object)((Object)componentType));
    }

    static BufferParameterStrategy direct(ObjectParameterType.ComponentType componentType) {
        return DIRECT_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()];
    }

    static BufferParameterStrategy heap(ObjectParameterType.ComponentType componentType) {
        return HEAP_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()];
    }

    static {
        EnumSet<ObjectParameterType.ComponentType> componentTypes = EnumSet.allOf(ObjectParameterType.ComponentType.class);
        DIRECT_BUFFER_PARAMETER_STRATEGIES = new BufferParameterStrategy[componentTypes.size()];
        HEAP_BUFFER_PARAMETER_STRATEGIES = new BufferParameterStrategy[componentTypes.size()];
        for (ObjectParameterType.ComponentType componentType : componentTypes) {
            BufferParameterStrategy.DIRECT_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()] = new BufferParameterStrategy(DIRECT, componentType);
            BufferParameterStrategy.HEAP_BUFFER_PARAMETER_STRATEGIES[componentType.ordinal()] = new BufferParameterStrategy(HEAP, componentType);
        }
    }
}

