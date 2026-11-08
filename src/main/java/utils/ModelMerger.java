package utils;

import java.lang.reflect.Field;

public class ModelMerger {

    public static <T> void merge(T existing, T submitted) {
        Field[] fields = submitted.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(submitted);
                if (value == null) continue;
                if (value instanceof String && ((String) value).trim().isEmpty()) continue;
                field.set(existing, value);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}