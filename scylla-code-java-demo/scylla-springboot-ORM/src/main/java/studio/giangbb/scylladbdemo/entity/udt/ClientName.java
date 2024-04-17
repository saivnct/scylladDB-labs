package studio.giangbb.scylladbdemo.entity.udt;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.NamingStrategy;
import com.giangbb.scylla.core.mapping.UserDefinedType;

import java.util.Objects;

import static com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention.SNAKE_CASE_INSENSITIVE;

/**
 * Created by Giangbb on 06/03/2024
 */
@UserDefinedType
@Entity
@NamingStrategy(convention = SNAKE_CASE_INSENSITIVE)
public class ClientName {

    public enum NameStyle {
        ASIA, EURO
    }

    @CqlName("first_name")
    private String firstName;
    private String lastName;

    private NameStyle nameStyle;

    public ClientName() {
    }

    public ClientName(String firstName, String lastName, NameStyle nameStyle) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nameStyle = nameStyle;
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

    public NameStyle getNameStyle() {
        return nameStyle;
    }

    public void setNameStyle(NameStyle nameStyle) {
        this.nameStyle = nameStyle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientName clientName = (ClientName) o;
        return Objects.equals(firstName, clientName.firstName) && Objects.equals(lastName, clientName.lastName) && Objects.equals(nameStyle, clientName.nameStyle);
    }

    @Override
    public String toString() {
        return "ClientName{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nameStyle=" + nameStyle +
                '}';
    }
}
