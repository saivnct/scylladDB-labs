package studio.giangbb.scylladbdemo.models;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Giangbb on 06/03/2024
 */
@UserDefinedType("client_info")
public class ClientInfo {
    @Column("zip_code")
    private int zipCode;

    @Column("age")
    private int age;

    @Column("phones")
    private Set<String> phones;

    private Map<String, InetAddress> sessions;

    @Column("favorite_places")
    private List<FavoritePlace> favoritePlaces;


    public ClientInfo() {
    }

    public ClientInfo(int zipCode, int age, Set<String> phones, Map<String, InetAddress> sessions, List<FavoritePlace> favoritePlaces) {
        this.zipCode = zipCode;
        this.age = age;
        this.phones = phones;
        this.sessions = sessions;
        this.favoritePlaces = favoritePlaces;
    }

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Set<String> getPhones() {
        return phones;
    }

    public void setPhones(Set<String> phones) {
        this.phones = phones;
    }


    public Map<String, InetAddress> getSessions() {
        return sessions;
    }

    public void setSessions(Map<String, InetAddress> sessions) {
        this.sessions = sessions;
    }

    public List<FavoritePlace> getFavoritePlaces() {
        return favoritePlaces;
    }

    public void setFavoritePlaces(List<FavoritePlace> favoritePlaces) {
        this.favoritePlaces = favoritePlaces;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientInfo clientInfo = (ClientInfo) o;
        return zipCode == clientInfo.zipCode && age == clientInfo.age && phones.equals(clientInfo.phones);
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "zipCode=" + zipCode +
                ", age=" + age +
                ", phones=" + phones +
                ", sessions=" + sessions +
                ", favoritePlaces=" + favoritePlaces +
                '}';
    }
}
