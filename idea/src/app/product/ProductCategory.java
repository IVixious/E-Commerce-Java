package app.product;

public enum ProductCategory {
    UNCATEGORIZED("Uncategorized"),
    TECH("Technology"),
    SPORTS("Sports"),
    LIFESTYLE("Lifestyle"),
    FOOD("Food");

    private final String formatted;

    ProductCategory(String formatted) {
        this.formatted = formatted;
    }

    public String getFormatted() {
        return formatted;
    }
}
