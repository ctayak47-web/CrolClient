
package org.freedesktop.dbus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.freedesktop.dbus.ArrayFrob;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.types.DBusStructType;
import org.freedesktop.dbus.types.Variant;

public final class StructHelper {
    private StructHelper() {
    }

    public static <T extends Struct> List<T> convertToStructList(List<Object[]> _obj, Class<T> _structType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ArrayList result = new ArrayList();
        StructHelper.convertToStructCollection(_obj, _structType, result);
        return result;
    }

    public static <T extends Struct> Set<T> convertToStructSet(Set<Object[]> _obj, Class<T> _structType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        LinkedHashSet result = new LinkedHashSet();
        StructHelper.convertToStructCollection(_obj, _structType, result);
        return result;
    }

    public static <T extends Struct> void convertToStructCollection(Collection<Object[]> _input, Class<T> _structType, Collection<T> _result) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Objects.requireNonNull(_structType, "Struct class required");
        Objects.requireNonNull(_result, "Collection for result storage required");
        Objects.requireNonNull(_input, "Input data required");
        Class[] constructorArgClasses = (Class[])Arrays.stream(_structType.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Position.class)).sorted((f1, f2) -> Integer.compare(f1.getAnnotation(Position.class).value(), f2.getAnnotation(Position.class).value())).map(Field::getType).toArray(Class[]::new);
        for (Object[] object : _input) {
            if (constructorArgClasses.length != object.length) {
                throw new IllegalArgumentException("Struct length does not match argument length");
            }
            T x = StructHelper.createStruct(constructorArgClasses, object, _structType);
            _result.add(x);
        }
    }

    public static <T extends Struct> T createStructFromVariant(Variant<?> _variant, Class<T> _structClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (_variant == null || _structClass == null) {
            return null;
        }
        if (_variant.getType() instanceof DBusStructType && _variant.getValue() instanceof Object[]) {
            Class[] argTypes = (Class[])Arrays.stream((Object[])_variant.getValue()).map(Object::getClass).toArray(Class[]::new);
            return StructHelper.createStruct(argTypes, _variant.getValue(), _structClass);
        }
        return null;
    }

    public static <T extends Struct> T createStruct(Class<?>[] _constructorArgs, Object _values, Class<T> _classToConstruct) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (_constructorArgs == null || _classToConstruct == null || _values == null) {
            return null;
        }
        try {
            Constructor<T> declaredConstructor = _classToConstruct.getDeclaredConstructor(_constructorArgs);
            declaredConstructor.setAccessible(true);
            if (_values instanceof Object[]) {
                Object[] oa = (Object[])_values;
                return (T)((Struct)declaredConstructor.newInstance(oa));
            }
            return (T)((Struct)declaredConstructor.newInstance(_values));
        }
        catch (NoSuchMethodException | SecurityException _ex) {
            for (int i = 0; i < _constructorArgs.length; ++i) {
                Class<?> class1 = _constructorArgs[i];
                if (!ArrayFrob.getWrapperToPrimitiveTypes().containsKey(class1)) continue;
                _constructorArgs[i] = ArrayFrob.getWrapperToPrimitiveTypes().get(class1);
                return StructHelper.createStruct(_constructorArgs, _values, _classToConstruct);
            }
            throw new NoSuchMethodException("Cannot find suitable constructor for arguments " + Arrays.toString(_constructorArgs) + " in class " + String.valueOf(_classToConstruct) + ".");
        }
    }
}

