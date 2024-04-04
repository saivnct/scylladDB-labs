package studio.giangbb.scylladbdemo.conf;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import org.cognitor.cassandra.migration.Database;
import org.cognitor.cassandra.migration.MigrationConfiguration;
import org.cognitor.cassandra.migration.MigrationRepository;
import org.cognitor.cassandra.migration.MigrationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.SessionBuilderConfigurer;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by Giangbb on 01/03/2024
 */
@Configuration
@EnableCassandraRepositories(basePackages = "studio.giangbb.scylladbdemo.repository")
public class ScyllaConfiguration extends AbstractCassandraConfiguration {
    private final Logger log = LoggerFactory.getLogger(ScyllaConfiguration.class);


    @Value("${spring.data.cassandra.keyspace-name}")
    protected String keyspaceName;

    @Value("${spring.data.cassandra.contact-points}")
    protected String contactPoints;

    @Value("${spring.data.cassandra.schema-action}")
    protected String schemaActions;

    @Value("${spring.data.cassandra.port:9042}")
    protected Integer port;

    @Value("${spring.data.cassandra.local-datacenter}")
    protected String localDc;

    @Value("${spring.data.cassandra.consistency-level:LOCAL_QUORUM}")
    protected String consistencyLevel;

    @Value("${spring.data.cassandra.serial-consistency-level:LOCAL_SERIAL}")
    protected String serialConsistencyLevel;

    @Value("${spring.data.cassandra.username}")
    protected String username;

    @Value("${spring.data.cassandra.password}")
    protected String password;


    @Value("${spring.data.cassandra.metadata-schema-req-timeout-millis}")
    protected long metadataSchemaReqTimeout;


    @Value("${spring.data.cassandra.conn-int-query-timeout-millis}")
    protected long connInitQueryTimeout;

    @Value("${spring.data.cassandra.rq-timeout-millis}")
    protected long rqTimeout;


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
        Set<String> cp = StringUtils.commaDelimitedListToSet(contactPoints);
        log.info("call getContactPoints: {}", cp);
        return contactPoints;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.valueOf(schemaActions);
    }

    @Override
    public String getLocalDataCenter() {
        return localDc;
    }


    @Override
    public CassandraAdminTemplate cassandraTemplate() {
        log.info("call cassandraTemplate");

        CassandraAdminTemplate adminTemplate = super.cassandraTemplate();
        CqlTemplate cqlTemplate = (CqlTemplate) adminTemplate.getCqlOperations();
        cqlTemplate.setConsistencyLevel(DefaultConsistencyLevel.valueOf(consistencyLevel));
        cqlTemplate.setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(serialConsistencyLevel));
        return adminTemplate;
    }


    @Override
    protected SessionBuilderConfigurer getSessionBuilderConfigurer() {
        return new SessionBuilderConfigurer() {
            @Override
            public CqlSessionBuilder configure(CqlSessionBuilder cqlSessionBuilder) {
                log.info("call Configuring CqlSession Builder");
                return cqlSessionBuilder
                        .withConfigLoader(DriverConfigLoader.programmaticBuilder()
                                // Resolves the timeout query 'SELECT * FROM system_schema.tables' timed out after PT2S
                                .withDuration(DefaultDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, Duration.ofMillis(metadataSchemaReqTimeout))
                                .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofMillis(connInitQueryTimeout))
                                .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofMillis(rqTimeout))
                                .build());
            }
        };
    }



//    @Bean
//    public Database migrationDatabase(){
//        log.info("INIT MIGRATION DB ...");
//        ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder = DriverConfigLoader.programmaticBuilder()
//                .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistencyLevel);
//
//        if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
//            configLoaderBuilder = configLoaderBuilder
//                    .withString(
//                            DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class.getName()
//                    )
//                    .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, username)
//                    .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, password);
//        }
//
//        List<String> contactPoints = Arrays.asList(this.contactPoints.split(","));
//        CqlSessionBuilder sessionBuilder = new CqlSessionBuilder().withConfigLoader(configLoaderBuilder.build());
//        for (String contactPoint : contactPoints) {
//            InetSocketAddress address;
//
//            String[] contactPointParts = contactPoint.split(":");
//            if (contactPointParts.length == 1) {
//                address = InetSocketAddress.createUnresolved(contactPointParts[0], port);
//            }else if (contactPointParts.length == 2){
//                address = InetSocketAddress.createUnresolved(contactPointParts[0], Integer.parseInt(contactPointParts[1]));
//            }else{
//                throw new IllegalArgumentException("Contact points must be in the format 'host:port'");
//            }
//
//
////            InetSocketAddress address = InetSocketAddress.createUnresolved(contactPoint, port);
//            log.info("Adding contact point {}:{} - {}", address.getHostString(), address.getPort(), address.toString());
//            sessionBuilder = sessionBuilder.addContactPoint(address);
//        }
//        log.info("localDc: {}",localDc);
//        sessionBuilder.withLocalDatacenter(localDc);
//
//        CqlIdentifier keyspace = CqlIdentifier.fromCql(keyspaceName);
//
//        CqlSession session = sessionBuilder.withKeyspace(keyspace).build();
//        Database database = new Database(session, new MigrationConfiguration().withKeyspaceName(keyspaceName));
//
//        return database;
//    }
//
//
//
//    @Bean
//    public MigrationTask migrationTask(Database migrationDatabase)  {
//        log.info("INIT MIGRATION TASK ...");
//        return new MigrationTask(migrationDatabase, new MigrationRepository(MigrationRepository.DEFAULT_SCRIPT_PATH));
//    }
}
