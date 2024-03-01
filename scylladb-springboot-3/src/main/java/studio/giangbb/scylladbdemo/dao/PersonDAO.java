package studio.giangbb.scylladbdemo.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.models.Person;

import java.util.UUID;

/**
 * Created by giangbb on 26/06/2023
 */

@Component
@Qualifier("personDAO")
public class PersonDAO extends AbstractScyllaDAO<UUID, Person> {

    @Autowired
    public PersonDAO(CassandraOperations cassandraOperations) {
        super(cassandraOperations, UUID.class, Person.class);
    }


}
