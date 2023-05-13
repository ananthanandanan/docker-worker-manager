package ai.openfabric.api.repository;


import ai.openfabric.api.model.WorkerStats;
import org.springframework.data.repository.CrudRepository;

public interface WorkerStatsRepository extends CrudRepository<WorkerStats, String> {
    WorkerStats findByWorkerId(String workerId);
}
