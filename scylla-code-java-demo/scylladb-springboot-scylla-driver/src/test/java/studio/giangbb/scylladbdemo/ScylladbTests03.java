package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateIndex;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableWithOptions;
import com.datastax.oss.driver.api.querybuilder.schema.CreateType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.dao.CarDao;
import studio.giangbb.scylladbdemo.dao.ClientDao;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.models.Car;
import studio.giangbb.scylladbdemo.models.Client;
import studio.giangbb.scylladbdemo.models.ClientInfo;
import studio.giangbb.scylladbdemo.models.ClientName;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ScylladbTests03 {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests03.class);


	private final CqlSession session;

	private final DaoMapper daoMapper;

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
					Car car = new Car(brand, subBrand, String.format("model_%d", k), make, 2000+i);
					carList.add(car);
				}

				String pk = String.format("%s_%s", brand, subBrand);
				carMap.put(pk, carList);
			}
		}

		return carMap;
	}


	@Autowired
	public ScylladbTests03(
			CqlSession session,
			DaoMapper daoMapper
	) {
		this.session = session;
		this.daoMapper = daoMapper;


		CreateTableWithOptions createTable = createTable("car")
				.ifNotExists()
				.withPartitionKey("brand", DataTypes.TEXT)
				.withPartitionKey("subBrand", DataTypes.TEXT)
				.withClusteringColumn("model", DataTypes.TEXT)
				.withColumn("make", DataTypes.TEXT)
				.withColumn("year", DataTypes.INT)
				.withCompaction(sizeTieredCompactionStrategy());

		CreateIndex createIndex = createIndex()
				.ifNotExists()
				.onTable("car")
				.andColumn("year");

		PreparedStatement preparedCreateTable = session.prepare(createTable.build());
		PreparedStatement preparedCreateIndex = session.prepare(createIndex.build());
		session.execute(preparedCreateTable.bind());
		session.execute(preparedCreateIndex.bind());
	}

	@Test
	public void testCount(){
		CarDao carDao = daoMapper.carDao();

		long count = carDao.countAll();
		logger.info("count {}", count);
	}

	@Test
	public void testCar() throws UnknownHostException, ExecutionException, InterruptedException {
		CarDao carDao = daoMapper.carDao();

		//delete all
		carDao.deleteAll();

		long count = carDao.countAll();
		assertThat(count).isEqualTo(0);

		Map<String, List<Car>> carMap = getDummyCarMap();
		List<Car> carList = carMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Car ca : carList) {
			CompletionStage<Void> saveCar = carDao.saveAsync(ca);
			futures.add(saveCar.toCompletableFuture());
		}

		logger.info("Wait for insertion completed...");

		CompletableFuture<Void>[] futureArr = futures.stream().<CompletableFuture<Void>>toArray(CompletableFuture[] ::new);
		CompletableFuture<Void> combinedFuture
				= CompletableFuture.allOf(futureArr);
		combinedFuture.get();


		count = carDao.countAll();
		assertThat(count).isEqualTo(carList.size());
		logger.info("count {}", count);

		//find with id(primkey)
		for (Car car: carList){
			Car fetchCar = carDao.findByPrimKey(car.getBrand(), car.getSubBrand(), car.getModel());
			assertThat(fetchCar).isNotNull();
			assertThat(fetchCar).isEqualTo(car);
		}

		//find with partition keys
		String brand = carList.get(0).getBrand();
		String subBrand = carList.get(0).getSubBrand();
		String pk = String.format("%s_%s", brand, subBrand);

		PagingIterable<Car> fetchCars = carDao.findByPartKey(brand,subBrand);
		List<Car> cars = new ArrayList<>();
		assertThat(fetchCars.iterator().hasNext()).isEqualTo(true);
		for (Car car : fetchCars) {
			cars.add(car);
		}
		assertThat(fetchCars.iterator().hasNext()).isEqualTo(false);
		assertThat(fetchCars.isFullyFetched()).isEqualTo(true);
		assertThat(cars.size()).isEqualTo(carMap.get(pk).size());
		logger.info("cars {}", cars);


		//find index column
		for (Car car: carList){
			fetchCars = carDao.getByYear(car.getYear());
			cars = new ArrayList<>();
			for (Car carFetch : fetchCars) {
				cars.add(carFetch);
			}
			assertThat(cars.size()).isEqualTo(carList.stream().filter(p -> p.getYear() == car.getYear()).count());
		}
	}




}
