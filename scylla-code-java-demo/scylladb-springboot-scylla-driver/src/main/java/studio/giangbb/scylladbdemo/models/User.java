package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.mapper.annotations.*;

import java.util.Objects;
import java.util.UUID;

import static com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention.SNAKE_CASE_INSENSITIVE;

@Entity
//@CqlName("user")
@NamingStrategy(convention = SNAKE_CASE_INSENSITIVE)
public class User {
    @PartitionKey
    private UUID id;

    @ClusteringColumn
    private int userAge;

    private String userName;

    //@Computed fields are only used for select-based queries
    @Computed("writetime(user_name)")
    private long writetime;

    public User() {
    }

    public User(String userName, int userAge) {
        this.id = Uuids.timeBased();
        this.userName = userName;
        this.userAge = userAge;
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
                ", userName='" + userName + '\'' +
                ", userAge=" + userAge +
                ", writetime=" + writetime +
                '}';
    }
}