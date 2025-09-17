package app.product;

public enum PaymentStatus {
    PENDING("Pending Payment"),
    FAILED("Failed"),
    COMPLETED("Completed"),
    REQUESTING_REFUND("Requesting Refund"),
    REFUNDED("Refunded");

    private final String formatted;

    PaymentStatus(String formatted) {
        this.formatted = formatted;
    }

    public String getFormatted() {
        return formatted;
    }
}
