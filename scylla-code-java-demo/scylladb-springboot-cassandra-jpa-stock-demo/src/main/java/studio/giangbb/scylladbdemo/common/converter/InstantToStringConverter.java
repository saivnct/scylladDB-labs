package studio.giangbb.scylladbdemo.common.converter;

/**
 * Created by giangbb on 14/06/2023
 */

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * A [Converter] that converts [Instant] instances into Strings using a compact format:
 * `yyyyMMddHHmmssSSS`, always expressed in UTC.
 */
@Component
public class InstantToStringConverter implements Converter<Instant, String> {
    private final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR_OF_ERA, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();


    @Override
    public String convert(@NonNull Instant source) {
        ZonedDateTime zonedDateTime = source.atZone(ZoneOffset.UTC);
        return FORMATTER.format(zonedDateTime);
    }
}
