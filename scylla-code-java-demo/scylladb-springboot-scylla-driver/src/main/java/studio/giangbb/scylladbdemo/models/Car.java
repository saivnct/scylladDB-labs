package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.mapper.annotations.*;

import java.util.Objects;

import static com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention.SNAKE_CASE_INSENSITIVE;

/**
 * Created by Giangbb on 04/04/2024
 */
@CqlName("car")
@Entity
@NamingStrategy(convention = SNAKE_CASE_INSENSITIVE)
public class Car extends Transportation {
    @PartitionKey(1)
    private String subBrand;

    @PartitionKey(0)
    private String brand;


    @ClusteringColumn(1)
    private String model;

    @ClusteringColumn(0)
    private int year;


    private String make;

    public Car() {
    }

    public Car(String brand, String subBrand, int year, String model, String make) {
        super(4, true);
        this.brand = brand;
        this.subBrand = subBrand;
        this.year = year;
        this.model = model;
        this.make = make;
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
        return Objects.equals(brand, car.brand) && Objects.equals(subBrand, car.subBrand) && Objects.equals(model, car.model) && Objects.equals(make, car.make) && year == car.year  && getWheels() == car.getWheels()&& isHasEngine() == car.isHasEngine();
    }


    @Override
    public String toString() {
        return "Car{" +
                "subBrand='" + subBrand + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", make='" + make + '\'' +
                "} " + super.toString();
    }
}