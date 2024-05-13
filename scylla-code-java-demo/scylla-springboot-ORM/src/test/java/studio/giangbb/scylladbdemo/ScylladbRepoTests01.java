package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.core.PagingIterable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.daoct.UserDAO;
import studio.giangbb.scylladbdemo.entity.User;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static studio.giangbb.scylladbdemo.ScylladbTests01.getDummyUserList;

/**
 * Created by giangbb on 12/05/2024
 */
@SpringBootTest
public class ScylladbRepoTests01 {
    private final Logger logger = LoggerFactory.getLogger(ScylladbRepoTests01.class);

    @Autowired
    private UserDAO userDAO;

    public static CompletableFuture<Void> sleepTask(int ttl){
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(ttl * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testClean() throws ExecutionException, InterruptedException {
        userDAO.deleteAll();

        long count = userDAO.countAll();
        long countASync = userDAO.countAllAsync().toCompletableFuture().get();
        assertThat(count).isEqualTo(0);
        assertThat(count).isEqualTo(countASync);
    }

    @Test
    public void test() throws ExecutionException, InterruptedException, UnknownHostException {
        long countStart = userDAO.countAll();


        ConsistencyLevel consistencyLevel = null;
//        ConsistencyLevel consistencyLevel = ConsistencyLevel.LOCAL_ONE;
//        ConsistencyLevel consistencyLevel = ConsistencyLevel.LOCAL_QUORUM;

        int n = 10;
        List<User> userList = getDummyUserList(10);

        User user0 = userList.get(0);
        User user1 = userList.get(1);


        userDAO.save(user0, consistencyLevel);
        User userFetch0 = userDAO.findByPrimaryKey(user0);
        User userFetch0Async = userDAO.findByPrimaryKeyAsync(user0).toCompletableFuture().get();
        assertThat(userFetch0).isNotNull();
        assertThat(userFetch0Async).isNotNull();
        assertThat(userFetch0).isEqualTo(user0);
        assertThat(userFetch0Async).isEqualTo(user0);

        userFetch0 = null;
        userFetch0Async = null;

        Map<CqlIdentifier, Object> pmkeys = Map.of(
                CqlIdentifier.fromCql("user_age"), user0.getUserAge(),
                CqlIdentifier.fromCql("id"), user0.getId()
        );
        userFetch0 = userDAO.findByPrimaryKey(pmkeys);
        userFetch0Async = userDAO.findByPrimaryKeyAsync(pmkeys).toCompletableFuture().get();
        assertThat(userFetch0).isNotNull();
        assertThat(userFetch0Async).isNotNull();
        assertThat(userFetch0).isEqualTo(user0);
        assertThat(userFetch0Async).isEqualTo(user0);


        userFetch0 = userDAO.findById(user0.getId());
        userFetch0Async = userDAO.findByIdAsync(user0.getId()).toCompletableFuture().get();
        assertThat(userFetch0).isEqualTo(user0);
        assertThat(userFetch0Async).isEqualTo(user0);


        boolean saved = userDAO.saveIfExists(user0, consistencyLevel);
        assertThat(saved).isTrue();
        saved = userDAO.saveIfExistsAsync(user0, consistencyLevel).toCompletableFuture().get();
        assertThat(saved).isTrue();

        saved = userDAO.saveIfExists(user1, consistencyLevel);
        assertThat(saved).isFalse();
        saved = userDAO.saveIfExistsAsync(user1, consistencyLevel).toCompletableFuture().get();
        assertThat(saved).isFalse();


        int ttl = 5; //5s
        userDAO.saveWithTtl(user0, ttl);
        userFetch0 = userDAO.findByPrimaryKey(user0);
        assertThat(userFetch0).isEqualTo(user0);
        CompletableFuture<Void> waitTask = sleepTask(ttl);
        logger.info("Wait for TTL...");
        waitTask.get();
        userFetch0 = userDAO.findByPrimaryKey(user0);
        assertThat(userFetch0).isNull();

        userDAO.saveWithTtlAsync(user0, ttl).toCompletableFuture().get();
        userFetch0 = userDAO.findByPrimaryKey(user0);
        assertThat(userFetch0).isEqualTo(user0);
        waitTask = sleepTask(ttl);
        logger.info("Wait for TTL Async...");
        waitTask.get();
        userFetch0 = userDAO.findByPrimaryKey(user0);
        assertThat(userFetch0).isNull();



        userDAO.save(user0, consistencyLevel);
        long count = userDAO.countAll();
        assertThat(count).isEqualTo(countStart + 1);
        userDAO.delete(user0, consistencyLevel);
        count = userDAO.countAll();
        assertThat(count).isEqualTo(countStart);


        userDAO.save(user0, consistencyLevel);
        count = userDAO.countAll();
        assertThat(count).isEqualTo(countStart + 1);
        userDAO.deleteAsync(user0, consistencyLevel).toCompletableFuture().get();
        count = userDAO.countAll();
        assertThat(count).isEqualTo(countStart);






        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (User user : userList) {
            CompletionStage<Void> saveCar = userDAO.saveAsync(user, consistencyLevel);
            futures.add(saveCar.toCompletableFuture());
        }
        logger.info("Wait for insertion completed...");
        CompletableFuture<Void>[] futureArr = futures.stream().<CompletableFuture<Void>>toArray(CompletableFuture[] ::new);
        CompletableFuture<Void> combinedFuture
                = CompletableFuture.allOf(futureArr);
        combinedFuture.get();

        count = userDAO.countAll();
        long countASync = userDAO.countAllAsync().toCompletableFuture().get();
        assertThat(count).isEqualTo(countASync);
        assertThat(count).isEqualTo(userList.size() + countStart);
        logger.info("count {}", count);





        List<User> users = userDAO.findByPartitionKey(user0);
        assertThat(users.size()).isEqualTo(1);
        for (User userFetch : users) {
            assertThat(userFetch.getId()).isEqualTo(user0.getId());
        }
        logger.info("users {}", users.size());

        long countByPKey = userDAO.countByPartitionKey(user0);
        long countByPKeyAsync = userDAO.countByPartitionKeyAsync(user0).toCompletableFuture().get();
        assertThat(countByPKey).isEqualTo(1);
        assertThat(countByPKeyAsync).isEqualTo( 1);



        PagingIterable<User> userFetchsPagingIterable = userDAO.findByPartitionKeyPagingIterable(user0);
        users = new ArrayList<>();
        for (User userFetch : userFetchsPagingIterable) {
            users.add(userFetch);
            assertThat(userFetch.getId()).isEqualTo(user0.getId());
        }
        logger.info("users: {}", users.size());
        assertThat(users.size()).isEqualTo(1);
        assertThat(userFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(userFetchsPagingIterable.isFullyFetched()).isEqualTo(true);


        Map<CqlIdentifier, Object> pkeys = Map.of(
                CqlIdentifier.fromCql("id"), user0.getId()
        );
        users = userDAO.findByPartitionKey(pkeys);
        assertThat(users.size()).isEqualTo(1);
        for (User userFetch : users) {
            assertThat(userFetch.getId()).isEqualTo(user0.getId());
        }


        countByPKey = userDAO.countByPartitionKey(pkeys);
        countByPKeyAsync = userDAO.countByPartitionKeyAsync(pkeys).toCompletableFuture().get();
        assertThat(countByPKey).isEqualTo(1);
        assertThat(countByPKeyAsync).isEqualTo(1);


        userFetchsPagingIterable = userDAO.findByPartitionKeyPagingIterable(pkeys);
        users = new ArrayList<>();
        for (User userFetch : userFetchsPagingIterable) {
            users.add(userFetch);
            assertThat(userFetch.getId()).isEqualTo(user0.getId());
        }
        logger.info("users: {}", users.size());
        assertThat(users.size()).isEqualTo(1);
        assertThat(userFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(userFetchsPagingIterable.isFullyFetched()).isEqualTo(true);





        users = userDAO.findAll();
        assertThat(users.size()).isEqualTo(userList.size() + countStart);


        userFetchsPagingIterable = userDAO.findAllPagingIterable();
        users = new ArrayList<>();
        for (User user : userFetchsPagingIterable) {
            users.add(user);
        }
        assertThat(users.size()).isEqualTo(userList.size() + countStart);
        assertThat(userFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(userFetchsPagingIterable.isFullyFetched()).isEqualTo(true);


        CompletionStage<MappedAsyncPagingIterable<User>> asyncFetchUsers = userDAO.findAllAsync();
        users = asyncFetchUsers.thenApply(mappedAsyncPagingIterable -> {
            List<User> allUsers = new ArrayList<>();
            boolean hasMorePages = true;
            while (hasMorePages){
                mappedAsyncPagingIterable.currentPage().forEach(allUsers::add);
                hasMorePages = mappedAsyncPagingIterable.hasMorePages();
                if (hasMorePages){
                    mappedAsyncPagingIterable.fetchNextPage();
                }
            }
            return allUsers;
        }).toCompletableFuture().get();
        assertThat(users.size()).isEqualTo(userList.size() + countStart);
        logger.info("users.size: {}", users.size());

        //find no index column with AllowFiltering
        int age = 20;
        userFetchsPagingIterable = userDAO.getUsersOlderThanAge(age);
        for (User userFetch : userFetchsPagingIterable) {
            assertThat(userFetch.getUserAge()).isGreaterThanOrEqualTo(age);
            logger.info("userFetch.getUserAge: {}", userFetch.getUserAge());
        }

        //find by primitive index column
        for (User user: userList){
            userFetchsPagingIterable = userDAO.getByUserName(user.getUserName());
            for (User userFetch : userFetchsPagingIterable) {
                assertThat(userFetch.getUserName()).isEqualTo(user.getUserName());
                logger.info("userFetch.getUserName: {}", userFetch.getUserName());
            }
        }

        //find by Tuple index column
        for (User user: userList){
            userFetchsPagingIterable = userDAO.getByUserTupleIndex(user.getUserTupleIndex());
            for (User userFetch : userFetchsPagingIterable) {
                assertThat(userFetch.getUserTupleIndex()).isEqualTo(user.getUserTupleIndex());
                logger.info("userFetch.getUserTupleIndex: {}", userFetch.getUserTupleIndex());
            }
        }
    }

}
