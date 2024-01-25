package studio.giangbb.scylladbdemo.model;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import studio.giangbb.scylladbdemo.common.utils.DateUtil;

import java.net.InetAddress;
import java.util.*;

/**
 * Created by giangbb on 26/06/2023
 */

/*
* NOTE:
* 0. must have @Table -> Once Table created, sping-data cannot modify automatically (add field, remove field, change type)
* 1. must have @PrimaryKey/@Id. @PrimaryKey similar to @Id but lets you specify the column name.
* 2. When your CQL table has a composite primary key, you must create a @PrimaryKeyClass to define the structure of the composite primary key.
* 3. must declare @UserDefinedType for UDT -> Once UDT created, sping-data cannot modify automatically (add field, remove field, change type)
* 4. can add @Indexed later => after adding @Indexed, spring-data cannot remove it automatically
* */
@Table()
public class Person {
    public enum Job {
        STUDENT,
        TEACHER,
        ENGINEER
    };

    @PrimaryKey("id")
//    @Id
    private UUID id;

    private PersonName name;

    private Job job;

    private Map<String, InetAddress> sessions;

    private Set<FavoritePlace> favoritePlaces;

    private List<String> addresses;

    @Indexed
    @Column("email")
    private String email;

    private Date createAt;


    //NOTE: must have null constructor or all-args constructor
    public Person() {
    }

    public Person(PersonName name, Job job, String email) {
        this.id = Uuids.timeBased();
        this.name = name;
        this.job = job;
        this.email = email;
        this.sessions = new HashMap<>();
        this.favoritePlaces = new HashSet<>();
        this.addresses = new ArrayList<>();
        this.createAt = DateUtil.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PersonName getName() {
        return name;
    }

    public void setName(PersonName name) {
        this.name = name;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Map<String, InetAddress> getSessions() {
        return sessions;
    }

    public void setSessions(Map<String, InetAddress> sessions) {
        this.sessions = sessions;
    }

    public Set<FavoritePlace> getFavoritePlaces() {
        return favoritePlaces;
    }

    public void setFavoritePlaces(Set<FavoritePlace> favoritePlaces) {
        this.favoritePlaces = favoritePlaces;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(name, person.name) && job == person.job && Objects.equals(createAt, person.createAt);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name=" + name +
                ", job=" + job +
                ", sessions=" + sessions +
                ", favoritePlaces=" + favoritePlaces +
                ", addresses=" + addresses +
                ", email='" + email + '\'' +
                ", createAt=" + createAt +
                '}';
    }
}
