package ai.openfabric.api.controller;

import ai.openfabric.api.model.Worker;
import ai.openfabric.api.model.WorkerStats;
import ai.openfabric.api.service.WorkerService;
import com.github.dockerjava.api.DockerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    private final WorkerService workerService;
    @Autowired
    public WorkerController(DockerClient dockerClient, WorkerService workerService) {

        this.workerService = workerService;
    }

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }


    @PostMapping("/start/{containerId}")
    public ResponseEntity<String> startWorker(@PathVariable String containerId) {
        return workerService.startWorker(containerId);
    }
    @PostMapping("/stop/{containerId}")
    public ResponseEntity<String> stopWorker(@PathVariable String containerId) {
        return workerService.stopWorker(containerId);
    }

    @GetMapping("/{containerId}")
    public ResponseEntity<Worker> getWorkerById(@PathVariable String containerId) {
        try {
            Worker worker = workerService.getWorkerById(containerId);
            return new ResponseEntity<>(worker, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/stats/{workerId}")
    public ResponseEntity<WorkerStats> getWorkerStatsById(@PathVariable String workerId) {
        try{
            WorkerStats workerStats = workerService.getWorkerStatsById(workerId);
            return new ResponseEntity<>(workerStats, HttpStatus.OK);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping(path = "/allworkers")
    public @ResponseBody Page<Worker> getWorkersPaginated(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "5") int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return workerService.getWorkersPaginated(pageable);
    }


}