package studio.giangbb.scylladbdemo.controller;

/**
 * Created by giangbb on 12/05/2024
 */

import com.datastax.oss.driver.api.core.DriverException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:8081"})
@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("Hello world");
    }


    /**
     * Converts [DriverException]s into HTTP 500 error codes and outputs the error message as
     * the response body.
     *
     * @param e The [DriverException].
     * @return The error message to be used as response body.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorHandler(DriverException e) {
        return e.getMessage();
    }
    
}
