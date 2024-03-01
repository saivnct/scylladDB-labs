package studio.giangbb.scylladbdemo.conf;

import org.cognitor.cassandra.migration.MigrationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Giangbb on 01/03/2024
 */
@Component
public class ScyllaMigration {
    private final Logger log = LoggerFactory.getLogger(ScyllaMigration.class);

    @Autowired
    private MigrationTask migrationTask;

    @PostConstruct
    public void run() {
        // Function logic to be executed on application startup after all beans are created
        log.info("ScyllaMigration ...");
        migrationTask.migrate();
    }
}
