package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraPersistentEntitySchemaCreator;
import org.springframework.data.cassandra.core.cql.keyspace.CreateUserTypeSpecification;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Giangbb on 04/04/2024
 */
@SpringBootTest
public class MapperTest {
    private final Logger logger = LoggerFactory.getLogger(MapperTest.class);

    @Autowired
    private CqlSession cqlSession;

//    @Autowired
//    private CqlSessionFactoryBean cassandraSessionBean;


    @Autowired
    private CassandraMappingContext cassandraMappingContext;
    @Autowired
    private CassandraAdminTemplate cassandraTemplate;

    @Test
    public void test(){
//        logger.info("---------tableEntity");
//        cassandraMappingContext.getTableEntities().forEach(tableEntity -> {
//            logger.info("tableEntity {} - {}", tableEntity.getTableName(), tableEntity.getName());
//        });
//
//
//        logger.info("---------userDefinedTypeEntity");
//        cassandraMappingContext.getUserDefinedTypeEntities().forEach(userDefinedTypeEntity -> {
//            logger.info("userDefinedTypeEntity {} - {}", userDefinedTypeEntity.getTableName(), userDefinedTypeEntity.getName());
//        });

        logger.info("---------persistentEntity");
        cassandraMappingContext.getPersistentEntities().forEach(persistentEntity -> {
            logger.info("TableName {} - {} - isCompositePrimaryKey: {} - isTupleType: {} - isUserDefinedType: {} - isImmutable: {}",
                    persistentEntity.getTableName(),
                    persistentEntity.getName(),
                    persistentEntity.isCompositePrimaryKey(),
                    persistentEntity.isTupleType(),
                    persistentEntity.isUserDefinedType(),
                    persistentEntity.isImmutable()
            );

            persistentEntity.forEach(property -> {
                logger.info(" -> Property: {} - Type: {} - ColumnName: {} - CompositePrimaryKey: {} - PartitionKeyColumn: {} - ClusterKeyColumn: {} - Ordinal: {} - Transient: {} - CollectionLike: {} - MapLike: {} - Embedded: {}",
                        property.getName(),
                        property.getType(),
                        !property.isCompositePrimaryKey() ? property.getColumnName() : "CompositePrimaryKey",
                        property.isCompositePrimaryKey(),
                        property.isPartitionKeyColumn(),
                        property.isClusterKeyColumn(),
                        (property.isPartitionKeyColumn() || property.isClusterKeyColumn()) ? property.getPrimaryKeyOrdering().ordinal() : "not key",
                        property.isTransient(),
                        property.isCollectionLike(),
                        property.isMapLike(),
                        property.isEmbedded()
                );
            });

        });
    }


//    @Test
//    public void testPerformSchemaActions(){
//        CassandraPersistentEntitySchemaCreator schemaCreator = new CassandraPersistentEntitySchemaCreator(cassandraMappingContext, cassandraTemplate);
//        schemaCreator.createUserTypes(true);
//        schemaCreator.createTables(true);
//        schemaCreator.createIndexes(true);
//    }
}
