package studio.giangbb.scylladbdemo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;

@SpringBootTest
class ScylladbTests_03_Using_DAO {
	private final Logger log = LoggerFactory.getLogger(ScylladbTests_03_Using_DAO.class);

	@Autowired
	private CassandraAdminTemplate cassandraTemplate;

	@Test
	void contextLoads() {
	}

}
