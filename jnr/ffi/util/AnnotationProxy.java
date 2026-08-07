
package jnr.ffi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashMap;
import java.util.Map;
import jnr.ffi.util.AnnotationProperty;

public final class AnnotationProxy<A extends Annotation>
implements Annotation,
InvocationHandler {
    private static final int MEMBER_NAME_MULTIPLICATOR = 127;
    private final Class<A> annotationType;
    private final Map<String, AnnotationProperty> properties = new LinkedHashMap<String, AnnotationProperty>();
    private final A proxedAnnotation;

    public static <A extends Annotation> AnnotationProxy<A> newProxy(Class<A> annotationType) {
        if (annotationType == null) {
            throw new IllegalArgumentException("Parameter 'annotationType' must be not null");
        }
        return new AnnotationProxy<A>(annotationType);
    }

    private static AnnotationProxy<?> getAnnotationProxy(Object obj) {
        InvocationHandler handler;
        if (Proxy.isProxyClass(obj.getClass()) && (handler = Proxy.getInvocationHandler(obj)) instanceof AnnotationProxy) {
            return (AnnotationProxy)handler;
        }
        return null;
    }

    private static <A extends Annotation> Method[] getDeclaredMethods(final Class<A> annotationType) {
        return AccessController.doPrivileged(new PrivilegedAction<Method[]>(){

            @Override
            public Method[] run() {
                AccessibleObject[] declaredMethods = annotationType.getDeclaredMethods();
                AccessibleObject.setAccessible(declaredMethods, true);
                return declaredMethods;
            }
        });
    }

    private AnnotationProxy(Class<A> annotationType) {
        this.annotationType = annotationType;
        for (Method method : AnnotationProxy.getDeclaredMethods(annotationType)) {
            String propertyName = method.getName();
            Class<?> returnType = method.getReturnType();
            Object defaultValue = method.getDefaultValue();
            AnnotationProperty property = new AnnotationProperty(propertyName, returnType);
            property.setValue(defaultValue);
            this.properties.put(propertyName, property);
        }
        this.proxedAnnotation = (Annotation)annotationType.cast(Proxy.newProxyInstance(annotationType.getClassLoader(), new Class[]{annotationType}, this));
    }

    public void setProperty(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' must be not null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Parameter 'value' must be not null");
        }
        if (!this.properties.containsKey(name)) {
            throw new IllegalArgumentException("Annotation '" + this.annotationType.getName() + "' does not contain a property named '" + name + "'");
        }
        this.properties.get(name).setValue(value);
    }

    public Object getProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' must be not null");
        }
        return this.properties.get(name).getValue();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (this.properties.containsKey(name)) {
            return this.properties.get(name).getValue();
        }
        return method.invoke((Object)this, args);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return this.annotationType;
    }

    public A getProxedAnnotation() {
        return this.proxedAnnotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!this.annotationType.isInstance(obj)) {
            return false;
        }
        for (Method method : AnnotationProxy.getDeclaredMethods(this.annotationType())) {
            String propertyName = method.getName();
            if (!this.properties.containsKey(propertyName)) {
                return false;
            }
            AnnotationProperty expected = this.properties.get(propertyName);
            AnnotationProperty actual = new AnnotationProperty(propertyName, method.getReturnType());
            AnnotationProxy<?> proxy = AnnotationProxy.getAnnotationProxy(obj);
            if (proxy != null) {
                actual.setValue(proxy.getProperty(propertyName));
            } else {
                try {
                    actual.setValue(method.invoke(obj, new Object[0]));
                }
                catch (IllegalArgumentException e) {
                    return false;
                }
                catch (IllegalAccessException e) {
                    throw new AssertionError((Object)e);
                }
                catch (InvocationTargetException e) {
                    return false;
                }
            }
            if (expected.equals(actual)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (Map.Entry<String, AnnotationProperty> property : this.properties.entrySet()) {
            hashCode += 127 * property.getKey().hashCode() ^ property.getValue().getValueHashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("@").append(this.annotationType.getName()).append('(');
        int counter = 0;
        for (Map.Entry<String, AnnotationProperty> property : this.properties.entrySet()) {
            if (counter > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(property.getKey()).append('=').append(property.getValue().valueToString());
            ++counter;
        }
        return stringBuilder.append(')').toString();
    }
}

