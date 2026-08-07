
package com.kenai.jffi;

import com.kenai.jffi.Foreign;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.ObjectParameterInfo;
import com.kenai.jffi.ObjectParameterInvoker;
import com.kenai.jffi.Type;

final class HeapObjectParameterInvoker
extends ObjectParameterInvoker {
    private final Foreign foreign;

    HeapObjectParameterInvoker(Foreign foreign) {
        this.foreign = foreign;
    }

    @Override
    public final boolean isNative() {
        return false;
    }

    private static int encode(HeapInvocationBuffer.Encoder encoder, byte[] paramBuffer, int off, Type type, long n) {
        if (type.size() <= 4) {
            return encoder.putInt(paramBuffer, off, (int)n);
        }
        return encoder.putLong(paramBuffer, off, n);
    }

    private long invokeO1(Function function, byte[] paramBuffer, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return function.getReturnType().size() == 8 ? Foreign.invokeArrayO1Int64(function.getContextAddress(), function.getFunctionAddress(), paramBuffer, o1, o1flags.asObjectInfo(), o1off, o1len) : (long)Foreign.invokeArrayO1Int32(function.getContextAddress(), function.getFunctionAddress(), paramBuffer, o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    private long invokeO2(Function function, byte[] paramBuffer, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        return function.getReturnType().size() == 8 ? Foreign.invokeArrayO2Int64(function.getContextAddress(), function.getFunctionAddress(), paramBuffer, o1, o1flags.asObjectInfo(), o1off, o1len, o2, o2flags.asObjectInfo(), o2off, o2len) : (long)Foreign.invokeArrayO2Int32(function.getContextAddress(), function.getFunctionAddress(), paramBuffer, o1, o1flags.asObjectInfo(), o1off, o1len, o2, o2flags.asObjectInfo(), o2off, o2len);
    }

    private long invokeO3(Function function, byte[] paramBuffer, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags, Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {
        int[] objInfo = new int[]{o1flags.asObjectInfo(), o1off, o1len, o2flags.asObjectInfo(), o2off, o2len, o3flags.asObjectInfo(), o3off, o3len};
        Object[] objects = new Object[]{o1, o2, o3};
        return function.getReturnType().size() == 8 ? Foreign.invokeArrayWithObjectsInt64(function.getContextAddress(), function.getFunctionAddress(), paramBuffer, 3, objInfo, objects) : (long)Foreign.invokeArrayWithObjectsInt32(function.getContextAddress(), function.getFunctionAddress(), paramBuffer, 3, objInfo, objects);
    }

    @Override
    public long invokeN1O1rN(Function function, long n1, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return this.invokeO1(function, new byte[HeapInvocationBuffer.Encoder.getInstance().getBufferSize(function.getCallContext())], o1, o1off, o1len, o1flags);
    }

    @Override
    public long invokeN2O1rN(Function function, long n1, long n2, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.Encoder.getInstance();
        byte[] paramBuffer = new byte[encoder.getBufferSize(function.getCallContext())];
        int poff = 0;
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(0), n1);
        HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(1), n2);
        return this.invokeO1(function, paramBuffer, o1, o1off, o1len, o1flags);
    }

    @Override
    public long invokeN2O2rN(Function function, long n1, long n2, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        return this.invokeO2(function, new byte[HeapInvocationBuffer.Encoder.getInstance().getBufferSize(function.getCallContext())], o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags);
    }

    private static byte[] encodeN3(Function function, long n1, long n2, long n3) {
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.Encoder.getInstance();
        byte[] paramBuffer = new byte[encoder.getBufferSize(function.getCallContext())];
        int poff = 0;
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(0), n1);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(1), n2);
        HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(2), n3);
        return paramBuffer;
    }

    @Override
    public long invokeN3O1rN(Function function, long n1, long n2, long n3, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return this.invokeO1(function, HeapObjectParameterInvoker.encodeN3(function, n1, n2, n3), o1, o1off, o1len, o1flags);
    }

    @Override
    public long invokeN3O2rN(Function function, long n1, long n2, long n3, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        return this.invokeO2(function, HeapObjectParameterInvoker.encodeN3(function, n1, n2, n3), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags);
    }

    @Override
    public long invokeN3O3rN(Function function, long n1, long n2, long n3, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags, Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {
        return this.invokeO3(function, HeapObjectParameterInvoker.encodeN3(function, n1, n2, n3), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags, o3, o3off, o3len, o3flags);
    }

    private static byte[] encodeN4(Function function, long n1, long n2, long n3, long n4) {
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.Encoder.getInstance();
        byte[] paramBuffer = new byte[encoder.getBufferSize(function.getCallContext())];
        int poff = 0;
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(0), n1);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(1), n2);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(2), n3);
        HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(3), n4);
        return paramBuffer;
    }

    @Override
    public long invokeN4O1rN(Function function, long n1, long n2, long n3, long n4, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return this.invokeO1(function, HeapObjectParameterInvoker.encodeN4(function, n1, n2, n3, n4), o1, o1off, o1len, o1flags);
    }

    @Override
    public long invokeN4O2rN(Function function, long n1, long n2, long n3, long n4, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        return this.invokeO2(function, HeapObjectParameterInvoker.encodeN4(function, n1, n2, n3, n4), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags);
    }

    @Override
    public long invokeN4O3rN(Function function, long n1, long n2, long n3, long n4, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags, Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {
        return this.invokeO3(function, HeapObjectParameterInvoker.encodeN4(function, n1, n2, n3, n4), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags, o3, o3off, o3len, o3flags);
    }

    private static byte[] encodeN5(Function function, long n1, long n2, long n3, long n4, long n5) {
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.Encoder.getInstance();
        byte[] paramBuffer = new byte[encoder.getBufferSize(function.getCallContext())];
        int poff = 0;
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(0), n1);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(1), n2);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(2), n3);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(3), n4);
        HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(4), n5);
        return paramBuffer;
    }

    @Override
    public long invokeN5O1rN(Function function, long n1, long n2, long n3, long n4, long n5, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return this.invokeO1(function, HeapObjectParameterInvoker.encodeN5(function, n1, n2, n3, n4, n5), o1, o1off, o1len, o1flags);
    }

    @Override
    public long invokeN5O2rN(Function function, long n1, long n2, long n3, long n4, long n5, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        return this.invokeO2(function, HeapObjectParameterInvoker.encodeN5(function, n1, n2, n3, n4, n5), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags);
    }

    @Override
    public long invokeN5O3rN(Function function, long n1, long n2, long n3, long n4, long n5, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags, Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {
        return this.invokeO3(function, HeapObjectParameterInvoker.encodeN5(function, n1, n2, n3, n4, n5), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags, o3, o3off, o3len, o3flags);
    }

    private static byte[] encodeN6(Function function, long n1, long n2, long n3, long n4, long n5, long n6) {
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.Encoder.getInstance();
        byte[] paramBuffer = new byte[encoder.getBufferSize(function.getCallContext())];
        int poff = 0;
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(0), n1);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(1), n2);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(2), n3);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(3), n4);
        poff = HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(4), n5);
        HeapObjectParameterInvoker.encode(encoder, paramBuffer, poff, function.getParameterType(5), n6);
        return paramBuffer;
    }

    @Override
    public long invokeN6O1rN(Function function, long n1, long n2, long n3, long n4, long n5, long n6, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return this.invokeO1(function, HeapObjectParameterInvoker.encodeN6(function, n1, n2, n3, n4, n5, n6), o1, o1off, o1len, o1flags);
    }

    @Override
    public long invokeN6O2rN(Function function, long n1, long n2, long n3, long n4, long n5, long n6, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        return this.invokeO2(function, HeapObjectParameterInvoker.encodeN6(function, n1, n2, n3, n4, n5, n6), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags);
    }

    @Override
    public long invokeN6O3rN(Function function, long n1, long n2, long n3, long n4, long n5, long n6, Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, Object o2, int o2off, int o2len, ObjectParameterInfo o2flags, Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {
        return this.invokeO3(function, HeapObjectParameterInvoker.encodeN6(function, n1, n2, n3, n4, n5, n6), o1, o1off, o1len, o1flags, o2, o2off, o2len, o2flags, o3, o3off, o3len, o3flags);
    }
}

