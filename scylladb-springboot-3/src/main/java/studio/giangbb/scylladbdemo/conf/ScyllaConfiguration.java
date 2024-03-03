package studio.giangbb.scylladbdemo.conf;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
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
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

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

    @Value("${spring.data.cassandra.port:9042}")
    protected Integer port;

    @Value("${spring.data.cassandra.local-datacenter}")
    protected String localDc;

    @Value("${spring.data.cassandra.consistency-level:LOCAL_QUORUM}")
    protected String consistency;

    @Value("${spring.data.cassandra.username}")
    protected String username;

    @Value("${spring.data.cassandra.password}")
    protected String password;


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
    public Database migrationDatabase(){
        log.info("INIT MIGRATION DB ...");
        ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder = DriverConfigLoader.programmaticBuilder()
                .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistency);

        if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
            configLoaderBuilder = configLoaderBuilder
                    .withString(
                            DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class.getName()
                    )
                    .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, username)
                    .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, password);
        }

        List<String> contactPoints = Arrays.asList(this.contactPoints.split(","));
        CqlSessionBuilder sessionBuilder = new CqlSessionBuilder().withConfigLoader(configLoaderBuilder.build());
        for (String contactPoint : contactPoints) {
            InetSocketAddress address = InetSocketAddress.createUnresolved(contactPoint, port);
            log.info("Adding contact point {}:{} - {}", address.getHostString(), address.getPort(), address.toString());
            sessionBuilder = sessionBuilder.addContactPoint(address);
        }
        sessionBuilder.withLocalDatacenter(localDc);

        CqlIdentifier keyspace = CqlIdentifier.fromCql(keyspaceName);

        CqlSession session = sessionBuilder.withKeyspace(keyspace).build();
        Database database = new Database(session, new MigrationConfiguration().withKeyspaceName(keyspaceName));

        return database;
    }



    @Bean
    public MigrationTask migrationTask(Database migrationDatabase)  {
        log.info("INIT MIGRATION TASK ...");
        return new MigrationTask(migrationDatabase, new MigrationRepository(MigrationRepository.DEFAULT_SCRIPT_PATH));
    }
}
