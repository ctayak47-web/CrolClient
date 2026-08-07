
package org.freedesktop.dbus.messages;

import java.util.Arrays;

final class EmptyCollectionHelper {
    private EmptyCollectionHelper() {
    }

    static int determineSignatureOffsetDict(byte[] _sigb, int _currentOffset) {
        return EmptyCollectionHelper.determineEndOfBracketStructure(_sigb, _currentOffset, '{', '}');
    }

    static int determineSignatureOffsetArray(byte[] _sigb, int _currentOffset) {
        String sigSubString = EmptyCollectionHelper.determineSubSignature(_sigb, _currentOffset);
        if (sigSubString.isEmpty()) {
            return _currentOffset;
        }
        ECollectionSubType newtype = EmptyCollectionHelper.determineCollectionSubType((char)_sigb[_currentOffset]);
        return switch (newtype.ordinal()) {
            case 2 -> EmptyCollectionHelper.determineSignatureOffsetArray(_sigb, _currentOffset + 1);
            case 1 -> EmptyCollectionHelper.determineSignatureOffsetDict(_sigb, _currentOffset);
            case 0 -> EmptyCollectionHelper.determineSignatureOffsetStruct(_sigb, _currentOffset);
            case 3 -> _currentOffset;
            default -> throw new IllegalStateException("Unable to parse signature for empty collection");
        };
    }

    private static int determineSignatureOffsetStruct(byte[] _sigb, int _currentOffset) {
        return EmptyCollectionHelper.determineEndOfBracketStructure(_sigb, _currentOffset, '(', ')');
    }

    private static int determineEndOfBracketStructure(byte[] _sigb, int _currentOffset, char _openChar, char _closeChar) {
        String sigSubString = EmptyCollectionHelper.determineSubSignature(_sigb, _currentOffset);
        if (sigSubString.isEmpty()) {
            return _currentOffset;
        }
        int i = 0;
        int depth = 0;
        for (char chr : sigSubString.toCharArray()) {
            if (chr == _openChar) {
                ++depth;
            } else if (chr == _closeChar) {
                --depth;
            }
            if (depth == 0) {
                return _currentOffset + i;
            }
            ++i;
        }
        throw new IllegalStateException("Unable to parse signature for empty collection");
    }

    private static String determineSubSignature(byte[] _sigb, int _currentOffset) {
        byte[] restSigbytes = Arrays.copyOfRange(_sigb, _currentOffset, _sigb.length);
        return new String(restSigbytes);
    }

    private static ECollectionSubType determineCollectionSubType(char _sig) {
        return switch (_sig) {
            case '(' -> ECollectionSubType.STRUCT;
            case '{' -> ECollectionSubType.DICT;
            case 'a' -> ECollectionSubType.ARRAY;
            default -> ECollectionSubType.PRIMITIVE;
        };
    }

    static enum ECollectionSubType {
        STRUCT,
        DICT,
        ARRAY,
        PRIMITIVE;

    }
}

