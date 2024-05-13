package studio.giangbb.scylladbdemo.daoct;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import studio.giangbb.scylladbdemo.entity.Client;
import studio.giangbb.scylladbdemo.entity.udt.ClientName;
import com.giangbb.scylla.core.ScyllaTemplate;
import com.giangbb.scylla.repository.SimpleScyllaRepository;

/**
 * Created by giangbb on 12/05/2024
 */

@Component
@Qualifier("clientDAO")
public class ClientDAO extends SimpleScyllaRepository<Client> {

    @Autowired
    public ClientDAO(ScyllaTemplate scyllaTemplate) {
        super(Client.class, scyllaTemplate);
    }


    public PagingIterable<Client> getByRole(Client.Role role){
        final String columnName = "role";
        SimpleStatement selectStatement = getScyllaEntityHelperImpl()
                .selectStart()
                .whereColumn(columnName)
                .isEqualTo(QueryBuilder.bindMarker(columnName))
                .allowFiltering()
                .build();

        PreparedStatement preparedStatement = this.prepare(selectStatement);

        return executeAndMapToEntityIterable(preparedStatement.bind(role));
    }

    //find by UDT index column
    public PagingIterable<Client> getByName(ClientName clientName) {
        final String columnName = "client_name";
        SimpleStatement selectStatement = getScyllaEntityHelperImpl()
                .selectStart()
                .whereColumn(columnName)
                .isEqualTo(QueryBuilder.bindMarker(columnName))
                .build();

        PreparedStatement preparedStatement = this.prepare(selectStatement);
        UdtValue bindValue = marshallUDTValue(columnName, clientName);

        return executeAndMapToEntityIterable(preparedStatement.bind(bindValue));
    }
}
