package studio.giangbb.scylladbdemo.common.conf;

import org.cognitor.cassandra.migration.MigrationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * Created by giangbb on 26/06/2023
 */


@Component
public class CassandraMigration {
    private final Logger log = LoggerFactory.getLogger(CassandraMigration.class);

    @Autowired
    private MigrationTask migrationTask;

    @PostConstruct
    public void run() {
        // Function logic to be executed on application startup after all beans are created
        log.info("CassandraMigration ...");
        migrationTask.migrate();
    }



}