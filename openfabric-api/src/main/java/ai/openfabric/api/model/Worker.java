package ai.openfabric.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table
public class Worker extends Datable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "of-uuid")
    @GenericGenerator(name = "of-uuid", strategy = "ai.openfabric.api.model.IDGenerator")
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String host;

    @Getter
    @Setter
    private int containerPort;

    @Getter
    @Setter
    private int hostPort;

    @Getter
    @Setter
    private String containerId;

    @Getter
    @Setter
    private String containerName;

    @Getter
    @Setter
    private String command;

    @Getter
    @Setter
    private String createdTime;

    @Getter
    @Setter
    private String bindedPorts;

    @Getter
    @Setter
    private String volumeBindings;


    @Getter
    @Setter
    private String healthCheck;


    public void setPort(String port) {
        this.containerPort = Integer.parseInt(port);
    }

    public void setBindedPorts(Map<ExposedPort, Ports.Binding[]> portBindings) {
        this.bindedPorts = portBindings.toString();
        if (!portBindings.isEmpty()) {
            ExposedPort exposedPort = portBindings.keySet().iterator().next();
            this.hostPort = Integer.parseInt(portBindings.get(exposedPort)[0].getHostPortSpec());
        }
    }

    @OneToOne(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference //handle stack overflowing
    @Getter
    @Setter
    private WorkerStats workerStats;

}
