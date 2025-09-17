package app.product;

import app.util.Utils;
import app.util.data.DataSerializer;
import app.util.data.DataSerializers;

import java.util.Map;
import java.util.UUID;

public class Order {
    private final UUID account;
    private final int orderId;
    private final long orderTimestamp;
    private long deliveredTimestamp = -1;
    private OrderStatus status = OrderStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private final double totalCost;
    private final Map<String, Integer> products;

    public Order(UUID account, int orderId, long orderTimestamp, double totalCost, Map<String, Integer> products) {
        this.account = account;
        this.orderId = orderId;
        this.orderTimestamp = orderTimestamp;
        this.totalCost = totalCost;
        this.products = products;
    }

    public Order(UUID account, int orderId, long orderTimestamp, long deliveredTimestamp, double totalCost, OrderStatus status, PaymentStatus paymentStatus, Map<String, Integer> products) {
        this(account, orderId, orderTimestamp, totalCost, products);
        this.deliveredTimestamp = deliveredTimestamp;
        this.status = status;
        this.paymentStatus = paymentStatus;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getDeliveredTimestamp() {
        return deliveredTimestamp;
    }

    public long getOrderTimestamp() {
        return orderTimestamp;
    }

    public int getOrderId() {
        return orderId;
    }

    public UUID getAccountUUID() {
        return account;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setDeliveredTimestamp(long deliveredTimestamp) {
        this.deliveredTimestamp = deliveredTimestamp;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Map<String, Integer> getProducts() {
        return products;
    }

    public static class Serializer extends DataSerializer<Order> {
        public Serializer() {
            super(Order.class);
        }

        @Override
        public String serialize(Order value) {
            return DataSerializers.writeSegmentedLine(Utils.allToStrings(
                value.getAccountUUID(),
                value.getOrderId(),
                value.getOrderTimestamp(),
                value.getDeliveredTimestamp(),
                value.getTotalCost(),
                value.getStatus().name(),
                value.getPaymentStatus().name(),
                DataSerializers.getSerializer("product_map").serialize(value.getProducts())
            ));
        }

        @Override
        public Order deserialize(String data) {
            var split = DataSerializers.readSegmentedLine(data);

            return new Order(
                UUID.fromString(split.get(0)),
                Integer.parseInt(split.get(1)),
                Long.parseLong(split.get(2)),
                Long.parseLong(split.get(3)),
                Double.parseDouble(split.get(4)),
                OrderStatus.valueOf(split.get(5)),
                PaymentStatus.valueOf(split.get(6)),
                (Map<String, Integer>) DataSerializers.getSerializer("product_map").deserialize(split.get(7))
            );
        }
    }

    static {
        DataSerializers.register("order", new Serializer());
    }

    public static void init() {}
}
