
package com.kenai.jffi;

import com.kenai.jffi.Foreign;

public final class LastError {
    private final Foreign foreign = Foreign.getInstance();

    private LastError() {
    }

    public static final LastError getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Deprecated
    public final int getError() {
        return Foreign.getLastError();
    }

    public final int get() {
        return Foreign.getLastError();
    }

    public final void set(int value) {
        Foreign.setLastError(value);
    }

    private static final class SingletonHolder {
        static final LastError INSTANCE = new LastError();

        private SingletonHolder() {
        }
    }
}

