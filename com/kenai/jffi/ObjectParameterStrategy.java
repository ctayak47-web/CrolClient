
package com.kenai.jffi;

import com.kenai.jffi.ObjectParameterInfo;
import com.kenai.jffi.ObjectParameterType;

public abstract class ObjectParameterStrategy<T> {
    private final boolean isDirect;
    final int typeInfo;
    protected static final StrategyType DIRECT = StrategyType.DIRECT;
    protected static final StrategyType HEAP = StrategyType.HEAP;

    public ObjectParameterStrategy(boolean isDirect) {
        this(isDirect, ObjectParameterType.INVALID);
    }

    public ObjectParameterStrategy(boolean isDirect, ObjectParameterType type) {
        this.isDirect = isDirect;
        this.typeInfo = type.typeInfo;
    }

    public ObjectParameterStrategy(StrategyType type) {
        this(type, ObjectParameterType.INVALID);
    }

    public ObjectParameterStrategy(StrategyType strategyType, ObjectParameterType parameterType) {
        this.isDirect = strategyType == DIRECT;
        this.typeInfo = parameterType.typeInfo;
    }

    public final boolean isDirect() {
        return this.isDirect;
    }

    final int objectInfo(ObjectParameterInfo info) {
        int objectInfo = info.asObjectInfo();
        if (this.typeInfo != 0) {
            return objectInfo & 0xFFFFFF | this.typeInfo;
        }
        return objectInfo;
    }

    public abstract long address(T var1);

    public abstract Object object(T var1);

    public abstract int offset(T var1);

    public abstract int length(T var1);

    protected static enum StrategyType {
        DIRECT,
        HEAP;

    }
}

