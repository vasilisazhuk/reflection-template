package org.example.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.text.Annotation;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class Generator {

    static Random rnd = new Random(42L);

    private static final Map<Class<?>, Supplier<Object>> PRIMITIVE_GENERATORS = Map.ofEntries(
            Map.entry(int.class, () -> rnd.nextInt()),
            Map.entry(Integer.class, () -> rnd.nextInt()),
            Map.entry(long.class, () -> rnd.nextLong()),
            Map.entry(Long.class, () -> rnd.nextLong()),
            Map.entry(double.class, () -> rnd.nextDouble()),
            Map.entry(Double.class, () -> rnd.nextDouble()),
            Map.entry(float.class, () -> rnd.nextFloat()),
            Map.entry(Float.class, () -> rnd.nextFloat()),
            Map.entry(boolean.class, rnd::nextBoolean),
            Map.entry(Boolean.class, rnd::nextBoolean),
            Map.entry(char.class, () -> (char) ('a' + rnd.nextInt(26))),
            Map.entry(Character.class, () -> (char) ('a' + rnd.nextInt(26))),
            Map.entry(byte.class, () -> (byte) rnd.nextInt()),
            Map.entry(Byte.class, () -> (byte) rnd.nextInt()),
            Map.entry(short.class, () -> (short) rnd.nextInt()),
            Map.entry(Short.class, () -> (short) rnd.nextInt())
    );

    //удельный вес конструктора. потом выбираем минимальный
    private static final ToIntFunction<Constructor<?>> CONSTRUCTOR_WEIGHT = constructor -> {
        int weight = 0;
        for (Class<?> ParamType : constructor.getParameterTypes()) {
            if (PRIMITIVE_GENERATORS.get(ParamType) != null || ParamType == String.class || ParamType.isEnum()) {
                weight++;
            } else {
                weight += 5;
            }
        }
        return weight;
    };
    private static Class<?> resolveElementType(java.lang.reflect.Parameter param,
                                               java.lang.reflect.AnnotatedType annotatedType){
        Generatable g = param.getAnnotation(Generatable.class);
        if (g != null) return g.value();

        java.lang.reflect.Type type = param.getParameterizedType();
        if (type instanceof java.lang.reflect.ParameterizedType pt) {
            java.lang.reflect.Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> c) return c;
        }
        return null;
    }

    private Object generateValueOfCustomClass(Class<?> clazz) throws ReflectiveOperationException {

        Constructor<?> bestConstructor = Arrays.stream(clazz.getDeclaredConstructors())
                .min(Comparator.comparingInt(CONSTRUCTOR_WEIGHT))
                .orElseThrow(() -> new IllegalArgumentException("No constructors found for " + clazz));

        bestConstructor.setAccessible(true);
        Class<?> [] parameterTypes = bestConstructor.getParameterTypes();
        java.lang.reflect.Parameter[] parameters = bestConstructor.getParameters();
        java.lang.reflect.AnnotatedType[] annotatedTypes = bestConstructor.getAnnotatedParameterTypes();

        Object [] args = new Object[parameterTypes.length];

        for (int i  = 0; i < parameterTypes.length; i++){
            //args[i] = generateValueOfType(parameterTypes[i]);
            Class<?> elementType = resolveElementType(parameters[i], annotatedTypes[i]);
            args[i] = generateValueOfType(parameterTypes[i], elementType);
        }

        return bestConstructor.newInstance(args);
    }

    private Object generateString(Class<?> clazz) {
        int length = rnd.nextInt(9);
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    private Object generateArray(Class<?> clazz) throws ReflectiveOperationException {
        Class<?> componentType = clazz.getComponentType();
        int length = rnd.nextInt(9);
        Object array = java.lang.reflect.Array.newInstance(componentType, length);
        for (int i = 0; i < length; i ++){
            java.lang.reflect.Array.set(array, i, generateValueOfType(componentType));
        }
        return array;
    }

    private Object generateEnum(Class<?> clazz) {
        Object[] e = clazz.getEnumConstants();
        return e[rnd.nextInt(e.length)];
    }

    private Object generateList(Class<?> clazz, Class<?> elementType) throws ReflectiveOperationException {

        int length = rnd.nextInt(9);
        List<Object> list = new ArrayList<>(length);
        Class<?> elem = (elementType != null) ? elementType : Object.class;
        for (int i = 0; i < length; i++){
            list.add(generateValueOfType(elem));
        }

        return list;
    }
    public Object generateValueOfType(Class<?> clazz, Class<?> elementType) throws ReflectiveOperationException {

        if (clazz == void.class) {
            throw new IllegalArgumentException("clazz must not be void");
        }

        // примитивы
        Supplier<Object> value = PRIMITIVE_GENERATORS.get(clazz);

        if (value != null) {
            return value.get();
        }
        if (clazz == String.class) {
            return generateString(clazz);
        }
        if (clazz.isEnum()) {
            return generateEnum(clazz);
        }
        if (clazz.isArray()){
            return generateArray(clazz);
        }
        if(List.class.isAssignableFrom(clazz)){
            return generateList(clazz, elementType);
        }
        if(clazz.isInterface() || clazz.isAnonymousClass() || Modifier.isAbstract(clazz.getModifiers())){
            throw new IllegalArgumentException(
                    "Cannot instantiate interface/abstract class: " + clazz);
        }

        return generateValueOfCustomClass(clazz);
    }
    public Object generateValueOfType(Class<?> clazz) throws ReflectiveOperationException {
        return generateValueOfType(clazz, null);
    }
}