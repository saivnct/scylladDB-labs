package studio.giangbb.scylladbdemo.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import studio.giangbb.scylladbdemo.model.Car;

import java.util.UUID;

@Repository
public interface CarRepository extends CassandraRepository<Car, UUID> {

}
