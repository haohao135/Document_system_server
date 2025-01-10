package com.document.demo.utils;

import com.document.demo.models.tracking.ChangeLog;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Component
public class UpdateFieldUtils {
    public static <T>  void updateField(Map<String, ChangeLog> changes, String fieldName, T oldValue, T newValue, Consumer<T> setter) {
        if (!Objects.equals(oldValue, newValue) && newValue != null) {
            changes.put(fieldName, ChangeLog.builder()
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build());
            setter.accept(newValue);
        }
    }
}
