package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import studio.giangbb.scylladbdemo.dao.PersonDAO;
import studio.giangbb.scylladbdemo.model.FavoritePlace;
import studio.giangbb.scylladbdemo.model.Person;
import studio.giangbb.scylladbdemo.model.PersonName;
import studio.giangbb.scylladbdemo.repository.CarRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@SpringBootTest
class ScylladbDemoApplicationTests3 {
	private final Logger log = LoggerFactory.getLogger(ScylladbDemoApplicationTests3.class);

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

	@Autowired
	private PersonDAO personDAO;



	@Test
	void testPerson() throws UnknownHostException {
		Person person = new Person(new PersonName("fist","last"), Person.Job.TEACHER, "test@test.com");
		person.getSessions().put("1", InetAddress.getByName("192.168.1.1"));
		person.getSessions().put("2", InetAddress.getByName("127.0.0.1"));

		person.getFavoritePlaces().add(new FavoritePlace("Singapore", "Singapore", 5));
		person.getFavoritePlaces().add(new FavoritePlace("Moscow", "Russia", 3));
		person.getFavoritePlaces().add(new FavoritePlace("Buhtan", "Buhtan", 2));

		person.getAddresses().add("Ho Chi Minh City");
		person.getAddresses().add("Can Tho City");
		person.getAddresses().add("Quang Binh City");

		person = cassandraTemplate.insert(person);

		Person personFetch = cassandraTemplate.selectOneById(person.getId(), Person.class);

		log.info("person: {}", person);
		log.info("personFetch: {}", personFetch);

		assertThat(person).isEqualTo(personFetch);
	}


	@Test
	void testFilter() {
		List<Person> persons = cassandraTemplate.select(
				query(
						where("name").
						is(new PersonName("fist","last"))
				).withAllowFiltering(),
				Person.class
		);

		log.info("persons: {}", persons);

		assertThat(persons.size()).isGreaterThan(0);
	}



	@Test
	void testPersonDAO() throws UnknownHostException {

//		SimpleStatement simpleStatement = SchemaBuilder.alterTable("person").addColumn("email", DataTypes.TEXT).build();
//		session.execute(simpleStatement);


		Person person = new Person(new PersonName("fist","last"), Person.Job.TEACHER, "test@test.com");
		person.getSessions().put("1", InetAddress.getByName("192.168.1.1"));
		person.getSessions().put("2", InetAddress.getByName("127.0.0.1"));

		person.getFavoritePlaces().add(new FavoritePlace("Singapore", "Singapore", 5));
		person.getFavoritePlaces().add(new FavoritePlace("Moscow", "Russia", 3));
		person.getFavoritePlaces().add(new FavoritePlace("Buhtan", "Buhtan", 2));

		person.getAddresses().add("Ho Chi Minh City");
		person.getAddresses().add("Can Tho City");
		person.getAddresses().add("Quang Binh City");

		person = personDAO.save(person);

		Person personFetch = personDAO.findById(person.getId()).orElse(null);

		assertThat(personFetch).isNotNull();

		log.info("person: {}", person);
		log.info("personFetch: {}", personFetch);

		assertThat(person).isEqualTo(personFetch);


		PersonName newName = new PersonName("fist2","last2");
		personFetch.setName(newName);
		personDAO.save(personFetch);
		assertThat(personFetch.getId()).isEqualTo(person.getId());
		assertThat(personFetch.getName()).isEqualTo(newName);
	}

	@Test
	void testPersonDAOFilter() {
		List<Person> persons = personDAO.findAll();
		assertThat(persons).isNotNull();
		log.info("persons: {}", persons);

//		PersonName newName = new PersonName("fist2","last2");
//		List<Person> persons = personDAO.select(query(where("name").is(newName)).withAllowFiltering());
//		log.info("persons: {} - {}", persons.size(), persons);
	}

	@Test
	void testPersonDAOFilter2() {
		List<Person> persons = personDAO.select(query(
				where("email").
						is("test@test.com")
		));

		assertThat(persons).isNotNull();
		log.info("persons: {}", persons.size());
		log.info("persons: {}", persons);

//		PersonName newName = new PersonName("fist2","last2");
//		List<Person> persons = personDAO.select(query(where("name").is(newName)).withAllowFiltering());
//		log.info("persons: {} - {}", persons.size(), persons);
	}



//	@Test
//	void testMigration() {
//		Database database = new Database(session, new MigrationConfiguration().withKeyspaceName("springdemo"));
//		MigrationTask migration = new MigrationTask(database, new MigrationRepository(MigrationRepository.DEFAULT_SCRIPT_PATH));
//		migration.migrate();
//	}


}
