## Project Description ⚙️	
This project is a SpringBoot application that exposes REST APIs to manage docker containers/workers. It uses docker-java library to interact with docker daemon. This project uses Gradle as the build tool. It uses PostgreSQL as the database, JPA for ORM, specifically Hibernate as the JPA provider and Liquibase for database migration.

## Development Setup 🛠️

1.  📁 Clone the repository and import the project in your IDE.

```bash
git clone git@github.com:ananthanandanan/docker-worker-manager.git
```

2. 🛠️ Import the project in your IDE as a Gradle project. We recommend using IntelliJ IDEA, which will automatically import the project as a Gradle project.

3. ⤵️ Before running the application, you need to set up the database. This project uses PostgreSQL as the database. You can use Docker to set up the database. Run the docker-compose.yml file in the root directory of the project.
    
```bash
docker-compose --compatibility up -d --build 
```
  - This command will start the PostgreSQL database container as well as an Alpine container (for demo purposes). The Alpine image is replicated to simulate multiple `containers/workers` in the system. In the current `docker-compose.yml` file, we are starting six Alpine containers. You can change the number of containers by changing the `replicas` value in the `docker-compose.yml` file.

4. 🏗️ Once that is done, build the project in your IDE. This will download all the dependencies and build the project.

5. 🚀 This will start the SpringBoot application. You can access the application at http://localhost:8080/swagger-ui/#/. This will show the Swagger UI of the application. You can use this to test the APIs.

----------------

## API Documentation 🚀

- Swagger UI: `http://localhost:8080/swagger-ui/#/`
- Get worker details by container id: `http://localhost:8080/api/v1/workers/{containerId}`: GET request to get the worker details by container id from the database. If the worker is not present in the database, it will check if the worker is present in the docker daemon. If the worker is present in the docker daemon, it will be persisted in the database and returned. If the worker is not present in the docker daemon, it will return 404. 
- Sample Response:

  ```json
      {
    "createdAt": 1684137966377,
    "updatedAt": 1684137966377,
    "deletedAt": null,
    "id": "80dc6662-a299-428b-9c5d-2fb0f8c5135b",
    "name": "alpine:latest",
    "status": "running",
    "host": "54406d06d23c",
    "containerPort": 0,
    "hostPort": 0,
    "containerId": "54406d06d23c",
    "containerName": "openfabric-test_alpine_2",
    "command": "[sh, -c, while true; do echo 'Hello, world!'; sleep 1; done]",
    "createdTime": "2023-05-15T08:04:18.063339963Z",
    "bindedPorts": null,
    "volumeBindings": "",
    "healthCheck": "null",
    "workerStats": null
    }

  ```


- Get worker statistics by worker id: `http://localhost:8080/api/v1/stats/{workerid}`: GET request to get the worker statistics by worker id from the database. If the worker is not present in the database, it will check if the worker is present in the docker daemon. If the worker is present in the docker daemon, it will be persisted in the database and returned. If the worker is not present in the docker daemon, it will return 404. 
- Sample Response:

  ```json 
  {
    "createdAt": 1684138109634,
    "updatedAt": 1684138109634,
    "deletedAt": null,
    "id": "0efc37c9-a2f3-4506-a40a-9a3cd03bc8e7",
    "cpuUsage": 61061000,
    "memoryUsage": 274432,
    "networkInput": 1418,
    "networkOutput": 0,
    "blockInput": null,
    "blockOutput": null,
    "processCount": 0
  }

    ```

- Get all workers: `http://localhost:8080/api/v1/allworkers`:
GET request to get all available workers in database in paginated format. 

    - `page`: page number(default: 0)
    - `size`: number of workers per page(default: 5)

  - Sample request parameters:
    - page: 0
    - pageSize: 3
  - Sample response:
  
  ```json
    {
    "content": [
      {
        "createdAt": 1684137966377,
        "updatedAt": 1684137966377,
        "deletedAt": null,
        "id": "80dc6662-a299-428b-9c5d-2fb0f8c5135b",
        "name": "alpine:latest",
        "status": "running",
        "host": "54406d06d23c",
        "containerPort": 0,
        "hostPort": 0,
        "containerId": "54406d06d23c",
        "containerName": "openfabric-test_alpine_2",
        "command": "[sh, -c, while true; do echo 'Hello, world!'; sleep 1; done]",
        "createdTime": "2023-05-15T08:04:18.063339963Z",
        "bindedPorts": null,
        "volumeBindings": "",
        "healthCheck": "null",
        "workerStats": {
          "createdAt": 1684138109634,
          "updatedAt": 1684138109634,
          "deletedAt": null,
          "id": "0efc37c9-a2f3-4506-a40a-9a3cd03bc8e7",
          "cpuUsage": 61061000,
          "memoryUsage": 274432,
          "networkInput": 1418,
          "networkOutput": 0,
          "blockInput": null,
          "blockOutput": null,
          "processCount": 0
        }
      },
      {
        "createdAt": 1684138181897,
        "updatedAt": 1684138181897,
        "deletedAt": null,
        "id": "b88d9118-8584-45f4-bd27-4ea79c8ede53",
        "name": "alpine:latest",
        "status": "running",
        "host": "547b7959549f",
        "containerPort": 0,
        "hostPort": 0,
        "containerId": "547b7959549f",
        "containerName": "openfabric-test_alpine_1",
        "command": "[sh, -c, while true; do echo 'Hello, world!'; sleep 1; done]",
        "createdTime": "2023-05-15T08:04:18.061410047Z",
        "bindedPorts": null,
        "volumeBindings": "",
        "healthCheck": "null",
        "workerStats": null
      },
      {
        "createdAt": 1684138192700,
        "updatedAt": 1684138192700,
        "deletedAt": null,
        "id": "31a2a3f5-d8ec-4ac0-8781-8bba0b040686",
        "name": "postgres:latest",
        "status": "running",
        "host": "ef3e98b1d16f",
        "containerPort": 5432,
        "hostPort": 5432,
        "containerId": "ef3e98b1d16f",
        "containerName": "db",
        "command": "[postgres]",
        "createdTime": "2023-05-15T08:04:18.061353463Z",
        "bindedPorts": "{5432/tcp=[Lcom.github.dockerjava.api.model.Ports$Binding;@316ae623}",
        "volumeBindings": "/var/lib/docker/volumes/openfabric-test_db_data/_data:/var/lib/postgresql/data",
        "healthCheck": "null",
        "workerStats": null
      }
    ],
    "pageable": {
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "offset": 0,
      "pageNumber": 0,
      "pageSize": 3,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 3,
    "totalElements": 7,
    "last": false,
    "size": 3,
    "number": 0,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "numberOfElements": 3,
    "first": true,
    "empty": false
    }

  ```

---

- Stop a worker using container id: `http://localhost:8080/api/v1/stop/{containerId}`: POST request to stop a worker using container id. If the worker is not present in the database, it will check if the worker is present in the docker daemon. If the worker is present in the docker daemon, it will be persisted in the database and stopped. If the worker is not present in the docker daemon, it will return 404.
  - Sample response:
  ```text
     Stopped worker with containerId: 547b7959549f
  ```

- Start a worker using container id: `http://localhost:8080/api/v1/start/{containerId}`: POST request to start a worker using container id. If the worker is not present in the database, it will check if the worker is present in the docker daemon. If the worker is present in the docker daemon, it will be persisted in the database and started. If the worker is not present in the docker daemon, it will return 404.
  - Sample response:
  ```text
  Started worker with containerId: 547b7959549f
  ```