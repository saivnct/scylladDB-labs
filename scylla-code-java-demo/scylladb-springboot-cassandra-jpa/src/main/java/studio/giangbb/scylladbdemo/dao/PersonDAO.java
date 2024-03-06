package studio.giangbb.scylladbdemo.dao;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.models.Car;
import studio.giangbb.scylladbdemo.models.Person;
import studio.giangbb.scylladbdemo.models.PersonName;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.cassandra.core.query.Criteria.where;

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


    public List<Person> findAllByJob(Person.Job job){
        return this.find(
                Query.query(
                        where("job").is(job)
                )
        );
    }

    public List<Person> findAllByName(PersonName name){
        return this.find(
                Query.query(
                        where("name").is(name)
                ).withAllowFiltering()
        );
    }

}
