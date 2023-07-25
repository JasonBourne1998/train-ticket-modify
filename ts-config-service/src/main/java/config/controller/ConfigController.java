package config.controller;

import config.entity.Config;
















import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import config.service.ConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.ResponseEntity.ok;

/**
 * @author  Chenjie Xu
 * @date 2017/5/11.
 */
@RestController
@RequestMapping("api/v1/configservice")
public class ConfigController { 
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);


















    @Autowired
    private ConfigService configService;

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get api/v1/configservice/welcome][headers:{}]","home",(headers != null ? headers.toString(): null));
        return "Welcome to [ Config Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/configs")
    public HttpEntity queryAll(@RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get api/v1/configservice/configs][headers:{}]","queryAll",(headers != null ? headers.toString(): null));
        return ok(configService.queryAll(headers));
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/configs")
    public HttpEntity<?> createConfig(@RequestBody Config info, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Post api/v1/configservice/configs][info:{}, headers:{}]","createConfig",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        return new ResponseEntity<>(configService.create(info, headers), HttpStatus.CREATED);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(value = "/configs")
    public HttpEntity updateConfig(@RequestBody Config info, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Put api/v1/configservice/configs][info:{}, headers:{}]","updateConfig",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        return ok(configService.update(info, headers));
    }


    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/configs/{configName}")
    public HttpEntity deleteConfig(@PathVariable String configName, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Delete api/v1/configservice/configs/{configName}][configName:{}, headers:{}]","deleteConfig",configName, (headers != null ? headers.toString(): null));
        return ok(configService.delete(configName, headers));
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/configs/{configName}")
    public HttpEntity retrieve(@PathVariable String configName, @RequestHeader HttpHeaders headers) {
        logger.info("[function name:{}, API:Get api/v1/configservice/configs/{configName}][configName:{}, headers:{}]","retrieve",configName, (headers != null ? headers.toString(): null));
        return ok(configService.query(configName, headers));
    }



}
