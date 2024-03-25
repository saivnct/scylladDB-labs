package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.dao.ClientDao;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.models.Client;
import studio.giangbb.scylladbdemo.models.ClientInfo;
import studio.giangbb.scylladbdemo.models.ClientName;





import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ScylladbTests03Measure {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests03Measure.class);


	private final CqlSession session;

	private final DaoMapper daoMapper;
	private final ClientDao clientDAO;

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


	@Autowired
	public ScylladbTests03Measure(
			CqlSession session,
			DaoMapper daoMapper
	) {
		this.session = session;
		this.daoMapper = daoMapper;
		this.clientDAO = daoMapper.clientDao();
	}

	@Test
	public void testCount(){
		long count = clientDAO.countAll();
		logger.info("count {}", count);

		Metadata metadata = session.getMetadata();
		logger.info("Connected session {}", session.getName());


		for (Node node : metadata.getNodes().values()) {
			logger.info("Node session {}, Datatacenter: {}; Host: {}; Rack: {}", node.getEndPoint().resolve(),
					node.getDatacenter(), node.getEndPoint(), node.getRack());
		}
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
		assertThat(count).isEqualTo(clientList.size() + startCount);
	}


	@Test
	public void testQuery() {
		long count = clientDAO.countAll();

		long startTime = System.nanoTime();
		PagingIterable<Client> fetchClients = clientDAO.findAll();
		List<Client> clients = new ArrayList<>();
		assertThat(fetchClients.iterator().hasNext()).isEqualTo(true);
		for (Client client : fetchClients) {
			clients.add(client);
		}
		long endTime = System.nanoTime();


		long duration = (endTime - startTime);  // compute the elapsed time in nanoseconds
		logger.info("Execution time in milliSec: {}", duration/1000000);


		Assertions.assertThat(count).isEqualTo(clients.size());
	}




}
