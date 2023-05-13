package ai.openfabric.api.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table
public class WorkerStats extends Datable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "of-uuid")
    @GenericGenerator(name = "of-uuid", strategy = "ai.openfabric.api.model.IDGenerator")
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private double cpuUsage;

    @Getter
    @Setter
    private long memoryUsage;

    @Getter
    @Setter
    private long networkInput;

    @Getter
    @Setter
    private long networkOutput;

    @Getter
    @Setter
    private long blockInput;

    @Getter
    @Setter
    private long blockOutput;

    @Getter
    @Setter
    private long diskRead;

    @Getter
    @Setter
    private long diskWrite;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    @Getter
    @Setter
    private Worker worker;

}


