
package jnr.ffi.mapper;

import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;

public interface DataConverter<J, N>
extends ToNativeConverter<J, N>,
FromNativeConverter<J, N> {
}

