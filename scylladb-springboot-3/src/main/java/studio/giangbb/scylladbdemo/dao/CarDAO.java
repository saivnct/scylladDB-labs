package studio.giangbb.scylladbdemo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.models.Car;

/**
 * Created by giangbb on 26/06/2023
 */

@Component
@Qualifier("carDAO")
public class CarDAO extends AbstractScyllaDAO<Car.Key, Car> {

    @Autowired
    public CarDAO(CassandraOperations cassandraOperations) {
        super(cassandraOperations, Car.Key.class, Car.class);
    }


}
