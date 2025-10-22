package com.finance.advisor.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.advisor.model.User;
import com.finance.advisor.repository.UserRepository;
import com.finance.advisor.service.SchedulingService;

@RestController    // spring ko bata de ga ki web me use hoga nd json me save
@RequestMapping("/api/admin")     // yh main gate hai kahi v jana hai yhi se jana hoga
public class AdminController {

    private final UserRepository userRepository;   //Yeh "Record Keeper"
    private final SchedulingService schedulingService;   //Yeh "Time Keeper" 

    public AdminController(UserRepository userRepository, SchedulingService schedulingService) {
        this.userRepository = userRepository;
        this.schedulingService = schedulingService;
    }

    // Feature 1: Saare users ki list nikalna
    // yh class ki mentor hai jispe  sabhi baccho ki info hai
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Feature 2: Ek user ko delete karna 
    // yh feature hai attendance katna 
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        Optional<User> userOptional = userRepository.findById(id);
        
        // Safety Check: Agar user "admin" hai, toh usse delete nahi karenge.
        // maan lo banda  itna  fudu hai ki  ADMIN ko dlt kr rha h  
        //to yh roke ga
        if (userOptional.isPresent() && "admin".equals(userOptional.get().getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    // Feature 3: Scheduler ka time update karna
    @PostMapping("/scheduler/rate")
    public ResponseEntity<Void> setSchedulerRate(@RequestBody Map<String, Long> payload) {
        Long rateInSeconds = payload.get("rate");
        if (rateInSeconds != null && rateInSeconds > 0) {
            schedulingService.setRateInSeconds(rateInSeconds);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    // Feature 4: Scheduler ka current time check karna
    @GetMapping("/scheduler/rate")
    public ResponseEntity<Map<String, Long>> getSchedulerRate() {
        long rateInSeconds = schedulingService.getRateInMilliseconds() / 1000;
        return ResponseEntity.ok(Map.of("rate", rateInSeconds));
    }
}

