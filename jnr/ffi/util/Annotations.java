
package jnr.ffi.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import jnr.ffi.util.AnnotationNameComparator;

public final class Annotations {
    public static final Collection<Annotation> EMPTY_ANNOTATIONS = Collections.emptyList();

    private Annotations() {
    }

    public static Collection<Annotation> sortedAnnotationCollection(Annotation[] annotations) {
        if (annotations.length > 1) {
            return Annotations.sortedAnnotationCollection(Arrays.asList(annotations));
        }
        if (annotations.length > 0) {
            return Collections.singletonList(annotations[0]);
        }
        return Collections.emptyList();
    }

    public static Collection<Annotation> sortedAnnotationCollection(Collection<Annotation> annotations) {
        if (annotations.size() < 2 || annotations instanceof SortedSet && ((SortedSet)annotations).comparator() instanceof AnnotationNameComparator) {
            return annotations;
        }
        TreeSet<Annotation> sorted2 = new TreeSet<Annotation>(AnnotationNameComparator.getInstance());
        sorted2.addAll(annotations);
        return Collections.unmodifiableSortedSet(sorted2);
    }

    public static final Collection<Annotation> mergeAnnotations(Collection<Annotation> a, Collection<Annotation> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return EMPTY_ANNOTATIONS;
        }
        if (!a.isEmpty() && b.isEmpty()) {
            return a;
        }
        if (a.isEmpty() && !b.isEmpty()) {
            return b;
        }
        ArrayList<Annotation> all = new ArrayList<Annotation>(a);
        all.addAll(b);
        return Annotations.sortedAnnotationCollection(all);
    }

    public static final Collection<Annotation> mergeAnnotations(Collection<Annotation> ... collections) {
        int totalLength = 0;
        for (Collection<Annotation> c : collections) {
            totalLength += c.size();
        }
        ArrayList<Annotation> all = new ArrayList<Annotation>(totalLength);
        for (Collection<Annotation> c : collections) {
            all.addAll(c);
        }
        return Annotations.sortedAnnotationCollection(all);
    }
}

