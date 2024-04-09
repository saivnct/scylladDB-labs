package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.NamingStrategy;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention.SNAKE_CASE_INSENSITIVE;

/**
 * Created by Giangbb on 06/03/2024
 */
@Entity
@NamingStrategy(convention = SNAKE_CASE_INSENSITIVE)
public class Client {

    public enum Role {
        ADMIN, USER
    }

    @PartitionKey
    private UUID id;

    private ClientName clientName;

    @CqlName("client_info")
    private ClientInfo clientInfo;


    //Must register with CodecRegistry
    //https://java-driver.docs.scylladb.com/stable/manual/core/custom_codecs/
    private Role role;

    private List<String> zones;

    public Client() {
    }

    public Client(ClientName clientName, ClientInfo clientInfo, Role role, List<String> zones) {
        this.id = Uuids.timeBased();
        this.clientName = clientName;
        this.clientInfo = clientInfo;
        this.role = role;
        this.zones = zones;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ClientName getClientName() {
        return clientName;
    }

    public void setClientName(ClientName clientName) {
        this.clientName = clientName;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<String> getZones() {
        return zones;
    }

    public void setZones(List<String> zones) {
        this.zones = zones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", clientName=" + clientName +
                ", clientInfo=" + clientInfo +
                ", role=" + role +
                ", zones=" + zones +
                '}';
    }
}
