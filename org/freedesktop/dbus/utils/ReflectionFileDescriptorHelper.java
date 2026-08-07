
package org.freedesktop.dbus.utils;

import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReflectionFileDescriptorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionFileDescriptorHelper.class);
    private static final Optional<ReflectionFileDescriptorHelper> INSTANCE = ReflectionFileDescriptorHelper.createInstance();
    private final Field fdField = FileDescriptor.class.getDeclaredField("fd");
    private final Constructor<FileDescriptor> constructor;

    private ReflectionFileDescriptorHelper() throws ReflectiveOperationException {
        this.fdField.setAccessible(true);
        this.constructor = FileDescriptor.class.getDeclaredConstructor(Integer.TYPE);
        this.constructor.setAccessible(true);
    }

    public Optional<Integer> getFileDescriptorValue(FileDescriptor _fd) {
        try {
            return Optional.of(this.fdField.getInt(_fd));
        }
        catch (IllegalAccessException | IllegalArgumentException | SecurityException _ex) {
            LOGGER.error("Could not get file descriptor by reflection.", _ex);
            return Optional.empty();
        }
    }

    public Optional<FileDescriptor> createFileDescriptor(int _fd) {
        try {
            return Optional.of(this.constructor.newInstance(_fd));
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException | InvocationTargetException _ex) {
            LOGGER.error("Could not create new FileDescriptor instance by reflection.", _ex);
            return Optional.empty();
        }
    }

    private static Optional<ReflectionFileDescriptorHelper> createInstance() {
        try {
            return Optional.of(new ReflectionFileDescriptorHelper());
        }
        catch (ReflectiveOperationException _ex) {
            LOGGER.error("Unable to hook up java.io.FileDescriptor by using reflection.", _ex);
            return Optional.empty();
        }
    }

    public static Optional<ReflectionFileDescriptorHelper> getInstance() {
        return INSTANCE;
    }
}

