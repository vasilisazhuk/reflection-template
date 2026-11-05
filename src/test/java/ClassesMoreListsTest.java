import org.example.classes.Cart;
import org.example.classes.Product;
import org.example.generator.Generatable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.generator.Generator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassesMoreListsTest {

    //при тесте не статиком вызвал рандомный конструктор (?)
    /*
    * суть в том что вместо product возвращает object. как вариант отсмотреть в баут коде
    */
    public static class Order {

        private final List<Product> products;


        private final List<String> tags;

        private final List<Integer> quantities;

        public Order( @Generatable(Product.class) List<Product> products,
                      @Generatable(String.class) List<String> tags,
                      @Generatable(Integer.class) List<Integer> quantities) {
            this.products = products;
            this.tags = tags;
            this.quantities = quantities;
        }

        public List<Product> getProducts() { return products; }
        public List<String> getTags() { return tags; }
        public List<Integer> getQuantities() { return quantities; }
    }

    private final Generator generator = new Generator();

    @Test
    void generatesObjectWithMultipleLists() throws ReflectiveOperationException {
        Object generated = generator.generateValueOfType(Order.class);

        assertNotNull(generated);
        assertInstanceOf(Order.class, generated);

        Order order = (Order) generated;
        assertNotNull(order.getProducts(), "products must not be null");
        assertNotNull(order.getTags(), "tags must not be null");
        assertNotNull(order.getQuantities(), "quantities must not be null");
    }

    @Test
    void debugConstructorGenericSignature() throws Exception {
        var ctor = Cart.class.getDeclaredConstructors()[0];
        var param = ctor.getParameters()[0];

        System.out.println("raw type        : " + param.getType());
        System.out.println("parameterized   : " + param.getParameterizedType());
        System.out.println("generic type    : " + ctor.getGenericParameterTypes()[0]);
        System.out.println("is Parameterized: " +
                (param.getParameterizedType() instanceof java.lang.reflect.ParameterizedType));
        System.out.println("@Generatable on param: " +
                param.getAnnotation(Generatable.class));
        System.out.println("@Generatable on field: " +
                Cart.class.getDeclaredField("items").getAnnotation(Generatable.class));
    }
    @Test
    void eachListGetsItsOwnElementType() throws ReflectiveOperationException {
        Order order = (Order) generator.generateValueOfType(Order.class);

        // products -> Product
        for (Object p : order.getProducts()) {
            assertNotNull(p);
            assertInstanceOf(Product.class, p,
                    "products element must be Product, was: " + p.getClass().getName());
        }

        // tags -> String
        for (Object t : order.getTags()) {
            assertNotNull(t);
            assertInstanceOf(String.class, t,
                    "tags element must be String, was: " + t.getClass().getName());
        }

        // quantities -> Integer
        for (Object q : order.getQuantities()) {
            assertNotNull(q);
            assertInstanceOf(Integer.class, q,
                    "quantities element must be Integer, was: " + q.getClass().getName());
        }
    }

    @Test
    void noBareObjectsInAnyList() throws ReflectiveOperationException {
        // Если resolveElementType "промахнётся" и вернёт null,
        // generateList подставит Object.class -> в списках появятся new Object().
        Order order = (Order) generator.generateValueOfType(Order.class);

        assertNoPlainObject(order.getProducts(), "products");
        assertNoPlainObject(order.getTags(), "tags");
        assertNoPlainObject(order.getQuantities(), "quantities");
    }

    @Test
    void listSizesWithinBounds() throws ReflectiveOperationException {
        // generateList: rnd.nextInt(9) -> [0, 8]
        for (int i = 0; i < 50; i++) {
            Order order = (Order) generator.generateValueOfType(Order.class);
            assertSizeInBounds(order.getProducts(), "products");
            assertSizeInBounds(order.getTags(), "tags");
            assertSizeInBounds(order.getQuantities(), "quantities");
        }
    }

    // --- helpers ---

    private static void assertNoPlainObject(List<?> list, String name) {
        boolean hasPlainObject = list.stream()
                .anyMatch(o -> o != null && o.getClass().equals(Object.class));
        assertFalse(hasPlainObject,
                name + " contains bare Object — elementType was not resolved");
    }

    private static void assertSizeInBounds(List<?> list, String name) {
        int size = list.size();
        assertTrue(size >= 0 && size <= 8,
                name + " size out of bounds: " + size);
    }
}

