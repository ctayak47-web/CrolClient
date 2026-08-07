
package org.freedesktop.dbus.utils;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class XmlErrorHandlers {
    private XmlErrorHandlers() {
    }

    public static class XmlErrorHandlerRuntimeException
    implements ErrorHandler {
        @Override
        public void warning(SAXParseException _exception) throws SAXException {
            throw new RuntimeException(_exception);
        }

        @Override
        public void error(SAXParseException _exception) throws SAXException {
            throw new RuntimeException(_exception);
        }

        @Override
        public void fatalError(SAXParseException _exception) throws SAXException {
            throw new RuntimeException(_exception);
        }
    }

    public static class XmlErrorHandlerQuiet
    implements ErrorHandler {
        @Override
        public void warning(SAXParseException _exception) throws SAXException {
        }

        @Override
        public void error(SAXParseException _exception) throws SAXException {
        }

        @Override
        public void fatalError(SAXParseException _exception) throws SAXException {
        }
    }
}

