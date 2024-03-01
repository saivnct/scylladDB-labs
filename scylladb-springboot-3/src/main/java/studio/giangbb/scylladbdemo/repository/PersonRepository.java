package studio.giangbb.scylladbdemo.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import studio.giangbb.scylladbdemo.models.Person;
import studio.giangbb.scylladbdemo.models.PersonName;

import java.util.List;
import java.util.UUID;

/**
 * Created by Giangbb on 01/03/2024
 */
public interface PersonRepository extends CassandraRepository<Person, UUID> {
    List<Person> findAllByJob(Person.Job jon);

    @Query("SELECT * FROM person WHERE job = ?0")
    List<Person> queryAllByJob(Person.Job jon);

    //CassandraRepository - not auto support Allowing filtering => create custom query
    @Query("SELECT * FROM person WHERE name = ?0 ALLOW FILTERING")
    List<Person> findAllByName(PersonName name);


}
