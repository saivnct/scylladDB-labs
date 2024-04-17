package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.core.PagingIterable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.daoct.ClientDAO;
import studio.giangbb.scylladbdemo.entity.Client;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static studio.giangbb.scylladbdemo.ScylladbRepoTests01.sleepTask;
import static studio.giangbb.scylladbdemo.ScylladbTests02.getDummyClientList;

/**
 * Created by Giangbb on 12/04/2024
 */
@SpringBootTest
public class ScylladbRepoTests02 {
    private final Logger logger = LoggerFactory.getLogger(ScylladbRepoTests02.class);


    @Autowired
    private ClientDAO clientDAO;


    @Test
    public void test() throws ExecutionException, InterruptedException, UnknownHostException {
        clientDAO.deleteAll();

        int n = 2;
        int m = 10;

        List<Client> clientList = getDummyClientList(2, 10);

        long count = clientDAO.countAll();
        long countASync = clientDAO.countAllAsync().toCompletableFuture().get();
        assertThat(count).isEqualTo(0);
        assertThat(count).isEqualTo(countASync);



        Client client0 = clientList.get(0);
        Client client1 = clientList.get(1);


        clientDAO.save(client0);
        Client clientFetch0 = clientDAO.findByPrimaryKey(client0);
        Client clientFetch0Async = clientDAO.findByPrimaryKeyAsync(client0).toCompletableFuture().get();
        assertThat(clientFetch0).isNotNull();
        assertThat(clientFetch0Async).isNotNull();
        assertThat(clientFetch0).isEqualTo(client0);
        assertThat(clientFetch0Async).isEqualTo(client0);

        clientFetch0 = null;
        clientFetch0Async = null;

        Map<CqlIdentifier, Object> pmkeys = Map.of(
                CqlIdentifier.fromCql("id"), client0.getId()
        );
        clientFetch0 = clientDAO.findByPrimaryKey(pmkeys);
        clientFetch0Async = clientDAO.findByPrimaryKeyAsync(pmkeys).toCompletableFuture().get();
        assertThat(clientFetch0).isNotNull();
        assertThat(clientFetch0Async).isNotNull();
        assertThat(clientFetch0).isEqualTo(client0);
        assertThat(clientFetch0Async).isEqualTo(client0);


        boolean saved = clientDAO.saveIfExists(client0);
        assertThat(saved).isTrue();
        saved = clientDAO.saveIfExistsAsync(client0).toCompletableFuture().get();
        assertThat(saved).isTrue();

        saved = clientDAO.saveIfExists(client1);
        assertThat(saved).isFalse();
        saved = clientDAO.saveIfExistsAsync(client1).toCompletableFuture().get();
        assertThat(saved).isFalse();


        int ttl = 5; //5s
        clientDAO.saveWithTtl(client0, ttl);
        clientFetch0 = clientDAO.findByPrimaryKey(client0);
        assertThat(clientFetch0).isEqualTo(client0);
        CompletableFuture<Void> waitTask = sleepTask(ttl);
        logger.info("Wait for TTL...");
        waitTask.get();
        clientFetch0 = clientDAO.findByPrimaryKey(client0);
        assertThat(clientFetch0).isNull();

        clientDAO.saveWithTtlAsync(client0, ttl).toCompletableFuture().get();
        clientFetch0 = clientDAO.findByPrimaryKey(client0);
        assertThat(clientFetch0).isEqualTo(client0);
        waitTask = sleepTask(ttl);
        logger.info("Wait for TTL Async...");
        waitTask.get();
        clientFetch0 = clientDAO.findByPrimaryKey(client0);
        assertThat(clientFetch0).isNull();



        clientDAO.save(client0);
        count = clientDAO.countAll();
        assertThat(count).isEqualTo(1);
        clientDAO.delete(client0);
        count = clientDAO.countAll();
        assertThat(count).isEqualTo(0);


        clientDAO.save(client0);
        count = clientDAO.countAll();
        assertThat(count).isEqualTo(1);
        clientDAO.deleteAsync(client0).toCompletableFuture().get();
        count = clientDAO.countAll();
        assertThat(count).isEqualTo(0);






        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Client cl : clientList) {
            CompletionStage<Void> saveClient = clientDAO.saveAsync(cl);
            futures.add(saveClient.toCompletableFuture());
        }
        logger.info("Wait for insertion completed...");
        CompletableFuture<Void>[] futureArr = futures.stream().<CompletableFuture<Void>>toArray(CompletableFuture[] ::new);
        CompletableFuture<Void> combinedFuture
                = CompletableFuture.allOf(futureArr);
        combinedFuture.get();

        count = clientDAO.countAll();
        countASync = clientDAO.countAllAsync().toCompletableFuture().get();
        assertThat(count).isEqualTo(countASync);
        assertThat(count).isEqualTo(clientList.size());
        logger.info("count {}", count);





