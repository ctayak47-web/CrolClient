
package kotlin.jvm.internal;

import kotlin.SinceKotlin;
import kotlin.jvm.internal.CallableReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.KCallable;
import kotlin.reflect.KProperty;

public abstract class PropertyReference
extends CallableReference
implements KProperty {
    private final boolean syntheticJavaProperty;

    public PropertyReference() {
        this.syntheticJavaProperty = false;
    }

    @SinceKotlin(version="1.1")
    public PropertyReference(Object receiver) {
        super(receiver);
        this.syntheticJavaProperty = false;
    }

    @SinceKotlin(version="1.4")
    public PropertyReference(Object receiver, Class owner, String name, String signature, int flags) {
        super(receiver, owner, name, signature, (flags & 1) == 1);
        this.syntheticJavaProperty = (flags & 2) == 2;
    }

    @Override
    @SinceKotlin(version="1.1")
    protected KProperty getReflected() {
        if (this.syntheticJavaProperty) {
            throw new UnsupportedOperationException("Kotlin reflection is not yet supported for synthetic Java properties. Please follow/upvote https:
        }
        return (KProperty)super.getReflected();
    }

    @Override
    public KCallable compute() {
        return this.syntheticJavaProperty ? this : super.compute();
    }

    @Override
    @SinceKotlin(version="1.1")
    public boolean isLateinit() {
        return this.getReflected().isLateinit();
    }

    @Override
    @SinceKotlin(version="1.1")
    public boolean isConst() {
        return this.getReflected().isConst();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PropertyReference) {
            PropertyReference other = (PropertyReference)obj;
            return this.getOwner().equals(other.getOwner()) && this.getName().equals(other.getName()) && this.getSignature().equals(other.getSignature()) && Intrinsics.areEqual(this.getBoundReceiver(), other.getBoundReceiver());
        }
        if (obj instanceof KProperty) {
            return obj.equals(this.compute());
        }
        return false;
    }

    public int hashCode() {
        return (this.getOwner().hashCode() * 31 + this.getName().hashCode()) * 31 + this.getSignature().hashCode();
    }

    public String toString() {
        KCallable reflected = this.compute();
        if (reflected != this) {
            return reflected.toString();
        }
        return "property " + this.getName() + " (Kotlin reflection is not available)";
    }
}

