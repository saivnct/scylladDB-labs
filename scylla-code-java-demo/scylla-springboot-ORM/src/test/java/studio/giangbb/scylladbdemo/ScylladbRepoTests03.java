package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.core.PagingIterable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.daoct.CarDAO;
import studio.giangbb.scylladbdemo.entity.Car;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static studio.giangbb.scylladbdemo.ScylladbRepoTests01.sleepTask;
import static studio.giangbb.scylladbdemo.ScylladbTests03.getDummyCarMap;

/**
 * Created by Giangbb on 12/04/2024
 */
@SpringBootTest
public class ScylladbRepoTests03 {
    private final Logger logger = LoggerFactory.getLogger(ScylladbRepoTests03.class);

    @Autowired
    private CarDAO carDAO;

    @Test
    public void test() throws ExecutionException, InterruptedException {
        carDAO.deleteAll();

        int n = 6;
        int m = 8;

        Map<String, List<Car>> carMap = getDummyCarMap(6,8);
        List<Car> carList = carMap.values().stream().flatMap(List::stream).collect(Collectors.toList());


        long count = carDAO.countAll();
        long countASync = carDAO.countAllAsync().toCompletableFuture().get();
        assertThat(count).isEqualTo(0);
        assertThat(count).isEqualTo(countASync);



        Car car0 = carList.get(0);
        Car car1 = carList.get(1);


        carDAO.save(car0);
        Car carFetch0 = carDAO.findByPrimaryKey(car0);
        Car carFetch0Async = carDAO.findByPrimaryKeyAsync(car0).toCompletableFuture().get();
        assertThat(carFetch0).isNotNull();
        assertThat(carFetch0Async).isNotNull();
        assertThat(carFetch0).isEqualTo(car0);
        assertThat(carFetch0Async).isEqualTo(car0);

        carFetch0 = null;
        carFetch0Async = null;

        Map<CqlIdentifier, Object> pmkeys = Map.of(
                CqlIdentifier.fromCql("brand"), car0.getBrand(),
                CqlIdentifier.fromCql("sub_brand"), car0.getSubBrand(),
                CqlIdentifier.fromCql("year"), car0.getYear(),
                CqlIdentifier.fromCql("model"), car0.getModel()
        );
        carFetch0 = carDAO.findByPrimaryKey(pmkeys);
        carFetch0Async = carDAO.findByPrimaryKeyAsync(pmkeys).toCompletableFuture().get();
        assertThat(carFetch0).isNotNull();
        assertThat(carFetch0Async).isNotNull();
        assertThat(carFetch0).isEqualTo(car0);
        assertThat(carFetch0Async).isEqualTo(car0);


        boolean saved = carDAO.saveIfExists(car0);
        assertThat(saved).isTrue();
        saved = carDAO.saveIfExistsAsync(car0).toCompletableFuture().get();
        assertThat(saved).isTrue();

        saved = carDAO.saveIfExists(car1);
        assertThat(saved).isFalse();
        saved = carDAO.saveIfExistsAsync(car1).toCompletableFuture().get();
        assertThat(saved).isFalse();


        int ttl = 5; //5s
        carDAO.saveWithTtl(car0, ttl);
        carFetch0 = carDAO.findByPrimaryKey(car0);
        assertThat(carFetch0).isEqualTo(car0);
        CompletableFuture<Void> waitTask = sleepTask(ttl);
        logger.info("Wait for TTL...");
        waitTask.get();
        carFetch0 = carDAO.findByPrimaryKey(car0);
        assertThat(carFetch0).isNull();

        carDAO.saveWithTtlAsync(car0, ttl).toCompletableFuture().get();
        carFetch0 = carDAO.findByPrimaryKey(car0);
        assertThat(carFetch0).isEqualTo(car0);
        waitTask = sleepTask(ttl);
        logger.info("Wait for TTL Async...");
        waitTask.get();
        carFetch0 = carDAO.findByPrimaryKey(car0);
        assertThat(carFetch0).isNull();



        carDAO.save(car0);
        count = carDAO.countAll();
        assertThat(count).isEqualTo(1);
        carDAO.delete(car0);
        count = carDAO.countAll();
        assertThat(count).isEqualTo(0);


        carDAO.save(car0);
        count = carDAO.countAll();
        assertThat(count).isEqualTo(1);
        carDAO.deleteAsync(car0).toCompletableFuture().get();
        count = carDAO.countAll();
        assertThat(count).isEqualTo(0);






        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Car ca : carList) {
            CompletionStage<Void> saveCar = carDAO.saveAsync(ca);
            futures.add(saveCar.toCompletableFuture());
        }
        logger.info("Wait for insertion completed...");
        CompletableFuture<Void>[] futureArr = futures.stream().<CompletableFuture<Void>>toArray(CompletableFuture[] ::new);
        CompletableFuture<Void> combinedFuture
                = CompletableFuture.allOf(futureArr);
        combinedFuture.get();

