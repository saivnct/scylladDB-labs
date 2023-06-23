package studio.giangbb.scylladbdemo.common.converter;

/**
 * Created by giangbb on 14/06/2023
 */

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * A [Converter] that converts Strings into [Instant] instances using a compact format:
 * `yyyyMMddHHmmssSSS`, always expressed in UTC. All parts are optional except the year.
 */
@Component
public class StringToInstantConverter implements Converter<String, Instant> {
    private final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR_OF_ERA, 4)
            .optionalStart()
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .optionalStart()
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .optionalStart()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .optionalStart()
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .toFormatter()
            .withZone(ZoneOffset.UTC);


    @Override
    public Instant convert(String source) {
        TemporalAccessor parsed = PARSER.parse(source);
        return Instant.from(parsed);
    }
}
