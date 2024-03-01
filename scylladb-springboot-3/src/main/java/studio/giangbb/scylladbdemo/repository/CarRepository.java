package studio.giangbb.scylladbdemo.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import studio.giangbb.scylladbdemo.models.Car;


/**
 * Created by Giangbb on 01/03/2024
 */
@Repository
public interface CarRepository extends CassandraRepository<Car, Car.Key> {

}
