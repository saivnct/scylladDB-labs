package studio.giangbb.scylladbdemo.common.jackson;

/**
 * Created by giangbb on 14/06/2023
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.Instant;

/**
 * A deserializer used to deserialize [Instant] instances using a compact format: `yyyyMMddHHmmssSSS`, always expressed in UTC.
 *
 * @see StringToInstantConverter
 */
@JsonComponent
public class InstantDeserializer extends StdScalarDeserializer<Instant> {


    private final Converter<String, Instant> converter;

    public InstantDeserializer(Converter<String, Instant> converter) {
        super(Instant.class);
        this.converter = converter;
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.readValueAs(String.class);
        if (text == null) {
            return null;
        } else {
            return converter.convert(text);
        }
    }
}
