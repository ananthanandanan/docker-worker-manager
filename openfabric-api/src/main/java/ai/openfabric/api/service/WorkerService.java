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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

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

    public Statistics getContainerStatistics(Worker worker) throws NotFoundException{
        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
        dockerClient.statsCmd(worker.getContainerId()).exec(callback);
        Statistics stats;
        try {
            stats = callback.awaitResult();
            callback.close();
        } catch (RuntimeException | IOException e) {
            throw new NotFoundException("Stats Not Found");
        }
        return stats;
    }

    public  WorkerStats getWorkerStatsById(String workerId) throws NotFoundException{
        WorkerStats workerStats = workerStatsRepository.findByWorkerId(workerId);

        if (workerStats == null) {
            // WorkerStats not found in the database, fetch it from Docker and save to database
            Optional<Worker> checkWorker  = workerRepository.findById(workerId);
            if (!checkWorker.isPresent()) {
                throw new NotFoundException("Worker Not Found");
            }

            // Set the worker
            Worker worker = checkWorker.get();
            try {
                Statistics containerStats =  getContainerStatistics(worker);

                workerStats = new WorkerStats();
                workerStats.setWorker(worker);
                workerStats.setCpuUsage(Objects.requireNonNull(containerStats.getCpuStats().getCpuUsage()).getTotalUsage());
                workerStats.setMemoryUsage(containerStats.getMemoryStats().getUsage());
                workerStats.setNetworkInput(Objects.requireNonNull(containerStats.getNetworks()).get("eth0").getRxBytes());
                workerStats.setNetworkOutput(Objects.requireNonNull(containerStats.getNetworks()).get("eth0").getTxBytes());

                if(containerStats.getBlkioStats().getIoServiceBytesRecursive() != null
                        && !containerStats.getBlkioStats().getIoServiceBytesRecursive().isEmpty()){
                    workerStats.setBlockInput(containerStats.getBlkioStats().getIoServiceBytesRecursive().get(0).getValue());
                } else {
                    workerStats.setBlockInput(null);
                }
                if(containerStats.getBlkioStats().getIoServiceBytesRecursive() != null
                        && !containerStats.getBlkioStats().getIoServiceBytesRecursive().isEmpty()){
                    workerStats.setBlockOutput(containerStats.getBlkioStats().getIoServiceBytesRecursive().get(1).getValue());
                } else {
                    workerStats.setBlockOutput(null);
                }

                workerStats.setProcessCount(containerStats.getNumProcs());

                // Save the new WorkerStats object to the database
                workerStatsRepository.save(workerStats);
                return workerStatsRepository.findByWorkerId(workerId);

            }catch (DockerException e) {
                throw new NotFoundException("Error Getting worker statistics");
            }

        }

        return workerStats;
    }


    public Worker getWorkerById(String containerId) throws  NotFoundException{
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

                // Get the Container and Host ports as well as set PortBindings
                Map<ExposedPort, Ports.Binding[]> portBindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();
                if (!portBindings.isEmpty()) {
                    ExposedPort exposedPort = portBindings.keySet().iterator().next();
                    worker.setPort(String.valueOf(exposedPort.getPort()));
                    worker.setBindedPorts(portBindings);
                }
                // Get the VolumeBinding: Get Source and Destination
                List<String> volumeBindingList = new ArrayList<>();
                for (InspectContainerResponse.Mount mount : Objects.requireNonNull(inspectContainerResponse.getMounts())) {

                    String source = mount.getSource();
                    String destination = String.valueOf(mount.getDestination());

                    // Add source and destination to the volumeBindingList
                    String volumeBinding = String.format("%s:%s", source, destination);
                    volumeBindingList.add(volumeBinding);
                }

                // Join volume bindings with a separator character
                String volumeBindings = String.join(",", volumeBindingList);

                worker.setVolumeBindings(volumeBindings);

                worker.setHealthCheck(String.valueOf(inspectContainerResponse.getConfig().getHealthcheck()));
                worker.setCommand(Arrays.toString(inspectContainerResponse.getConfig().getCmd()));
                worker.setCreatedTime(inspectContainerResponse.getCreated());

                workerRepository.save(worker);
                return workerRepository.findByContainerId(containerId);

            }catch (DockerException e) {
                throw new NotFoundException("Worker not found");
            }
        }

    }

    public ResponseEntity<String> startWorker(String containerId) {
        try {
            // Start the container
            dockerClient.startContainerCmd(containerId).exec();

            Optional<Worker> checkWorker = Optional.ofNullable(workerRepository.findByContainerId(containerId));
            // Check if container started exist in database

            if (checkWorker.isPresent()) {
                Worker fetchWorker = checkWorker.get();
                // Inspect again to fetch the status
                InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
                fetchWorker.setStatus(inspectContainerResponse.getState().getStatus());
                workerRepository.save(fetchWorker);
            }
            else {
                Worker newWorker = getWorkerById(containerId);

            }

            return ResponseEntity.ok("Started worker with containerId: " + containerId);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Worker with containerId: " + containerId + " not found");
        } catch (DockerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start worker with containerId: " + containerId);
        }

    }

    public ResponseEntity<String> stopWorker(String containerId) {
        try {
            // Stop the container
            dockerClient.stopContainerCmd(containerId).exec();
            Worker worker = workerRepository.findByContainerId(containerId);
            if (worker != null) {
                // Set worker status to "exited"
                worker.setStatus("exited");
                workerRepository.save(worker);
            }
            return ResponseEntity.ok("Stopped worker with containerId: " + containerId);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Worker with containerId: " + containerId + " not found");
        } catch (DockerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to stop worker with containerId: " + containerId);
        }
    }

    public Page<Worker> getWorkersPaginated(Pageable pageable) {
        return workerRepository.findAll(pageable);
    }
}
