package studio.giangbb.scylladbdemo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import studio.giangbb.scylladbdemo.dao.CarDAO;
import studio.giangbb.scylladbdemo.dao.PersonDAO;
import studio.giangbb.scylladbdemo.models.Car;
import studio.giangbb.scylladbdemo.models.Person;
import studio.giangbb.scylladbdemo.models.PersonName;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static studio.giangbb.scylladbdemo.ScylladbTests01UsingCassandraTemplate.getDummyCarMap;
import static studio.giangbb.scylladbdemo.ScylladbTests01UsingCassandraTemplate.getDummyPersonList;

@SpringBootTest
class ScylladbTests03UsingAbstractScyllaDAO {

	@Autowired
	private CarDAO carDAO;

	@Autowired
	private PersonDAO personDAO;

	private final Logger logger = LoggerFactory.getLogger(ScylladbTests03UsingAbstractScyllaDAO.class);


	@Test
	public void testPersonWithCassandraRepository() throws UnknownHostException, ExecutionException, InterruptedException {
		//delete all
		personDAO.deleteAll();

		long count = personDAO.countAll();
		assertThat(count).isEqualTo(0);

		List<Person> personList = getDummyPersonList();
		List<CompletableFuture<Person>> futures = new ArrayList<>();
		for (Person person : personList) {
			CompletableFuture<Person> completableFuture = CompletableFuture.supplyAsync(() -> {
				long threadId = Thread.currentThread().getId();
				logger.info("insert Person: {} - threadId: {}", person.getId().toString(), threadId);
				Person personInsert = personDAO.save(person);
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



		count = personDAO.countAll();
		assertThat(count).isEqualTo(personList.size());

		//find with id(primkey)
		for (Person person: personList){
			Person fetchPerson = personDAO.getByKey(person.getId());
			assertThat(fetchPerson).isNotNull();
			assertThat(fetchPerson).isEqualTo(person);
		}

		//find all
		List<Person> fetchPersons = personDAO.findAll();
		assertThat(fetchPersons.size()).isEqualTo(personList.size());



		//find with index column
		fetchPersons = personDAO.findAllByJob(Person.Job.TEACHER);
		assertThat(fetchPersons.size()).isEqualTo(personList.size()/2);

		logger.info("findAllByJob: {}", fetchPersons.stream().map(Person::getJob).collect(Collectors.toList()));
		for (Person person: fetchPersons) {
			assertThat(person.getJob()).isEqualTo(Person.Job.TEACHER);
		}

		//find no index column with AllowFiltering
		PersonName personName = new PersonName("fist","last");
		fetchPersons = personDAO.findAllByName(new PersonName("fist","last"));
		assertThat(fetchPersons.size()).isEqualTo(personList.size() / 2);
		logger.info("findAllByName: {}", fetchPersons.stream().map(Person::getName).collect(Collectors.toList()));
		for (Person person: fetchPersons) {
			assertThat(person.getName()).isEqualTo(personName);
		}
	}


	@Test
	public void testCarWithCassandraRepository() throws ExecutionException, InterruptedException {
		//delete all
		carDAO.deleteAll();

		long count = carDAO.countAll();
		assertThat(count).isEqualTo(0);

		Map<String, List<Car>> carMap = getDummyCarMap();
		List<Car> carList = carMap.values().stream().flatMap(List::stream).collect(Collectors.toList());

		List<CompletableFuture<Car>> futures = new ArrayList<>();
		for (Car car : carList) {
			CompletableFuture<Car> completableFuture = CompletableFuture.supplyAsync(() -> {
				long threadId = Thread.currentThread().getId();
				logger.info("insert Car: {} - threadId: {}", car.getKey().toString(), threadId);

				Car carInsert = carDAO.save(car);
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

		count = carDAO.countAll();
		assertThat(count).isEqualTo(carList.size());

		//find with id(primkey)
		for (Car car: carList){
			Car fetchCar = carDAO.getByKey(car.getKey());
			assertThat(fetchCar).isNotNull();
			assertThat(fetchCar).isEqualTo(car);
		}

		//find all
		List<Car> fetchCars = carDAO.findAll();
		assertThat(fetchCars.size()).isEqualTo(carList.size());

		//find with partition key
		String brand = carList.get(0).getKey().getBrand();
		fetchCars = carDAO.findAllByBrand(brand);
		assertThat(fetchCars.size()).isEqualTo(carMap.get(brand).size());

		//find with pagination
		fetchCars = new ArrayList<>();
		Slice<Car> carsSlice = carDAO.findAll(PageRequest.of(0, 5));
		while (carsSlice.hasContent()) {
			fetchCars.addAll(carsSlice.getContent());
			// process the cars
			if (!carsSlice.hasNext()) {
				break;
			}
			Pageable nextPageable = carsSlice.nextPageable();
			carsSlice = carDAO.findAll(nextPageable);
		}
		assertThat(fetchCars.size()).isEqualTo(carList.size());
//		for (Car car: fetchCars){
//			logger.info("car: {}", car.getKey());
//		}


		//find by index with pagination
		final int year = 2000;
		fetchCars = new ArrayList<>();
		carsSlice = carDAO.findAllByYear(year, PageRequest.of(0, 3));
		while (carsSlice.hasContent()) {
			fetchCars.addAll(carsSlice.getContent());
			// process the cars
			if (!carsSlice.hasNext()) {
				break;
			}
			Pageable nextPageable = carsSlice.nextPageable();
			logger.info("nextPageable: {} - {}", nextPageable.getPageNumber(), nextPageable.getPageSize());
			carsSlice = carDAO.findAllByYear(year, nextPageable);
		}
		assertThat(fetchCars.size()).isEqualTo(
				carList.stream()
						.filter(car -> car.getYear() == year)
						.count()
		);
		for (Car car: fetchCars){
			logger.info("car: {} - {}", car.getKey(), car.getYear());
		}

	}
}