        count = carDAO.countAll();
        countASync = carDAO.countAllAsync().toCompletableFuture().get();
        assertThat(count).isEqualTo(countASync);
        assertThat(count).isEqualTo(carList.size());
        logger.info("count {}", count);





        List<Car> cars = carDAO.findByPartitionKey(car0);
        assertThat(cars.size()).isEqualTo(m);
        for (Car carFetch : cars) {
            assertThat(carFetch.getBrand()).isEqualTo(car0.getBrand());
            assertThat(carFetch.getSubBrand()).isEqualTo(car0.getSubBrand());
        }
        logger.info("carFetchs {}", cars.size());

        long countByPKey = carDAO.countByPartitionKey(car0);
        long countByPKeyAsync = carDAO.countByPartitionKeyAsync(car0).toCompletableFuture().get();
        assertThat(countByPKey).isEqualTo(m);
        assertThat(countByPKeyAsync).isEqualTo(m);


        PagingIterable<Car> carFetchsPagingIterable = carDAO.findByPartitionKeyPagingIterable(car0);
        cars = new ArrayList<>();
        for (Car carFetch : carFetchsPagingIterable) {
            cars.add(carFetch);
            assertThat(carFetch.getBrand()).isEqualTo(car0.getBrand());
            assertThat(carFetch.getSubBrand()).isEqualTo(car0.getSubBrand());
        }
        logger.info("Cars: {}", cars.size());
        assertThat(cars.size()).isEqualTo(m);
        assertThat(carFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(carFetchsPagingIterable.isFullyFetched()).isEqualTo(true);


        Map<CqlIdentifier, Object> pkeys = Map.of(
                CqlIdentifier.fromCql("brand"), car0.getBrand(),
                CqlIdentifier.fromCql("sub_brand"), car0.getSubBrand()
        );
        cars = carDAO.findByPartitionKey(pkeys);
        assertThat(cars.size()).isEqualTo(m);
        for (Car carFetch : cars) {
            assertThat(carFetch.getBrand()).isEqualTo(car0.getBrand());
            assertThat(carFetch.getSubBrand()).isEqualTo(car0.getSubBrand());
        }

        countByPKey = carDAO.countByPartitionKey(pkeys);
        countByPKeyAsync = carDAO.countByPartitionKeyAsync(pkeys).toCompletableFuture().get();
        assertThat(countByPKey).isEqualTo(m);
        assertThat(countByPKeyAsync).isEqualTo(m);



        carFetchsPagingIterable = carDAO.findByPartitionKeyPagingIterable(pkeys);
        cars = new ArrayList<>();
        for (Car carFetch : carFetchsPagingIterable) {
            cars.add(carFetch);
            assertThat(carFetch.getBrand()).isEqualTo(car0.getBrand());
            assertThat(carFetch.getSubBrand()).isEqualTo(car0.getSubBrand());
        }
        logger.info("Cars: {}", cars.size());
        assertThat(cars.size()).isEqualTo(m);
        assertThat(carFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(carFetchsPagingIterable.isFullyFetched()).isEqualTo(true);





        cars = carDAO.findAll();
        assertThat(cars.size()).isEqualTo(carList.size());


        carFetchsPagingIterable = carDAO.findAllPagingIterable();
        cars = new ArrayList<>();
        for (Car car : carFetchsPagingIterable) {
            cars.add(car);
        }
        assertThat(cars.size()).isEqualTo(carList.size());
        assertThat(carFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(carFetchsPagingIterable.isFullyFetched()).isEqualTo(true);


        CompletionStage<MappedAsyncPagingIterable<Car>> asyncFetchCars = carDAO.findAllAsync();
        cars = asyncFetchCars.thenApply(mappedAsyncPagingIterable -> {
            List<Car> allCars = new ArrayList<>();
            boolean hasMorePages = true;
            while (hasMorePages){
                mappedAsyncPagingIterable.currentPage().forEach(allCars::add);
                hasMorePages = mappedAsyncPagingIterable.hasMorePages();
                if (hasMorePages){
                    logger.info("fetchNextPage");
                    mappedAsyncPagingIterable.fetchNextPage();
                }
            }
            return allCars;
        }).toCompletableFuture().get();
        assertThat(cars.size()).isEqualTo(carList.size());




    }

}