        List<Client> clients = clientDAO.findByPartitionKey(client0);
        assertThat(clients.size()).isEqualTo(1);
        for (Client clientFetch : clients) {
            assertThat(clientFetch.getId()).isEqualTo(client0.getId());
        }
        logger.info("clientFetchs {}", clients.size());

        long countByPKey = clientDAO.countByPartitionKey(client0);
        long countByPKeyAsync = clientDAO.countByPartitionKeyAsync(client0).toCompletableFuture().get();
        assertThat(countByPKey).isEqualTo(1);
        assertThat(countByPKeyAsync).isEqualTo(1);



        PagingIterable<Client> clientFetchsPagingIterable = clientDAO.findByPartitionKeyPagingIterable(client0);
        clients = new ArrayList<>();
        for (Client clientFetch : clientFetchsPagingIterable) {
            clients.add(clientFetch);
            assertThat(clientFetch.getId()).isEqualTo(client0.getId());
        }
        logger.info("clients: {}", clients.size());
        assertThat(clients.size()).isEqualTo(1);
        assertThat(clientFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(clientFetchsPagingIterable.isFullyFetched()).isEqualTo(true);



        Map<CqlIdentifier, Object> pkeys = Map.of(
                CqlIdentifier.fromCql("id"), client0.getId()
        );
        clients = clientDAO.findByPartitionKey(pkeys);
        assertThat(clients.size()).isEqualTo(1);
        for (Client clientFetch : clients) {
            assertThat(clientFetch.getId()).isEqualTo(client0.getId());
        }

        countByPKey = clientDAO.countByPartitionKey(pkeys);
        countByPKeyAsync = clientDAO.countByPartitionKeyAsync(pkeys).toCompletableFuture().get();
        assertThat(countByPKey).isEqualTo(1);
        assertThat(countByPKeyAsync).isEqualTo(1);


        clientFetchsPagingIterable = clientDAO.findByPartitionKeyPagingIterable(pkeys);
        clients = new ArrayList<>();
        for (Client clientFetch : clientFetchsPagingIterable) {
            clients.add(clientFetch);
            assertThat(clientFetch.getId()).isEqualTo(client0.getId());
        }
        logger.info("Clients: {}", clients.size());
        assertThat(clients.size()).isEqualTo(1);
        assertThat(clientFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(clientFetchsPagingIterable.isFullyFetched()).isEqualTo(true);









        clients = clientDAO.findAll();
        assertThat(clients.size()).isEqualTo(clientList.size());


        clientFetchsPagingIterable = clientDAO.findAllPagingIterable();
        clients = new ArrayList<>();
        for (Client client : clientFetchsPagingIterable) {
            clients.add(client);
        }
        assertThat(clients.size()).isEqualTo(clientList.size());
        assertThat(clientFetchsPagingIterable.iterator().hasNext()).isEqualTo(false);
        assertThat(clientFetchsPagingIterable.isFullyFetched()).isEqualTo(true);


        CompletionStage<MappedAsyncPagingIterable<Client>> asyncFetchClients = clientDAO.findAllAsync();
        clients = asyncFetchClients.thenApply(mappedAsyncPagingIterable -> {
            List<Client> allClients = new ArrayList<>();
            boolean hasMorePages = true;
            while (hasMorePages){
                mappedAsyncPagingIterable.currentPage().forEach(allClients::add);
                hasMorePages = mappedAsyncPagingIterable.hasMorePages();
                if (hasMorePages){
                    logger.info("fetchNextPage");
                    mappedAsyncPagingIterable.fetchNextPage();
                }
            }
            return allClients;
        }).toCompletableFuture().get();
        assertThat(clients.size()).isEqualTo(clientList.size());



        //find no index column with AllowFiltering
        Client.Role role = Client.Role.USER;
        clientFetchsPagingIterable = clientDAO.getByRole(role);
        clients = new ArrayList<>();
        for (Client clientFetch : clientFetchsPagingIterable) {
            clients.add(clientFetch);
        }
        logger.info("Clients: {}", clients.size());
        assertThat(clients.size()).isEqualTo(clientList.stream().filter(p -> p.getRole() == role).count());



        //find with index column
        for (Client client: clientList){
            clientFetchsPagingIterable = clientDAO.getByName(client.getClientName());
            clients = new ArrayList<>();
            for (Client clientFetch : clientFetchsPagingIterable) {
                clients.add(clientFetch);
            }
//            logger.info("Found Clients: {}", clients);
            assertThat(clients.size()).isEqualTo(clientList.stream().filter(p -> p.getClientName().equals(client.getClientName())).count());
            if (!clients.isEmpty()){
                assertThat(clients.get(0)).isEqualTo(client);
            }
        }




    }

}
