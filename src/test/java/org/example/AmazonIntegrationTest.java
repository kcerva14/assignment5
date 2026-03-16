package org.example;

import org.example.Amazon.Amazon;
import org.example.Amazon.Cost.DeliveryPrice;
import org.example.Amazon.Cost.ExtraCostForElectronics;
import org.example.Amazon.Cost.ItemType;
import org.example.Amazon.Cost.RegularCost;
import org.example.Amazon.Database;
import org.example.Amazon.Item;
import org.example.Amazon.ShoppingCartAdaptor;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AmazonIntegrationTest {
    private Database database;
    private Amazon amazon;
    private ShoppingCartAdaptor shoppingCart;

    @BeforeEach
    public void setup() {
        database = new Database();
        database.resetDatabase();
        shoppingCart = new ShoppingCartAdaptor(database);
    }
    @AfterEach
    public void teardown() {
        database.resetDatabase();
    }

    @Nested
    @DisplayName("ShoppingCartAdaptor w/ Database")
    class ShoppingCartAdaptorIntegrationTests {

        @Test
        @DisplayName("specification-based")
        public void correctItemInCart(){
            Item item = new Item(ItemType.OTHER, "Book", 2, 9.99);
            shoppingCart.add(item);
            List<Item> items = shoppingCart.getItems();

            assertEquals(1, items.size());
            assertEquals("Book",  items.get(0).getName());
            assertEquals(ItemType.OTHER,items.get(0).getType());
            assertEquals(2,  items.get(0).getQuantity());
            assertEquals(9.99, items.get(0).getPricePerUnit());
        }

        @Test
        @DisplayName("specification-based")
        public void multipleItemsCorrectlyIn(){
            shoppingCart.add( new Item(ItemType.OTHER, "Book", 1, 10.00) );
            shoppingCart.add( new Item(ItemType.OTHER, "Pen", 1, 2.00) );
            shoppingCart.add( new Item(ItemType.ELECTRONIC, "phone", 1, 500.00) );
            List<Item> items = shoppingCart.getItems();

            assertEquals(3, items.size());
        }

        @Test
        @DisplayName("specification-based")
        public void emptyDatabase(){
            List<Item> items = shoppingCart.getItems();
            assertTrue(items.isEmpty());
        }

        @Test
        @DisplayName("structural-based")
        public void resetDatabase(){
            shoppingCart.add( new Item(ItemType.OTHER, "Book", 1, 9.99));
            database.resetDatabase();
            List<Item> items = shoppingCart.getItems();
            assertTrue(items.isEmpty());
        }

        @Test
        @DisplayName("structural-based")
        public void addSameItemTwice(){
            Item item = new Item(ItemType.OTHER, "Book", 1, 9.99);
            shoppingCart.add(item);
            shoppingCart.add(item);

            List<Item> items = shoppingCart.getItems();
            assertEquals(2, items.size());
        }
    }

    @Nested
    @DisplayName("Amazon + RegularCost & ShoppingCartAdaptor w/ Database")
    class AmazonRegularCostIntegrationTests{
        @BeforeEach
        public void setUpAmazon(){
            amazon = new Amazon(shoppingCart, List.of(new RegularCost()));
        }

        @Test
        @DisplayName("specification-based")
        public void emptyCart(){
            assertEquals(0.0, amazon.calculate());
        }

        @Test
        @DisplayName("specification-based")
        public void oneItem(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 2, 10.00));

            assertEquals(20.0, amazon.calculate());
        }

        @Test
        @DisplayName("specification-based")
        public void twoItems(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 2, 10.00));
            amazon.addToCart(new Item(ItemType.ELECTRONIC, "phone", 1, 500.00));

            assertEquals(520.0, amazon.calculate());
        }

        @Test
        @DisplayName("structural-based")
        public void calculateCalledTwice(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 2, 10.00));

            assertEquals(amazon.calculate(), amazon.calculate());
        }
    }

    @Nested
    @DisplayName("Amazon w/ DeliveryPrice")
    class AmazonDeliveryIntegrationTests{
        @BeforeEach
        public void setUpAmazon(){
            amazon = new Amazon(shoppingCart, List.of(new DeliveryPrice()));
        }

        @Test
        @DisplayName("specification-based")
        public void oneItem(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 10.00));
            assertEquals(5.00, amazon.calculate());
        }

        @Test
        @DisplayName("specification-based")
        public void fourItems(){
            for (int i = 0; i < 4; i++){
                amazon.addToCart(new Item(ItemType.OTHER, "Item" + i, 1, 1.0));
            }
            assertEquals(12.50, amazon.calculate());
        }

        @Test
        @DisplayName("specification-based")
        public void elevnItems(){
            for (int i = 0; i < 11; i++){
                amazon.addToCart(new Item(ItemType.OTHER, "Item" + i, 1, 1.0));
            }
            assertEquals(20.00, amazon.calculate());
        }

        @Test
        @DisplayName("structural-based")
        public void singleRowMultipleItems(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 15, 10.00));

            assertEquals(5.00, amazon.calculate());
        }
    }

    @Nested
    @DisplayName("Amazon w/ ExtraCostForElectronics")
    class AmazonElectronicsIntegrationTests{
        @BeforeEach
        public void setUpAmazon(){
            amazon = new Amazon(shoppingCart, List.of(new ExtraCostForElectronics()));
        }

        @Test
        @DisplayName("specification-based")
        public void addsExtraCost(){
            amazon.addToCart(new Item(ItemType.ELECTRONIC, "phone", 1, 300.00));
            assertEquals(7.50, amazon.calculate());
        }

        @Test
        @DisplayName("specification-based")
        public void noExtraCost(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 30.00));
            assertEquals(0.0, amazon.calculate());
        }

        @Test
        @DisplayName("structural-based")
        public void flatExtraCost(){
            amazon.addToCart(new Item(ItemType.ELECTRONIC, "phone", 1, 300.00));
            amazon.addToCart(new Item(ItemType.ELECTRONIC, "laptop", 1, 600.00));
            assertEquals(7.50, amazon.calculate());
        }
    }

    @Nested
    @DisplayName("Full Integration Tests")
    class FullIntegrationTests{
        @BeforeEach
        public void setUpAmazon(){
            amazon = new Amazon(shoppingCart, List.of(
                    new RegularCost(),
                    new DeliveryPrice(),
                    new ExtraCostForElectronics()
            ));
        }

        @Test
        @DisplayName("specification-based")
        public void emptyCart(){
            assertEquals(0.0, amazon.calculate());
        }

        @Test
        @DisplayName("specification-based")
        public void mixedWithElectronic(){
            amazon.addToCart(new Item(ItemType.ELECTRONIC, "phone", 1, 500.00));
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 2, 10.00));

            assertEquals(532.50, amazon.calculate());
        }

        @Test
        @DisplayName("specification-based")
        public void noElectronics(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 25.00));
            amazon.addToCart(new Item(ItemType.OTHER, "Bottle", 1, 15.00));

            assertEquals(45.00, amazon.calculate());
        }

        @Test
        @DisplayName("structural-based")
        public void totalUpdatesCorrectly(){
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 10.00));
            double firstTotal =  amazon.calculate();
            amazon.addToCart(new Item(ItemType.OTHER, "Pen", 1, 5.0));
            double secondTotal = amazon.calculate();

            assertNotEquals(firstTotal, secondTotal);
        }

        @Test
        @DisplayName("Structural-based")
        public void cartWithMultipleItems(){
            amazon.addToCart(new Item(ItemType.ELECTRONIC, "phone", 1, 200.00));
            for(int i = 0; i < 10; i++){
                amazon.addToCart(new Item(ItemType.OTHER, "Item" + i, 1, 5.00));
            }

            assertEquals(277.5,  amazon.calculate());
        }
    }
}