
package kotlinx.serialization;

import java.lang.reflect.Type;
import java.util.List;
import kotlin.Metadata;
import kotlin.PublishedApi;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.MagicApiIntrinsics;
import kotlin.reflect.KClass;
import kotlin.reflect.KClassifier;
import kotlin.reflect.KType;
import kotlinx.serialization.ExperimentalSerializationApi;
import kotlinx.serialization.InternalSerializationApi;
import kotlinx.serialization.KSerializer;
import kotlinx.serialization.SerializersKt__SerializersJvmKt;
import kotlinx.serialization.SerializersKt__SerializersKt;
import kotlinx.serialization.modules.SerializersModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(mv={1, 9, 0}, k=4, xi=48, d1={"kotlinx/serialization/SerializersKt__SerializersJvmKt", "kotlinx/serialization/SerializersKt__SerializersKt"})
public final class SerializersKt {
    @NotNull
    public static final KSerializer<Object> serializer(@NotNull Type type) {
        return SerializersKt__SerializersJvmKt.serializer(type);
    }

    @Nullable
    public static final KSerializer<Object> serializerOrNull(@NotNull Type type) {
        return SerializersKt__SerializersJvmKt.serializerOrNull(type);
    }

    @NotNull
    public static final KSerializer<Object> serializer(@NotNull SerializersModule $this$serializer, @NotNull Type type) {
        return SerializersKt__SerializersJvmKt.serializer($this$serializer, type);
    }

    @Nullable
    public static final KSerializer<Object> serializerOrNull(@NotNull SerializersModule $this$serializerOrNull, @NotNull Type type) {
        return SerializersKt__SerializersJvmKt.serializerOrNull($this$serializerOrNull, type);
    }

    public static final /* synthetic */ <T> KSerializer<T> serializer() {
        Intrinsics.reifiedOperationMarker(6, "T");
        MagicApiIntrinsics.voidMagicApiCall("kotlinx.serialization.serializer.simple");
        return SerializersKt.serializer(null);
    }

    public static final /* synthetic */ <T> KSerializer<T> serializer(SerializersModule $this$serializer) {
        SerializersModule serializersModule = $this$serializer;
        Intrinsics.reifiedOperationMarker(6, "T");
        MagicApiIntrinsics.voidMagicApiCall("kotlinx.serialization.serializer.withModule");
        return SerializersKt.serializer(serializersModule, null);
    }

    @NotNull
    public static final KSerializer<Object> serializer(@NotNull KType type) {
        return SerializersKt__SerializersKt.serializer(type);
    }

    @ExperimentalSerializationApi
    @NotNull
    public static final KSerializer<Object> serializer(@NotNull KClass<?> kClass, @NotNull List<? extends KSerializer<?>> typeArgumentsSerializers, boolean isNullable) {
        return SerializersKt__SerializersKt.serializer(kClass, typeArgumentsSerializers, isNullable);
    }

    @Nullable
    public static final KSerializer<Object> serializerOrNull(@NotNull KType type) {
        return SerializersKt__SerializersKt.serializerOrNull(type);
    }

    @NotNull
    public static final KSerializer<Object> serializer(@NotNull SerializersModule $this$serializer, @NotNull KType type) {
        return SerializersKt__SerializersKt.serializer($this$serializer, type);
    }

    @ExperimentalSerializationApi
    @NotNull
    public static final KSerializer<Object> serializer(@NotNull SerializersModule $this$serializer, @NotNull KClass<?> kClass, @NotNull List<? extends KSerializer<?>> typeArgumentsSerializers, boolean isNullable) {
        return SerializersKt__SerializersKt.serializer($this$serializer, kClass, typeArgumentsSerializers, isNullable);
    }

    @Nullable
    public static final KSerializer<Object> serializerOrNull(@NotNull SerializersModule $this$serializerOrNull, @NotNull KType type) {
        return SerializersKt__SerializersKt.serializerOrNull($this$serializerOrNull, type);
    }

    @Nullable
    public static final List<KSerializer<Object>> serializersForParameters(@NotNull SerializersModule $this$serializersForParameters, @NotNull List<? extends KType> typeArguments, boolean failOnMissingTypeArgSerializer) {
        return SerializersKt__SerializersKt.serializersForParameters($this$serializersForParameters, typeArguments, failOnMissingTypeArgSerializer);
    }

    @InternalSerializationApi
    @NotNull
    public static final <T> KSerializer<T> serializer(@NotNull KClass<T> $this$serializer) {
        return SerializersKt__SerializersKt.serializer($this$serializer);
    }

    @InternalSerializationApi
    @Nullable
    public static final <T> KSerializer<T> serializerOrNull(@NotNull KClass<T> $this$serializerOrNull) {
        return SerializersKt__SerializersKt.serializerOrNull($this$serializerOrNull);
    }

    @Nullable
    public static final KSerializer<? extends Object> parametrizedSerializerOrNull(@NotNull KClass<Object> $this$parametrizedSerializerOrNull, @NotNull List<? extends KSerializer<Object>> serializers, @NotNull Function0<? extends KClassifier> elementClassifierIfArray) {
        return SerializersKt__SerializersKt.parametrizedSerializerOrNull($this$parametrizedSerializerOrNull, serializers, elementClassifierIfArray);
    }

    @PublishedApi
    @NotNull
    public static final KSerializer<?> noCompiledSerializer(@NotNull String forClass) {
        return SerializersKt__SerializersKt.noCompiledSerializer(forClass);
    }

    @PublishedApi
    @NotNull
    public static final KSerializer<?> noCompiledSerializer(@NotNull SerializersModule module, @NotNull KClass<?> kClass) {
        return SerializersKt__SerializersKt.noCompiledSerializer(module, kClass);
    }

    @PublishedApi
    @NotNull
    public static final KSerializer<?> noCompiledSerializer(@NotNull SerializersModule module, @NotNull KClass<?> kClass, @NotNull KSerializer<?>[] argSerializers) {
        return SerializersKt__SerializersKt.noCompiledSerializer(module, kClass, argSerializers);
    }
}

