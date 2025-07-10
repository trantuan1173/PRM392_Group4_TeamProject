package com.example.prm392_group4_teamproject.Cards;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String formatRelativeTime(String isoTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date utcDate = sdf.parse(isoTime);

            Date localDate = new Date(utcDate.getTime());

            long now = System.currentTimeMillis();
            long diffMillis = now - localDate.getTime();

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

            if (minutes < 1) return "vừa xong";
            if (minutes < 60) return minutes + " phút trước";
            if (hours < 24) return hours + " giờ trước";
            if (days == 1) return "hôm qua";
            if (days < 7) return days + " ngày trước";
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(localDate);

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

}
