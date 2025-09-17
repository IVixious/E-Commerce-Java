package app.product;

import app.auth.Account;
import app.util.data.DataSerializers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductManager {
    private static final ProductManager instance = new ProductManager();

    public static ProductManager getInstance() {
        return instance;
    }

    private final List<Product> products = new ArrayList<>();
    private final File productsFile = new File("products.txt");

    private ProductManager() {
        this.load();
    }

    public Collection<Product> products() {
        return this.products;
    }

    public Product addProduct(String barcode, Account seller, String name, String description, double price, int stock) {
        var product = new Product(seller.getUUID(), barcode, name, description, price, stock, 0.0, ProductCategory.UNCATEGORIZED);
        products.add(product);

        this.save();
        return product;
    }

    public void removeProduct(String barcode) {
        var product = getProduct(barcode);
        if (product == null)
            return;

        this.products.remove(product);
        this.save();
    }

    public Product getProduct(String barcode) {
        for (Product product : products) {
            if (product.getBarcode().equals(barcode)) {
                return product;
            }
        }

        return null;
    }

    public void load() {
        products.clear();

        DataSerializers.deserializeLines(Product.class, productsFile, products);
    }

    public void save() {
        DataSerializers.serializeValues(Product.class, productsFile, products);
    }
}
