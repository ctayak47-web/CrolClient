
package org.freedesktop.dbus.messages;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.MethodTuple;
import org.freedesktop.dbus.StrongReference;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
import org.freedesktop.dbus.annotations.DBusIgnore;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.annotations.DBusProperties;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Introspectable;
import org.freedesktop.dbus.interfaces.Peer;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.propertyref.PropertyRef;
import org.freedesktop.dbus.utils.DBusNamingUtil;
import org.freedesktop.dbus.utils.Util;
import org.slf4j.LoggerFactory;

public class ExportedObject {
    private final Map<MethodTuple, Method> methods;
    private final Map<PropertyRef, Method> propertyMethods;
    private final String introspectionData;
    private final Reference<DBusInterface> object;

    public ExportedObject(DBusInterface _object, boolean _weakreferences) throws DBusException {
        this.object = _weakreferences ? new WeakReference<DBusInterface>(_object) : new StrongReference<DBusInterface>(_object);
        this.methods = new HashMap<MethodTuple, Method>();
        this.propertyMethods = new HashMap<PropertyRef, Method>();
        Set<Class<?>> implementedInterfaces = this.getDBusInterfaces(_object.getClass());
        implementedInterfaces.add(Introspectable.class);
        implementedInterfaces.add(Peer.class);
        this.introspectionData = this.generateIntrospectionXml(implementedInterfaces);
    }

