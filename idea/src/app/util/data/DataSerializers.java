package app.util.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSerializers {
    private static final Map<String, DataSerializer<?>> serializers = new HashMap<>();

    public static DataSerializer<?> register(String name, DataSerializer<?> serializer) {
        return serializers.put(name, serializer);
    }

    public static <T> DataSerializer<T> getSerializer(String name) {
        return (DataSerializer<T>) serializers.get(name);
    }

    public static String getSerializerName(DataSerializer<?> serializer) {
        for (String key : serializers.keySet()) {
            var other = serializers.get(key);

            if (other == serializer) {
                return key;
            }
        }

        return null;
    }

    public static <T> DataSerializer<T> getSerializerFor(Class<T> clazz) {
        for (DataSerializer<?> serializer : serializers.values()) {
            if (serializer.getSerializableClass() == null)
                continue;

            if (serializer.getSerializableClass().isAssignableFrom(clazz)) {
                return (DataSerializer<T>) serializer;
            }
        }

        return null;
    }

    public static <T> void serializeValues(Class<T> serializable, File file, List<T> list) {
        try {
            var serializer = getSerializerFor(serializable);

            if (!file.exists()) {
                file.createNewFile();
            }

            var lines = new ArrayList<String>();

            for (T value : list) {
                lines.add(serializer.serialize(value));
            }

            try (FileOutputStream stream = new FileOutputStream(file)) {
                try (OutputStreamWriter streamWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                    for (String line : lines) {
                        streamWriter.write(line);
                        streamWriter.write('\n');
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void deserializeLines(Class<T> serializable, File file, List<T> list) {
        try {
            if (file.exists()) {
                var serializer = getSerializerFor(serializable);
                var lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

                for (String line : lines) {
                    list.add(serializer.deserialize(line));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String SEGMENT_DELIMITER = ", ";
    private static final char SEGMENT_CONTAINER = '"';

    // Allows reading lines in a format of "segment 1""segment2" without worrying about any characters causing breakage to the system.
    // Additionally, almost any character can be within these segments, allowing for delimiters.
    public static List<String> readSegmentedLine(String line) {
        var segments = new ArrayList<String>();
        var stringBuilder = new StringBuilder();

        boolean isInSegment = false;
        boolean isInEscape = false;
        for (var i = 0; i < line.length(); i++) {
            var c = line.charAt(i);

            if (c == '\\' && !isInEscape) {
                isInEscape = true;
            } else if (isInEscape) {
                if (isInSegment) {
                    stringBuilder.append(c);
                    isInEscape = false;
                }
            } else if (c == SEGMENT_CONTAINER) {
                if (isInSegment) {
                    isInSegment = false;
                    segments.add(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                } else {
                    isInSegment = true;
                }
            } else if (isInSegment) {
                stringBuilder.append(c);
            }
        }

        return segments;
    }

    public static String writeSegmentedLine(List<String> segments) {
        var stringBuilder = new StringBuilder();

        for (var i = 0; i < segments.size(); i++) {
            var segment = segments.get(i);
            stringBuilder.append(SEGMENT_CONTAINER);

            stringBuilder.append(
                segment
                    .replace("\\", "\\\\") // Ensure that backslashes aren't used to escape anything themselves.
                    .replace("" + SEGMENT_CONTAINER, "\\" + SEGMENT_CONTAINER)
            );

            stringBuilder.append(SEGMENT_CONTAINER);

            // Add delimiter unless this is the last segment.
            if (i < segments.size() - 1) {
                stringBuilder.append(SEGMENT_DELIMITER);
            }
        }

        return stringBuilder.toString();
    }
}
