package studio.giangbb.scylladbdemo.common.controller;

/**
 * Created by giangbb on 14/06/2023
 */

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import studio.giangbb.scylladbdemo.model.Stock;

import java.net.URI;
import java.time.Instant;

/** A helper class that creates URIs for controllers dealing with [Stock] objects.  */
@Component
public class StockUriHelper {


    @Autowired
    private Converter<Instant, String> instantToStringConverter;

    /**
     * Creates an URI pointing to a specific stock value.
     *
     * @param request The HTTP request that will serve as the base for new URI.
     * @param stock The stock value to create an URI for.
     * @return An URI pointing to a specific stock value.
     */
    @NonNull
    public URI buildDetailsUri(@NonNull HttpServletRequest request, @NonNull Stock stock) {
        String date = instantToStringConverter.convert(stock.getDate());
        return ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath("/api/v1/stocks/{symbol}/{date}")
                .buildAndExpand(stock.getSymbol(), date)
                .toUri();
    }
}
