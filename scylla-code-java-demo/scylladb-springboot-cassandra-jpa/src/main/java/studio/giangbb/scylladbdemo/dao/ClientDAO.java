package studio.giangbb.scylladbdemo.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.models.Client;

import java.util.UUID;

/**
 * Created by giangbb on 26/06/2023
 */

@Component
@Qualifier("clientDAO")
public class ClientDAO extends AbstractScyllaDAO<UUID, Client> {
    @Autowired
    public ClientDAO(CassandraOperations cassandraOperations) {
        super(cassandraOperations, UUID.class, Client.class);
    }
}
