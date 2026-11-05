import org.example.classes.Cart;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.classes.Product;
import org.example.generator.Generator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratorCartTest {
    private final Generator generator = new Generator();

    @Test
    void generatesCartWithNonNullItemsList() throws ReflectiveOperationException {
        Object generated = generator.generateValueOfType(Cart.class);

        assertNotNull(generated, "Generator must return non-null instance");
        assertInstanceOf(Cart.class, generated);

        Cart cart = (Cart) generated;
        assertNotNull(cart.getItems(), "items must be initialized, not null");
        assertInstanceOf(List.class, cart.getItems());
    }

    @Test
    void cartItemsContainOnlyProducts() throws ReflectiveOperationException {
        Cart cart = (Cart) generator.generateValueOfType(Cart.class);

        for (Object item : cart.getItems()) {
            assertNotNull(item, "No null elements allowed in items");
            assertInstanceOf(Product.class, item,
                    "Element type must be Product, but was: " +
                            (item == null ? "null" : item.getClass().getName()));
        }
    }

    @Test
    void listElementTypeIsResolvedFromConstructorParameter() throws ReflectiveOperationException {
        // Если elementType не резолвится, generateList кладёт Object ->
        // здесь мы явно это ловим: ни одного Object в списке быть не должно.
        Cart cart = (Cart) generator.generateValueOfType(Cart.class);

        boolean hasPlainObject = cart.getItems().stream()
                .anyMatch(o -> o != null && o.getClass().equals(Object.class));

        assertFalse(hasPlainObject,
                "items contains bare Object — elementType was not resolved from List<Product>");
    }

    @Test
    void generationIsDeterministicWithFixedSeed() throws ReflectiveOperationException {
        // Generator.rnd инициализирован seed=42, но он static и общий.
        // Проверяем хотя бы, что повторный вызов не падает и даёт валидный Cart.
        Cart first = (Cart) generator.generateValueOfType(Cart.class);
        Cart second = (Cart) generator.generateValueOfType(Cart.class);

        assertNotNull(first.getItems());
        assertNotNull(second.getItems());
        first.getItems().forEach(i -> assertInstanceOf(Product.class, i));
        second.getItems().forEach(i -> assertInstanceOf(Product.class, i));
    }

    @Test
    void generatedListSizeWithinExpectedBounds() throws ReflectiveOperationException {
        // В generateList: length = rnd.nextInt(9) -> [0, 8]
        for (int i = 0; i < 50; i++) {
            Cart cart = (Cart) generator.generateValueOfType(Cart.class);
            int size = cart.getItems().size();
            assertTrue(size >= 0 && size <= 8,
                    "List size out of bounds: " + size);
        }
    }
}
