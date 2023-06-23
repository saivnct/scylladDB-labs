package studio.giangbb.scylladbdemo.common.conf;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.dao.DaoMapperBuilder;


/**
 * Created by giangbb on 23/06/2023
 */
@Configuration
@EnableCassandraRepositories(basePackages = "studio.giangbb.scylladbdemo")
public class CassandraConfiguration extends AbstractCassandraConfiguration{

    @Value("${spring.data.cassandra.keyspace-name}")
    protected String keyspaceName;

    @Value("${spring.data.cassandra.contact-points}")
    protected String contactPoints;

    @Value("${spring.data.cassandra.local-datacenter}")
    protected String localDc;

    @Value("${spring.data.cassandra.consistency-level:LOCAL_QUORUM}")
    protected String consistency;


    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"studio.giangbb.scylladbdemo"};
    }

    @Override
    public String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public String getLocalDataCenter() {
        return localDc;
    }

    @Bean
    public DaoMapper daoMapper(CqlSession session)  {
        return new DaoMapperBuilder(session).build();
    }
}
