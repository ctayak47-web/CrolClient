
package com.kenai.jffi;

import com.kenai.jffi.Foreign;
import java.util.ArrayList;
import java.util.Arrays;

public final class FaultException
extends RuntimeException {
    private final int signal;

    FaultException(int signal, long[] ip, long[] procname, long[] libname) {
        super(String.format("Received signal %d", signal));
        this.setStackTrace(FaultException.createStackTrace(ip, procname, libname, this.fillInStackTrace().getStackTrace()));
        this.signal = signal;
    }

    private static StackTraceElement[] createStackTrace(long[] ip, long[] procname, long[] libname, StackTraceElement[] existingTrace) {
        ArrayList<StackTraceElement> trace = new ArrayList<StackTraceElement>();
        for (int i = 0; i < ip.length; ++i) {
            String procName = new String(Foreign.getZeroTerminatedByteArray(procname[i]));
            String libName = new String(Foreign.getZeroTerminatedByteArray(libname[i]));
            trace.add(new StackTraceElement("native", procName, libName, -1));
        }
        trace.addAll(Arrays.asList(existingTrace));
        return trace.toArray(new StackTraceElement[trace.size()]);
    }

    public int getSignal() {
        return this.signal;
    }
}

