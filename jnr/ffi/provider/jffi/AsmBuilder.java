
package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Function;
import com.kenai.jffi.ObjectParameterInfo;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import jnr.ffi.Runtime;
import jnr.ffi.Variable;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.jffi.AsmClassLoader;
import jnr.ffi.provider.jffi.AsmUtil;
import jnr.ffi.provider.jffi.CodegenUtils;
import jnr.ffi.provider.jffi.SkinnyMethodAdapter;
import org.objectweb.asm.ClassVisitor;

class AsmBuilder {
    private final Runtime runtime;
    private final String classNamePath;
    private final ClassVisitor classVisitor;
    private final AsmClassLoader classLoader;
    private final ObjectNameGenerator functionId = new ObjectNameGenerator("functionAddress");
    private final ObjectNameGenerator contextId = new ObjectNameGenerator("callContext");
    private final ObjectNameGenerator toNativeConverterId = new ObjectNameGenerator("toNativeConverter");
    private final ObjectNameGenerator toNativeContextId = new ObjectNameGenerator("toNativeContext");
    private final ObjectNameGenerator fromNativeConverterId = new ObjectNameGenerator("fromNativeConverter");
    private final ObjectNameGenerator fromNativeContextId = new ObjectNameGenerator("fromNativeContext");
    private final ObjectNameGenerator objectParameterInfoId = new ObjectNameGenerator("objectParameterInfo");
    private final ObjectNameGenerator variableAccessorId = new ObjectNameGenerator("variableAccessor");
    private final ObjectNameGenerator genericObjectId = new ObjectNameGenerator("objectField");
    private final Map<ToNativeConverter, ObjectField> toNativeConverters = new IdentityHashMap<ToNativeConverter, ObjectField>();
    private final Map<ToNativeContext, ObjectField> toNativeContexts = new IdentityHashMap<ToNativeContext, ObjectField>();
    private final Map<FromNativeConverter, ObjectField> fromNativeConverters = new IdentityHashMap<FromNativeConverter, ObjectField>();
    private final Map<FromNativeContext, ObjectField> fromNativeContexts = new IdentityHashMap<FromNativeContext, ObjectField>();
    private final Map<ObjectParameterInfo, ObjectField> objectParameterInfo = new HashMap<ObjectParameterInfo, ObjectField>();
    private final Map<Variable, ObjectField> variableAccessors = new HashMap<Variable, ObjectField>();
    private final Map<CallContext, ObjectField> callContextMap = new HashMap<CallContext, ObjectField>();
    private final Map<Long, ObjectField> functionAddresses = new HashMap<Long, ObjectField>();
    private final Map<Object, ObjectField> genericObjects = new IdentityHashMap<Object, ObjectField>();
    private final List<ObjectField> objectFields = new ArrayList<ObjectField>();

    AsmBuilder(Runtime runtime, String classNamePath, ClassVisitor classVisitor, AsmClassLoader classLoader) {
        this.runtime = runtime;
        this.classNamePath = classNamePath;
        this.classVisitor = classVisitor;
        this.classLoader = classLoader;
    }

    public String getClassNamePath() {
        return this.classNamePath;
    }

    ClassVisitor getClassVisitor() {
        return this.classVisitor;
    }

    public AsmClassLoader getClassLoader() {
        return this.classLoader;
    }

    public Runtime getRuntime() {
        return this.runtime;
    }

    <T> ObjectField addField(Map<T, ObjectField> map, T value, Class klass, ObjectNameGenerator objectNameGenerator) {
        ObjectField field = new ObjectField(objectNameGenerator.generateName(), value, klass);
        this.objectFields.add(field);
        map.put(value, field);
        return field;
    }

    <T> ObjectField getField(Map<T, ObjectField> map, T value, Class klass, ObjectNameGenerator objectNameGenerator) {
        ObjectField field = map.get(value);
        return field != null ? field : this.addField(map, value, klass, objectNameGenerator);
    }

    String getCallContextFieldName(Function function) {
        return this.getField(this.callContextMap, function.getCallContext(), CallContext.class, (ObjectNameGenerator)this.contextId).name;
    }

    String getCallContextFieldName(CallContext callContext) {
        return this.getField(this.callContextMap, callContext, CallContext.class, (ObjectNameGenerator)this.contextId).name;
    }

