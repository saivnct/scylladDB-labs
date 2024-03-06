package studio.giangbb.scylladbdemo.repository;

import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import studio.giangbb.scylladbdemo.models.Person;
import studio.giangbb.scylladbdemo.models.PersonName;

import java.util.List;
import java.util.UUID;

/**
 * Created by Giangbb on 01/03/2024
 */
public interface PersonRepository extends CassandraRepository<Person, UUID> {
    List<Person> findAllByJob(Person.Job job);

    @Query("SELECT * FROM person WHERE job = :job")
    List<Person> queryAllByJob(@Param("job") Person.Job job);


    @AllowFiltering
    List<Person> findAllByName(PersonName name);
    //using @Query
//    @Query("SELECT * FROM person WHERE name = :name ALLOW FILTERING")
//    List<Person> findAllByName(@Param("name") PersonName name);


}