    protected String generateAnnotationsXml(AnnotatedElement _c) {
        StringBuilder ans = new StringBuilder();
        for (Annotation a : _c.getDeclaredAnnotations()) {
            if (!a.annotationType().isAnnotationPresent(DBusInterfaceName.class)) continue;
            Class<? extends Annotation> t = a.annotationType();
            String value = "";
            try {
                Method m = t.getMethod("value", new Class[0]);
                if (m != null) {
                    value = m.invoke((Object)a, new Object[0]).toString();
                }
            }
            catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException _ex) {
                LoggerFactory.getLogger(this.getClass()).trace("Could not find value", _ex);
            }
            String name = DBusNamingUtil.getAnnotationName(t);
            ans.append("  <annotation name=\"").append(name).append("\" value=\"").append(value).append("\" />\n");
        }
        return ans.toString();
    }

    protected String generatePropertyXml(DBusProperty _property) throws DBusException {
        return this.generatePropertyXml(_property.name(), _property.type(), _property.access());
    }

    protected String generatePropertyXml(String _propertyName, Class<?> _propertyTypeClass, DBusProperty.Access _access) throws DBusException {
        String propertyTypeString;
        if (TypeRef.class.isAssignableFrom(_propertyTypeClass)) {
            Type actualType = Optional.ofNullable(Util.unwrapTypeRef(_propertyTypeClass)).orElseThrow(() -> new DBusException("Could not read TypeRef type for property '" + _propertyName + "'"));
            propertyTypeString = Marshalling.getDBusType(new Type[]{actualType});
        } else {
            propertyTypeString = List.class.equals(_propertyTypeClass) ? "av" : (Map.class.equals(_propertyTypeClass) ? "a{vv}" : Marshalling.getDBusType(new Type[]{_propertyTypeClass}));
        }
        String access = _access.getAccessName();
        return "<property name=\"" + _propertyName + "\" type=\"" + propertyTypeString + "\" access=\"" + access + "\" />";
    }

    protected String generatePropertiesXml(Class<?> _clz) throws DBusException {
        DBusProperty property;
        StringBuilder xml = new StringBuilder();
        HashMap<String, PropertyRef> map = new HashMap<String, PropertyRef>();
        DBusProperties properties = _clz.getAnnotation(DBusProperties.class);
        if (properties != null) {
            for (DBusProperty property2 : properties.value()) {
                if (map.containsKey(property2.name())) {
                    throw new DBusException(MessageFormat.format("Property ''{0}'' defined multiple times.", property2.name()));
                }
                map.put(property2.name(), new PropertyRef(property2));
            }
        }
        if ((property = _clz.getAnnotation(DBusProperty.class)) != null) {
            if (map.containsKey(property.name())) {
                throw new DBusException(MessageFormat.format("Property ''{0}'' defined multiple times.", property.name()));
            }
            map.put(property.name(), new PropertyRef(property));
        }
        for (Method method : _clz.getDeclaredMethods()) {
            DBusBoundProperty propertyAnnot = method.getAnnotation(DBusBoundProperty.class);
            if (propertyAnnot == null) continue;
            String name = DBusNamingUtil.getPropertyName(method);
            DBusProperty.Access access = PropertyRef.accessForMethod(method);
            PropertyRef.checkMethod(method);
            Class<?> type = PropertyRef.typeForMethod(method);
            PropertyRef ref = new PropertyRef(name, type, access);
            this.propertyMethods.put(ref, method);
            if (map.containsKey(name)) {
                PropertyRef existing = (PropertyRef)map.get(name);
                if (access.equals((Object)existing.getAccess())) {
                    throw new DBusException(MessageFormat.format("Property ''{0}'' has access mode ''{1}'' defined multiple times.", new Object[]{name, access}));
                }
                map.put(name, new PropertyRef(name, type, DBusProperty.Access.READ_WRITE));
                continue;
            }
            map.put(name, ref);
        }
        for (PropertyRef ref : map.values()) {
            xml.append("  ").append(this.generatePropertyXml(ref.getName(), ref.getType(), ref.getAccess())).append("\n");
        }
        return xml.toString();
    }

    protected String generateMethodsXml(Class<?> _clz) throws DBusException {
        StringBuilder sb = new StringBuilder();
        for (Method meth : _clz.getDeclaredMethods()) {
            if (ExportedObject.isExcluded(meth)) continue;
            String methodName = DBusNamingUtil.getMethodName(meth);
            if (methodName.length() > 255) {
                throw new DBusException("Introspected method name exceeds 255 characters. Cannot export objects with method " + methodName);
            }
            sb.append("  <method name=\"").append(methodName).append("\" >\n");
            sb.append(this.generateAnnotationsXml(meth));
            for (Class<?> ex : meth.getExceptionTypes()) {
                if (!DBusExecutionException.class.isAssignableFrom(ex)) continue;
                sb.append("   <annotation name=\"org.freedesktop.DBus.Method.Error\" value=\"").append(AbstractConnection.DOLLAR_PATTERN.matcher(ex.getName()).replaceAll(".")).append("\" />\n");
            }
            StringBuilder ms = new StringBuilder();
            for (Type pt : meth.getGenericParameterTypes()) {
                for (String s : Marshalling.getDBusType(pt)) {
                    sb.append("   <arg type=\"").append(s).append("\" direction=\"in\"/>\n");
                    ms.append(s);
                }
            }
            if (!Void.TYPE.equals(meth.getGenericReturnType())) {
                if (Tuple.class.isAssignableFrom(meth.getReturnType())) {
                    Type[] ts;
                    ParameterizedType tc = (ParameterizedType)meth.getGenericReturnType();
                    for (Type t : ts = tc.getActualTypeArguments()) {
                        if (t == null) continue;
                        for (String s : Marshalling.getDBusType(t)) {
                            sb.append("   <arg type=\"").append(s).append("\" direction=\"out\"/>\n");
                        }
                    }
                } else {
                    if (Object[].class.equals((Object)meth.getGenericReturnType())) {
                        throw new DBusException("Return type of Object[] cannot be introspected properly");
                    }
                    for (String s : Marshalling.getDBusType(meth.getGenericReturnType())) {
                        sb.append("   <arg type=\"").append(s).append("\" direction=\"out\"/>\n");
                    }
                }
            }
            sb.append("  </method>\n");
            this.methods.putIfAbsent(new MethodTuple(methodName, ms.toString()), meth);
        }
        return sb.toString();
    }

    protected String generateSignalsXml(Class<?> _clz) throws DBusException {
        StringBuilder sb = new StringBuilder();
        for (Class<?> sig : _clz.getDeclaredClasses()) {
            if (!DBusSignal.class.isAssignableFrom(sig)) continue;
            String signalName = DBusNamingUtil.getSignalName(sig);
            if (sig.isAnnotationPresent(DBusMemberName.class)) {
                DBusSignal.addSignalMap(sig.getSimpleName(), signalName);
            }
            if (signalName.length() > 255) {
                throw new DBusException("Introspected signal name exceeds 255 characters. Cannot export objects with signals of type " + signalName);
            }
            sb.append("  <signal name=\"").append(signalName).append("\">\n");
            Constructor<?> con = sig.getConstructors()[0];
            Type[] ts = con.getGenericParameterTypes();
            for (int j = 1; j < ts.length; ++j) {
                for (String s : Marshalling.getDBusType(ts[j])) {
                    sb.append("   <arg type=\"").append(s).append("\" direction=\"out\" />\n");
                }
            }
            sb.append(this.generateAnnotationsXml(sig));
            sb.append("  </signal>\n");
        }
        return sb.toString();
    }

    protected Set<Class<?>> getDBusInterfaces(Class<?> _inputClazz) {
        Objects.requireNonNull(_inputClazz, "inputClazz must not be null");
        LinkedHashSet result = new LinkedHashSet();
        LinkedHashSet<Class> checked = new LinkedHashSet<Class>();
        LinkedList toCheck = new LinkedList();
        toCheck.add(_inputClazz);
        while (!toCheck.isEmpty()) {
            List<Class<?>> interfaces;
            Class clazz = (Class)toCheck.poll();
            checked.add(clazz);
            Class superClass = clazz.getSuperclass();
            if (superClass != null && DBusInterface.class.isAssignableFrom(superClass)) {
                toCheck.add(superClass);
            }
            if ((interfaces = Arrays.asList(clazz.getInterfaces())).contains(DBusInterface.class)) {
                result.add(clazz);
            }
            interfaces.stream().filter(DBusInterface.class::isAssignableFrom).filter(i -> i != DBusInterface.class).filter(i -> !checked.contains(i)).forEach(toCheck::add);
        }
        return result;
    }

    private String generateIntrospectionXml(Set<Class<?>> _interfaces) throws DBusException {
        StringBuilder sb = new StringBuilder();
        for (Class<?> iface : _interfaces) {
            String ifaceName = DBusNamingUtil.getInterfaceName(iface);
            if (ifaceName.equals(iface.getSimpleName())) {
                throw new DBusException("DBusInterfaces cannot be declared outside a package");
            }
            if (ifaceName.length() > 255) {
                throw new DBusException("Introspected interface name exceeds 255 characters. Cannot export objects of type " + ifaceName);
            }
            if (iface.isAnnotationPresent(DBusInterfaceName.class)) {
                DBusSignal.addInterfaceMap(iface.getName(), ifaceName);
            }
            sb.append(" <interface name=\"").append(ifaceName).append("\">\n");
            sb.append(this.generateAnnotationsXml(iface));
            sb.append(this.generateMethodsXml(iface));
            sb.append(this.generatePropertiesXml(iface));
            sb.append(this.generateSignalsXml(iface));
            sb.append(" </interface>\n");
        }
        return sb.toString();
    }

    public Map<MethodTuple, Method> getMethods() {
        return this.methods;
    }

    public Map<PropertyRef, Method> getPropertyMethods() {
        return this.propertyMethods;
    }

    public Reference<DBusInterface> getObject() {
        return this.object;
    }

    public String getIntrospectiondata() {
        return this.introspectionData;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [methodCount=" + this.methods.size() + ", propertyMethodCount=" + this.propertyMethods.size() + ", object=" + (this.object.get() != null ? Objects.toString(this.object) : "<no object referenced>") + "]";
    }

    public static boolean isExcluded(Method _meth) {
        return _meth == null || !Modifier.isPublic(_meth.getModifiers()) || _meth.isSynthetic() || _meth.isDefault() || _meth.isBridge() || _meth.getAnnotation(DBusIgnore.class) != null || _meth.getAnnotation(DBusBoundProperty.class) != null || _meth.getName().equals("getObjectPath") && _meth.getReturnType().equals(String.class) && _meth.getParameterCount() == 0;
    }
}

