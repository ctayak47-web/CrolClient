
package jnr.ffi;

public interface Variable<T> {
    public T get();

    public void set(T var1);
}

