package trainFood.controller;


import org.springframework.beans.factory.annotation.Autowired;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import trainFood.service.TrainFoodService;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/trainfoodservice")
public class TrainFoodController { 
    private static final Logger logger = LoggerFactory.getLogger(TrainFoodController.class);




    @Autowired
    TrainFoodService trainFoodService;

    @GetMapping(path = "/trainfoods/welcome")
    public String home() {
        logger.info("[function name:home]");
        return "Welcome to [ Train Food Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/trainfoods")
    public HttpEntity getAllTrainFood(@RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}][HttpHeaders:{}]","getAllTrainFood",(headers != null ? headers.toString(): null));
        return ok(trainFoodService.listTrainFood(headers));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/trainfoods/{tripId}")
    public HttpEntity getTrainFoodOfTrip(@PathVariable String tripId, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}][String:{}, HttpHeaders:{}]","getTrainFoodOfTrip",tripId, (headers != null ? headers.toString(): null));
        return ok(trainFoodService.listTrainFoodByTripId(tripId, headers));
    }
}
