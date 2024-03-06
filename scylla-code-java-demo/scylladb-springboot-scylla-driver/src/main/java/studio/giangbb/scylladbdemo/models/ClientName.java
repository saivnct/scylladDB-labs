package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Giangbb on 06/03/2024
 */
@Entity
public class ClientName {
    private String firstName;
    private String lastName;

    public ClientName() {
    }

    public ClientName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientName clientName = (ClientName) o;
        return Objects.equals(firstName, clientName.firstName) && Objects.equals(lastName, clientName.lastName);
    }

    @Override
    public String toString() {
        return "ClientName{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
