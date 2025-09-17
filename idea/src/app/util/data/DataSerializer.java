package app.util.data;

public abstract class DataSerializer<T> {
    private final Class<T> clazz;

    public DataSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getSerializableClass() {
        return this.clazz;
    }

    public abstract String serialize(T value);
    public abstract T deserialize(String data);
}
