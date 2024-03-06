package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.*;
import com.datastax.oss.driver.shaded.guava.common.base.Function;

import java.util.concurrent.CompletionStage;

import static com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy.DO_NOT_SET;

/**
 * Created by Giangbb on 04/03/2024
 */

@DefaultNullSavingStrategy(DO_NOT_SET)
public interface BaseDao<T> {
    @Insert
    void save(T t);

    @Insert
    CompletionStage<Void> saveAsync(T t);

    @Insert(ttl = ":ttl")
    void saveWithTtl(T t, int ttl);

    @Insert(ttl = ":ttl")
    CompletionStage<Void> saveWithTtlAsync(T t);

//    @Update
//    void update(T t);
//
//    @Update
//    CompletionStage<Void> updateAsync(T t);
//
//    @Update(ttl = ":ttl")
//    CompletionStage<Void> updateWithTtlAsync(T t, int ttl);

//    @Select
//    T findById(KeyType id);
//
//    @Select
//    CompletionStage<T> findByIdAsync(KeyType id);


    @Select
    PagingIterable<T> findAll();

    @Select
    CompletionStage<MappedAsyncPagingIterable<T>> findAllAsync();


    @Delete
    void delete(T t);

    @Delete
    CompletionStage<Void> deleteAsync(T t);


    //region Mapper converter

    //"lenient" mode:
    // - all entity's properties that have a matching column in the source row will be set.
    // - unmatched properties will be left untouched.
    @GetEntity(lenient = true)
    T rowToEntity(Row row);

    @SetEntity(lenient = true)
    BoundStatement entityToBoundStatement(T t, BoundStatement stmt);
    //endregion
}
