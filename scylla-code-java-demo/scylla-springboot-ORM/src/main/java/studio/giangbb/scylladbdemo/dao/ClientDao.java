package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import studio.giangbb.scylladbdemo.entity.Client;
import studio.giangbb.scylladbdemo.entity.udt.ClientName;

import java.util.UUID;

/**
 * Created by Giangbb on 06/03/2024
 */
@Dao
public interface ClientDao extends BaseDao<Client>{
    @Select
    Client findByPrimKey(UUID id);

    @Query("SELECT count(*) FROM client")
    long countAll();

    @Query(value = "TRUNCATE client")
    void deleteAll();

    @Select(customWhereClause = "role = :role ALLOW FILTERING")
    PagingIterable<Client> getByRole(Client.Role role);

    @Select(customWhereClause = "client_name = :clientName")
    PagingIterable<Client> getByName(ClientName clientName);
}