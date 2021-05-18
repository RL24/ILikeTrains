package me.rl24.iliketrains;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    private static final Locale LOCALE = Locale.forLanguageTag("en_NZ");
    private static final SimpleDateFormat SDF_FROM = new SimpleDateFormat("HH:mm", LOCALE);
    private static final SimpleDateFormat SDF_TO = new SimpleDateFormat("hh:mm a", LOCALE);
    private static final SimpleDateFormat SDF_DEFAULT = new SimpleDateFormat("yyyy-MM-dd", LOCALE);

    public static String convert(String date) {
        Date dateObj = null;
        try {
            dateObj = SDF_FROM.parse(date);
        } catch (ParseException ignored) { }
        return dateObj != null ? SDF_TO.format(dateObj) : date;
    }

    public static String getCurrent() {
        return SDF_DEFAULT.format(Calendar.getInstance().getTime());
    }

}
