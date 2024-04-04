package studio.giangbb.scylladbdemo.common.conf;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * Created by giangbb on 14/06/2023
 */
@Configuration
public class StockQueriesConfiguration {
    public static final CqlIdentifier STOCKS = CqlIdentifier.fromCql("stocks");
    public static final CqlIdentifier SYMBOL = CqlIdentifier.fromCql("symbol");
    public static final CqlIdentifier DATE = CqlIdentifier.fromCql("date");
    public static final CqlIdentifier VALUE = CqlIdentifier.fromCql("value");
    public static final CqlIdentifier START = CqlIdentifier.fromCql("start");
    public static final CqlIdentifier END = CqlIdentifier.fromCql("end");

    @Bean("stocks.simple.create")
    public SimpleStatement createTable(@NonNull CqlIdentifier keyspace) {
        return SchemaBuilder.createTable(keyspace, STOCKS)
                .ifNotExists()
                .withPartitionKey(SYMBOL, DataTypes.TEXT)
                .withClusteringColumn(DATE, DataTypes.TIMESTAMP)
                .withColumn(VALUE, DataTypes.DECIMAL)
                .withClusteringOrder(DATE, ClusteringOrder.DESC)
                .build();
    }


    @Bean("stocks.simple.drop")
    public SimpleStatement dropTable(@NonNull CqlIdentifier keyspace) {
        return SchemaBuilder.dropTable(keyspace, STOCKS).ifExists().build();
    }

    @Bean("stocks.simple.truncate")
    public SimpleStatement truncate(@NonNull CqlIdentifier keyspace) {
        return QueryBuilder.truncate(keyspace, STOCKS).build();
    }


    @Bean("stocks.simple.insert")
    public SimpleStatement insert(@NonNull CqlIdentifier keyspace) {
        return QueryBuilder.insertInto(keyspace, STOCKS)
                .value(SYMBOL, QueryBuilder.bindMarker(SYMBOL))
                .value(DATE, QueryBuilder.bindMarker(DATE))
                .value(VALUE, QueryBuilder.bindMarker(VALUE))
                .build();
    }


    @Bean("stocks.simple.deleteById")
    public SimpleStatement deleteById(@NonNull CqlIdentifier keyspace)  {
        return QueryBuilder.deleteFrom(keyspace, STOCKS)
                .where(Relation.column(SYMBOL).isEqualTo(QueryBuilder.bindMarker(SYMBOL)))
                .where(Relation.column(DATE).isEqualTo(QueryBuilder.bindMarker(DATE)))
                .build();
    }

    @Bean("stocks.simple.findById")
    public SimpleStatement findById(@NonNull CqlIdentifier keyspace) {
        return QueryBuilder.selectFrom(keyspace, STOCKS)
                .columns(SYMBOL, DATE, VALUE)
                .where(Relation.column(SYMBOL).isEqualTo(QueryBuilder.bindMarker(SYMBOL)))
                .where(Relation.column(DATE).isEqualTo(QueryBuilder.bindMarker(DATE)))
                .build();
    }


    @Bean("stocks.simple.findBySymbol")
    public SimpleStatement findBySymbol(@NonNull CqlIdentifier keyspace) {
        return QueryBuilder.selectFrom(keyspace, STOCKS)
                .columns(SYMBOL, DATE, VALUE)
                .where(
                        Relation.column(SYMBOL).isEqualTo(QueryBuilder.bindMarker(SYMBOL)),  // start inclusive
                        Relation.column(DATE).isGreaterThanOrEqualTo(QueryBuilder.bindMarker(START)),  // end exclusive
                        Relation.column(DATE).isLessThan(QueryBuilder.bindMarker(END))
                )
                .build();
    }

    @Bean("stocks.simple.findStockBySymbol")
    public SimpleStatement findStockBySymbol(@NonNull CqlIdentifier keyspace){
        return QueryBuilder.selectFrom(keyspace, STOCKS)
                .columns(SYMBOL, DATE, VALUE)
                .where(
                        Relation.column(SYMBOL).isEqualTo(QueryBuilder.bindMarker(SYMBOL))  // start inclusive
                )
                .build();
    }

    // a very ineffective way to do a full scan
    @Bean("stocks.simple.findAll")
    public SimpleStatement findAll(@NonNull CqlIdentifier keyspace){
        return QueryBuilder.selectFrom(keyspace, STOCKS)
                .columns(SYMBOL, DATE, VALUE)
                .build();
    }


    @Bean("stocks.prepared.insert")
    public PreparedStatement prepareInsert(
            CqlSession session, @Qualifier("stocks.simple.insert") SimpleStatement stockInsert
    ){
        return session.prepare(stockInsert);
    }

    @Bean("stocks.prepared.deleteById")
    public PreparedStatement prepareDeleteById(
            CqlSession session, @Qualifier("stocks.simple.deleteById") SimpleStatement stockDeleteById
    ){
        return session.prepare(stockDeleteById);
    }

    @Bean("stocks.prepared.findById")
    public PreparedStatement prepareFindById(
            CqlSession session, @Qualifier("stocks.simple.findById") SimpleStatement stockFindById
    ){
        return session.prepare(stockFindById);
    }

    @Bean("stocks.prepared.findBySymbol")
    public PreparedStatement prepareFindBySymbol(
            CqlSession session,
            @Qualifier("stocks.simple.findBySymbol") SimpleStatement stockFindBySymbol
    ){
        return session.prepare(stockFindBySymbol);
    }

    @Bean("stocks.prepared.findStockBySymbol")
    public PreparedStatement prepareFindStockBySymbol(
            CqlSession session,
            @Qualifier("stocks.simple.findStockBySymbol") SimpleStatement stockFindBySymbol
    ){
        return session.prepare(stockFindBySymbol);
    }

}
