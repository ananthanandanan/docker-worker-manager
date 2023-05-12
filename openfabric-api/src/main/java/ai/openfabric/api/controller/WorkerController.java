package ai.openfabric.api.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    private final DockerClient dockerClient;
    public WorkerController(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }

    @GetMapping(path = "/containers")
    public @ResponseBody List<Container> containersAvailable() {
        List<Container> containers = dockerClient.listContainersCmd().exec();

        if(containers!=null){
            return containers;
        }

        return null;
    }
}