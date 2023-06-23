package studio.giangbb.scylladbdemo.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.io.Serializable;
import java.util.Objects;

@Table("user_profile")
public class UserV3 {
    @PrimaryKeyClass
    public static class Key implements Serializable {
        @PrimaryKeyColumn(
                name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private int id;

        @PrimaryKeyColumn(
                name = "user_age",
                ordinal = 1,
                type = PrimaryKeyType.CLUSTERED,
                ordering = Ordering.DESCENDING)
        private int userAge;

        public Key(int id, int userAge) {
            this.id = id;
            this.userAge = userAge;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return id == key.id && userAge == key.userAge;
        }

    }

    @PrimaryKey
    private Key key;

    @Column("username")
    private String userName;

    @Column("writetime")
    private Long writetime;



    public UserV3(Key key, String userName, Long writetime) {
        this.key = key;
        this.userName = userName;
        this.writetime = writetime;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getWritetime() {
        return writetime;
    }

    public void setWritetime(Long writetime) {
        this.writetime = writetime;
    }
}