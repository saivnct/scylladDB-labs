package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import studio.giangbb.scylladbdemo.models.User;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Dao
public interface UserDao extends BaseDao<User>{
    @Select
    User findByPrimKey(UUID id, int userAge);

    @Select
    CompletionStage<User> findByPrimKeyAsync(UUID id, int userAge);


    @Query("SELECT count(*) FROM user")
    long countAll();


    @Query(value = "TRUNCATE user")
    void deleteAll();

    @Query(value = "TRUNCATE user")
    CompletionStage<Void> deleteAllAsync();

    @Select(customWhereClause = "user_name = :username")
    PagingIterable<User> getByUserName(String username);


    @Select(customWhereClause = "user_age > :userAge ALLOW FILTERING")
    PagingIterable<User> getUsersOlderThanAge(int userAge);
}
