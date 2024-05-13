package studio.giangbb.scylladbdemo.entity;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.mapper.annotations.*;
import studio.giangbb.scylladbdemo.entity.tuple.UserTuple;
import studio.giangbb.scylladbdemo.entity.tuple.UserTupleIndex;
import com.giangbb.scylla.core.mapping.Indexed;
import com.giangbb.scylla.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

import static com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention.SNAKE_CASE_INSENSITIVE;

/**
 * Created by giangbb on 12/05/2024
 */
@CqlName("user")
@Table
@Entity
@NamingStrategy(convention = SNAKE_CASE_INSENSITIVE)
public class User {
    @PartitionKey
    private UUID id;

    @ClusteringColumn
    private int userAge;

    @Indexed
    private String userName;

    //NOTE: java-driver-mapper not supporting TUPLE!!!!!!!!!!!!!
    @Indexed
    private UserTupleIndex userTupleIndex;

    private UserTuple userTuple;

    //@Computed fields are only used for select-based queries
    @Computed("writetime(user_name)")
    private long writetime;

    public User() {
    }

    public User(String userName, int userAge, UserTupleIndex userTupleIndex, UserTuple userTuple) {
        this.id = Uuids.timeBased();
        this.userName = userName;
        this.userAge = userAge;
        this.userTupleIndex = userTupleIndex;
        this.userTuple = userTuple;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public long getWritetime() {
        return writetime;
    }

    public void setWritetime(long writetime) {
        this.writetime = writetime;
    }

    public UserTuple getUserTuple() {
        return userTuple;
    }

    public void setUserTuple(UserTuple userTuple) {
        this.userTuple = userTuple;
    }

    public UserTupleIndex getUserTupleIndex() {
        return userTupleIndex;
    }

    public void setUserTupleIndex(UserTupleIndex userTupleIndex) {
        this.userTupleIndex = userTupleIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(userName, user.userName) && userAge == user.userAge;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userAge=" + userAge +
                ", userName='" + userName + '\'' +
                ", userTupleIndex=" + userTupleIndex +
                ", userTuple=" + userTuple +
                ", writetime=" + writetime +
                '}';
    }
}