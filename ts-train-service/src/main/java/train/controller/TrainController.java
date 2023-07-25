package train.controller;

import edu.fudan.common.util.Response;















import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import train.entity.TrainType;
import train.service.TrainService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@RequestMapping("/api/v1/trainservice")
public class TrainController { 
    private static final Logger logger = LoggerFactory.getLogger(TrainController.class);

















    @Autowired
    private TrainService trainService;

    @GetMapping(path = "/trains/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get /api/v1/trainservice/trains/welcome][headers:{}]","home",(headers != null ? headers.toString(): null));
        return "Welcome to [ Train Service ] !";
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/trains")
    public HttpEntity create(@RequestBody TrainType trainType, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Post /api/v1/trainservice/trains][trainType:{}, headers:{}]","create",(trainType != null ? trainType.toString(): null), (headers != null ? headers.toString(): null));
        boolean isCreateSuccess = trainService.create(trainType, headers);
        if (isCreateSuccess) {
            return ok(new Response(1, "create success", null));
        } else {
            return ok(new Response(0, "train type already exist", trainType));
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/trains/{id}")
    public HttpEntity retrieve(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get /api/v1/trainservice/trains/{id}][id:{}, headers:{}]","retrieve",id, (headers != null ? headers.toString(): null));
        TrainType trainType = trainService.retrieve(id, headers);
        if (trainType == null) {
            return ok(new Response(0, "here is no TrainType with the trainType id: " + id, null));
        } else {
            return ok(new Response(1, "success", trainType));
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/trains/byName/{name}")
    public HttpEntity retrieveByName(@PathVariable String name, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get /api/v1/trainservice/trains/byName/{name}][name:{}, headers:{}]","retrieveByName",name, (headers != null ? headers.toString(): null));
        TrainType trainType = trainService.retrieveByName(name, headers);
        if (trainType == null) {
            return ok(new Response(0, "here is no TrainType with the trainType name: " + name, null));
        } else {
            return ok(new Response(1, "success", trainType));
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/trains/byNames")
    public HttpEntity retrieveByNames(@RequestBody List<String> names, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Post /api/v1/trainservice/trains/byNames][names:{}, headers:{}]","retrieveByNames",(names != null ? names.toString(): null), (headers != null ? headers.toString(): null));
        List<TrainType> trainTypes = trainService.retrieveByNames(names, headers);
        if (trainTypes == null) {
            return ok(new Response(0, "here is no TrainTypes with the trainType names: " + names, null));
        } else {
            return ok(new Response(1, "success", trainTypes));
        }
    }

    @CrossOrigin(origins = "*")
    @PutMapping(value = "/trains")
    public HttpEntity update(@RequestBody TrainType trainType, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Put /api/v1/trainservice/trains][trainType:{}, headers:{}]","update",(trainType != null ? trainType.toString(): null), (headers != null ? headers.toString(): null));
        boolean isUpdateSuccess = trainService.update(trainType, headers);
        if (isUpdateSuccess) {
            return ok(new Response(1, "update success", isUpdateSuccess));
        } else {
            return ok(new Response(0, "there is no trainType with the trainType id", isUpdateSuccess));
        }
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/trains/{id}")
    public HttpEntity delete(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Delete /api/v1/trainservice/trains/{id}][id:{}, headers:{}]","delete",id, (headers != null ? headers.toString(): null));
        boolean isDeleteSuccess = trainService.delete(id, headers);
        if (isDeleteSuccess) {
            return ok(new Response(1, "delete success", isDeleteSuccess));
        } else {
            return ok(new Response(0, "there is no train according to id", null));
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/trains")
    public HttpEntity query(@RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get /api/v1/trainservice/trains][headers:{}]","query",(headers != null ? headers.toString(): null));
        List<TrainType> trainTypes = trainService.query(headers);
        if (trainTypes != null && !trainTypes.isEmpty()) {
            return ok(new Response(1, "success", trainTypes));
        } else {
            return ok(new Response(0, "no content", trainTypes));
        }
    }
}
