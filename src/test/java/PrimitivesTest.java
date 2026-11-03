import org.example.generator.Generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;


//тесты сгенерированы
class PrimitivesTest {

    private final Generator generator = new Generator();

    @Test
    void intPrimitive_returnsInteger() throws Exception {
        Object result = generator.generateValueOfType(int.class);
        assertInstanceOf(Integer.class, result);
    }

    @Test
    void integerWrapper_returnsInteger() throws Exception {
        Object result = generator.generateValueOfType(Integer.class);
        assertInstanceOf(Integer.class, result);
    }

    @Test
    void longPrimitive_returnsLong() throws Exception {
        Object result = generator.generateValueOfType(long.class);
        assertInstanceOf(Long.class, result);
    }

    @Test
    void longWrapper_returnsLong() throws Exception {
        Object result = generator.generateValueOfType(Long.class);
        assertInstanceOf(Long.class, result);
    }

    @Test
    void doublePrimitive_returnsDouble() throws Exception {
        Object result = generator.generateValueOfType(double.class);
        assertInstanceOf(Double.class, result);
    }

    @Test
    void doubleWrapper_returnsDouble() throws Exception {
        Object result = generator.generateValueOfType(Double.class);
        assertInstanceOf(Double.class, result);
    }

    @Test
    void floatPrimitive_returnsFloat() throws Exception {
        Object result = generator.generateValueOfType(float.class);
        assertInstanceOf(Float.class, result);
    }

    @Test
    void floatWrapper_returnsFloat() throws Exception {
        Object result = generator.generateValueOfType(Float.class);
        assertInstanceOf(Float.class, result);
    }

    @Test
    void booleanPrimitive_returnsBoolean() throws Exception {
        Object result = generator.generateValueOfType(boolean.class);
        assertInstanceOf(Boolean.class, result);
    }

    @Test
    void booleanWrapper_returnsBoolean() throws Exception {
        Object result = generator.generateValueOfType(Boolean.class);
        assertInstanceOf(Boolean.class, result);
    }

    @Test
    void charPrimitive_returnsCharacterInRange() throws Exception {
        Object result = generator.generateValueOfType(char.class);
        assertInstanceOf(Character.class, result);
        char c = (Character) result;
        assertTrue(c >= 'a' && c <= 'z', "char must be in [a..z], but was " + c);
    }

    @Test
    void charWrapper_returnsCharacterInRange() throws Exception {
        Object result = generator.generateValueOfType(Character.class);
        assertInstanceOf(Character.class, result);
        char c = (Character) result;
        assertTrue(c >= 'a' && c <= 'z', "char must be in [a..z], but was " + c);
    }

    @Test
    void bytePrimitive_returnsByte() throws Exception {
        Object result = generator.generateValueOfType(byte.class);
        assertInstanceOf(Byte.class, result);
    }

    @Test
    void byteWrapper_returnsByte() throws Exception {
        Object result = generator.generateValueOfType(Byte.class);
        assertInstanceOf(Byte.class, result);
    }

    @Test
    void shortPrimitive_returnsShort() throws Exception {
        Object result = generator.generateValueOfType(short.class);
        assertInstanceOf(Short.class, result);
    }

    @Test
    void shortWrapper_returnsShort() throws Exception {
        Object result = generator.generateValueOfType(Short.class);
        assertInstanceOf(Short.class, result);
    }

    @Test
    void string_returnString() throws Exception {
        Object result = generator.generateValueOfType(String.class);
        assertInstanceOf(String.class, result);
    }

    @Test
    void voidClass_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateValueOfType(void.class));
    }

    /**
     * Проверка, что генерация действительно случайна, а не константна.
     * (Не гарантирует отсутствие повторов на 100%, но на 200 вызовах вероятность
     * ложного падения для int/double практически нулевая.)
     */
    @ParameterizedTest
    @ValueSource(classes = {int.class, long.class, double.class, float.class})
    void primitiveGeneration_producesDifferentValues(Class<?> clazz) throws Exception {
        Object first = generator.generateValueOfType(clazz);
        boolean allSame = true;
        for (int i = 0; i < 200; i++) {
            Object next = generator.generateValueOfType(clazz);
            if (!first.equals(next)) {
                allSame = false;
                break;
            }
        }
        assertFalse(allSame, "Generator seems to return constant value for " + clazz);
    }
}