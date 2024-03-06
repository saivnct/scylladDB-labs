package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

import java.util.Set;

/**
 * Created by Giangbb on 06/03/2024
 */
@Entity
public class ClientInfo {
    private int zipCode;
    private int age;
    private Set<String> phones;


    public ClientInfo() {
    }

    public ClientInfo(int zipCode, int age, Set<String> phones) {
        this.zipCode = zipCode;
        this.age = age;
        this.phones = phones;
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
                '}';
    }
}
