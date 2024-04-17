package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.dao.ClientDao;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.entity.Client;
import studio.giangbb.scylladbdemo.entity.udt.ClientInfo;
import studio.giangbb.scylladbdemo.entity.udt.ClientName;
import studio.giangbb.scylladbdemo.entity.udt.FavoritePlace;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ScylladbTests02 {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests02.class);


	private final CqlSession session;

	private final DaoMapper daoMapper;

	public static List<Client> getDummyClientList(int n, int m) throws UnknownHostException {
		List<Client> clientList= new ArrayList<>();

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				Client client = new Client(
						new ClientName(
								String.format("first_%d", j),
								String.format("last_%d", i),
								j % 2 == 0 ? ClientName.NameStyle.ASIA : ClientName.NameStyle.EURO
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
								),
								new HashMap<String, InetAddress>(Map.of(
										"1", InetAddress.getByName(String.format("192.168.1.%d", i)),
										"2", InetAddress.getByName("127.0.0.1")
								)),
								List.of(
										new FavoritePlace("Singapore", "Singapore", 5),
										new FavoritePlace("Moscow", "Russia", 3),
										new FavoritePlace("Buhtan", "Buhtan", 2)
								)
						),
						i == 0 ? Client.Role.ADMIN : Client.Role.USER,
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
	}

	@Test
	public void testCount(){
		ClientDao clientDao = daoMapper.clientDao();

		long count = clientDao.countAll();
		logger.info("count {}", count);
	}

	@Test
	public void testClient() throws UnknownHostException, ExecutionException, InterruptedException {
		ClientDao clientDao = daoMapper.clientDao();

		//delete all
		clientDao.deleteAll();

		long count = clientDao.countAll();
		assertThat(count).isEqualTo(0);

		List<Client> clientList = getDummyClientList(2, 10);
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
		logger.info("clients - {} - {}", clients.size(), clients);
		fetchClients.forEach(client -> {
			logger.info("fetchClient {}", client);
		});



		//find no index column with AllowFiltering
		Client.Role role = Client.Role.USER;
		fetchClients = clientDao.getByRole(role);
		clients = new ArrayList<>();
		for (Client clientFetch : fetchClients) {
			clients.add(clientFetch);
		}
		assertThat(clients.size()).isEqualTo(clientList.stream().filter(p -> p.getRole() == role).count());


		//find with index column
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
