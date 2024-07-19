package studio.giangbb.scylladbdemo.daoct;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.data.TupleValue;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.entity.User;
import studio.giangbb.scylladbdemo.entity.tuple.UserTupleIndex;
import com.giangbb.scylla.core.ScyllaTemplate;
import com.giangbb.scylla.repository.SimpleScyllaRepository;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * Created by giangbb on 12/05/2024
 */
@Component
@Qualifier("clientDAO")
public class UserDAO extends SimpleScyllaRepository<User> {

    @Autowired
    public UserDAO(ScyllaTemplate scyllaTemplate) {
        super(User.class, scyllaTemplate);
    }

    public User findById(UUID id) {
        SimpleStatement selectStatement = getScyllaEntityHelperImpl()
                .selectByPartitionKey()
                .build();

        PreparedStatement preparedStatement = this.prepare(selectStatement);

        return executeAndMapToSingleEntity(preparedStatement.bind(id));
    }

    public CompletionStage<User> findByIdAsync(UUID id) {
        SimpleStatement selectStatement = getScyllaEntityHelperImpl()
                .selectByPartitionKey()
                .build();

        PreparedStatement preparedStatement = this.prepare(selectStatement);

        try {
            return this.executeAsyncAndMapToSingleEntity(preparedStatement.bind(id));
        } catch (Exception e) {
            return CompletableFutures.failedFuture(e);
        }
    }


    //find by non-index column with allow filtering
    public PagingIterable<User> getUsersOlderThanAge(int userAge) {
        final String columnName = "user_age";
        SimpleStatement selectStatement = getScyllaEntityHelperImpl()
                .selectStart()
                .whereColumn(columnName)
                .isGreaterThan(QueryBuilder.bindMarker(columnName))
                .allowFiltering()
                .build();

        PreparedStatement preparedStatement = this.prepare(selectStatement);

        return executeAndMapToEntityIterable(preparedStatement.bind(userAge));
    }


    //find by Primitive index column
    public PagingIterable<User> getByUserName(String username) {
        final String columnName = "user_name";
        SimpleStatement selectStatement = getScyllaEntityHelperImpl()
                .selectStart()
                .whereColumn(columnName)
                .isEqualTo(QueryBuilder.bindMarker(columnName))
                .build();

        PreparedStatement preparedStatement = this.prepare(selectStatement);

        return executeAndMapToEntityIterable(preparedStatement.bind(username));
    }

    //find by Tuple index column
    public PagingIterable<User> getByUserTupleIndex(UserTupleIndex userTupleIndex){
        final String columnName = "user_tuple_index";
        SimpleStatement selectStatement = getScyllaEntityHelperImpl()
                .selectStart()
                .whereColumn(columnName)
                .isEqualTo(QueryBuilder.bindMarker(columnName))
                .build();

        PreparedStatement preparedStatement = this.prepare(selectStatement);
        TupleValue bindValue = marshallTupleValue(columnName, userTupleIndex);

        return executeAndMapToEntityIterable(preparedStatement.bind(bindValue));
    }
}
