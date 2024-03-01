package studio.giangbb.scylladbdemo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.models.Car;
import studio.giangbb.scylladbdemo.models.FavoritePlace;
import studio.giangbb.scylladbdemo.models.Person;
import studio.giangbb.scylladbdemo.models.PersonName;
import studio.giangbb.scylladbdemo.repository.CarRepository;
import studio.giangbb.scylladbdemo.repository.PersonRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;
import static studio.giangbb.scylladbdemo.ScylladbTests01UsingCassandraTemplate.getDummyCarList;
import static studio.giangbb.scylladbdemo.ScylladbTests01UsingCassandraTemplate.getDummyPersonList;

@SpringBootTest
class ScylladbTests02UsingCassandraRepository {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests02UsingCassandraRepository.class);

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private CarRepository carRepository;


	@Test
	public void testPersonWithCassandraRepository() throws UnknownHostException, ExecutionException, InterruptedException {
		//delete all
		personRepository.deleteAll();

		long count = personRepository.count();
		assertThat(count).isEqualTo(0);

		List<Person> personList = getDummyPersonList();
		List<CompletableFuture<Person>> futures = new ArrayList<>();
		for (Person person : personList) {
			CompletableFuture<Person> completableFuture = CompletableFuture.supplyAsync(() -> {
				long threadId = Thread.currentThread().getId();
				logger.info("insert Person: {} - threadId: {}", person.getId().toString(), threadId);

				Person personInsert = personRepository.insert(person);
				assertThat(person).isEqualTo(personInsert);
				return personInsert;
			});

			futures.add(completableFuture);
		}

		logger.info("Wait for insertion completed...");

		CompletableFuture<Person>[] futureArr = futures.stream().<CompletableFuture<Person>>toArray(CompletableFuture[] ::new);
		CompletableFuture<Void> combinedFuture
				= CompletableFuture.allOf(futureArr);
		combinedFuture.get();



		count = personRepository.count();
		assertThat(count).isEqualTo(personList.size());

		//find with id(primkey)
		for (Person person: personList){
			Person fetchPerson = personRepository.findById(person.getId()).orElse(null);
			assertThat(fetchPerson).isNotNull();
			assertThat(fetchPerson).isEqualTo(person);
		}

		//find all
		List<Person> fetchPersons = personRepository.findAll();
		assertThat(fetchPersons.size()).isEqualTo(personList.size());



		//find with index column
		fetchPersons = personRepository.findAllByJob(Person.Job.TEACHER);
		assertThat(fetchPersons.size()).isEqualTo(personList.size()/2);

		logger.info("findAllByJob: {}", fetchPersons.stream().map(Person::getJob).collect(Collectors.toList()));
		for (Person person: fetchPersons) {
			assertThat(person.getJob()).isEqualTo(Person.Job.TEACHER);
		}

		//find with index column - custom query
		fetchPersons = personRepository.queryAllByJob(Person.Job.ENGINEER);
		assertThat(fetchPersons.size()).isEqualTo(personList.size()/2);
		logger.info("findAllByJob: {}", fetchPersons.stream().map(Person::getJob).collect(Collectors.toList()));
		for (Person person: fetchPersons) {
			assertThat(person.getJob()).isEqualTo(Person.Job.ENGINEER);
		}


		//find no index column with AllowFiltering
		PersonName personName = new PersonName("fist","last");
		fetchPersons = personRepository.findAllByName(new PersonName("fist","last"));
		assertThat(fetchPersons.size()).isEqualTo(personList.size() / 2);
		logger.info("findAllByName: {}", fetchPersons.stream().map(Person::getName).collect(Collectors.toList()));
		for (Person person: fetchPersons) {
			assertThat(person.getName()).isEqualTo(personName);
		}

	}


	@Test
	public void testCarWithCassandraRepository() throws ExecutionException, InterruptedException {
		//delete all
		carRepository.deleteAll();

		long count = carRepository.count();
		assertThat(count).isEqualTo(0);

		List<Car> carList = getDummyCarList();
		List<CompletableFuture<Car>> futures = new ArrayList<>();
		for (Car car : carList) {
			CompletableFuture<Car> completableFuture = CompletableFuture.supplyAsync(() -> {
				long threadId = Thread.currentThread().getId();
				logger.info("insert Car: {} - threadId: {}", car.getKey().toString(), threadId);

				Car carInsert = carRepository.insert(car);
				assertThat(car).isEqualTo(carInsert);
				return carInsert;
			});

			futures.add(completableFuture);
		}

		logger.info("Wait for insertion completed...");

		CompletableFuture<Car>[] futureArr = futures.stream().<CompletableFuture<Car>>toArray(CompletableFuture[] ::new);
		CompletableFuture<Void> combinedFuture
				= CompletableFuture.allOf(futureArr);
		combinedFuture.get();



		count = carRepository.count();
		assertThat(count).isEqualTo(carList.size());
		logger.info("count: {}", count);

		//find with id(primkey)
		for (Car car: carList){
			Car fetchCar = carRepository.findById(car.getKey()).orElse(null);
			assertThat(fetchCar).isNotNull();
			assertThat(fetchCar).isEqualTo(car);
		}

		//find all
		List<Car> fetchCars = carRepository.findAll();
		assertThat(fetchCars.size()).isEqualTo(carList.size());
	}

}
