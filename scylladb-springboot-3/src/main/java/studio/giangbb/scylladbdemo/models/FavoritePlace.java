package studio.giangbb.scylladbdemo.models;

import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * Created by Giangbb on 01/03/2024
 */
@UserDefinedType()
public class FavoritePlace {

    private String city;
    private String country;
    private Integer rating = 0;

    public FavoritePlace() {
    }

    public FavoritePlace(String city, String country, Integer rating) {
        this.city = city;
        this.country = country;
        this.rating = rating;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "FavoritePlace{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", rating=" + rating +
                '}';
    }
}
