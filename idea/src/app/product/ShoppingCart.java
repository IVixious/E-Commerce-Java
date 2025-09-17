package app.product;

import app.util.Utils;
import app.util.data.DataSerializer;
import app.util.data.DataSerializers;

import java.util.Map;
import java.util.UUID;

public record ShoppingCart(UUID customerID, Map<String, Integer> products) {
    public static class Serializer extends DataSerializer<ShoppingCart> {
        public Serializer() {
            super(ShoppingCart.class);
        }

        @Override
        public String serialize(ShoppingCart value) {
            return DataSerializers.writeSegmentedLine(Utils.allToStrings(value.customerID(), DataSerializers.getSerializer("product_map").serialize(value.products())));
        }

        @Override
        public ShoppingCart deserialize(String data) {
            var segments = DataSerializers.readSegmentedLine(data);
            return new ShoppingCart(UUID.fromString(segments.get(0)), (Map<String, Integer>) DataSerializers.getSerializer("product_map").deserialize(segments.get(1)));
        }
    }

    static {
        DataSerializers.register("shopping_cart", new Serializer());
    }

    public static void init() {
    }
}
