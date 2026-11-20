<<<<<<< HEAD
package com.valoria.emailsender;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of(
            "application", "Valoria Email Sender",
            "status", "Running"
        );
    }
}
=======

package com.valoria.emailsender;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of(
            "application", "Valoria Email Sender",
            "status", "Running"
        );
    }
}
>>>>>>> 9d8df65b797d2f62b9fd6c70b08923b2cc642eeb
