package user.controller;

import edu.fudan.common.util.Response;





import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.dto.UserDto;
import user.service.UserService;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/userservice/users")
public class UserController { 
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);






    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public String testHello() {
        logger.info("[function name:testHello]");
        return "Hello";
    }

    @GetMapping
    public ResponseEntity<Response> getAllUser(@RequestHeader HttpHeaders headers) {
        return ok(userService.getAllUsers(headers));
    }

    @GetMapping("/{userName}")
    public ResponseEntity<Response> getUserByUserName(@PathVariable String userName, @RequestHeader HttpHeaders headers) {
        return ok(userService.findByUserName(userName, headers));
    }
    @GetMapping("/id/{userId}")
    public ResponseEntity<Response> getUserByUserId(@PathVariable String userId, @RequestHeader HttpHeaders headers) {
        return ok(userService.findByUserId(userId, headers));
    }

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody UserDto userDto, @RequestHeader HttpHeaders headers) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(userDto, headers));
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Response> deleteUserById(@PathVariable String userId,
                                                   @RequestHeader HttpHeaders headers) {
        return ok(userService.deleteUser(userId, headers));
    }

    @PutMapping
    public ResponseEntity<Response> updateUser(@RequestBody UserDto user,
                                               @RequestHeader HttpHeaders headers) {
        return ok(userService.updateUser(user, headers));
    }

}
