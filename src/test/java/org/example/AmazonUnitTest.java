package org.example;

import org.example.Amazon.*;
import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AmazonUnitTest {

    //Amazon Class AmazonTests

    @Nested
    @DisplayName("Amazon AmazonTests")
    class AmazonTests {

        private ShoppingCart mockCart;
        private PriceRule mockRule1;
        private PriceRule mockRule2;

        @BeforeEach
        public void setUp() {
            mockCart = Mockito.mock(ShoppingCart.class);
            mockRule1 = Mockito.mock(PriceRule.class);
            mockRule2 = Mockito.mock(PriceRule.class);
        }

        @Test
        @DisplayName("Specification-based")
        public void calculateSumsAll(){
            List<Item>items = List.of(new Item(ItemType.OTHER, "Book", 1, 10.0));
            when(mockCart.getItems()).thenReturn(items);
            when(mockRule1.priceToAggregate(items)).thenReturn(10.0);
            when(mockRule2.priceToAggregate(items)).thenReturn(5.0);

            Amazon amazon = new Amazon(mockCart, List.of(mockRule1, mockRule2));
            assertEquals(15.0, amazon.calculate());
        }

        @Test
        @DisplayName("Specification-based")
        public void addToCartToShoppingCart(){
            Amazon amazon = new Amazon(mockCart, new ArrayList<>());
            Item item = new Item(ItemType.OTHER, "Book", 1, 10.0);
            amazon.addToCart(item);
            verify(mockCart, times(1)).add(item);
        }

        @Test
        @DisplayName("Specification-based")
        public void calculateZeroReturn(){
            when(mockCart.getItems()).thenReturn(new ArrayList<>());

            Amazon amazon = new Amazon(mockCart, new ArrayList<>());
            assertEquals(0.0, amazon.calculate());
        }

        @Test
        @DisplayName("Structural-Based")
        public void calculateCallsGetItemsOnce(){
            List <Item>items = List.of(new Item(ItemType.OTHER, "Book", 1, 10.0));
            when(mockCart.getItems()).thenReturn(items);
            when(mockRule1.priceToAggregate(items)).thenReturn(10.0);
            when(mockRule2.priceToAggregate(items)).thenReturn(20.0);

            Amazon amazon = new Amazon(mockCart, List.of(mockRule1, mockRule2));
            amazon.calculate();

            verify(mockCart, times(2)).getItems();
        }

        @Test
        @DisplayName("Structural-Based")
        public void addToCartNotCallGetItems(){
            Amazon amazon = new Amazon(mockCart, new ArrayList<>());
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 10.0));

            verify(mockCart, never()).getItems();
        }
    }

    @Nested
    @DisplayName("ShoppingCartAdaptor Tests")
    class ShoppingCartAdaptorTests {

        private Database mockDatabase;
        private java.sql.Connection mockConnection;
        private java.sql.Statement mockPs;
        private java.sql.ResultSet mockRs;

        @BeforeEach
        public void setUp() throws Exception {
            mockDatabase = Mockito.mock(Database.class);
            mockConnection = Mockito.mock(java.sql.Connection.class);
            mockPs = Mockito.mock(java.sql.Statement.class);
            mockRs = Mockito.mock(java.sql.ResultSet.class);

            when(mockDatabase.getConnection()).thenReturn(mockConnection);
        }

        @Test
        @DisplayName("Specification-based")
        public void adapterAddWithSql(){
            ShoppingCartAdaptor adapter = new ShoppingCartAdaptor(mockDatabase);
            Item item = new Item(ItemType.OTHER, "Book", 1, 10.0);

            adapter.add(item);

            verify(mockDatabase, atLeastOnce()).withSql(any());
        }

        @Test
        @DisplayName("Structural-based")
        public void addWithSql(){
            ShoppingCartAdaptor adapter = new ShoppingCartAdaptor(mockDatabase);
            adapter.add(new Item(ItemType.OTHER, "Book", 1, 10.0));

            verify(mockDatabase, times(1)).withSql(any());
        }
    }

    @Nested
    @DisplayName("DeliverPrice Tests")
    class DeliverPriceTests {
        private DeliveryPrice deliveryPrice;
        private List<Item> buildCartOfSize(int n ){
            List<Item> cart = new ArrayList<>();
            for(int i = 0; i < n; i++){
                cart.add(new Item(ItemType.OTHER, "item" + i, 1, 1.0));
            }
            return cart;
        }

        @BeforeEach
        public void setUp() {
            deliveryPrice = new  DeliveryPrice();
        }

        @Test
        @DisplayName("specification-based")
        public void emptyCartReturnsZero(){
            assertEquals(0.0, deliveryPrice.priceToAggregate(new ArrayList<>()));
        }

        @Test
        @DisplayName("specification-based")
        public void oneItem(){
            List<Item>cart = List.of(new Item(ItemType.OTHER, "Book", 1, 10.0));
            assertEquals(10.0, deliveryPrice.priceToAggregate(cart));
        }

        @Test
        @DisplayName("specification-based")
        public void threeItems(){
            List<Item> cart = buildCartOfSize(3);
            assertEquals(5.0, deliveryPrice.priceToAggregate(cart));
        }

        @Test
        @DisplayName("structural-based")
        public void largeCart(){
            List<Item>cart = buildCartOfSize(50);
            assertEquals(20.0, deliveryPrice.priceToAggregate(cart));
        }

        @Test
        @DisplayName("structural-based")
        public void twoItemsReturningFive(){
            List<Item>cart = buildCartOfSize(2);
            assertEquals(5.0, deliveryPrice.priceToAggregate(cart));
        }
    }

    @Nested
    @DisplayName("ExtraCostForElectronicsTests")
    class ExtraCostForElectronicsTests {
        private ExtraCostForElectronics electronicsFee;

        @BeforeEach
        void setUp() {
            electronicsFee = new ExtraCostForElectronics();
        }

        @Test
        @DisplayName("specification-based")
        public void emptyCartReturnsZero(){
            assertEquals(0.0, electronicsFee.priceToAggregate(new ArrayList<>()));
        }

        @Test
        @DisplayName("specification-based")
        public void cartWithNoElectronics(){
            List<Item> cart = List.of(new Item(ItemType.OTHER, "Book", 1, 10.0));
            assertEquals(0.0, electronicsFee.priceToAggregate(cart));
        }

        @Test
        @DisplayName("specification-based")
        public void cartWithOneElectronics(){
            List<Item> cart = List.of(new Item(ItemType.ELECTRONIC, "phone", 1, 300.0));
            assertEquals(7.50, electronicsFee.priceToAggregate(cart));
        }

        @Test
        @DisplayName("structural-based")
        public void multipleElectronics(){
            List<Item> cart = List.of(
                    new Item(ItemType.ELECTRONIC, "phone", 1, 300.0),
                    new Item(ItemType.ELECTRONIC, "laptop", 1, 600.00)
            );
            assertEquals(7.50, electronicsFee.priceToAggregate(cart));
        }
    }

    @Nested
    @DisplayName("Item tests")
    class ItemTests {

        @Test
        @DisplayName("specification-based")
        public void itemStoresEverything(){
            Item item = new Item(ItemType.ELECTRONIC, "phone", 1, 300.0);

            assertEquals(ItemType.ELECTRONIC, item.getType());
            assertEquals("phone", item.getName());
            assertEquals(1, item.getQuantity());
            assertEquals(300, item.getPricePerUnit());
        }

        @Test
        @DisplayName("specification-based")
        public void itemZeroQuantity(){
            Item item = new Item(ItemType.ELECTRONIC, "phone", 0, 300.0);
            assertEquals(0,  item.getQuantity());
        }

        @Test
        @DisplayName("structural-based")
        public void itemZeroPrice(){
            Item item = new Item(ItemType.ELECTRONIC, "phone", 1, 0);
            assertEquals(0,  item.getPricePerUnit());
        }
    }

    @Nested
    @DisplayName("RegularCost tests")
    class RegularCostTests {
        private RegularCost regularCost;

        @BeforeEach
        void setUp() {
            regularCost = new RegularCost();
        }

        @Test
        @DisplayName("specification-based")
        public void emptyCartReturnsZero(){
            assertEquals(0.0, regularCost.priceToAggregate(new ArrayList<>()));
        }

        @Test
        @DisplayName("specification-based")
        public void singleItem() {
            Item item = new Item(ItemType.OTHER, "Book", 3, 10.0);
            assertEquals(30.0, regularCost.priceToAggregate(List.of(item)));
        }

        @Test
        @DisplayName("specification-based")
        public void multipleItems() {
            List<Item> cart = List.of(
                    new Item(ItemType.ELECTRONIC, "laptop", 1, 500.00),
                    new Item(ItemType.OTHER, "book", 1, 10.00)
            );
            assertEquals(520.0, regularCost.priceToAggregate(cart));
        }

        @Test
        @DisplayName("structural-based")
        public void largeQuantity(){
            Item item  = new Item(ItemType.ELECTRONIC, "paper", 1000, 0.05);
            assertEquals(50, regularCost.priceToAggregate(List.of(item)));
        }
    }
}