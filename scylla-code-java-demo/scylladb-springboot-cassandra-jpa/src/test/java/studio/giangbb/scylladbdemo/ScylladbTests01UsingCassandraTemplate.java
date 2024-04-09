package studio.giangbb.scylladbdemo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import studio.giangbb.scylladbdemo.models.Car;
import studio.giangbb.scylladbdemo.models.FavoritePlace;
import studio.giangbb.scylladbdemo.models.Person;
import studio.giangbb.scylladbdemo.models.PersonName;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@SpringBootTest
class ScylladbTests01UsingCassandraTemplate {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests01UsingCassandraTemplate.class);

	@Autowired
	private CassandraAdminTemplate cassandraTemplate;


	public static List<Person> getDummyPersonList() throws UnknownHostException {
		List<Person> personList= new ArrayList<>();
		int n = 10;
		for (int i = 0; i < n; i++) {
			Person.Job job = i % 2 == 0 ? Person.Job.TEACHER : Person.Job.ENGINEER;
			PersonName personName = i % 2 == 0 ? new PersonName("fist","last") : new PersonName("fist1","last1");

			Person person = new Person(personName, 30, job, String.format("test_%d@test.com", i));
			person.getSessions().put("1", InetAddress.getByName(String.format("192.168.1.%d", i)));
			person.getSessions().put("2", InetAddress.getByName("127.0.0.1"));

			person.getFavoritePlaces().add(new FavoritePlace("Singapore", "Singapore", 5));
			person.getFavoritePlaces().add(new FavoritePlace("Moscow", "Russia", 3));
			person.getFavoritePlaces().add(new FavoritePlace("Buhtan", "Buhtan", 2));

			person.getAddresses().add("Ho Chi Minh City");
			person.getAddresses().add("Can Tho City");
			person.getAddresses().add("Quang Binh City");

			personList.add(person);
		}

		return personList;
	}
	public static Map<String, List<Car>> getDummyCarMap() {
		Map<String, List<Car>> carMap = new HashMap<>();

		int n = 6;
		int m = 8;
		for (int i = 0; i < n; i++) {
			String make = i % 2 == 0 ? "Japan" : "USA";
			String brand = String.format("brand_%d", i);


			for (int j = 0; j < n/2; j++) {
				String subBrand = String.format("subBrand_%d", j);
				List<Car> carList= new ArrayList<>();
				for (int k = 0; k < m; k++){
					Car.Key key = new Car.Key(brand, subBrand, 2000+i, String.format("model_%d", k));
					Car car = new Car(key, make);
					carList.add(car);
				}

				String pk = String.format("%s_%s", brand, subBrand);
				carMap.put(pk, carList);
			}
		}

		return carMap;
	}

	@Test
	public void testPersonWithCassandraTemplate() throws UnknownHostException, ExecutionException, InterruptedException {
		//delete all
		cassandraTemplate.truncate(Person.class);

		long count = cassandraTemplate.count(Person.class);
		assertThat(count).isEqualTo(0);

		List<Person> personList = getDummyPersonList();
		List<CompletableFuture<Person>> futures = new ArrayList<>();
		for (Person person : personList) {
			CompletableFuture<Person> completableFuture = CompletableFuture.supplyAsync(() -> {
				long threadId = Thread.currentThread().getId();
				logger.info("insert Person: {} - threadId: {}", person.getId().toString(), threadId);

				Person personInsert = cassandraTemplate.insert(person);
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



		count = cassandraTemplate.count(Person.class);
		assertThat(count).isEqualTo(personList.size());

		//find with id(primkey)
		for (Person person: personList){
			Person fetchPerson = cassandraTemplate.selectOneById(person.getId(), Person.class);
			assertThat(fetchPerson).isNotNull();
			assertThat(fetchPerson).isEqualTo(person);
		}

		//find all
		List<Person> fetchPersons = cassandraTemplate.select(query(),Person.class);
		assertThat(fetchPersons.size()).isEqualTo(personList.size());


		//find no index column with AllowFiltering
		PersonName personName = new PersonName("fist","last");
		fetchPersons = cassandraTemplate.select(
				query(
						where("name").is(personName)
				).withAllowFiltering(), Person.class);
		assertThat(fetchPersons.size()).isEqualTo(personList.size() / 2);
		for (Person person: fetchPersons) {
			assertThat(person.getName()).isEqualTo(personName);
		}


		//find index column
		fetchPersons = cassandraTemplate.select(
				query(
						where("job")
								.is(Person.Job.TEACHER)
				), Person.class);
		assertThat(fetchPersons.size()).isEqualTo(personList.size()/2);
		for (Person person: fetchPersons) {
			assertThat(person.getJob()).isEqualTo(Person.Job.TEACHER);
		}
	}




}
