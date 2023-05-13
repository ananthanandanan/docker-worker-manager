package ai.openfabric.api.controller;

import ai.openfabric.api.model.Worker;
import ai.openfabric.api.model.WorkerStats;
import ai.openfabric.api.service.WorkerService;
import com.github.dockerjava.api.DockerClient;
import org.springframework.beans.factory.annotation.Autowired;
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

//    @PostMapping(path = "/add")
//    public  ResponseEntity<Worker> addWorker(@RequestBody Worker worker) {
//        try {
//            workerService.addWorker(worker);
//            return new ResponseEntity<>(worker, HttpStatus.CREATED);
//        }catch (DockerException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/{containerId}")
    public ResponseEntity<Worker> getWorkerById(@PathVariable String containerId) {
        Worker worker = workerService.getWorkerById(containerId);
        return new ResponseEntity<>(worker, HttpStatus.OK);
    }

    @GetMapping("/stats/{workerId}")
    public ResponseEntity<WorkerStats> getWorkerStatsById(@PathVariable String workerId) {
        WorkerStats workerStats = workerService.getWorkerStatsById(workerId);
        return new ResponseEntity<>(workerStats, HttpStatus.OK);
    }

}