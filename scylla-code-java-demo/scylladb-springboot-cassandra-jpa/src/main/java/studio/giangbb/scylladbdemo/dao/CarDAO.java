package studio.giangbb.scylladbdemo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.models.Car;

import java.util.List;

import static org.springframework.data.cassandra.core.query.Criteria.where;

/**
 * Created by giangbb on 26/03/2023
 */
@Component
@Qualifier("carDAO")
public class CarDAO extends AbstractScyllaDAO<Car.Key, Car> {
    @Autowired
    public CarDAO(CassandraOperations cassandraOperations) {
        super(cassandraOperations, Car.Key.class, Car.class);
    }

    public List<Car> findAllByPK(String brand, String subBrand){
        return this.find(
                Query.query(
                        where("brand").is(brand),
                        where("sub_brand").is(subBrand)
                )
        );
    }

    public Slice<Car> findAllByYear(int year, Pageable pageable){
        return this.find(
                Query.query(
                        where("year").is(year)
                ), pageable
        );
    }

}
