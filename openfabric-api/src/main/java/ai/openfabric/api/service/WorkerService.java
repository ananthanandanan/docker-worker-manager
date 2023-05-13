package ai.openfabric.api.service;

import ai.openfabric.api.model.Worker;
import ai.openfabric.api.model.WorkerStats;
import ai.openfabric.api.repository.WorkerRepository;
import ai.openfabric.api.repository.WorkerStatsRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.InvocationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private  final WorkerStatsRepository workerStatsRepository;

    private final DockerClient dockerClient;

    @Autowired
    public WorkerService(WorkerRepository workerRepository, WorkerStatsRepository workerStatsRepository, DockerClient dockerClient) {
        this.workerRepository = workerRepository;
        this.workerStatsRepository = workerStatsRepository;
        this.dockerClient = dockerClient;
    }

    public Statistics getContainerStatistics(Worker worker) {
        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
        dockerClient.statsCmd(worker.getContainerId()).exec(callback);
        Statistics stats = null;
        try {
            stats = callback.awaitResult();
            callback.close();
        } catch (RuntimeException | IOException e) {
            // you may want to throw an exception here
        }
        return stats; // this may be null or invalid if the container has terminated
    }

    public  WorkerStats getWorkerStatsById(String workerId) {
        WorkerStats workerStats = workerStatsRepository.findByWorkerId(workerId);

        if (workerStats == null) {
            System.out.println("Worker Stats is null");
            // WorkerStats not found in the database, fetch it from Docker and save to database
            Optional<Worker> checkWorker  = workerRepository.findById(workerId);
            if (!checkWorker.isPresent()) {
                return null;
            }
            System.out.println("Worker is present");
            // Set the worker
            Worker worker = checkWorker.get();
            Statistics containerStats =  getContainerStatistics(worker);

            workerStats = new WorkerStats();
            workerStats.setWorker(worker);
            workerStats.setCpuUsage(Objects.requireNonNull(containerStats.getCpuStats().getCpuUsage()).getTotalUsage());
            workerStats.setMemoryUsage(containerStats.getMemoryStats().getUsage());
            workerStats.setNetworkInput(containerStats.getNetworks().get("eth0").getRxBytes());
            workerStats.setNetworkOutput(containerStats.getNetworks().get("eth0").getTxBytes());
            workerStats.setBlockInput(containerStats.getBlkioStats().getIoServiceBytesRecursive().get(0).getValue());
            workerStats.setBlockOutput(containerStats.getBlkioStats().getIoServiceBytesRecursive().get(1).getValue());
            //TODO: Add field called processes instead of Disk write and read


            // Save the new WorkerStats object to the database
            workerStatsRepository.save(workerStats);
            return workerStatsRepository.findByWorkerId(workerId);

        }

        return workerStats;
    }


    public Worker getWorkerById(String containerId) {
        Optional<Worker> checkWorker = Optional.ofNullable(workerRepository.findByContainerId(containerId));
        if (checkWorker.isPresent()) {
            return checkWorker.get();
        }
        else {
            try {
                InspectContainerResponse inspectContainerResponse  = dockerClient.inspectContainerCmd(containerId).exec();
                Worker worker = new Worker();
                worker.setContainerId(containerId);
                worker.setName(inspectContainerResponse.getConfig().getImage());
                worker.setContainerName(inspectContainerResponse.getName().split("/")[1]);
                worker.setStatus(inspectContainerResponse.getState().getStatus());
                worker.setHost(inspectContainerResponse.getConfig().getHostName());
                Map<ExposedPort, Ports.Binding[]> portBindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();
                if (!portBindings.isEmpty()) {
                    ExposedPort exposedPort = portBindings.keySet().iterator().next();
                    worker.setPort(String.valueOf(exposedPort.getPort()));
                    worker.setPortBinds(portBindings);
                }
//                worker.setVolumeBindings(Objects.requireNonNull(inspectContainerResponse.getMounts()).toString());
//                worker.setEnvironmentVariables(Arrays.toString(inspectContainerResponse.getConfig().getEnv()));
                worker.setHealthCheck(String.valueOf(inspectContainerResponse.getConfig().getHealthcheck()));

                worker.setCommand(Arrays.toString(inspectContainerResponse.getConfig().getCmd()));
                worker.setCreatedTime(inspectContainerResponse.getCreated());
                // TODO: Add the start/stop API, Pagination for getting all workers
                // TODO: Change up the fields that might not be needed

                workerRepository.save(worker);
                return workerRepository.findByContainerId(containerId);

            }catch (DockerException e) {
                throw new NotFoundException("Worker not found");
            }
        }

    }


}