    String getFunctionAddressFieldName(Function function) {
        return this.getField(this.functionAddresses, Long.valueOf((long)function.getFunctionAddress()), Long.TYPE, (ObjectNameGenerator)this.functionId).name;
    }

    ObjectField getRuntimeField() {
        return this.getObjectField(this.runtime, this.runtime.getClass());
    }

    String getFromNativeConverterName(FromNativeConverter converter) {
        return this.getFromNativeConverterField((FromNativeConverter)converter).name;
    }

    String getToNativeConverterName(ToNativeConverter converter) {
        return this.getToNativeConverterField((ToNativeConverter)converter).name;
    }

    private static Class nearestClass(Object obj, Class defaultClass) {
        return Modifier.isPublic(obj.getClass().getModifiers()) ? obj.getClass() : defaultClass;
    }

    ObjectField getToNativeConverterField(ToNativeConverter converter) {
        return this.getField(this.toNativeConverters, converter, AsmBuilder.nearestClass(converter, ToNativeConverter.class), this.toNativeConverterId);
    }

    ObjectField getFromNativeConverterField(FromNativeConverter converter) {
        return this.getField(this.fromNativeConverters, converter, AsmBuilder.nearestClass(converter, FromNativeConverter.class), this.fromNativeConverterId);
    }

    ObjectField getToNativeContextField(ToNativeContext context) {
        return this.getField(this.toNativeContexts, context, AsmBuilder.nearestClass(context, ToNativeContext.class), this.toNativeContextId);
    }

    ObjectField getFromNativeContextField(FromNativeContext context) {
        return this.getField(this.fromNativeContexts, context, AsmBuilder.nearestClass(context, FromNativeContext.class), this.fromNativeContextId);
    }

    String getObjectParameterInfoName(ObjectParameterInfo info) {
        return this.getField(this.objectParameterInfo, info, ObjectParameterInfo.class, (ObjectNameGenerator)this.objectParameterInfoId).name;
    }

    String getObjectFieldName(Object obj, Class klass) {
        return this.getField(this.genericObjects, obj, (Class)klass, (ObjectNameGenerator)this.genericObjectId).name;
    }

    ObjectField getObjectField(Object obj, Class klass) {
        return this.getField(this.genericObjects, obj, klass, this.genericObjectId);
    }

    String getVariableName(Variable variableAccessor) {
        return this.getField(this.variableAccessors, variableAccessor, Variable.class, (ObjectNameGenerator)this.variableAccessorId).name;
    }

    ObjectField[] getObjectFieldArray() {
        return this.objectFields.toArray(new ObjectField[this.objectFields.size()]);
    }

    Object[] getObjectFieldValues() {
        Object[] fieldObjects = new Object[this.objectFields.size()];
        int i = 0;
        for (ObjectField f : this.objectFields) {
            fieldObjects[i++] = f.value;
        }
        return fieldObjects;
    }

    void emitFieldInitialization(SkinnyMethodAdapter init, int objectsParameterIndex) {
        int i = 0;
        for (ObjectField f : this.objectFields) {
            this.getClassVisitor().visitField(18, f.name, CodegenUtils.ci(f.klass), null, null);
            init.aload(0);
            init.aload(objectsParameterIndex);
            init.pushInt(i++);
            init.aaload();
            if (f.klass.isPrimitive()) {
                Class boxedType = AsmUtil.boxedType(f.klass);
                init.checkcast(boxedType);
                AsmUtil.unboxNumber(init, boxedType, f.klass);
            } else {
                init.checkcast(f.klass);
            }
            init.putfield(this.getClassNamePath(), f.name, CodegenUtils.ci(f.klass));
        }
    }

    private static final class ObjectNameGenerator {
        private final String baseName;
        private int value;

        ObjectNameGenerator(String baseName) {
            this.baseName = baseName;
            this.value = 0;
        }

        String generateName() {
            return this.baseName + "_" + ++this.value;
        }
    }

    public static final class ObjectField {
        public final String name;
        public final Object value;
        public final Class klass;

        public ObjectField(String fieldName, Object fieldValue, Class fieldClass) {
            this.name = fieldName;
            this.value = fieldValue;
            this.klass = fieldClass;
        }
    }
}

