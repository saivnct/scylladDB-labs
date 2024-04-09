package studio.giangbb.scylladbdemo.models;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.Objects;

/**
 * Created by Giangbb on 01/03/2024
 */
@Table("car")
public class Car extends Transportation{
    @PrimaryKeyClass
    public static class Key{
        @PrimaryKeyColumn(name = "brand", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String brand;

        @PrimaryKeyColumn(name = "sub_brand", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private String subBrand;

        @Indexed
        @PrimaryKeyColumn(name = "year", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private int year;

        @PrimaryKeyColumn(name = "model", ordinal = 3, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
        private String model;

        public Key(String brand, String subBrand, int year, String model) {
            this.brand = brand;
            this.subBrand = subBrand;
            this.year = year;
            this.model = model;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getSubBrand() {
            return subBrand;
        }

        public void setSubBrand(String subBrand) {
            this.subBrand = subBrand;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(brand, key.brand) && Objects.equals(subBrand, key.subBrand) && year == key.year && Objects.equals(model, key.model);
        }

        @Override
        public String toString() {
            return "Key{" +
                    "brand='" + brand + '\'' +
                    ", subBrand='" + subBrand + '\'' +
                    ", year=" + year +
                    ", model='" + model + '\'' +
                    '}';
        }
    }

    @PrimaryKey
    private Key key;

    private String make;

    public Car(Key key, String make) {
        super(4, true);
        this.key = key;
        this.make = make;
    }


    //GETTER AND SETTER


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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(key, car.key) && Objects.equals(make, car.make) && getWheels() == car.getWheels()&& isHasEngine() == car.isHasEngine();
    }

    @Override
    public String toString() {
        return "Car{" +
                "key=" + key +
                ", make='" + make + '\'' +
                "} " + super.toString();
    }
}
