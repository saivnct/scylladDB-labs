package studio.giangbb.scylladbdemo;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention;
import com.datastax.oss.driver.internal.mapper.processor.entity.CqlNameGenerator;
import com.datastax.oss.driver.internal.mapper.processor.util.Capitalizer;
import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.giangbb.scylladbdemo.models.ClientName;

import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Created by solgo on 07/04/2024
 */
public class CommonTest {
    private final Logger logger = LoggerFactory.getLogger(CommonTest.class);
    @Test
    public void testCQLName() {
//        String entityJavaName = Capitalizer.decapitalize(ClientName.class.getSimpleName().toString());
//        logger.info("Entity name: {}", entityJavaName);
//
//        String customName = null;
//        Optional<String> customCqlName = Optional.ofNullable(customName).map(name -> customName);
//
//        CqlNameGenerator cqlNameGenerator = new CqlNameGenerator(NamingConvention.SNAKE_CASE_INSENSITIVE);
//        String cqlName =
//                customCqlName
//                        .map(n -> CodeBlock.of("$S", n))
//                        .orElse(cqlNameGenerator.buildCqlName(entityJavaName)).toString();

        String cqlName = getCQLName(NamingConvention.SNAKE_CASE_INSENSITIVE, new ClientName());

        logger.info("cql name: {}", cqlName);
    }


    public static  <T> String getCQLName(NamingConvention namingConvention, T element) {
        CqlNameGenerator cqlNameGenerator = new CqlNameGenerator(namingConvention);
        String entityJavaName = element.getClass().getSimpleName();

        String cqlName = cqlNameGenerator.buildCqlName(entityJavaName).toString();
        return cqlName;
    }
}
