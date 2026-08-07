
package jnr.ffi.util;

import java.lang.annotation.Annotation;
import java.util.Comparator;

final class AnnotationNameComparator
implements Comparator<Annotation> {
    static final Comparator<Annotation> INSTANCE = new AnnotationNameComparator();

    AnnotationNameComparator() {
    }

    public static Comparator<Annotation> getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(Annotation o1, Annotation o2) {
        return o1.annotationType().getName().compareTo(o2.annotationType().getName());
    }

    @Override
    public boolean equals(Object other) {
        return other != null && this.getClass().equals(other.getClass());
    }
}

