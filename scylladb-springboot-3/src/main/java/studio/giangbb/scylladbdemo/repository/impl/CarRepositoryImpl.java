package studio.giangbb.scylladbdemo.repository.impl;

import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.query.Query;
import studio.giangbb.scylladbdemo.models.Car;
import studio.giangbb.scylladbdemo.repository.CarRepositoryCustom;

import java.util.List;

import static org.springframework.data.cassandra.core.query.Criteria.where;

public class CarRepositoryImpl implements CarRepositoryCustom {
    private final CassandraOperations operations;

    public CarRepositoryImpl(CassandraOperations operations) {
        this.operations = operations;
    }

    @Override
    public List<Car> FindByYearAndMake(int year, String make) {
        List<Car> cars = this.operations.select(
                Query.query(
                        where("year").is(year)
                ).and(
                        where("make").is(make)
                ).withAllowFiltering(), Car.class);


        return cars;
    }
}
