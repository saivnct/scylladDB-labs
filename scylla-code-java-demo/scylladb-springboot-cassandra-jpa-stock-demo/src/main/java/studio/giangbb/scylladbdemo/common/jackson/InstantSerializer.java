package studio.giangbb.scylladbdemo.common.jackson;

/**
 * Created by giangbb on 14/06/2023
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.Instant;

/**
 * A serializer used to serialize [Instant] instances using a compact format: `yyyyMMddHHmmssSSS`, always expressed in UTC.
 *
 * @see InstantToStringConverter
 */
@JsonComponent
public class InstantSerializer extends StdSerializer<Instant> {

    private final Converter<Instant, String> converter;

    public InstantSerializer(Converter<Instant, String> converter) {
        super(Instant.class);
        this.converter = converter;
    }

    @Override
    public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (instant == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeString(converter.convert(instant));
        }
    }
}
