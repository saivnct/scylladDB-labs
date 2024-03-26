package studio.giangbb.scylladbdemo.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.models.Client;
import studio.giangbb.scylladbdemo.models.ClientName;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.cassandra.core.query.Criteria.where;

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


    public List<Client> findAllByName(ClientName clientName){
        return this.find(
                Query.query(
                        where("client_name").is(clientName)
                )
        );
    }


    public List<Client> findAllByRole(Client.Role role){
        return this.find(
                Query.query(
                        where("role").is(role.ordinal())
                )
        );
    }
}
