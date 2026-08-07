
package org.freedesktop.dbus.utils;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.InvalidBusNameException;
import org.freedesktop.dbus.exceptions.InvalidObjectPathException;

public final class DBusObjects {
    private static final int MAX_NAME_LENGTH = 255;
    private static final Pattern OBJECT_REGEX_PATTERN = Pattern.compile("^/([-_a-zA-Z0-9]+(/[-_a-zA-Z0-9]+)*)?$");
    private static final Pattern BUSNAME_REGEX = Pattern.compile("^[-_a-zA-Z][-_a-zA-Z0-9]*(\\.[-_a-zA-Z][-_a-zA-Z0-9]*)*$");
    private static final Pattern CONNID_REGEX = Pattern.compile("^:[0-9]*\\.[0-9]*$");

    private DBusObjects() {
    }

    private static <T, X extends DBusException> T requireBase(T _input, Predicate<T> _validation, Function<String, X> _exSupplier, String _customMessage) throws X {
        String str;
        if (_input == null) {
            throw (DBusException)_exSupplier.apply(_customMessage != null ? _customMessage : null);
        }
        if (_input instanceof String && (str = (String)_input).isBlank()) {
            throw (DBusException)_exSupplier.apply(_customMessage != null ? _customMessage : "<Empty String>");
        }
        if (!_validation.test(_input)) {
            throw (DBusException)_exSupplier.apply(_customMessage != null ? _customMessage : String.valueOf(_input));
        }
        return _input;
    }

    public static String requireObjectPath(String _objectPath) throws InvalidObjectPathException {
        return DBusObjects.requireBase(_objectPath, DBusObjects::validateObjectPath, InvalidObjectPathException::new, null);
    }

    public static String requireBusName(String _busName) throws InvalidBusNameException {
        return DBusObjects.requireBusName(_busName, null);
    }

    public static String requireBusName(String _busName, String _customMessage) throws InvalidBusNameException {
        return DBusObjects.requireBase(_busName, DBusObjects::validateBusName, InvalidBusNameException::new, _customMessage);
    }

    public static String requireNotBusName(String _busName, String _customMessage) throws InvalidBusNameException {
        return DBusObjects.requireBase(_busName, DBusObjects::validateNotBusName, InvalidBusNameException::new, _customMessage);
    }

    public static String requireConnectionId(String _connId) throws InvalidBusNameException {
        return DBusObjects.requireBase(_connId, DBusObjects::validateConnectionId, InvalidBusNameException::new, null);
    }

    public static String requireBusNameOrConnectionId(String _busNameOrConnId) throws InvalidBusNameException {
        if (DBusObjects.validateBusName(_busNameOrConnId)) {
            return _busNameOrConnId;
        }
        if (DBusObjects.validateConnectionId(_busNameOrConnId)) {
            return _busNameOrConnId;
        }
        throw new InvalidBusNameException(_busNameOrConnId);
    }

    public static boolean validateBusName(String _busName) {
        return _busName != null && _busName.length() < 255 && BUSNAME_REGEX.matcher(_busName).matches();
    }

    public static boolean validateNotBusName(String _busName) {
        return !DBusObjects.validateBusName(_busName);
    }

    public static boolean validateObjectPath(String _objectPath) {
        return !DBusObjects.validateNotObjectPath(_objectPath);
    }

    public static boolean validateNotObjectPath(String _objectPath) {
        return _objectPath == null || _objectPath.length() > 255 || !_objectPath.startsWith("/") || !OBJECT_REGEX_PATTERN.matcher(_objectPath).matches();
    }

    public static boolean validateNotConnectionId(String _connectionId) {
        return !DBusObjects.validateConnectionId(_connectionId);
    }

    public static boolean validateConnectionId(String _connectionId) {
        return _connectionId != null && CONNID_REGEX.matcher(_connectionId).matches();
    }

    public static <T, X extends Exception> T requireNotNull(T _input, Supplier<X> _exception) throws X {
        if (_input == null) {
            throw (Exception)_exception.get();
        }
        return _input;
    }
}

