package studio.giangbb.scylladbdemo.models;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Giangbb on 06/03/2024
 */
@Table("client")
public class Client {

    public enum Role {
        ADMIN, USER
    }

    @PrimaryKey("id")
    private UUID id;

    @Indexed
    @Column("client_name")
    private ClientName clientName;

    @Column("client_info")
    private ClientInfo clientInfo;

    @Column("role")
    private int role;
    @Column("zones")
    private List<String> zones;

    public Client() {
    }

    public Client(ClientName clientName, ClientInfo clientInfo, int role, List<String> zones) {
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

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
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
