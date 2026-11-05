import org.example.generator.Generator;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class CustomClassesTest {


    // простой класс с примитивом и String
    public class SimpleUser {
        private final int age;
        private final String name;

        public SimpleUser(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public int getAge() { return age; }
        public String getName() { return name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleUser u)) return false;
            return age == u.age && Objects.equals(name, u.name);
        }
        @Override public int hashCode() { return Objects.hash(age, name); }
    }

    // вложенный пользовательский класс + enum
    enum Color { RED, GREEN, BLUE }

    class Order {
        private final SimpleUser user;
        private final Color color;
        private final long id;

        public Order(SimpleUser user, Color color, long id) {
            this.user = user;
            this.color = color;
            this.id = id;
        }

        public SimpleUser getUser() { return user; }
        public Color getColor() { return color; }
        public long getId() { return id; }
    }

    // класс без явного конструктора (дефолтный)
    class EmptyClass {
        public EmptyClass() {}
    }

    // класс только с приватным конструктором
    class PrivateCtor {
        private final int x;
        private PrivateCtor(int x) { this.x = x; }
        public int getX() { return x; }
    }
    private final Generator generator = new Generator();

    @Test
    void simpleUser_isGeneratedWithPrimitiveAndString() throws Exception {
        Object result = generator.generateValueOfType(SimpleUser.class);

        assertInstanceOf(SimpleUser.class, result);
        SimpleUser user = (SimpleUser) result;
        assertNotNull(user.getName(), "String-поле не должно быть null");
        assertFalse(user.getName().isEmpty() || user.getName() == null);
    }

    @Test
    void order_isGeneratedRecursivelyWithNestedUserAndEnum() throws Exception {
        Object result = generator.generateValueOfType(Order.class);

        assertInstanceOf(Order.class, result);
        Order order = (Order) result;

        assertNotNull(order.getUser(), "вложенный пользовательский класс должен быть создан");
        assertNotNull(order.getUser().getName());
        assertNotNull(order.getColor(), "enum должен быть выбран");
        assertTrue(order.getColor() instanceof Color);
    }

    @Test
    void emptyClass_defaultConstructorIsUsed() throws Exception {
        Object result = generator.generateValueOfType(EmptyClass.class);
        assertInstanceOf(EmptyClass.class, result);
    }

    @Test
    void privateConstructor_isAccessedViaReflection() throws Exception {
        Object result = generator.generateValueOfType(PrivateCtor.class);
        assertInstanceOf(PrivateCtor.class, result);
        // просто проверяем, что инстанс создан, значение x не проверяем —
        // оно сгенерировано случайно, но инстанс существует
    }

    @Test
    void interface_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateValueOfType(Runnable.class));
    }

    @Test
    void abstractClass_isRejected() {
        abstract class AbstractThing {}
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateValueOfType(AbstractThing.class));
    }

    @Test
    void voidClass_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateValueOfType(void.class));
    }

    @Test
    void constructorWithMinimalWeight_isChosen() throws Exception {
        // класс с двумя конструкторами: (int) вес=1 и (SimpleUser, long) вес=5+1=6
        // должен быть выбран конструктор с int
        class TwoCtors {
            final Object marker;
            public TwoCtors(int x) { this.marker = "int"; }
            public TwoCtors(SimpleUser u, long id) { this.marker = "user"; }
        }

        TwoCtors result = (TwoCtors) generator.generateValueOfType(TwoCtors.class);
        assertEquals("int", result.marker,
                "должен быть выбран конструктор с минимальным весом (int → вес 1)");
    }

}
