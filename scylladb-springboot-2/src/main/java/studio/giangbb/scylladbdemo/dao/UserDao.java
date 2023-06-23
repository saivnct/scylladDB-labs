package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.*;
import studio.giangbb.scylladbdemo.model.User;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Select
    User getUserById(int id);

    @Select
    PagingIterable<User> getAllUsers();

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @GetEntity
    User getUser(Row row);

    @SetEntity
    BoundStatement setUser(BoundStatement udtValue, User user);

    @Query(value = "select * from user_profile where user_age > :userAge ALLOW FILTERING")
    PagingIterable<User> getUsersOlderThanAge(int userAge);


    @Query(value = "select * from user_profile where username = :username")
    PagingIterable<User> getByUserName(String username);
}
