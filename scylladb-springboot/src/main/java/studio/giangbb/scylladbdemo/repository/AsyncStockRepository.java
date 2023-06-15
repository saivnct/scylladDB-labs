package studio.giangbb.scylladbdemo.repository;

/**
 * Created by giangbb on 15/06/2023
 */

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import studio.giangbb.scylladbdemo.model.Stock;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

/** A DAO that manages the persistence of [Stock] instances.  */
@Repository
public class AsyncStockRepository {
    private final CqlSession session;
    private final PreparedStatement insert;
    private final PreparedStatement deleteById;
    private final PreparedStatement findById;
    private final PreparedStatement findBySymbol;
    private final PreparedStatement findStockBySymbol;
    private final SimpleStatement findAll;
    private final Function<Row, Stock> rowMapper;

    @Autowired
    public AsyncStockRepository(
            CqlSession session,
            @Qualifier("stocks.prepared.insert") PreparedStatement insert,
            @Qualifier("stocks.prepared.deleteById") PreparedStatement deleteById,
            @Qualifier("stocks.prepared.findById") PreparedStatement findById,
            @Qualifier("stocks.prepared.findBySymbol") PreparedStatement findBySymbol,
            @Qualifier("stocks.prepared.findStockBySymbol") PreparedStatement findStockBySymbol,
            @Qualifier("stocks.simple.findAll") SimpleStatement findAll, Function<Row, Stock> rowMapper) {
        this.session = session;
        this.insert = insert;
        this.deleteById = deleteById;
        this.findById = findById;
        this.findBySymbol = findBySymbol;
        this.findStockBySymbol = findStockBySymbol;
        this.findAll = findAll;
        this.rowMapper = rowMapper;
    }

    /**
     * Saves the given stock value.
     *
     * @param stock The stock value to save.
     * @return A future that will complete with the saved stock value.
     */
    @NonNull
    public CompletionStage<Stock> save(@NonNull Stock stock) {
        BoundStatement bound = insert
                .bind(stock.getSymbol(), stock.getDate(), stock.getValue())
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        CompletionStage<AsyncResultSet> stage = session.executeAsync(bound);
        return stage.thenApply(resultSet -> stock);
    }

    /**
     * Deletes the stock value for the given symbol and date.
     *
     * @param symbol The stock symbol to delete.
     * @param date The stock date to delete.
     * @return A future that will complete when the operation is completed.
     */
    @NonNull
    public CompletionStage<Void> deleteById(@NonNull String symbol, @NonNull Instant date) {
        BoundStatement bound = deleteById.bind(symbol, date);//.setConsistencyLevel(ConsistencyLevel.ALL);
        CompletionStage<AsyncResultSet> stage = session.executeAsync(bound);
        return stage.thenApply(resultSet -> null);
    }


    /**
     * Retrieves the stock value uniquely identified by its symbol and date.
     *
     * @param symbol The stock symbol to find.
     * @param date The stock date to find.
     * @return A future that will complete with the retrieved stock value, or empty if not found.
     */
    @NonNull
    public CompletionStage<Optional<Stock>> findById(@NonNull String symbol, @NonNull Instant date) {
        BoundStatement bound = findById.bind(symbol, date);
        CompletionStage<AsyncResultSet> stage = session.executeAsync(bound);
        return stage
                .thenApply (resultSet -> resultSet.one())
            .thenApply(row -> Optional.ofNullable(row))
            .thenApply(optionalRow -> optionalRow.map(rowMapper));
    }



    /**
     * Retrieves all the stock values for a given symbol in a given date range, page by page.
     *
     * @param symbol The stock symbol to find.
     * @param start The date range start (inclusive).
     * @param end The date range end (exclusive).
     * @param offset The zero-based index of the first result to return.
     * @param limit The maximum number of results to return.
     * @return A future that will complete with a [Stream] of results.
     */
    @NonNull
    public CompletionStage<Stream<Stock>> findAllBySymbol(
            @NonNull String symbol,
            @NonNull Instant start,
            @NonNull Instant end,
            Long offset,
            Long limit
    ) {
        BoundStatement bound = findBySymbol.bind(symbol, start, end);
        CompletionStage<AsyncResultSet> stage = session.executeAsync(bound);
        return stage
                .thenCompose(resultSet -> new RowCollector(resultSet, offset, limit))
                .thenApply(rows -> rows.stream().map(rowMapper));
    }


    /**
     * Retrieves all the stock values for a given symbol in a given date range, page by page.
     *
     * @param symbol The stock symbol to find.
     * @param start The date range start (inclusive).
     * @param end The date range end (exclusive).
     * @param offset The zero-based index of the first result to return.
     * @param limit The maximum number of results to return.
     * @return A future that will complete with a [Stream] of results.
     */
    @NonNull
    public CompletionStage<Stream<Stock>> findStockBySymbol(@NonNull String symbol){
        BoundStatement bound = findStockBySymbol.bind(symbol);
        CompletionStage<AsyncResultSet> stage = session.executeAsync(bound);
        return stage
                .thenCompose(resultSet -> new UnlimitedRowCollector(resultSet))
                .thenApply (rows -> rows.stream().map(rowMapper));
    }


    /**
     * Retrieves all the stock values.
     *
     * a very ineffective way to do a full scan
     * @return A future that will complete with a [Stream] of results.
     */
    @NonNull
    public CompletionStage<Stream<Stock>> findAll() {
        SimpleStatement bound = findAll;//.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        CompletionStage<AsyncResultSet> stage = session.executeAsync(bound);
        return stage
                .thenCompose(resultSet -> new UnlimitedRowCollector(resultSet))
                .thenApply(rows -> rows.stream().map(rowMapper));
    }
}
