package studio.giangbb.scylladbdemo.models;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.Objects;

/**
 * Created by Giangbb on 01/03/2024
 */
@Table("car")
public class Car{
    @PrimaryKeyClass
    public static class Key{
        @PrimaryKeyColumn(name = "brand", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String brand;

        @PrimaryKeyColumn(name = "model", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private String model;

        public Key(String brand, String model) {
            this.brand = brand;
            this.model = model;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(brand, key.brand) && Objects.equals(model, key.model);
        }

        @Override
        public String toString() {
            return "Key{" +
                    "brand='" + brand + '\'' +
                    ", model='" + model + '\'' +
                    '}';
        }
    }

    @PrimaryKey
    private Key key;

    @Column("make")
    private String make;

    @Indexed
    @Column("year")
    private int year;

    public Car(Key key, String make, int year) {
        this.key = key;
        this.make = make;
        this.year = year;
    }


    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(key, car.key) && Objects.equals(make, car.make) && year == car.year;
    }

    @Override
    public String toString() {
        return "Car{" +
                "key=" + key +
                ", make='" + make + '\'' +
                ", year=" + year +
                '}';
    }
}
