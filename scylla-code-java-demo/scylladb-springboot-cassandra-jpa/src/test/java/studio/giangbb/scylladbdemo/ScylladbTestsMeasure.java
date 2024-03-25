package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import studio.giangbb.scylladbdemo.dao.ClientDAO;
import studio.giangbb.scylladbdemo.models.Client;
import studio.giangbb.scylladbdemo.models.ClientInfo;
import studio.giangbb.scylladbdemo.models.ClientName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScylladbTestsMeasure {

	@Autowired
	private ClientDAO clientDAO;

	@Autowired
	private CqlSession cqlSession;

	@Autowired
	private CqlSessionFactoryBean cassandraSessionBean;

	private final Logger logger = LoggerFactory.getLogger(ScylladbTestsMeasure.class);

	public static List<Client> getDummyClientList(int n, int m){
		List<Client> clientList= new ArrayList<>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				Client client = new Client(
						new ClientName(
								String.format("first_%d", j),
								String.format("last_%d", i)
						),
						new ClientInfo(
								i+j,
								20+j,
								new HashSet<>(
										List.of(
												String.format("+%d111111%d", i,j),
												String.format("+%d222222%d", i,j),
												String.format("+%d333333%d", i,j)
										)
								)
						),
						i == 0 ? Client.Role.ADMIN.ordinal() : Client.Role.USER.ordinal(),
						List.of(String.format("lzone-%d-%d", i,i), String.format("szone-%d-%d", i,j))
				);
				clientList.add(client);
			}
		}

		return clientList;
	}


	@Test
	public void testCount(){
		Metadata metadata =  cqlSession.getMetadata();
		logger.info("Connected session {}", cqlSession.getName());



		for (Node node : metadata.getNodes().values()) {
			logger.info("Node session {}, Datatacenter: {}; Host: {}; Rack: {}", node.getEndPoint().resolve(),
					node.getDatacenter(), node.getEndPoint(), node.getRack());
		}

		logger.info("Node: {}", metadata.getNodes().values().size());


		long count = clientDAO.countAll();
		logger.info("count {}", count);
	}


	@Test
	public void testDelete(){
		long startTime = System.nanoTime();
		//delete all
		clientDAO.deleteAll();
		long endTime = System.nanoTime();

		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);

		long count = clientDAO.countAll();
		AssertionsForClassTypes.assertThat(count).isEqualTo(0);
	}

	@Test
	public void testInsert() {
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
		AssertionsForClassTypes.assertThat(count).isEqualTo(clientList.size() + startCount);
	}


	@Test
	public void testQuery() {
		long count = clientDAO.countAll();

		long startTime = System.nanoTime();
		List<Client> fetchClients = clientDAO.findAll();
		long endTime = System.nanoTime();


		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);


		assertThat(count).isEqualTo(fetchClients.size());
	}

}
