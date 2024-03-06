package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateIndex;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableWithOptions;
import com.datastax.oss.driver.api.querybuilder.schema.CreateType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.dao.ClientDao;
import studio.giangbb.scylladbdemo.dao.DaoFactory;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.dao.UserDao;
import studio.giangbb.scylladbdemo.models.Client;
import studio.giangbb.scylladbdemo.models.ClientInfo;
import studio.giangbb.scylladbdemo.models.ClientName;
import studio.giangbb.scylladbdemo.models.User;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ScylladbTests02 {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests02.class);


	private final CqlSession session;

	private final DaoMapper daoMapper;

	public static List<Client> getDummyClientList() throws UnknownHostException {
		List<Client> clientList= new ArrayList<>();
		int n = 3;
		int m = 10;

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
	public ScylladbTests02(
			CqlSession session,
			DaoMapper daoMapper
	) {
		this.session = session;
		this.daoMapper = daoMapper;

		CreateType createUDTClientInfo = createType("client_info")
				.ifNotExists()
				.withField("zip_code", DataTypes.INT)
				.withField("age", DataTypes.INT)
				.withField("phones", DataTypes.setOf(DataTypes.TEXT));

		CreateType createUDTClientName = createType("client_name")
				.ifNotExists()
				.withField("first_name", DataTypes.TEXT)
				.withField("last_name", DataTypes.TEXT);

		CreateTableWithOptions createTable = createTable("client")
				.ifNotExists()
				.withPartitionKey("id", DataTypes.TIMEUUID)
				.withColumn("client_name", udt("client_name", true))
				.withColumn("client_info", udt("client_info", true))
				.withColumn("role", DataTypes.INT)
				.withColumn("zones", DataTypes.listOf(DataTypes.TEXT))
				.withCompaction(sizeTieredCompactionStrategy());

		CreateIndex createIndex = createIndex()
				.ifNotExists()
				.onTable("client")
				.andColumn("client_name");


		PreparedStatement preparedCreateUDTClientInfo = session.prepare(createUDTClientInfo.build());
		PreparedStatement preparedCreateUDTClientName = session.prepare(createUDTClientName.build());
		session.execute(preparedCreateUDTClientInfo.bind());
		session.execute(preparedCreateUDTClientName.bind());

		PreparedStatement preparedCreateTable = session.prepare(createTable.build());
		PreparedStatement preparedCreateIndex = session.prepare(createIndex.build());
		session.execute(preparedCreateTable.bind());
		session.execute(preparedCreateIndex.bind());
	}


	@Test
	public void testClient() throws UnknownHostException, ExecutionException, InterruptedException {
		ClientDao clientDao = daoMapper.clientDao();

		//delete all
		clientDao.deleteAll();

		long count = clientDao.countAll();
		assertThat(count).isEqualTo(0);

		List<Client> clientList = getDummyClientList();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Client client : clientList) {
			CompletionStage<Void> saveClient = clientDao.saveAsync(client);
			futures.add(saveClient.toCompletableFuture());
		}

		logger.info("Wait for insertion completed...");

		CompletableFuture<Void>[] futureArr = futures.stream().<CompletableFuture<Void>>toArray(CompletableFuture[] ::new);
		CompletableFuture<Void> combinedFuture
				= CompletableFuture.allOf(futureArr);
		combinedFuture.get();


		count = clientDao.countAll();
		assertThat(count).isEqualTo(clientList.size());
		logger.info("count {}", count);

		//find with id(primkey)
		for (Client client: clientList){
			Client fetchClient = clientDao.findByPrimKey(client.getId());
			assertThat(fetchClient).isNotNull();
			assertThat(fetchClient).isEqualTo(client);
		}

		//find all
		PagingIterable<Client> fetchClients = clientDao.findAll();
		List<Client> clients = new ArrayList<>();
		assertThat(fetchClients.iterator().hasNext()).isEqualTo(true);
		for (Client client : fetchClients) {
			clients.add(client);
		}
		assertThat(fetchClients.iterator().hasNext()).isEqualTo(false);
		assertThat(fetchClients.isFullyFetched()).isEqualTo(true);
		assertThat(clients.size()).isEqualTo(clientList.size());
		logger.info("clients {}", clients);
		fetchClients.forEach(client -> {
			logger.info("client {}", client);
		});


		//find no index column with AllowFiltering
		int role = Client.Role.USER.ordinal();
		fetchClients = clientDao.getByRole(role);
		clients = new ArrayList<>();
		for (Client clientFetch : fetchClients) {
			clients.add(clientFetch);
		}
		assertThat(clients.size()).isEqualTo(clientList.stream().filter(p -> p.getRole() == role).count());


		//find index column
		for (Client client: clientList){
			fetchClients = clientDao.getByName(client.getClientName());
			clients = new ArrayList<>();
			for (Client clientFetch : fetchClients) {
				clients.add(clientFetch);
			}
			assertThat(clients.size()).isEqualTo(clientList.stream().filter(p -> p.getClientName().equals(client.getClientName())).count());
			if (!clients.isEmpty()){
				assertThat(clients.get(0)).isEqualTo(client);
			}
		}
	}




}
