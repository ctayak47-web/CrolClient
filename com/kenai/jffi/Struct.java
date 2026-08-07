
package com.kenai.jffi;

import com.kenai.jffi.Aggregate;
import com.kenai.jffi.Foreign;
import com.kenai.jffi.Type;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Struct
extends Aggregate {
    private static final Map<List<Type>, StructReference> structCache = new ConcurrentHashMap<List<Type>, StructReference>();
    private static final ReferenceQueue<Struct> structReferenceQueue = new ReferenceQueue();
    private final Type[] fields;

    public static Struct newStruct(Type ... fields) {
        Struct s;
        List<Type> fieldsList = Arrays.asList(fields);
        StructReference ref = structCache.get(fieldsList);
        Struct struct = s = ref != null ? (Struct)ref.get() : null;
        if (s != null) {
            return s;
        }
        while ((ref = (StructReference)structReferenceQueue.poll()) != null) {
            structCache.remove(ref.fieldsList);
        }
        s = new Struct(Foreign.getInstance(), fields);
        structCache.put(fieldsList, new StructReference(s, structReferenceQueue, fieldsList));
        return s;
    }

    private Struct(Foreign foreign, Type ... fields) {
        super(foreign, foreign.newStruct(Type.nativeHandles(fields), false));
        this.fields = (Type[])fields.clone();
    }

    @Deprecated
    public Struct(Type ... fields) {
        super(Foreign.getInstance(), Foreign.getInstance().newStruct(Type.nativeHandles(fields), false));
        this.fields = (Type[])fields.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Arrays.equals(this.fields, ((Struct)o).fields);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(this.fields);
        return result;
    }

    private static final class StructReference
    extends WeakReference<Struct> {
        List<Type> fieldsList;

        private StructReference(Struct struct, ReferenceQueue<? super Struct> referenceQueue, List<Type> fieldsList) {
            super(struct, referenceQueue);
            this.fieldsList = fieldsList;
        }
    }
}

