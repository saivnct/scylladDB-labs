package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.PagingIterable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.daoct.ClientDAO;
import studio.giangbb.scylladbdemo.entity.Client;
import studio.giangbb.scylladbdemo.entity.udt.ClientInfo;
import studio.giangbb.scylladbdemo.entity.udt.ClientName;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static studio.giangbb.scylladbdemo.ScylladbTests02.getDummyClientList;

@SpringBootTest
class ScylladbTestsMeasureRepo {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTestsMeasureRepo.class);


	@Autowired
	private ClientDAO clientDAO;


	@Test
	public void testCount(){
		long count = clientDAO.countAll();
		logger.info("count {}", count);
	}


	@Test
	public void testDelete() {
		long startTime = System.nanoTime();
		//delete all
		clientDAO.deleteAll();
		long endTime = System.nanoTime();

		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);

		long count = clientDAO.countAll();
		assertThat(count).isEqualTo(0);
	}

	@Test
	public void testInsert() throws UnknownHostException{
		long startCount = clientDAO.countAll();

		List<Client> clientList = getDummyClientList(100, 1000);
		logger.info("Wait for insertion completed...");

		long startTime = System.nanoTime();
		for (Client client : clientList) {
			clientDAO.save(client);
		}
		long endTime = System.nanoTime();


		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);

		long count = clientDAO.countAll();
		assertThat(count).isEqualTo(clientList.size() + startCount);
	}


	@Test
	public void testUpdate() {
		long startCount = clientDAO.countAll();

		PagingIterable<Client> fetchClients = clientDAO.findAllPagingIterable();
		List<Client> clients = new ArrayList<>();
		for (Client client : fetchClients) {
			ClientInfo clientInfo = client.getClientInfo();
			clientInfo.setAge(clientInfo.getAge() + 1);
			clients.add(client);
		}

		logger.info("Wait for update completed...");
		long startTime = System.nanoTime();
		for (Client client : clients) {
			clientDAO.save(client);
		}
		long endTime = System.nanoTime();


		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);

		long count = clientDAO.countAll();
		assertThat(count).isEqualTo(startCount);
	}


	@Test
	public void testQuery() {
		long count = clientDAO.countAll();

		long startTime = System.nanoTime();
		PagingIterable<Client> fetchClients = clientDAO.findAllPagingIterable();
		List<Client> clients = new ArrayList<>();
		for (Client client : fetchClients) {
			clients.add(client);
		}
		long endTime = System.nanoTime();

		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);

		Assertions.assertThat(count).isEqualTo(clients.size());
	}


	@Test
	public void testFindByPrimKey(){
		UUID uuid = UUID.fromString("42cad5e1-f9ff-11ee-bd96-c93e812aefd2");
		Map<CqlIdentifier, Object> pmkeys = Map.of(
				CqlIdentifier.fromCql("id"), uuid
		);
		long startTime = System.nanoTime();
		Client client = clientDAO.findByPrimaryKey(pmkeys);
		long endTime = System.nanoTime();

		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);

		Assertions.assertThat(client.getId()).isEqualTo(uuid);
	}

	@Test
	public void testFindByIndex_High_Selectivity(){
		ClientName clientName = new ClientName("first_120", "last_33", ClientName.NameStyle.ASIA); //120%2

		long startTime = System.nanoTime();
		PagingIterable<Client> fetchClients = clientDAO.getByName(clientName);
		List<Client> clients = new ArrayList<>();
		for (Client client : fetchClients) {
			clients.add(client);
		}
		long endTime = System.nanoTime();

		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);


		Assertions.assertThat(clients.size()).isGreaterThan(0);
		logger.info("client {}", clients.size());
		for (Client client: clients){
			Assertions.assertThat(client.getClientName()).isEqualTo(clientName);
		}
	}


	@Test
	public void testFindByNoIndex_Low_Selectivity(){
		long startTime = System.nanoTime();
		Client.Role role = Client.Role.USER;
		PagingIterable<Client> fetchClients = clientDAO.getByRole(role);
		List<Client> clients = new ArrayList<>();
		for (Client client : fetchClients) {
			clients.add(client);
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);


		Assertions.assertThat(clients.size()).isGreaterThan(0);
		logger.info("client {}", clients.size());
		for (Client client: clients){
			Assertions.assertThat(client.getRole()).isEqualTo(role);
		}
	}




}
