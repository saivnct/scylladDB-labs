package studio.giangbb.scylladbdemo.common.repository;

/**
 * Created by giangbb on 14/06/2023
 */

import com.datastax.oss.driver.api.core.cql.Row;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.common.conf.StockQueriesConfiguration;
import studio.giangbb.scylladbdemo.model.Stock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;

/**
 * A row mapping function that creates a [Stock] instance from a database [Row].
 *
 *
 * The row is expected to contain all 3 columns in the `stocks` table: `symbol
 ` * , `date` and `value`, as if it were obtained by a CQL query such as
 * `SELECT symbol, date, value FROM stocks WHERE ...`.
 */
@Component
public class RowToStockMapper implements Function<Row, Stock> {

    @Override
    public Stock apply(Row row) {
        String symbol = Objects.requireNonNull(row.getString(StockQueriesConfiguration.SYMBOL), "column symbol cannot be null");
        Instant date = Objects.requireNonNull(row.getInstant(StockQueriesConfiguration.DATE), "column date cannot be null");
        BigDecimal value = Objects.requireNonNull(row.getBigDecimal(StockQueriesConfiguration.VALUE), "column value cannot be null");


        return new Stock(symbol, date, value);
    }
}
