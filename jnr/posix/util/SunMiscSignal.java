
package jnr.posix.util;

import jnr.posix.SignalHandler;
import sun.misc.Signal;

public class SunMiscSignal {
    public static SignalHandler signal(jnr.constants.platform.Signal sig, SignalHandler handler) {
        Signal s = new Signal(sig.name().substring("SIG".length()));
        sun.misc.SignalHandler oldHandler = Signal.handle(s, new SunMiscSignalHandler(handler));
        if (oldHandler instanceof SunMiscSignalHandler) {
            return ((SunMiscSignalHandler)oldHandler).handler;
        }
        return null;
    }

    private static class SunMiscSignalHandler
    implements sun.misc.SignalHandler {
        final SignalHandler handler;

        public SunMiscSignalHandler(SignalHandler handler) {
            this.handler = handler;
        }

        @Override
        public void handle(Signal signal) {
            this.handler.handle(signal.getNumber());
        }
    }
}

