package studio.giangbb.scylladbdemo.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by Giangbb on 01/03/2024
 */
public class AbstractScyllaDAO<KeyType, T> {
    protected Class<KeyType> keyTypeClass;
    protected Class<T> tClass;

    public static final Logger logger = LogManager.getLogger(AbstractScyllaDAO.class);

    private final SimpleCassandraRepository<T, KeyType> simpleCassandraRepository;
    private final CassandraOperations operations;
    private final AbstractMappingContext<BasicCassandraPersistentEntity<?>, CassandraPersistentProperty> mappingContext;

    public AbstractScyllaDAO(CassandraOperations operations, Class<KeyType> keyTypeClass, Class<T> tClass) {
        Assert.notNull(keyTypeClass, "keyTypeClass must not be null");
        Assert.notNull(tClass, "tClass must not be null");
        Assert.notNull(operations, "CassandraOperations must not be null");

        this.keyTypeClass = keyTypeClass;
        this.tClass = tClass;
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
        this.simpleCassandraRepository = new SimpleCassandraRepository<T, KeyType>(getEntityInformation(tClass), this.operations);
    }


    private CassandraEntityInformation<T, KeyType> getEntityInformation(Class<T> domainClass) {
        CassandraPersistentEntity<?> entity = (CassandraPersistentEntity)this.mappingContext.getRequiredPersistentEntity(domainClass);
        return new MappingCassandraEntityInformation(entity, this.operations.getConverter());
    }


    public Class<KeyType> getKeyTypeClass() {
        return keyTypeClass;
    }

    public Class<T> getTClass() {
        return tClass;
    }


    public void update(T obj) {
        save(obj);
    }


    public <S extends T> S save(S entity) {
        return this.simpleCassandraRepository.save(entity);
    }

    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        return this.simpleCassandraRepository.saveAll(entities);
    }

    public T getByKey(KeyType key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        T obj = this.simpleCassandraRepository.findById(key).orElse(null);

        return obj;
    }

    public void deleteByKey(KeyType key) {
        this.simpleCassandraRepository.deleteById(key);
    }



    public List<T> findAllByKey(Iterable<KeyType> keys) {
        return this.simpleCassandraRepository.findAllById(keys);
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

    public void delete(T entity) {
        this.simpleCassandraRepository.delete(entity);
    }

    public void deleteAllByKeys(Iterable<? extends KeyType> keys) {
        this.simpleCassandraRepository.deleteAllById(keys);
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

    public List<T> find(Query query){
        return this.operations.select(query, this.tClass);
    }

    public Slice<T> find(Query query, Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        return this.operations.slice(query.pageRequest(pageable), this.tClass);
    }

    public T findOne(Query query){
        return this.operations.selectOne(query, this.tClass);
    }

}
