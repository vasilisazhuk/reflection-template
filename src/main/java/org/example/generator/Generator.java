package org.example.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class Generator {

    static Random rnd = new Random();

    private static final Map<Class<?>, Supplier<Object>> PRIMITIVE_GENERATORS = Map.ofEntries(
            Map.entry(int.class,     () -> rnd.nextInt()),
            Map.entry(Integer.class, () -> rnd.nextInt()),
            Map.entry(long.class,    () -> rnd.nextLong()),
            Map.entry(Long.class,    () -> rnd.nextLong()),
            Map.entry(double.class,  () -> rnd.nextDouble()),
            Map.entry(Double.class,  () -> rnd.nextDouble()),
            Map.entry(float.class,   () -> rnd.nextFloat()),
            Map.entry(Float.class,   () -> rnd.nextFloat()),
            Map.entry(boolean.class, rnd::nextBoolean),
            Map.entry(Boolean.class, rnd::nextBoolean),
            Map.entry(char.class,    () -> (char) ('a' + rnd.nextInt(26))),
            Map.entry(Character.class, () -> (char) ('a' + rnd.nextInt(26))),
            Map.entry(byte.class,    () -> (byte) rnd.nextInt()),
            Map.entry(Byte.class,    () -> (byte) rnd.nextInt()),
            Map.entry(short.class,   () -> (short) rnd.nextInt()),
            Map.entry(Short.class,   () -> (short) rnd.nextInt())
    );

    private Object generateValueOfCustomClass(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        //todo
        return new Object();
    }

    private Object generateString(Class<?> clazz) {
        byte[] array = new byte[9];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
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
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        int randomConstructorIndex = new Random().nextInt(constructors.length);
        Constructor<?> randomConstructor = constructors[randomConstructorIndex];
        return randomConstructor.newInstance(111);
    }
}
