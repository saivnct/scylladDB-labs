package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.dao.UserDao;
import studio.giangbb.scylladbdemo.entity.User;
import studio.giangbb.scylladbdemo.entity.tuple.UserTuple;
import studio.giangbb.scylladbdemo.entity.tuple.UserTupleIndex;
import studio.giangbb.scylladbdemo.entity.udt.FavoritePlace;
import studio.giangbb.scylladbdemo.entity.udt.UserFavoritePlace;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ScylladbTests01 {
	private final Logger logger = LoggerFactory.getLogger(ScylladbTests01.class);


	private final CqlSession session;

	private final DaoMapper daoMapper;

	public static List<User> getDummyUserList(int n) throws UnknownHostException {
		List<User> useList= new ArrayList<>();
		for (int i = 0; i < n; i++) {
			User user = new User(
					String.format("user_%d", i),
					20 + i,
					new UserTupleIndex(String.format("index_%d", i),i),
					new UserTuple(String.format("user_nick_%d", i), 100 + i, new UserFavoritePlace(String.format("city_%d", i), String.format("country_%d", i), i))
			);
			useList.add(user);
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
	}

//NOTE: java-driver-mapper not supporting TUPLE!!!!!!!!!!!!!!!!!!!

//	@Test
//	public void testUser() throws UnknownHostException, ExecutionException, InterruptedException {
//		UserDao userDao = daoMapper.userDao();
//
//		//delete all
//		userDao.deleteAll();
//
//		long count = userDao.countAll();
//		assertThat(count).isEqualTo(0);
//
//		List<User> userList = getDummyUserList(10);
//		List<CompletableFuture<Void>> futures = new ArrayList<>();
//		for (int i = 0; i < userList.size() - 2; i++) {
//			User user = userList.get(i);
//			CompletionStage<Void> saveUser = userDao.saveAsync(user);
//			futures.add(saveUser.toCompletableFuture());
//		}
//		logger.info("Wait for insertion completed...");
//
//		CompletableFuture<Void>[] futureArr = futures.stream().<CompletableFuture<Void>>toArray(CompletableFuture[] ::new);
//		CompletableFuture<Void> combinedFuture
//				= CompletableFuture.allOf(futureArr);
//		combinedFuture.get();
//
//		User myUser = userList.get(userList.size() - 2);
//		userDao.save(myUser);
//
//
//
//		myUser = userList.get(userList.size() - 1);
//		boolean saveIfExist = userDao.saveIfExists(myUser);
//		assertThat(saveIfExist).isFalse();
//		userDao.save(myUser);
//
//
//
//		count = userDao.countAll();
//		assertThat(count).isEqualTo(userList.size());
//		logger.info("count {}", count);
//
//
//
//		//find with id(primkey)
//		for (User user: userList){
//			User fetchUser = userDao.findByPrimKey(user.getId(), user.getUserAge());
//			assertThat(fetchUser).isNotNull();
//			assertThat(fetchUser).isEqualTo(user);
//		}
//
//		//find all
//		PagingIterable<User> fetchUsers = userDao.findAll();
//		List<User> users = new ArrayList<>();
//		assertThat(fetchUsers.iterator().hasNext()).isEqualTo(true);
//		for (User user : fetchUsers) {
//			users.add(user);
//		}
//		assertThat(fetchUsers.iterator().hasNext()).isEqualTo(false);
//		assertThat(fetchUsers.isFullyFetched()).isEqualTo(true);
//		assertThat(users.size()).isEqualTo(userList.size());
//		logger.info("find all - {} - users {}", users.size(), users);
//
//
//
//		//find no index column with AllowFiltering
//		int age = 20;
//		fetchUsers = userDao.getUsersOlderThanAge(age);
//		users = new ArrayList<>();
//		for (User userFetch : fetchUsers) {
//			users.add(userFetch);
////			logger.info("userFetch {}", userFetch);
//		}
//		assertThat(users.size()).isEqualTo(userList.stream().filter(p -> p.getUserAge() > age).count());
//		logger.info("find with AllowFiltering - {} - users {}", users.size(), users);
//
//
//
//		//find index column
//		for (User user: userList){
//			fetchUsers = userDao.getByUserName(user.getUserName());
//			users = new ArrayList<>();
//			for (User userFetch : fetchUsers) {
//				users.add(userFetch);
//			}
//			assertThat(users.size()).isEqualTo(userList.stream().filter(p -> p.getUserName().equals(user.getUserName())).count());
//			if (!users.isEmpty()){
//				assertThat(users.get(0)).isEqualTo(user);
//			}
//		}
//
//
//
//		//test TTL
//		int ttl = 5;	//10 second
//		myUser = new User(String.format("ttlUser"), 20, new UserTuple("ttk nick", 100, new UserFavoritePlace("Singapore", "Singapore", 5)));
//		userDao.saveWithTtl(myUser, ttl);
//		User userFetch = userDao.findByPrimKey(myUser.getId(), myUser.getUserAge());
//		assertThat(userFetch).isNotNull();
//
//
//		CompletableFuture<Void> waitTask = CompletableFuture.runAsync(() -> {
//			try {
//				Thread.sleep(ttl * 1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		});
//		logger.info("Wait for TTL...");
//		waitTask.get();
//		userFetch = userDao.findByPrimKey(myUser.getId(), myUser.getUserAge());
//		assertThat(userFetch).isNull();
//	}




}
