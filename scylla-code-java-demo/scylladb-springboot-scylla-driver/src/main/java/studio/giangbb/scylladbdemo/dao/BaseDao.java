package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.*;

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

    /**
     * @param t - entity.
     * @param ttl - time to live in second.
     */
    @Insert(ttl = ":ttl")
    void saveWithTtl(T t, int ttl);


    /**
     * @param t - entity.
     * @param ttl - time to live in second.
     * @return a {@link CompletionStage} providing a handle to the entity insertion completion.
     */
    @Insert(ttl = ":ttl")
    CompletionStage<Void> saveWithTtlAsync(T t, int ttl);


    @Update(ifExists = true)
    boolean saveIfExists(T t);

    @Update(ifExists = true)
    CompletionStage<Boolean> saveIfExistsAsync(T t);




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
