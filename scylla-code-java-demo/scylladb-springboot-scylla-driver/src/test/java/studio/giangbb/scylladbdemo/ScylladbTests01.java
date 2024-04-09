package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateIndex;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableWithOptions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.dao.DaoFactory;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.dao.UserDao;
import studio.giangbb.scylladbdemo.models.User;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ScylladbTests01 {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests01.class);


	private final CqlSession session;

	private final DaoMapper daoMapper;

	public static List<User> getDummyUserList() throws UnknownHostException {
		List<User> useList= new ArrayList<>();
		int n = 10;
		for (int i = 0; i < n; i++) {
			User person = new User(String.format("user_%d", i), 20 + i);
			useList.add(person);
		}

		return useList;
	}


	@Autowired
	public ScylladbTests01(
			CqlSession session,
			DaoMapper daoMapper
	) {
		this.session = session;
		this.daoMapper = daoMapper;

		CreateTableWithOptions createTable = createTable("user")
				.ifNotExists()
				.withPartitionKey("id", DataTypes.TIMEUUID)
				.withClusteringColumn("user_age", DataTypes.INT)
				.withColumn("user_name", DataTypes.TEXT)
				.withClusteringOrder("user_age", ClusteringOrder.DESC)
				.withCompaction(sizeTieredCompactionStrategy());

		CreateIndex createIndex = createIndex()
				.ifNotExists()
				.onTable("user")
				.andColumn("user_name");

		PreparedStatement preparedCreateTable = session.prepare(createTable.build());
		PreparedStatement preparedCreateIndex = session.prepare(createIndex.build());


		session.execute(preparedCreateTable.bind());
		session.execute(preparedCreateIndex.bind());
	}


	@Test
	public void testPerson() throws UnknownHostException, ExecutionException, InterruptedException {
		UserDao userDao = daoMapper.userDao();

		//delete all
		userDao.deleteAll();

		long count = userDao.countAll();
		assertThat(count).isEqualTo(0);

		List<User> userList = getDummyUserList();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (User user : userList) {
			CompletionStage<Void> saveUser = userDao.saveAsync(user);
			futures.add(saveUser.toCompletableFuture());

//			CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//				long threadId = Thread.currentThread().getId();
//				logger.info("insert Person: {} - threadId: {}", user.getId().toString(), threadId);
//				userDao.save(user);
//			});
//			futures.add(completableFuture);
		}

		logger.info("Wait for insertion completed...");

		CompletableFuture<Void>[] futureArr = futures.stream().<CompletableFuture<Void>>toArray(CompletableFuture[] ::new);
		CompletableFuture<Void> combinedFuture
				= CompletableFuture.allOf(futureArr);
		combinedFuture.get();


		count = userDao.countAll();
		assertThat(count).isEqualTo(userList.size());
		logger.info("count {}", count);

		//find with id(primkey)
		for (User user: userList){
			User fetchUser = userDao.findByPrimKey(user.getId(), user.getUserAge());
			assertThat(fetchUser).isNotNull();
			assertThat(fetchUser).isEqualTo(user);
		}

		//find all
		PagingIterable<User> fetchUsers = userDao.findAll();
		List<User> users = new ArrayList<>();
		assertThat(fetchUsers.iterator().hasNext()).isEqualTo(true);
		for (User user : fetchUsers) {
			users.add(user);
		}
		assertThat(fetchUsers.iterator().hasNext()).isEqualTo(false);
		assertThat(fetchUsers.isFullyFetched()).isEqualTo(true);
		assertThat(users.size()).isEqualTo(userList.size());
//		logger.info("users {}", users);



		//find no index column with AllowFiltering
		int age = 20;
		fetchUsers = userDao.getUsersOlderThanAge(age);
		users = new ArrayList<>();
		for (User userFetch : fetchUsers) {
			users.add(userFetch);
//			logger.info("userFetch {}", userFetch);
		}
		assertThat(users.size()).isEqualTo(userList.stream().filter(p -> p.getUserAge() > age).count());


		//find index column
		for (User user: userList){
			fetchUsers = userDao.getByUserName(user.getUserName());
			users = new ArrayList<>();
			for (User userFetch : fetchUsers) {
				users.add(userFetch);
			}
			assertThat(users.size()).isEqualTo(userList.stream().filter(p -> p.getUserName().equals(user.getUserName())).count());
			if (!users.isEmpty()){
				assertThat(users.get(0)).isEqualTo(user);
			}
		}
	}




}
