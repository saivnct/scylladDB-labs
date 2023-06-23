package studio.giangbb.scylladbdemo.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import javax.annotation.Nullable;
import java.io.Serializable;

@Table("user_profile")
public class UserV2 {

    @PrimaryKeyColumn(
            name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private int id;

    @PrimaryKeyColumn(
            name = "user_age",
            ordinal = 1,
            type = PrimaryKeyType.CLUSTERED,
            ordering = Ordering.DESCENDING)
    private int userAge;

    @Column("username")
    private String userName;

    @Column("writetime")
    private Long writetime;



    public UserV2(int id, int userAge, String userName, Long writetime) {
        this.id = id;
        this.userAge = userAge;
        this.userName = userName;
        this.writetime = writetime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getWritetime() {
        return writetime;
    }

    public void setWritetime(long writetime) {
        this.writetime = writetime;
    }
}