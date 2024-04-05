package studio.giangbb.scylladbdemo.models;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.Objects;

/**
 * Created by Giangbb on 01/03/2024
 */
@Table("bike")
public class Bike {
    @PrimaryKeyColumn(name = "brand", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String brand;

    @PrimaryKeyColumn(name = "sub_brand", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String subBrand;

    @PrimaryKeyColumn(name = "model", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private String model;

    @Column("make")
    private String make;

    @Indexed
    @Column("year")
    private int year;

    public Bike(String brand, String subBrand, String model, String make, int year) {
        this.brand = brand;
        this.subBrand = subBrand;
        this.model = model;
        this.make = make;
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bike car = (Bike) o;
        return Objects.equals(brand, car.brand) && Objects.equals(subBrand, car.subBrand) && Objects.equals(model, car.model) && Objects.equals(make, car.make) && year == car.year;
    }

    @Override
    public String toString() {
        return "Bike{" +
                "brand='" + brand + '\'' +
                ", subBrand='" + subBrand + '\'' +
                ", model='" + model + '\'' +
                ", make='" + make + '\'' +
                ", year=" + year +
                '}';
    }
}
