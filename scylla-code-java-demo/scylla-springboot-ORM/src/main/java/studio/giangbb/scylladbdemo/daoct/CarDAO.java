package studio.giangbb.scylladbdemo.daoct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.entity.Car;
import com.giangbb.scylla.core.ScyllaTemplate;
import com.giangbb.scylla.repository.SimpleScyllaRepository;

/**
 * Created by giangbb on 12/05/2024
 */

@Component
@Qualifier("carDAO")
public class CarDAO extends SimpleScyllaRepository<Car> {

    @Autowired
    public CarDAO(ScyllaTemplate scyllaTemplate) {
        super(Car.class, scyllaTemplate);
    }



}
