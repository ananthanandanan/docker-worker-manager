package ai.openfabric.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    private Long memoryUsage;

    @Getter
    @Setter
    private Long networkInput;

    @Getter
    @Setter
    private Long networkOutput;

    @Getter
    @Setter
    private Long blockInput;

    @Getter
    @Setter
    private Long blockOutput;

    @Getter
    @Setter
    private Long processCount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    @JsonBackReference //handle stack overflowing
    @Getter
    @Setter
    private Worker worker;

}


