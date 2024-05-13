package studio.giangbb.scylladbdemo.conf;//package studio.giangbb.scylladbdemo.common.conf;

import com.datastax.oss.driver.api.core.CqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import studio.giangbb.scylladbdemo.dao.DaoMapper;
import studio.giangbb.scylladbdemo.dao.DaoMapperBuilder;
import com.giangbb.scylla.config.AbstractScyllaConfiguration;
import com.giangbb.scylla.config.CqlSessionFactoryBean;
import com.giangbb.scylla.config.SchemaAction;

import java.time.Duration;

/**
 * Created by giangbb on 12/05/2024
 */
@Configuration
public class ScyllaConfiguration extends AbstractScyllaConfiguration {
    private final Logger log = LoggerFactory.getLogger(ScyllaConfiguration.class);

    @Value("${spring.data.scylla.contact-points}")
    protected String contactPoints;

    @Value("${spring.data.scylla.schema-action}")
    protected String schemaActions;

    @Value("${spring.data.scylla.port:9042}")
    protected Integer port;

    @Value("${spring.data.scylla.local-datacenter}")
    protected String localDc;

    @Value("${spring.data.scylla.keyspace-name}")
    protected String keyspaceName;

    @Value("${spring.data.scylla.consistency-level}")
    protected String consistency;

    @Value("${spring.data.scylla.username}")
    protected String username;

    @Value("${spring.data.scylla.password}")
    protected String password;


    @Value("${spring.data.scylla.metadata-schema-req-timeout-millis}")
    protected long metadataSchemaReqTimeout;


    @Value("${spring.data.scylla.conn-int-query-timeout-millis}")
    protected long connInitQueryTimeout;

    @Value("${spring.data.scylla.rq-timeout-millis}")
    protected long rqTimeout;


    @Override
    public String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    @NonNull
    public String getLocalDataCenter() {
        return localDc;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        if ( port != null )  return port;
        return CqlSessionFactoryBean.DEFAULT_PORT;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.valueOf(schemaActions);
    }

    @Override
    protected String getConsistencyLV() {
        log.info("Consistency level: {}", consistency);
        return consistency;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"studio.giangbb.scylladbdemo.entity"};
    }


    @Override
    protected String getUsername() {
        return username;
    }

    @Override
    protected String getPassword() {
        return password;
    }

    @Override
    protected Duration getMetaDataSchemaRequestTimeout() {
        log.info("Metadata schema request timeout: {}", metadataSchemaReqTimeout);
        return Duration.ofMillis(metadataSchemaReqTimeout);
    }

    @Override
    protected Duration getConnectionInitTimeout() {
        log.info("Connection init query timeout: {}", connInitQueryTimeout);
        return Duration.ofMillis(connInitQueryTimeout);
    }

    @Override
    protected Duration getRequestTimeout() {
        log.info("Request timeout: {}", rqTimeout);
        return Duration.ofMillis(rqTimeout);
    }

    @Bean
    public DaoMapper daoMapper(CqlSession session)  {
        return new DaoMapperBuilder(session).build();
    }

//    @Bean
//    public MutableCodecRegistry mutableCodecRegistry(CqlSession session){
//        //register Custom codecs
//        TypeCodec<Client.Role> clientRoleByNameCodec = ExtraTypeCodecs.enumNamesOf(Client.Role.class);
//        MutableCodecRegistry registry =
//                (MutableCodecRegistry) session.getContext().getCodecRegistry();
//        registry.register(clientRoleByNameCodec);
//
//        return registry;
//    }
}
