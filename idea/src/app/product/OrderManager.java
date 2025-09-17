package app.product;

import app.util.data.DataSerializers;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class OrderManager {
    private static final OrderManager instance = new OrderManager();

    public static OrderManager getInstance() {
        return instance;
    }

    private final List<ShoppingCart> customerCarts = new ArrayList<>();
    private final Map<UUID, List<Order>> customerOrderHistory = new HashMap<>();
    private int lastOrderId = 0;

    private final File cartFile = new File("shopping_carts.txt");
    private final File ordersFile = new File("customer_orders.txt");

    private OrderManager() {
        this.load();
    }

    public Map<String, Integer> getAllProductsForSeller(UUID sellerId, Order order) {
        var products = new HashMap<String, Integer>();

        order.getProducts().forEach((barcode, amount) -> {
            var product = ProductManager.getInstance().getProduct(barcode);

            if (product.getSeller().equals(sellerId)) {
                products.put(barcode, amount);
            }
        });

        return products;
    }

    public Collection<Order> getAllOrders() {
        var orders = new ArrayList<Order>();

        customerOrderHistory.forEach(($, customerOrders) -> {
            orders.addAll(customerOrders);
        });

        return orders;
    }

    public List<Order> getAllOrdersWithSeller(UUID sellerId) {
        var orders = new ArrayList<Order>();

        customerOrderHistory.forEach(($, orderHistory) -> {
            for (Order order : orderHistory) {
                if (!getAllProductsForSeller(sellerId, order).isEmpty()) {
                    orders.add(order);
                }
            }
        });

        return orders;
    }

    public void addToCart(UUID customerId, Product product, int quantity) {
        var cart = this.getCart(customerId);
        // Sets the value of [barcode] to [quantity] if it does not exist,
        // otherwise the value of [barcode] = (value of [barcode]) + [quantity]
        cart.products().compute(product.getBarcode(), (k, v) -> v == null ? quantity : v + quantity);
        this.save();
    }

    public void removeFromCart(UUID customerId, Product product, int quantity) {
        var cart = this.getCart(customerId);
        var current = cart.products().getOrDefault(product.getBarcode(), 0);

        if (current - quantity <= 0) {
            cart.products().remove(product.getBarcode());
        } else {
            cart.products().put(product.getBarcode(), current - quantity);
        }

        this.save();
    }

    public Order placeOrder(UUID customerId) {
        var cart = this.getCart(customerId);
        var products = new HashMap<>(cart.products()); // Copy the products map so we can clear ours
        List<Order> history = customerOrderHistory.computeIfAbsent(customerId, k -> new ArrayList<>());

        var totalCost = new AtomicReference<>(0.0);
        products.forEach((barcode, amount) -> {
            var product = ProductManager.getInstance().getProduct(barcode);
            totalCost.set(totalCost.get() + (product.getPriceWithDiscount() * (double) amount));
        });

        var order = new Order(customerId, ++lastOrderId, System.currentTimeMillis(), totalCost.get(), products);
        history.add(order);
        cart.products().clear();

        this.save();

        return order;
    }

    public int countCartItems(UUID customerId) {
        var count = 0;
        for (Integer value : getCart(customerId).products().values()) {
            count += value;
        }

        return count;
    }

    public ShoppingCart getCart(UUID customerId) {
        for (ShoppingCart cart : customerCarts) {
            if (cart.customerID().equals(customerId))
                return cart;
        }

        var cart = new ShoppingCart(customerId, new HashMap<>());
        customerCarts.add(cart);
        return cart;
    }

    public List<Order> getOrderHistory(UUID customerId) {
        return customerOrderHistory.computeIfAbsent(customerId, $ -> new ArrayList<>());
    }

    public void load() {
        customerCarts.clear();
        customerOrderHistory.clear();
        lastOrderId = 0;

        DataSerializers.deserializeLines(ShoppingCart.class, cartFile, customerCarts);

        // We can't serialize a map, so let's reimplement this.
        try {
            if (ordersFile.exists()) {
                var lines = Files.readAllLines(ordersFile.toPath(), StandardCharsets.UTF_8);

                for (String line : lines) {
                    var segments = DataSerializers.readSegmentedLine(line);
                    var uuid = UUID.fromString(segments.get(0));
                    var serializedOrders = DataSerializers.readSegmentedLine(segments.get(1));
                    var orderHistory = this.getOrderHistory(uuid);

                    for (String serializedOrder : serializedOrders) {
                        var order = DataSerializers.getSerializerFor(Order.class).deserialize(serializedOrder);

                        // Order ID should be incremental.
                        if (lastOrderId < order.getOrderId())
                            lastOrderId = order.getOrderId();

                        orderHistory.add(order);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        DataSerializers.serializeValues(ShoppingCart.class, cartFile, customerCarts);

        try {
            if (!ordersFile.exists()) {
                ordersFile.createNewFile();
            }

            var lines = new ArrayList<String>();

            customerOrderHistory.forEach((customerId, orders) -> {
                var serializedOrders = new ArrayList<String>();

                for (Order order : orders) {
                    serializedOrders.add(DataSerializers.getSerializerFor(Order.class).serialize(order));
                }

                lines.add(DataSerializers.writeSegmentedLine(List.of(
                    customerId.toString(),
                    DataSerializers.writeSegmentedLine(serializedOrders)
                )));
            });

            Files.write(ordersFile.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
