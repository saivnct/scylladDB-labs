package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;
import studio.giangbb.scylladbdemo.model.Car;
import studio.giangbb.scylladbdemo.model.UserV2;
import studio.giangbb.scylladbdemo.model.UserV3;
import studio.giangbb.scylladbdemo.repository.CarRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@SpringBootTest
class ScylladbDemoApplicationTests2 {
	private final Logger log = LoggerFactory.getLogger(ScylladbDemoApplicationTests2.class);

	@Autowired
	private CqlSession session;

	@Autowired
	private CarRepository carRepository;


	@Autowired
	private CassandraAdminTemplate cassandraTemplate;

	@Autowired
	private CassandraMappingContext mappingContext;

	@Autowired
	private CassandraOperations cassandraOperations;


	@Test
	void getEntities(){
		mappingContext.getTableEntities().forEach(x -> {
			log.info("table: {}, class: {}", x.getTableName(), x.getType().getName());


			x.doWithProperties(new SimplePropertyHandler() {
				@Override
				public void doWithPersistentProperty(PersistentProperty<?> property) {
					log.info("prop: {} - {}",property.getName(), property.getActualType().getName());
				}
			});

			log.info("-------------------------------");
		});
	}

	@Test
	void testCar() {


		UUID carId = Uuids.timeBased();
		Car newCar = new Car(carId, "Nissan", "Qashqai", 2018);

		carRepository.save(newCar);

		List<Car> savedCars = carRepository.findAllById(List.of(carId));
		assertThat(savedCars.get(0)).isEqualTo(newCar);


		UUID carId2 = Uuids.timeBased();
		Car newCar2 = cassandraTemplate.insert(new Car(carId2, "Toy", "Camry", 2023));

		assertThat(carId2).isEqualTo(newCar2.getId());
	}


	@Test
	void testUserV2() {
		UserV2 user = cassandraTemplate.insert(new UserV2(8, 13, "test", 2018l));

//		UserV2 userFetch = cassandraTemplate.selectOne(query(where("id").is(8)), UserV2.class);
//		assertThat(user.getId()).isEqualTo(userFetch.getId());

		List<UserV2> userFetchs = cassandraTemplate.select(query(where("username").is("JohnDoe")), UserV2.class);
		assertThat(userFetchs.size()).isEqualTo(6);
	}


	@Test
	void testUserV3() {
		UserV3 user = cassandraTemplate.insert(new UserV3(new UserV3.Key(9, 20), "test2", 2018l));

		UserV3 userFetch = cassandraTemplate.selectOneById(new UserV3.Key(9, 20), UserV3.class);
		assertThat(user.getKey()).isEqualTo(userFetch.getKey());
	}
}
