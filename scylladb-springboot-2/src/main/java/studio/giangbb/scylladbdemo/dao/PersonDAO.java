package studio.giangbb.scylladbdemo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.model.Person;

import java.util.UUID;

/**
 * Created by giangbb on 26/06/2023
 */

@Component
public class PersonDAO extends AbstractDAO<Person, UUID>{

    @Autowired
    public PersonDAO(CassandraOperations cassandraOperations) {
        super(Person.class, cassandraOperations);
    }


}
