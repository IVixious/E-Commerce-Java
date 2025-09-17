package app.product;

public enum OrderStatus {
    PENDING("Pending"),
    CANCELLED("Cancelled"),
    READY_FOR_DELIVERY("Ready for Delivery"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered");

    private final String formatted;

    OrderStatus(String formatted) {
        this.formatted = formatted;
    }

    public String getFormatted() {
        return formatted;
    }
}
