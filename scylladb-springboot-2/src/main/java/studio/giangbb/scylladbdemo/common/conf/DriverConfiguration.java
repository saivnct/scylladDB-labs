//package studio.giangbb.scylladbdemo.common.conf;
//
//import com.datastax.oss.driver.api.core.CqlIdentifier;
//import com.datastax.oss.driver.api.core.CqlSession;
//import com.datastax.oss.driver.api.core.CqlSessionBuilder;
//import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
//import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
//import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
//import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.lang.NonNull;
//import org.springframework.util.StringUtils;
//import studio.giangbb.scylladbdemo.dao.DaoMapper;
//import studio.giangbb.scylladbdemo.dao.DaoMapperBuilder;
//
//import java.net.InetSocketAddress;
//import java.util.List;
//
///**
// * Created by giangbb on 14/06/2023
// */
//@Configuration
//public class DriverConfiguration {
//    private final Logger log = LoggerFactory.getLogger(DriverConfiguration.class);
//
//    @Value("#{'${spring.data.cassandra.contact-points}'.split(',')}")
//    protected List<String> contactPoints;
//
//    @Value("${spring.data.cassandra.port:9042}")
//    protected Integer port;
//
//    @Value("${spring.data.cassandra.local-datacenter}")
//    protected String localDc;
//
//    @Value("${spring.data.cassandra.keyspace-name}")
//    protected String keyspaceName;
//
//    @Value("${spring.data.cassandra.consistency-level:LOCAL_QUORUM}")
//    protected String consistency;
//
//    @Value("${spring.data.cassandra.username}")
//    protected String username;
//
//    @Value("${spring.data.cassandra.password}")
//    protected String password;
//
//    /**
//     * Returns the keyspace to connect to. The keyspace specified here must exist.
//     *
//     * @return The [keyspace][CqlIdentifier] bean.
//     */
//    @Bean
//    public CqlIdentifier keyspace() {
//        log.info("INTIALIZING KEYSPACE {}", keyspaceName);
//        return CqlIdentifier.fromCql(keyspaceName);
//    }
//
//
//    /**
//     * Returns a [ProgrammaticDriverConfigLoaderBuilder] to load driver options.
//     *
//     *
//     * Use this loader if you need to programmatically override default values for any driver
//     * setting. In this example, we manually set the default consistency level to use, and, if a
//     * username and password are present, we define a basic authentication scheme using [ ].
//     *
//     *
//     * Any value explicitly set through this loader will take precedence over values found in the
//     * driver's standard application.conf file.
//     *
//     * @return The [ProgrammaticDriverConfigLoaderBuilder] bean.
//     */
//    @Bean
//    public ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder() {
//        ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder = DriverConfigLoader.programmaticBuilder()
//                .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistency);
//
//        if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
//            configLoaderBuilder = configLoaderBuilder
//                    .withString(
//                            DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class.getName()
//                )
//                .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, username)
//                .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, password);
//        }
//        return configLoaderBuilder;
//    }
//
//
//
//    /**
//     * Returns a [CqlSessionBuilder] that will configure sessions using the provided [ ], as well as the contact points and
//     * local datacenter name found in application.yml, merged with other options found in
//     * application.conf.
//     *
//     * @param driverConfigLoaderBuilder The [ProgrammaticDriverConfigLoaderBuilder] bean to use.
//     * @return The [CqlSessionBuilder] bean.
//     */
//    @Bean
//    public CqlSessionBuilder sessionBuilder(
//            @NonNull ProgrammaticDriverConfigLoaderBuilder driverConfigLoaderBuilder
//    ) {
//        CqlSessionBuilder sessionBuilder = new CqlSessionBuilder().withConfigLoader(driverConfigLoaderBuilder.build());
//        for (String contactPoint : contactPoints) {
//            InetSocketAddress address = InetSocketAddress.createUnresolved(contactPoint, port);
//            log.info("Adding contact point {}:{} - {}", address.getHostString(), address.getPort(), address.toString());
//            sessionBuilder = sessionBuilder.addContactPoint(address);
//        }
//        return sessionBuilder.withLocalDatacenter(localDc);
//    }
//
//    /**
//     * Returns the [CqlSession] to use, configured with the provided [ session builder][CqlSessionBuilder]. The returned session will be automatically connected to the given keyspace.
//     *
//     * @param sessionBuilder The [CqlSessionBuilder] bean to use.
//     * @param keyspace The [keyspace][CqlIdentifier] bean to use.
//     * @return The [CqlSession] bean.
//     */
//    @Bean
//    public CqlSession session(
//            @NonNull CqlSessionBuilder sessionBuilder,
//            @Qualifier("keyspace") @NonNull CqlIdentifier keyspace)  {
//        log.info("INTIALIZING CQL SESSION");
//        return sessionBuilder.withKeyspace(keyspace).build();
//    }
//
//
//    @Bean
//    public DaoMapper daoMapper(CqlSession session)  {
//        return new DaoMapperBuilder(session).build();
//    }
//}
