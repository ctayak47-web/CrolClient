
package jnr.ffi;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public abstract class Union
extends Struct {
    protected Union(Runtime runtime) {
        super(runtime, true);
    }
}

