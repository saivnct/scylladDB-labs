package studio.giangbb.scylladbdemo.dao;

import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.BasicCassandraPersistentEntity;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentProperty;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.MappingCassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;


/**
 * Created by giangbb on 26/06/2023
 */
public class AbstractDAO<T, ID>{
    protected Class<T> tClass;
    private final SimpleCassandraRepository<T, ID> simpleCassandraRepository;
    private final CassandraOperations operations;
    private final AbstractMappingContext<BasicCassandraPersistentEntity<?>, CassandraPersistentProperty> mappingContext;

    public AbstractDAO(Class<T> tClass, CassandraOperations operations) {
        Assert.notNull(tClass, "tClass must not be null");
        Assert.notNull(operations, "CassandraOperations must not be null");
        this.tClass = tClass;
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
        this.simpleCassandraRepository = new SimpleCassandraRepository<T, ID>(getEntityInformation(tClass), this.operations);
    }


    private CassandraEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        CassandraPersistentEntity<?> entity = (CassandraPersistentEntity)this.mappingContext.getRequiredPersistentEntity(domainClass);
        return new MappingCassandraEntityInformation(entity, this.operations.getConverter());
    }


    public <S extends T> S save(S entity) {
        return this.simpleCassandraRepository.save(entity);
    }

    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        return this.simpleCassandraRepository.saveAll(entities);
    }

    public Optional<T> findById(ID id) {
        return this.simpleCassandraRepository.findById(id);
    }

    public List<T> findAllById(Iterable<ID> ids) {
        return this.simpleCassandraRepository.findAllById(ids);
    }

    public List<T> findAll() {
        return this.simpleCassandraRepository.findAll();
    }

    public Slice<T> findAll(Pageable pageable) {
        return this.simpleCassandraRepository.findAll(pageable);
    }

    public long count(Query query){
        return this.operations.count(query, this.tClass);
    }

    public long countAll() {
        return this.simpleCassandraRepository.count();
    }

    public void deleteById(ID id) {
        this.simpleCassandraRepository.deleteById(id);
    }

    public void delete(T entity) {
        this.simpleCassandraRepository.delete(entity);
    }

    public void deleteAllById(Iterable<? extends ID> ids) {
        this.simpleCassandraRepository.deleteAllById(ids);
    }

    public void deleteAll(Iterable<? extends T> entities) {
        this.simpleCassandraRepository.deleteAll(entities);
    }

    public void deleteAll() {
        this.simpleCassandraRepository.deleteAll();
    }

    public void delete(Query query) {
        this.operations.delete(query, this.tClass);
    }



    public List<T> select(Query query){
        return this.operations.select(query, this.tClass);
    }

    public T selectOne(Query query){
        return this.operations.selectOne(query, this.tClass);
    }


}
