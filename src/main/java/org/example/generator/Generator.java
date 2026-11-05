package org.example.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
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

    private Object generateValueOfCustomClass(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        Constructor<?> bestConstructor = Arrays.stream(clazz.getDeclaredConstructors())
                .min(Comparator.comparingInt(CONSTRUCTOR_WEIGHT))
                .orElseThrow(() -> new IllegalArgumentException("No constructors found for " + clazz));

        bestConstructor.setAccessible(true);
        Class<?> [] parameterTypes = bestConstructor.getParameterTypes();
        Object [] args = new Object[parameterTypes.length];

        for (int i  = 0; i < parameterTypes.length; i++){
            args[i] = generateValueOfType(parameterTypes[i]);
        }

        return bestConstructor.newInstance(args);
    }

    private Object generateString(Class<?> clazz) {
        int length = rnd.nextInt(9);
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    private Object generateArray(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
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

    public Object generateValueOfType(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {

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
        if(clazz.isInterface() || clazz.isAnonymousClass()){
            throw new IllegalArgumentException(
                    "Cannot instantiate interface/abstract class: " + clazz);
        }
        if (clazz.isArray()){
            return generateArray(clazz);
        }
        return generateValueOfCustomClass(clazz);
    }
}
