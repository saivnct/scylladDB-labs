package studio.giangbb.scylladbdemo.repository;

import studio.giangbb.scylladbdemo.models.Car;

import java.util.List;

public interface CarRepositoryCustom {
    public List<Car> FindByYearAndMake(int year, String make);
}
