package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateIndex;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableWithOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.dao.UserDao;
import studio.giangbb.scylladbdemo.model.User;

import java.util.List;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.*;

@SpringBootTest
class ScylladbDemoApplicationTests {

	private final CqlSession session;

	private final DaoMapper daoMapper;


	private final PreparedStatement preparedCreateTable;
	private final PreparedStatement preparedCreateIndex;

	@Autowired
	public ScylladbDemoApplicationTests(
			CqlSession session,
			DaoMapper daoMapper
	) {
		this.session = session;
		this.daoMapper = daoMapper;

		CreateTableWithOptions createTable = createTable("user_profile")
				.ifNotExists()
				.withPartitionKey("id", DataTypes.INT)
				.withClusteringColumn("user_age", DataTypes.INT)
				.withColumn("username", DataTypes.TEXT)
				.withColumn("writetime", DataTypes.BIGINT)
				.withClusteringOrder("user_age", ClusteringOrder.DESC)
				.withCompaction(sizeTieredCompactionStrategy());

		CreateIndex createIndex = createIndex()
				.ifNotExists()
				.onTable("user_profile")
				.andColumn("username");

		preparedCreateTable = session.prepare(createTable.build());
		preparedCreateIndex = session.prepare(createIndex.build());
	}


	@Test
	void givenUser_whenInsert_thenRetrievedDuringGet() {
//		CassandraSchemaGenerator schemaGenerator = new CassandraSchemaGenerator();
//
//		try{
//			String query = schemaGenerator.createTableQuery(User.class);
//			System.out.println("query:"+query);
//		}catch (Exception e){
//			e.printStackTrace();
//		}

		session.execute(preparedCreateTable.bind());
		session.execute(preparedCreateIndex.bind());

		UserDao userDao = daoMapper.getUserDao();


		User user = new User(1, "JohnDoe", 31);
		userDao.insertUser(user);

		User user2 = new User(2, "JohnDoe", 32);
		userDao.insertUser(user2);

		User user3 = new User(3, "JohnDoe", 33);
		userDao.insertUser(user3);


		User user4 = new User(4, "JohnDoe", 34);
		userDao.insertUser(user4);

		User user5 = new User(5, "JohnDoe", 35);
		userDao.insertUser(user5);

		User user6 = new User(6, "JohnDoe", 36);
		userDao.insertUser(user6);


		User retrievedUser = userDao.getUserById(1);
		Assertions.assertEquals(retrievedUser.getId(), user.getId());


		List<User> userList = userDao.getByUserName("JohnDoe").all();
		Assertions.assertEquals(userList.size(), 6);

	}
}
