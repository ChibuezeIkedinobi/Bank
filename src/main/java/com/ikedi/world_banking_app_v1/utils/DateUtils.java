package com.ikedi.world_banking_app_v1.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTimeWithoutMilliseconds(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }
}
