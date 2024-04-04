package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.util.Objects;

/**
 * Created by Giangbb on 04/04/2024
 */
@Entity
public class Car {
    @PartitionKey(0)
    private String brand;

    @PartitionKey(1)
    private String subBrand;

    @ClusteringColumn
    private String model;

    private String make;

    private int year;

    public Car() {
    }

    public Car(String brand, String subBrand, String model, String make, int year) {
        this.brand = brand;
        this.subBrand = subBrand;
        this.model = model;
        this.make = make;
        this.year = year;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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
        return Objects.equals(brand, car.brand) && Objects.equals(subBrand, car.subBrand) && Objects.equals(model, car.model) && Objects.equals(make, car.make) && year == car.year;
    }


    @Override
    public String toString() {
        return "Car{" +
                "brand='" + brand + '\'' +
                ", subBrand='" + subBrand + '\'' +
                ", model='" + model + '\'' +
                ", make='" + make + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}